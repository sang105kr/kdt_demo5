package com.kh.demo.domain.payment.svc;

import com.kh.demo.domain.payment.dto.PaymentRequest;
import com.kh.demo.domain.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class MockPaymentService {
    
    private final Random random = new Random();
    
    /**
     * 모의 결제 처리
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("모의 결제 처리 시작: {}", request.getPaymentMethod());
        
        // 결제 검증
        if (!validatePayment(request)) {
            return PaymentResponse.failure("결제 정보가 올바르지 않습니다.");
        }
        
        // 결제 성공률 시뮬레이션 (90% 성공)
        if (random.nextInt(100) < 90) {
            return simulateSuccessfulPayment(request);
        } else {
            return simulateFailedPayment(request);
        }
    }
    
    /**
     * 결제 정보 검증
     */
    private boolean validatePayment(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            return false;
        }
        
        switch (request.getPaymentMethod()) {
            case "CARD":
                return validateCardPayment(request);
            case "BANK_TRANSFER":
                return validateBankTransfer(request);
            case "CASH":
                return true; // 현금결제는 항상 유효
            default:
                return false;
        }
    }
    
    /**
     * 카드 결제 검증
     */
    private boolean validateCardPayment(PaymentRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().length() < 13) {
            return false;
        }
        
        if (request.getCardExpiryMonth() == null || request.getCardExpiryYear() == null) {
            return false;
        }
        
        if (request.getCardCvc() == null || request.getCardCvc().length() < 3) {
            return false;
        }
        
        // 유효기간 검증 (간단한 검증)
        try {
            int month = Integer.parseInt(request.getCardExpiryMonth());
            int year = Integer.parseInt(request.getCardExpiryYear());
            
            if (month < 1 || month > 12) {
                return false;
            }
            
            if (year < 2024) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 계좌이체 검증
     */
    private boolean validateBankTransfer(PaymentRequest request) {
        if (request.getBankCode() == null || request.getBankCode().isEmpty()) {
            return false;
        }
        
        if (request.getAccountNumber() == null || request.getAccountNumber().length() < 10) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 성공적인 결제 시뮬레이션
     */
    private PaymentResponse simulateSuccessfulPayment(PaymentRequest request) {
        // 결제번호 생성 (실제로는 PG사에서 생성)
        String paymentNumber = generatePaymentNumber();
        String approvalNumber = generateApprovalNumber();
        
        log.info("모의 결제 성공: 결제번호={}, 승인번호={}", paymentNumber, approvalNumber);
        
        return PaymentResponse.success(paymentNumber, approvalNumber, request.getAmount());
    }
    
    /**
     * 실패한 결제 시뮬레이션
     */
    private PaymentResponse simulateFailedPayment(PaymentRequest request) {
        String[] failureReasons = {
            "잔액 부족",
            "카드 한도 초과",
            "유효하지 않은 카드",
            "네트워크 오류",
            "결제 시스템 점검 중"
        };
        
        String reason = failureReasons[random.nextInt(failureReasons.length)];
        log.info("모의 결제 실패: {}", reason);
        
        return PaymentResponse.failure(reason);
    }
    
    /**
     * 결제번호 생성
     */
    private String generatePaymentNumber() {
        return "PAY" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
    }
    
    /**
     * 승인번호 생성
     */
    private String generateApprovalNumber() {
        return String.format("%012d", random.nextInt(1000000000));
    }
    
    /**
     * 카드사 감지
     */
    public String detectCardCompany(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "알 수 없음";
        }
        
        String prefix = cardNumber.substring(0, 4);
        
        if (prefix.startsWith("1234")) return "신한카드";
        if (prefix.startsWith("5678")) return "KB국민카드";
        if (prefix.startsWith("9012")) return "삼성카드";
        if (prefix.startsWith("3456")) return "현대카드";
        if (prefix.startsWith("7890")) return "롯데카드";
        
        return "기타카드";
    }
} 