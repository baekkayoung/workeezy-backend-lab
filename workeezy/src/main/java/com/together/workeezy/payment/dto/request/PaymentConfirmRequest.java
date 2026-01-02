package com.together.workeezy.payment.dto.request;

import com.together.workeezy.payment.dto.PaymentConfirmCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class PaymentConfirmRequest {

    @NotBlank
    private String paymentKey;   // toss에서 successUrl로 전달됨

    @NotBlank
    private String orderId;      // = reservation_no

    @NotNull
    @Positive
    private Long amount;         // 검증용

    public PaymentConfirmCommand toCommand() {
        return new PaymentConfirmCommand(
                orderId,
                paymentKey,
                amount,
                null
        );
    }
}
