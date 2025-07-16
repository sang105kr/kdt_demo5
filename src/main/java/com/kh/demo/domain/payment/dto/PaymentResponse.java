package com.kh.demo.domain.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    
    private boolean success;
    private String message;
    private String paymentNumber;
    private String approvalNumber;
    private LocalDateTime approvedAt;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    
    // 성공 응답 생성
    public static PaymentResponse success(String paymentNumber, String approvalNumber, BigDecimal amount) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setMessage("결제가 성공적으로 완료되었습니다.");
        response.setPaymentNumber(paymentNumber);
        response.setApprovalNumber(approvalNumber);
        response.setApprovedAt(LocalDateTime.now());
        response.setAmount(amount);
        response.setStatus("COMPLETED");
        return response;
    }
    
    // 실패 응답 생성
    public static PaymentResponse failure(String reason) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(false);
        response.setMessage("결제에 실패했습니다.");
        response.setFailureReason(reason);
        response.setStatus("FAILED");
        return response;
    }
} 