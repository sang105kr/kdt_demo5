package com.kh.demo.domain.order.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderId;            // 주문 식별자 (PK)
    private Long memberId;           // 회원 식별자 (FK)
    private String orderNumber;      // 주문번호 (YYYYMMDD-XXXXX)
    private Long orderStatusId;      // 주문상태 (code_id, gcode='ORDER_STATUS')
    private Integer totalAmount;     // 총 주문금액
    private Long paymentMethodId;    // 결제방법 (code_id, gcode='PAYMENT_METHOD')
    private Long paymentStatusId;    // 결제상태 (code_id, gcode='PAYMENT_STATUS')
    private String recipientName;    // 수령인명
    private String recipientPhone;   // 수령인 연락처
    private String shippingAddress;  // 배송주소
    private String shippingMemo;     // 배송메모
    private LocalDateTime cdate;     // 생성일시
    private LocalDateTime udate;     // 수정일시
} 