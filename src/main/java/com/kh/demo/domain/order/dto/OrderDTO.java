package com.kh.demo.domain.order.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 정보 DTO (View용)
 * Order와 OrderItem 정보를 함께 담는 데이터 전송 객체
 */
@Data
public class OrderDTO {
    
    // Order 기본 정보
    private Long orderId;
    private Long memberId;
    private String orderNumber;
    private Integer totalAmount;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String shippingMemo;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    // 코드 참조 필드들
    private Long orderStatusId;
    private Long paymentMethodId;
    private Long paymentStatusId;
    
    // 코드 decode 값들 (조인으로 조회)
    private String orderStatusCode;     // 주문상태 코드 (PENDING, CONFIRMED, etc.)
    private String orderStatusName;     // 주문상태명 (주문대기, 주문확정, etc.)
    private String paymentMethodCode;   // 결제방법 코드 (CARD, BANK_TRANSFER, etc.)
    private String paymentMethodName;   // 결제방법명 (신용카드, 계좌이체, etc.)
    private String paymentStatusCode;   // 결제상태 코드 (PENDING, COMPLETED, etc.)
    private String paymentStatusName;   // 결제상태명 (결제대기, 결제완료, etc.)
    
    // OrderItem 목록
    private List<OrderItemDTO> orderItems;
    
    /**
     * 주문 상태 한글명 반환 (코드 캐시 사용 권장)
     * @deprecated 코드 테이블의 decode 값 사용 권장
     */
    @Deprecated
    public String getOrderStatusText() {
        return orderStatusName != null ? orderStatusName : orderStatusCode;
    }
    
    /**
     * 결제 상태 한글명 반환 (코드 캐시 사용 권장)
     * @deprecated 코드 테이블의 decode 값 사용 권장
     */
    @Deprecated
    public String getPaymentStatusText() {
        return paymentStatusName != null ? paymentStatusName : paymentStatusCode;
    }
    
    /**
     * 결제 방법 한글명 반환 (코드 캐시 사용 권장)
     * @deprecated 코드 테이블의 decode 값 사용 권장
     */
    @Deprecated
    public String getPaymentMethodText() {
        return paymentMethodName != null ? paymentMethodName : paymentMethodCode;
    }
    
    /**
     * 주문 취소 가능 여부
     */
    public boolean isCancelable() {
        return "PENDING".equals(orderStatusCode) || "CONFIRMED".equals(orderStatusCode);
    }
    
    /**
     * 배송 완료 여부
     */
    public boolean isDelivered() {
        return "DELIVERED".equals(orderStatusCode);
    }
    
    /**
     * 주문 상품 개수 반환
     */
    public int getTotalItemCount() {
        if (orderItems == null) return 0;
        return orderItems.stream()
                .mapToInt(OrderItemDTO::getQuantity)
                .sum();
    }
} 