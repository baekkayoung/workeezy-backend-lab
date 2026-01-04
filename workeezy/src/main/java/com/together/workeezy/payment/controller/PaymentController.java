package com.together.workeezy.payment.controller;

import com.together.workeezy.auth.security.user.CustomUserDetails;
import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.payment.dto.request.PaymentCancelRequest;
import com.together.workeezy.payment.dto.request.PaymentConfirmRequest;
import com.together.workeezy.payment.dto.response.PaymentCancelResponse;
import com.together.workeezy.payment.dto.response.PaymentConfirmResponse;
import com.together.workeezy.payment.dto.response.PaymentReadyResponse;
import com.together.workeezy.payment.service.PaymentCancelService;
import com.together.workeezy.payment.service.PaymentConfirmService;
import com.together.workeezy.payment.service.PaymentReadyService;
import com.together.workeezy.reservation.domain.Reservation;
import com.together.workeezy.reservation.repository.ReservationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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

    private final PaymentConfirmService paymentConfirmService;
    private final PaymentCancelService paymentCancelService;
    private final PaymentReadyService paymentReadyService;

    @Transactional(readOnly = true)
    @GetMapping("/{reservationId:\\d+}")
    public PaymentReadyResponse readyPayment(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("🔥 PaymentReady Controller 진입");

        return paymentReadyService.ready(reservationId, user.getUserId());
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @RequestBody @Valid PaymentConfirmRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        log.info("🔥 confirm API called");

        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(
                paymentConfirmService.confirm(
                        request.toCommand() // email 없음
                )
        );
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentCancelResponse> cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody @Valid PaymentCancelRequest request) {

        return ResponseEntity.ok(
                paymentCancelService.cancel(paymentId, request));
    }

//    @GetMapping("/receipt/{reservationId}")
//    public ResponseEntity<PaymentConfirmResponse> getPayment(@PathVariable("reservationId") String reservationId) {
//    }

}