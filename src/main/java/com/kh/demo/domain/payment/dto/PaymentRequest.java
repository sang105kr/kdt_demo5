package com.kh.demo.domain.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    
    // 카드 결제 정보
    private String cardNumber;
    private String cardExpiryMonth;
    private String cardExpiryYear;
    private String cardCvc;
    private String cardCompany;
    
    // 계좌이체 정보
    private String bankCode;
    private String accountNumber;
    
    // 결제자 정보
    private String payerName;
    private String payerPhone;
    private String payerEmail;
} 