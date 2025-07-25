package com.kh.demo.domain.payment.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {
    
    private Long paymentId;
    private Long orderId;
    private String paymentNumber;      // 결제번호 (PG사에서 생성)
    private String paymentMethod;      // 결제방법 (CARD, BANK_TRANSFER, CASH)
    private BigDecimal amount;         // 결제금액
    private String status;             // 결제상태 (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED)
    private String cardNumber;         // 카드번호 (마스킹 처리)
    private String cardCompany;        // 카드사
    private String approvalNumber;     // 승인번호
    private LocalDateTime approvedAt;  // 승인일시
    private String failureReason;      // 실패사유
    private String refundReason;       // 환불사유
    private LocalDateTime refundedAt;  // 환불일시
    
    // 결제 상태 상수
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    
    // 결제 방법 상수
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String METHOD_CASH = "CASH";
    
    /**
     * 결제 상태 텍스트 반환
     */
    public String getStatusText() {
        switch (status) {
            case STATUS_PENDING: return "결제 대기";
            case STATUS_PROCESSING: return "결제 처리중";
            case STATUS_COMPLETED: return "결제 완료";
            case STATUS_FAILED: return "결제 실패";
            case STATUS_CANCELLED: return "결제 취소";
            case STATUS_REFUNDED: return "환불 완료";
            default: return "알 수 없음";
        }
    }
    
    /**
     * 결제 방법 텍스트 반환
     */
    public String getPaymentMethodText() {
        switch (paymentMethod) {
            case METHOD_CARD: return "신용카드";
            case METHOD_BANK_TRANSFER: return "계좌이체";
            case METHOD_CASH: return "현금결제";
            default: return "알 수 없음";
        }
    }
    
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