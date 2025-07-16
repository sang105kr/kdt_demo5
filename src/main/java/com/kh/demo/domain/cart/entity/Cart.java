package com.kh.demo.domain.cart.entity;

import com.kh.demo.domain.member.entity.Member;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    
    private Long cartId;
    private Long memberId;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
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
     * 장바구니 총 금액
     */
    public int getTotalAmount() {
        return cartItems.stream()
                .mapToInt(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
    }
} 