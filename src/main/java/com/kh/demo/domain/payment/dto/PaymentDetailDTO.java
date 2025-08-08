package com.kh.demo.domain.payment.dto;

import com.kh.demo.domain.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 상세 정보 DTO
 * Payment 엔티티와 Code 테이블을 조인한 결과
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentDetailDTO extends BaseDTO {
    
    // Payment 기본 정보
    private Long paymentId;
    private Long orderId;
    private String paymentNumber;
    private BigDecimal amount;
    private String cardNumber;
    private String cardCompany;
    private String approvalNumber;
    private LocalDateTime approvedAt;
    private String failureReason;
    private String refundReason;
    private LocalDateTime refundedAt;
    
    // 코드 참조 필드들
    private Long paymentMethod;     // 결제방법 코드 ID
    private Long status;            // 결제상태 코드 ID
    
    // 코드 decode 값들 (조인으로 조회)
    private String paymentMethodCode;   // 결제방법 코드 (CARD, BANK_TRANSFER, CASH)
    private String paymentMethodName;   // 결제방법명 (신용카드, 계좌이체, 현금결제)
    private String statusCode;          // 상태 코드 (PENDING, COMPLETED, FAILED, etc.)
    private String statusName;          // 상태명 (결제대기, 결제완료, 결제실패, etc.)
    
    /**
     * 마스킹된 카드번호 반환
     */
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * 결제 완료 여부
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(statusCode);
    }
    
    /**
     * 결제 실패 여부
     */
    public boolean isFailed() {
        return "FAILED".equals(statusCode);
    }
    
    /**
     * 환불 처리 여부
     */
    public boolean isRefunded() {
        return "REFUNDED".equals(statusCode);
    }
    
    /**
     * 카드 결제 여부
     */
    public boolean isCardPayment() {
        return "CARD".equals(paymentMethodCode);
    }
}