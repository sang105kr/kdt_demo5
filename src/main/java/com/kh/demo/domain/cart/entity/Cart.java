package com.kh.demo.domain.cart.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import com.kh.demo.domain.member.entity.Member;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Cart extends BaseEntity {
    
    private Long cartId;
    private Long memberId;

    // 연관 관계
    private Member member;
    private List<CartItem> cartItems = new ArrayList<>();
    
    /**
     * 장바구니 총 상품 수량
     */
    public int getTotalItemCount() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    /**
     * 장바구니 총 금액 (CartItem의 totalPrice 사용)
     */
    public int getTotalAmount() {
        return cartItems.stream()
                .mapToInt(item -> item.getTotalPrice() != null ? item.getTotalPrice().intValue() : 0)
                .sum();
    }
} 