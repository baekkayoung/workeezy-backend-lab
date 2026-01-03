package com.together.workeezy.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.together.workeezy.payment.dto.response.TossConfirmResponse;
import com.together.workeezy.payment.entity.Payment;
import com.together.workeezy.payment.entity.PaymentLog;
import com.together.workeezy.payment.repository.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLogService {

    private final PaymentLogRepository paymentLogRepository;
    private final ObjectMapper objectMapper;


    // 결제 성공 로그
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSuccess(Payment payment, TossConfirmResponse api) {
        try {
            String json = objectMapper.writeValueAsString(api);

            paymentLogRepository.save(
                    PaymentLog.success(
                            payment,
                            json,
                            200
                    )
            );
        } catch (Exception e) {
            log.error("결제 성공 로그 저장 실패 (무시됨)", e);
        }
    }

    // 결제 실패 로그
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFail(Payment payment, Exception e) {
        try {
            paymentLogRepository.save(
                    PaymentLog.fail(
                            payment,
                            e.getMessage(),
                            500
                    )
            );
        } catch (Exception ex) {
            log.error("결제 실패 로그 저장 실패 (무시됨)", ex);
        }
    }
}