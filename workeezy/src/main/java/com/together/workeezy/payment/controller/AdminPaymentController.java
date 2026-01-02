//package com.together.workeezy.payment.controller;
//
//import com.together.workeezy.payment.dto.response.PaymentCancelResponse;
//import com.together.workeezy.payment.service.AdminApproveCancelUseCase;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/admin/payments")
//@RequiredArgsConstructor
//public class AdminPaymentController {
      // TODO: 관리자 승인 취소
//    private final AdminApproveCancelUseCase adminApproveCancelUseCase;
//
//    @PostMapping("/{paymentId}/cancel/approve")
//    public ResponseEntity<PaymentCancelResponse> approveCancel(
//            @PathVariable Long paymentId
//    ) {
//        return ResponseEntity.ok(
//                adminApproveCancelUseCase.approve(paymentId)
//        );
//    }
//}