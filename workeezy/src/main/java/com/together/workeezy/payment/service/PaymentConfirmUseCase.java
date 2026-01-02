package com.together.workeezy.payment.service;

import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.payment.client.TossPaymentClient;
import com.together.workeezy.payment.dto.PaymentConfirmCommand;
import com.together.workeezy.payment.dto.response.PaymentConfirmResponse;
import com.together.workeezy.payment.dto.response.TossConfirmResponse;
import com.together.workeezy.payment.entity.Payment;
import com.together.workeezy.payment.enums.PaymentMethod;
import com.together.workeezy.payment.enums.PaymentStatus;
import com.together.workeezy.payment.repository.PaymentRepository;
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
public class PaymentConfirmUseCase {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public PaymentConfirmResponse confirm(PaymentConfirmCommand cmd) {
        log.info("🔥 confirm START orderId={}, amount={}, paymentKey={}, user={}",
                cmd.orderId(), cmd.amount(), cmd.paymentKey(), cmd.userEmail());

        // 예약 조회
        Reservation reservation = reservationRepository
                .findByReservationNo(cmd.orderId())
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));

        // 도메인 규칙 검증
        reservation.assertPayable();
        reservation.assertOrderId(cmd.orderId());
        reservation.assertTotalAmount(cmd.amount());

        Payment payment = reservation.getPayment();

        // 멱등성 처리 @@버튼 여러 번 눌러도 한 번만 처리@@
        // 같은 요청을 여러 번 보내도
        // 결과가 한 번 보낸 것과 완전히 동일해야하는 성질
        if (payment != null && payment.getStatus() == PaymentStatus.paid) {
            log.info("🔥 이미 결제 완료된 요청 - orderId={}, paymentId={}",
                    payment.getOrderId(), payment.getId());
            return PaymentConfirmResponse.of(payment, reservation);
        }

        // 결제 생성
        if (payment == null) {
            log.info("🔥 creating payment");
            payment = Payment.create(reservation, cmd.amount());
            paymentRepository.save(payment);
        }

        // Toss confirm
        TossConfirmResponse api = tossPaymentClient.confirm(
                cmd.paymentKey(),
                cmd.orderId(),
                cmd.amount()
        );

        log.info("🔥 Toss confirm response orderId={}, amount={}, method={}, approvedAt={}",
                api.getOrderId(), api.getAmount(), api.getMethod(), api.getApprovedAt());

        // 승인
        payment.approve(
                api.getOrderId(),
                api.getPaymentKey(),
                cmd.amount(),
                PaymentMethod.fromToss(api.getMethod()),
                api.getApprovedAt()
        );

        log.info("🔥 payment approved paymentId={}, status={}, approvedAt={}",
                payment.getId(), payment.getStatus(), payment.getApprovedAt());

        // 예약 상태 변경
        reservation.markConfirmed();
        reservationRepository.save(reservation);

        log.info("🔥 reservation confirmed id={}, status={}",
                reservation.getId(), reservation.getStatus());

        return PaymentConfirmResponse.of(payment, reservation);
    }
}

        // TODO: PaymentLog (결제 시도/성공/실패 기록)
        // confirm / cancel 지점에서 이벤트 발행 예정