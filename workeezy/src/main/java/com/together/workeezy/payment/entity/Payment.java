package com.together.workeezy.payment.entity;

import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.payment.enums.PaymentMethod;
import com.together.workeezy.payment.enums.PaymentStatus;
import com.together.workeezy.reservation.domain.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static com.together.workeezy.common.exception.ErrorCode.*;

@Getter
@Entity
@Table(name = "tb_payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.PERSIST)
    private Refund refund;

    @Size(max = 100)
    @Column(name = "order_id", length = 100)
    private String orderId;

    @Size(max = 200)
    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus status; // ready, paid, cancelled, failed

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod method; // unknown, card, transfer, easy_pay

    // 결제 승인 시각
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // 결제 요청 시각
    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Payment() {
    }

    private Payment(Reservation reservation, Long amount) {
        this.reservation = reservation;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.status = PaymentStatus.ready;
        this.method = PaymentMethod.unknown;
    }

    // 결제 생성
    public static Payment create(Reservation reservation, Long amount) {
        Payment payment = new Payment(reservation, amount);
        reservation.linkPayment(payment);
        return payment;
    }

    // 결제 승인
    public void approve(
            String orderId,
            String paymentKey,
            Long amount,
            PaymentMethod method,
            OffsetDateTime approvedAt) {

        if (this.status == PaymentStatus.paid) {
            throw new CustomException(PAYMENT_ALREADY_COMPLETED);
        }

        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.method = (method == null ? PaymentMethod.unknown : method);
        this.approvedAt = approvedAt.toLocalDateTime();
        this.status = PaymentStatus.paid;
    }

    // 결제 상태 변경 - cancelled
    public void cancel() {
        if (this.status != PaymentStatus.paid) {
            throw new CustomException(PAYMENT_NOT_REFUNDABLE);
        }
        this.status = PaymentStatus.cancelled;
    }

    // payment - refund 연결
    public void addRefund(Refund refund) {
        if (this.refund != null) {
            throw new CustomException(REFUND_ALREADY_EXISTS);
        }
        this.refund = refund;
    }
}