package com.kh.demo.domain.order.entity;

import com.kh.demo.domain.product.entity.Products;
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
    
    // 연관 관계
    private Order order;
    private Products product;
    
    /**
     * 상품별 총액 계산
     */
    public void calculateSubtotal() {
        if (productPrice != null && quantity != null) {
            this.subtotal = productPrice * quantity;
        }
    }
} 