package com.together.workeezy.payment.dto.response;

import com.together.workeezy.payment.entity.Payment;
import com.together.workeezy.payment.enums.PaymentMethod;
import com.together.workeezy.reservation.domain.Reservation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentConfirmResponse {

    private final Long paymentId;
    private final String paymentKey;
    private final String orderId;
    private final Long amount;
    private final PaymentMethod method;
    private final LocalDateTime approvedAt;
    private final String reservationNo;

    public static PaymentConfirmResponse of(Payment payment, Reservation reservation) {
        return new PaymentConfirmResponse(
                payment.getId(),
                payment.getPaymentKey(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getApprovedAt(),
                reservation.getReservationNo()
        );
    }
}
