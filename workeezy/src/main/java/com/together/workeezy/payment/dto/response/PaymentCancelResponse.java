package com.together.workeezy.payment.dto.response;

import com.together.workeezy.payment.entity.Payment;
import com.together.workeezy.payment.enums.PaymentStatus;
import com.together.workeezy.reservation.domain.Reservation;
import com.together.workeezy.reservation.enums.ReservationStatus;
import lombok.Getter;

@Getter
public class PaymentCancelResponse {

    private final Long paymentId;
    private final PaymentStatus paymentStatus;
    private final ReservationStatus reservationStatus;
    private final Long refundAmount;
    private final String message;

    private PaymentCancelResponse(
            Long paymentId,
            PaymentStatus paymentStatus,
            ReservationStatus reservationStatus,
            Long refundAmount,
            String message
    ) {
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.reservationStatus = reservationStatus;
        this.refundAmount = refundAmount;
        this.message = message;
    }

    // 즉시 취소 완료
    public static PaymentCancelResponse completed(Payment payment) {
        return new PaymentCancelResponse(
                payment.getId(),
                payment.getStatus(), // cancelled
                payment.getReservation().getStatus(), // cancelled
                payment.getAmount(),
                "결제가 취소되었습니다."
        );
    }

    // 취소 요청(관리자 승인 대기)
    public static PaymentCancelResponse requested(Reservation reservation) {
        return new PaymentCancelResponse(
                null,
                null,
                reservation.getStatus(), // cancel_requested
                null,
                "취소 요청이 접수되었습니다. 관리자 승인 후 처리됩니다."
        );
    }
}
