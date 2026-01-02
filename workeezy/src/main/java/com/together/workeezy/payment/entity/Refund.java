package com.together.workeezy.payment.entity;

import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.payment.enums.RefundStatus;
import com.together.workeezy.payment.enums.RequestedBy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static com.together.workeezy.common.exception.ErrorCode.*;

@Getter
@Entity
@Table(name = "tb_refund")
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @NotNull
    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;

    @Size(max = 200)
    @Column(name = "refund_reason", length = 200)
    private String refundReason;

    @Size(max = 200)
    @Column(name = "cancel_key", length = 200)
    private String cancelKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus = RefundStatus.pending;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_by")
    private RequestedBy requestedBy = RequestedBy.user;

    // 환불 완료 시점
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Refund() {}

    private Refund(Payment payment, Long amount, String reason){
        this.payment = payment;
        this.refundAmount = amount;
        this.refundReason = reason;
    }

    public static Refund create(Payment payment, Long amount, String reason) {
        Refund refund = new Refund(payment, amount, reason);
        payment.addRefund(refund);
        return refund;
    }

    public void complete(String cancelKey, LocalDateTime refundedAt) {
        if(this.refundStatus == RefundStatus.completed) {
            throw new CustomException(REFUND_ALREADY_COMPLETED);
        }
        this.cancelKey = cancelKey;
        this.refundedAt = refundedAt;
        this.refundStatus = RefundStatus.completed;
    }
}