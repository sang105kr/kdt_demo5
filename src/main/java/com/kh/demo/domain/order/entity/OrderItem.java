package com.kh.demo.domain.order.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderItem {
    
    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
    private Integer subtotal;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    /**
     * 상품별 총액 계산
     */
    public void calculateSubtotal() {
        if (productPrice != null && quantity != null) {
            this.subtotal = productPrice * quantity;
        }
    }
} 