package com.kh.demo.domain.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 주문 상품 정보 DTO (View용)
 */
@Data
public class OrderItemDTO {
    
    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
    private Integer subtotal;
    private LocalDateTime cdate;
    private LocalDateTime udate;
} 