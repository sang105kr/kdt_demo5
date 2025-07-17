package com.kh.demo.domain.cart.dao;

import com.kh.demo.domain.cart.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartDAO {
    
    /**
     * 회원의 장바구니 아이템 목록 조회
     */
    List<CartItem> findByMemberId(Long memberId);
    
    /**
     * 회원의 특정 상품 장바구니 아이템 조회
     */
    Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);
    
    /**
     * 회원의 장바구니 아이템 개수 조회
     */
    int countByMemberId(Long memberId);
    
    /**
     * 회원의 장바구니 총 금액 조회
     */
    Long getTotalAmountByMemberId(Long memberId);
    
    /**
     * 회원의 장바구니 전체 삭제
     */
    int deleteByMemberId(Long memberId);
    
    /**
     * 장바구니 아이템 수량 업데이트
     */
    int updateQuantity(Long cartItemId, Integer quantity);
    
    /**
     * 장바구니 아이템 가격 정보 업데이트
     */
    int updatePriceInfo(Long cartItemId, Long salePrice, Long originalPrice, Double discountRate);
    
    /**
     * 회원의 장바구니 ID 조회
     */
    Optional<Long> findCartIdByMemberId(Long memberId);
    
    /**
     * 장바구니 ID로 회원 ID 조회
     */
    Optional<Long> findMemberIdByCartId(Long cartId);
    
    /**
     * 회원의 장바구니 생성
     */
    Long createCart(Long memberId);
    
    /**
     * 기본 CRUD 메서드들
     */
    Long save(CartItem cartItem);
    Optional<CartItem> findById(Long cartItemId);
    List<CartItem> findAll();
    int updateById(Long cartItemId, CartItem cartItem);
    int deleteById(Long cartItemId);
    int getTotalCount();
} 