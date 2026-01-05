package com.together.workeezy.program.program.application.service;

import com.together.workeezy.program.program.domain.model.entity.Place;
import com.together.workeezy.program.program.domain.model.entity.PlaceType;
import com.together.workeezy.program.program.interfaces.dto.PlaceDto;
import com.together.workeezy.program.program.interfaces.dto.RoomDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProgramDetailAssembler {

    private ProgramDetailAssembler() {}

    public record PlacesSplit(PlaceDto hotel, List<PlaceDto> offices, List<PlaceDto> attractions) {}

    public static List<String> buildSubImages(Place stay, Place office) {
        List<String> subImages = new ArrayList<>();

        if (stay != null) {
            if (stay.getPlacePhoto2() != null) subImages.add(stay.getPlacePhoto2());
            if (stay.getPlacePhoto3() != null) subImages.add(stay.getPlacePhoto3());
        }
        if (office != null) {
            if (office.getPlacePhoto1() != null) subImages.add(office.getPlacePhoto1());
            if (office.getPlacePhoto2() != null) subImages.add(office.getPlacePhoto2());
        }
        if (subImages.size() > 4) subImages = subImages.subList(0, 4);

        return subImages;
    }

    public static PlacesSplit splitPlacesToDtos(List<Place> places, Map<Long, List<RoomDto>> roomsByPlaceId) {

        PlaceDto hotel = null;
        List<PlaceDto> offices = new ArrayList<>();
        List<PlaceDto> attractions = new ArrayList<>();

        for (Place p : places) {

            List<RoomDto> roomDtos = roomsByPlaceId.getOrDefault(p.getId(), List.of());

            PlaceDto dto = new PlaceDto(
                    p.getId(),
                    p.getName(),
                    p.getPlaceAddress(),
                    p.getPlacePhone(),
                    p.getPlacePhoto1(),
                    p.getPlacePhoto2(),
                    p.getPlacePhoto3(),
                    p.getPlacePhoto4(),
                    p.getPlaceEquipment(),
                    p.getAttractionUrl(),
                    p.getPlaceType(),
                    p.getPlaceRegion(),
                    roomDtos
            );

            if (p.getPlaceType() == PlaceType.stay) hotel = dto;
            if (p.getPlaceType() == PlaceType.office) offices.add(dto);
            if (p.getPlaceType() == PlaceType.attraction) attractions.add(dto);
        }

        return new PlacesSplit(hotel, offices, attractions);
    }
}
