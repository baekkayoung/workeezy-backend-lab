package com.together.workeezy.payment.entity;

import com.together.workeezy.payment.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "tb_payment_logs")
public class PaymentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentlog_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "response_data", columnDefinition = "json")
    private String responseData;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType; // request, response, callback, fail

    @Column(name = "http_status")
    private Integer httpStatus;

    @NotNull
    @CreationTimestamp
    @Column(name = "logged_at", nullable = false, updatable = false)
    private LocalDateTime loggedAt;

    protected  PaymentLog() {}

    public static PaymentLog success(Payment payment, String responseJson, int httpStatus) {
        PaymentLog log = new PaymentLog();
        log.payment = payment;
        log.responseData = responseJson;
        log.eventType = EventType.response;
        log.httpStatus = httpStatus;

        return log;
    }

    public static PaymentLog fail(Payment payment, String responseJson, int httpStatus) {
        PaymentLog log = new PaymentLog();
        log.payment = payment;
        log.responseData = responseJson;
        log.eventType = EventType.fail;
        log.httpStatus = httpStatus;

        return log;
    }
}