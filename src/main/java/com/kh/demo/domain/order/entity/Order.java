package com.kh.demo.domain.order.entity;

import com.kh.demo.domain.member.entity.Member;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {
    
    private Long orderId;
    private Long memberId;
    private String orderNumber;
    private String orderStatus;
    private Integer totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String shippingMemo;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    // 연관 관계
    private Member member;
    private List<OrderItem> orderItems = new ArrayList<>();
    
    /**
     * 주문 상태 한글명 반환
     */
    public String getOrderStatusText() {
        switch (orderStatus) {
            case "PENDING": return "주문대기";
            case "CONFIRMED": return "주문확정";
            case "SHIPPED": return "배송중";
            case "DELIVERED": return "배송완료";
            case "CANCELLED": return "주문취소";
            default: return orderStatus;
        }
    }
    
    /**
     * 결제 상태 한글명 반환
     */
    public String getPaymentStatusText() {
        switch (paymentStatus) {
            case "PENDING": return "결제대기";
            case "COMPLETED": return "결제완료";
            case "FAILED": return "결제실패";
            case "REFUNDED": return "환불완료";
            default: return paymentStatus;
        }
    }
    
    /**
     * 결제 방법 한글명 반환
     */
    public String getPaymentMethodText() {
        switch (paymentMethod) {
            case "CARD": return "신용카드";
            case "BANK_TRANSFER": return "계좌이체";
            case "CASH": return "현금결제";
            default: return paymentMethod;
        }
    }
    
    /**
     * 주문 상품 개수 반환
     */
    public int getTotalItemCount() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
} 