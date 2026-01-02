package com.together.workeezy.payment.dto.response;

import lombok.Getter;

@Getter
public class TossCancelResponse {

    private String paymentKey;
    private String cancelKey;
    private String canceledAt;
    private String status;

}