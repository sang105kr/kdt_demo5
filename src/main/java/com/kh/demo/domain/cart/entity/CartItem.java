package com.kh.demo.domain.cart.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartItem extends BaseEntity {
    
    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private BigDecimal salePrice;        // 실제 판매가 (할인 적용된 가격)
    private BigDecimal originalPrice;    // 원가 (참고용)
    private BigDecimal discountRate;     // 할인율 (예: 0.20 = 20% 할인)
    private BigDecimal totalPrice;       // 총액 (salePrice * quantity)
    
    // 계산된 필드
    public void calculateTotalPrice() {
        if (salePrice != null && quantity != null) {
            this.totalPrice = salePrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        calculateTotalPrice();
    }
    
    // 할인 적용 메서드
    public void applyDiscount(BigDecimal discountRate) {
        if (originalPrice != null && discountRate != null) {
            this.discountRate = discountRate;
            this.salePrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
            calculateTotalPrice();
        }
    }
    
    // 할인 금액 계산
    public BigDecimal getDiscountAmount() {
        if (originalPrice != null && salePrice != null) {
            return originalPrice.subtract(salePrice);
        }
        return BigDecimal.ZERO;
    }
} 