package com.together.workeezy.reservation.controller;

import com.together.workeezy.reservation.dto.CursorPageResponse;
import com.together.workeezy.reservation.enums.ReservationStatus;
import com.together.workeezy.reservation.dto.AdminReservationDetailDto;
import com.together.workeezy.reservation.dto.AdminReservationListDto;
import com.together.workeezy.reservation.dto.RejectReservationRequestDto;
import com.together.workeezy.reservation.service.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')") // 관리자만 접근
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    @GetMapping
    public ResponseEntity<Page<AdminReservationListDto>> getReservationList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) ReservationStatus status, // 예약 상태
            @RequestParam(required = false) String keyword // 프로그램명/예약자
    ) {
        return ResponseEntity.ok(
                adminReservationService.getAdminReservationLists(page, status, keyword)
        );
    }

    // 커서 기반 페이지 네이션
    @GetMapping("/cursor")
    public CursorPageResponse<AdminReservationListDto> getReservationsByCursor(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminReservationService.getAdminReservationsByCursor(
                status,
                cursor,
                size
        );
    }

    // 상세 조회
    @GetMapping("/{reservationId}")
    public ResponseEntity<AdminReservationDetailDto> getReservationDetail(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                adminReservationService.getReservationDetail(reservationId)
        );
    }
    
    // 예약 승인
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        adminReservationService.approveReservation(id);
        return ResponseEntity.ok().build();
    }
    
    // 예약 거절
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @RequestBody RejectReservationRequestDto dto
    ) {
        adminReservationService.rejectReservation(id, dto.getReason());
        return ResponseEntity.ok().build();
    }

    // 취소 승인
    @PatchMapping("/{id}/cancel/approve")
    public ResponseEntity<Void> approveCancel(@PathVariable Long id) {
        adminReservationService.approveCancel(id);
        return ResponseEntity.ok().build();
    }

    // 취소 반려
    @PatchMapping("/{id}/cancel/reject")
    public ResponseEntity<Void> rejectCancel(
            @PathVariable Long id,
            @RequestBody RejectReservationRequestDto dto
    ) {
        adminReservationService.rejectCancel(id, dto.getReason());
        return ResponseEntity.ok().build();
    }
}
