package com.kh.demo.domain.cart.svc;

import com.kh.demo.domain.cart.entity.CartItem;

import java.util.List;

public interface CartService {
    
    /**
     * 장바구니에 상품 추가
     */
    Long addToCart(Long memberId, Long productId, Integer quantity);
    
    /**
     * 회원의 장바구니 목록 조회
     */
    List<CartItem> getCartItems(Long memberId);
    
    /**
     * 장바구니 아이템 수량 업데이트
     */
    boolean updateQuantity(Long memberId, Long cartItemId, Integer quantity);
    
    /**
     * 장바구니 아이템 삭제
     */
    boolean removeFromCart(Long memberId, Long cartItemId);
    
    /**
     * 장바구니 전체 비우기
     */
    boolean clearCart(Long memberId);
    
    /**
     * 장바구니 아이템 개수 조회
     */
    int getCartItemCount(Long memberId);
    
    /**
     * 장바구니 총 금액 조회
     */
    Long getCartTotalAmount(Long memberId);
} 