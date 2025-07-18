package com.kh.demo.domain.cart.svc;

import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.cart.dto.CartItemDTO;

import java.util.List;
import java.util.Optional;

public interface CartService {
    
    /**
     * 장바구니에 상품 추가
     */
    Long addToCart(Long memberId, Long productId, Integer quantity);
    
    /**
     * 회원의 장바구니 목록 조회 (상품 정보 포함)
     */
    List<CartItemDTO> getCartItemsWithProduct(Long memberId);
    
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
    
    /**
     * 장바구니 아이템에 할인 적용
     */
    boolean applyDiscount(Long memberId, Long cartItemId, Double discountRate);
    
    /**
     * 장바구니 아이템 조회
     */
    Optional<CartItem> getCartItemById(Long cartItemId);
} 