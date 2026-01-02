//package com.together.workeezy.payment.service;
//
//import com.together.workeezy.common.exception.CustomException;
//import com.together.workeezy.payment.client.TossPaymentClient;
//import com.together.workeezy.payment.dto.response.PaymentCancelResponse;
//import com.together.workeezy.payment.dto.response.TossCancelResponse;
//import com.together.workeezy.payment.entity.Payment;
//import com.together.workeezy.payment.entity.Refund;
//import com.together.workeezy.payment.repository.PaymentRepository;
//import com.together.workeezy.reservation.domain.Reservation;
//import com.together.workeezy.reservation.enums.ReservationStatus;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//
//import static com.together.workeezy.common.exception.ErrorCode.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AdminApproveCancelUseCase {
      // TODO: 관리자 승인 취소
//    private final PaymentRepository paymentRepository;
//    private final TossPaymentClient tossPaymentClient;
//
//    @Transactional
//    public PaymentCancelResponse approve(Long paymentId) {
//
//        // 결제 조회
//        Payment payment = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new CustomException(PAYMENT_NOT_FOUND));
//
//        Reservation reservation = payment.getReservation();
//
//        // 예약 상태 검증 (cancel_requested만 가능)
//        if (reservation.getStatus() != ReservationStatus.cancel_requested) {
//            throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED);
//        }
//
//        // Toss 결제 취소
//        TossCancelResponse response =
//                tossPaymentClient.cancel(payment.getPaymentKey(), "관리자 승인 취소");
//
//        // 결제 상태 변경
//        payment.cancel();
//
//        // 환불 완료 처리
//        Refund refund = payment.getRefund();
//
//        refund.complete(
//                response.getCancelKey(),
//                OffsetDateTime.parse(response.getCanceledAt()).toLocalDateTime()
//        );
//
//        // 예약 상태 변경
//        reservation.approveCancel();
//
//        paymentRepository.save(payment);
//
//        return PaymentCancelResponse.completed(payment);
//    }
//}