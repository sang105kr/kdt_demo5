package com.kh.demo.domain.cart.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import com.kh.demo.domain.product.entity.Products;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartItem extends BaseEntity {
    
    private Long cartItemId;
    private Long memberId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    // 연관 엔티티
    private Products product;
    
    // 계산된 필드
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        calculateTotalPrice();
    }
} 