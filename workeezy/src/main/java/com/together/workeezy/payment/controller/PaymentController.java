package com.together.workeezy.payment.controller;

import com.together.workeezy.auth.security.user.CustomUserDetails;
import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.payment.dto.request.PaymentCancelRequest;
import com.together.workeezy.payment.dto.request.PaymentConfirmRequest;
import com.together.workeezy.payment.dto.response.PaymentCancelResponse;
import com.together.workeezy.payment.dto.response.PaymentConfirmResponse;
import com.together.workeezy.payment.dto.response.PaymentReadyResponse;
import com.together.workeezy.payment.service.PaymentCancelUseCase;
import com.together.workeezy.payment.service.PaymentConfirmUseCase;
import com.together.workeezy.reservation.domain.Reservation;
import com.together.workeezy.reservation.repository.ReservationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import static com.together.workeezy.common.exception.ErrorCode.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentConfirmUseCase paymentConfirmUseCase;
    private final PaymentCancelUseCase paymentCancelUseCase;
    private final ReservationRepository reservationRepository;

    @GetMapping("/{reservationId:\\d+}")
    public PaymentReadyResponse readyPayment(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("🔥 PaymentReady 진입");
        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));

        reservation.assertPayable();

        return new PaymentReadyResponse(
                reservation.getReservationNo(),
                reservation.getProgram().getTitle(),
                reservation.getTotalPrice()
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @RequestBody @Valid PaymentConfirmRequest request) {
        log.info("🔥 confirm API called");

        return ResponseEntity.ok(
                paymentConfirmUseCase.confirm(
                        request.toCommand() // email 없음
                )
        );
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentCancelResponse> cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody @Valid PaymentCancelRequest request) {

        return ResponseEntity.ok(
                paymentCancelUseCase.cancel(paymentId, request));
    }

//    @GetMapping("/receipt/{reservationId}")
//    public ResponseEntity<PaymentConfirmResponse> getPayment(@PathVariable("reservationId") String reservationId) {
//    }

}