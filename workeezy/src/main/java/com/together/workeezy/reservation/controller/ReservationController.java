package com.together.workeezy.reservation.controller;

import com.together.workeezy.auth.security.user.CustomUserDetails;
import com.together.workeezy.reservation.dto.ReservationCreateDto;
import com.together.workeezy.reservation.dto.ReservationResponseDto;
import com.together.workeezy.reservation.dto.ReservationUpdateDto;
import com.together.workeezy.reservation.enums.ReservationStatus;
import com.together.workeezy.reservation.service.ReservationConfirmationService;
import com.together.workeezy.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations") // 기본 url
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationConfirmationService reservationConfirmationService;

    /* 예약 생성 */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody ReservationCreateDto dto,
            Authentication authentication) {


            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
            reservationService.validateReservationCreate(userId);
            reservationService.createNewReservation(dto, authentication.getName());
            return ResponseEntity.ok("예약 성공");
    }

    // 예약 가능 날짜 체크
    @GetMapping("/availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam OffsetDateTime startDate,
            @RequestParam OffsetDateTime endDate,
            @RequestParam(required = false) Long excludeId
    ) {
        boolean available = reservationService.isRoomAvailable(
                roomId,
                startDate.toLocalDateTime(),
                endDate.toLocalDateTime(),
                excludeId
        );

        return ResponseEntity.ok(Map.of("available", available));
    }


    // 내 예약 목록 조회
//    @GetMapping("/me")
//    public ResponseEntity<?> getMyReservations(Authentication authentication) {
//
//        System.out.println("🧩 authentication = " + authentication);
//
//        if (authentication != null) {
//            System.out.println("🧩 principal = " + authentication.getPrincipal());
//            System.out.println("🧩 name = " + authentication.getName());
//            System.out.println("🧩 authorities = " + authentication.getAuthorities());
//        } else {
//            System.out.println("❌ authentication is NULL");
//        }
//
//        String email = authentication.getName();
//
//        try {
//            return ResponseEntity.ok(reservationService.getMyReservations(email));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("예약 조회 실패: " + e.getMessage());
//        }
//    }



    // 사용자 예약 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyReservations(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime cursorDate,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            String keyword,
            @RequestParam(required = false)
            ReservationStatus status,
            Authentication authentication
    ) {
        System.out.println("🧩 authentication = " + authentication);

        if (authentication != null) {
            System.out.println("🧩 principal = " + authentication.getPrincipal());
            System.out.println("🧩 name = " + authentication.getName());
            System.out.println("🧩 authorities = " + authentication.getAuthorities());
        } else {
            System.out.println("❌ authentication is NULL");
        }
        String email = authentication.getName();

        Slice<ReservationResponseDto> result =
                reservationService.getMyReservations(email, cursorDate, cursorId, size, keyword,
                        status);

        return ResponseEntity.ok(result);
    }



    // 예약 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getMyReservation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        ReservationResponseDto dto =
                reservationService.getMyReservation(id, email);

        return ResponseEntity.ok(dto);
    }
        
    // * 예약 수정 *
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMyReservation(
            @PathVariable Long id,
            @RequestBody ReservationUpdateDto dto,
            Authentication authentication
    ) {
        String email = authentication.getName();

        reservationService.updateMyReservation(id, dto, email);
        return ResponseEntity.ok("예약 수정 성공");
    }

    // * 예약 재신청 *
    @PostMapping("/{id}/resubmit")
    public ResponseEntity<?> resubmitReservation(
            @PathVariable Long id,
            @RequestBody ReservationUpdateDto dto,
            Authentication authentication
    ){
        String email = authentication.getName();
        reservationService.resubmitReservation(id, email,dto);
        return ResponseEntity.ok("예약 재신청 완료");
    }

    // * 예약 취소 *
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelMyReservation(@PathVariable Long id,
                                               Authentication authentication
    ) {
        reservationService.cancelMyReservation(id, authentication.getName());
        return ResponseEntity.ok("예약 취소 완료");
    }

    // * 예약 취소 요청 *
    @PatchMapping("/{id}/cancel-request")
    public ResponseEntity<?> requestCancelReservation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        reservationService.requestCancelMyReservation(id, authentication.getName());
        return ResponseEntity.ok("예약 취소 요청 완료");
    }


    /// =============== pdf ============= //
    ///
    // pdf 조회(미리보기용) JSON
    @GetMapping("/{id}/confirmation")
    public ResponseEntity<?> getConfirmation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                reservationConfirmationService.getPreview(id, email)
        );
    }

    /**
     * 2) 생성/업데이트(재생성)
     * - 확정 상태에서만
     * - S3 업로드 후 confirm_pdf_key 갱신
     */
    @PostMapping("/{id}/confirmation")
    public ResponseEntity<?> regenerateConfirmationPdf(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        reservationConfirmationService.regenerate(id, email);
        return ResponseEntity.ok("확정서 PDF 생성/갱신 완료");
    }

    /** 3) 다운로드: PDF 파일 */
    @GetMapping("/{id}/confirmation/pdf")
    public ResponseEntity<InputStreamResource> downloadConfirmationPdf(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return reservationConfirmationService.download(id, email);
    }
}
