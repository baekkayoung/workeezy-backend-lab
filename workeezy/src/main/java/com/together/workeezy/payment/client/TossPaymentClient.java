package com.together.workeezy.payment.client;

import com.together.workeezy.payment.config.TossPaymentProperties;
import com.together.workeezy.payment.dto.response.TossCancelResponse;
import com.together.workeezy.payment.dto.response.TossConfirmResponse;
import com.together.workeezy.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final WebClient webClient;
    private final TossPaymentProperties props;

    public TossConfirmResponse confirm(String paymentKey,
                                       String orderId,
                                       long amount) {

        // Basic Auth 헤더 만들기 (secretKey:)
        // body: paymentKey, orderId, amount
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
        // POST /v1/payments/confirm 호출
        // 성공 시 TossConfirmResponse 로 매핑

        return webClient.post()
                .uri("/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, basicAuth())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TossConfirmResponse.class)
                .block(); // 동기식으로 결과 받기
    }

    public TossCancelResponse cancel(String paymentKey, String reason) {

        Map<String, Object> body = Map.of(
                "cancelReason", reason
        );

        return webClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .header(HttpHeaders.AUTHORIZATION, basicAuth())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TossCancelResponse.class)
                .block(); // 동기식
        //                추후 수정 가능
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, resp ->
//                        resp.bodyToMono(String.class)
//                                .map(body -> new CustomException(PAYMENT_CANCEL_FAILED))
//                )
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((props.getSecretKey() + ":").getBytes());
    }

}
