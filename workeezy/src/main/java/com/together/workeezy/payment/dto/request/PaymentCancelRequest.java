package com.together.workeezy.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PaymentCancelRequest {

    @NotBlank
    private String reason;

}
