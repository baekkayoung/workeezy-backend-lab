package com.together.workeezy.payment.service;

import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.payment.dto.response.PaymentReadyResponse;
import com.together.workeezy.reservation.domain.Reservation;
import com.together.workeezy.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.together.workeezy.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReadyService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public PaymentReadyResponse ready(Long reservationId, Long userId) {
        log.info("🔥 PaymentReadyService.ready reservationId={}, userId={}", reservationId, userId);

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));

        return new PaymentReadyResponse(
                reservation.getReservationNo(),
                reservation.getProgram().getTitle(),
                reservation.getTotalPrice()
        );
    }
    }
