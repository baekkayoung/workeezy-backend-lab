package com.together.workeezy.program.program.application.service;

import com.together.workeezy.program.program.domain.model.entity.Place;
import com.together.workeezy.program.program.domain.model.entity.PlaceType;
import com.together.workeezy.program.program.domain.model.entity.Program;
import com.together.workeezy.program.program.domain.model.entity.Room;
import com.together.workeezy.program.program.domain.repository.PlaceRepository;
import com.together.workeezy.program.program.domain.repository.ProgramRepository;
import com.together.workeezy.program.program.interfaces.dto.*;
import com.together.workeezy.program.review.application.service.ReviewService;
import com.together.workeezy.search.domain.model.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class ProgramService {

    private final ProgramRepository programRepository;
    private final PlaceRepository placeRepository;
    private final RoomRepository roomRepository;
    private final ReviewService reviewService;

    /**
     * 🔍 검색 기능 — 기존 코드 유지
     */
    public List<ProgramCardDto> search(String keyword, String region) {

        List<Program> programs = programRepository.searchByKeyword(keyword);

        return programs.stream()
                .map(p -> {

                    // ⭐ Lazy 로딩 피하기 위해 repository 사용
                    String placeRegion = placeRepository.findRegionByProgramId(p.getId());

                    String photo = placeRepository.findPhotosByProgramId(p.getId())
                            .stream()
                            .findFirst()
                            .orElse(null);

                    return new ProgramCardDto(
                            p.getId(),
                            p.getTitle(),
                            photo,
                            p.getProgramPrice(),
                            placeRegion
                    );
                })
                .toList();
    }

    /**
     * ⭐ 상세 조회 기능 (✅ N+1 제거) - 동작 그대로, 조립만 분리
     */
    public ProgramDetailResponseDto getProgramDetail(Long programId) {

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        // 장소 조회 (1번)
        List<Place> places = placeRepository.findByProgramId(programId);

        // ✅ rooms를 placeIds로 한 번에 조회 (너 코드 그대로)
        List<Long> placeIds = places.stream()
                .map(Place::getId)
                .toList();

        Map<Long, List<RoomDto>> roomsByPlaceId = Collections.emptyMap();

        if (!placeIds.isEmpty()) {
            List<Room> rooms = roomRepository.findByPlaceIdIn(placeIds); // (1번)

            roomsByPlaceId = rooms.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getPlace().getId(),
                            Collectors.mapping(
                                    r -> new RoomDto(
                                            r.getId(),
                                            r.getRoomNo(),
                                            r.getRoomPeople(),
                                            r.getRoomService(),
                                            r.getRoomType()
                                    ),
                                    Collectors.toList()
                            )
                    ));
        }

        // stay / office 찾기 (너 코드 그대로)
        Place stay = places.stream()
                .filter(p -> p.getPlaceType() == PlaceType.stay)
                .findFirst()
                .orElse(null);

        Place office = places.stream()
                .filter(p -> p.getPlaceType() == PlaceType.office)
                .findFirst()
                .orElse(null);

        // ⭐ 메인 이미지 (숙소 1번 사진) - 그대로
        String mainImage = (stay != null) ? stay.getPlacePhoto1() : null;

        // ⭐ 서브 이미지 - Assembler로 이동(결과 동일)
        List<String> subImages = ProgramDetailAssembler.buildSubImages(stay, office);

        // 장소별 DTO 조립 - Assembler로 이동(결과 동일)
        ProgramDetailAssembler.PlacesSplit split =
                ProgramDetailAssembler.splitPlacesToDtos(places, roomsByPlaceId);

        return new ProgramDetailResponseDto(
                program.getId(),
                program.getTitle(),
                program.getProgramInfo(),
                program.getProgramPeople(),
                program.getProgramPrice(),
                mainImage,
                subImages,
                split.hotel(),
                split.offices(),
                split.attractions(),
                null   // ⭐ 리뷰는 이제 ReviewService에서 조회함
        );
    }

    public List<Program> getAllPrograms() {
        return programRepository.findAll();
    }

    public List<ProgramCardDto> getProgramCards(int limit) {

        return programRepository.findAllProgramCardsOrderByIdAsc(limit)
                .stream()
                .map(v -> new ProgramCardDto(
                        v.getId(),
                        v.getTitle(),
                        v.getPhoto(),
                        v.getPrice(),
                        v.getRegion()
                ))
                .toList();
    }


    //    @Transactional(readOnly = true)
    public ProgramReservationInfoDto getProgramForReservation(Long programId) {

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("프로그램 없음"));

        // 숙소
        Place stay = program.getPlaces().stream()
                .filter(p -> p.getPlaceType() == PlaceType.stay)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("숙소(STAY) 없음"));

        // 오피스
        Place office = program.getPlaces().stream()
                .filter(p -> p.getPlaceType() == PlaceType.office)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("오피스(OFFICE) 없음"));

        List<RoomSimpleDto> rooms = roomRepository.findByPlaceId(stay.getId())
                .stream()
                .map(room -> new RoomSimpleDto(
                        room.getId(),
                        room.getRoomType().name()   // economy / standard / superior
                ))
                .toList();

        return new ProgramReservationInfoDto(
                program.getId(),
                program.getTitle(),
                program.getProgramPrice(),
                stay.getId(),
                stay.getName(),
                office.getId(),
                office.getName(),
                rooms,
                program.getProgramPeople()
        );
    }
}
