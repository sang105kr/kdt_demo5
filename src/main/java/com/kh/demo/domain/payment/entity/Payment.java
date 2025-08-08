package com.kh.demo.domain.payment.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 * 
 * 결제 정보를 관리하는 엔티티입니다.
 * code 테이블과 연계하여 결제 방법과 상태를 관리합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {
    
    private Long paymentId;
    private Long orderId;
    private String paymentNumber;      // 결제번호 (PG사에서 생성)
    private Long paymentMethod;        // 결제방법 (code_id 참조, gcode='PAYMENT_METHOD')
    private BigDecimal amount;         // 결제금액
    private Long status;               // 결제상태 (code_id 참조, gcode='PAYMENT_STATUS')
    private String cardNumber;         // 카드번호 (마스킹 처리)
    private String cardCompany;        // 카드사
    private String approvalNumber;     // 승인번호
    private LocalDateTime approvedAt;  // 승인일시
    private String failureReason;      // 실패사유
    private String refundReason;       // 환불사유
    private LocalDateTime refundedAt;  // 환불일시
    
    /**
     * 카드번호 마스킹 처리
     */
    public void setCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            this.cardNumber = "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
        } else {
            this.cardNumber = cardNumber;
        }
    }
} 