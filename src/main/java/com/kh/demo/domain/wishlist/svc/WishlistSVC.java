package com.kh.demo.domain.wishlist.svc;

import com.kh.demo.domain.wishlist.entity.Wishlist;
import com.kh.demo.domain.wishlist.dto.WishlistItemDto;

import java.util.List;

/**
 * 위시리스트 서비스 인터페이스
 */
public interface WishlistSVC {
    
    /**
     * 회원별 위시리스트 조회
     * @param memberId 회원 ID
     * @return 위시리스트 목록 (상품 정보 포함)
     */
    List<WishlistItemDto> getWishlistByMemberId(Long memberId);
    
    /**
     * 회원별 위시리스트 조회 (페이징)
     * @param memberId 회원 ID
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 항목 수
     * @return 위시리스트 목록 (상품 정보 포함)
     */
    List<WishlistItemDto> getWishlistByMemberId(Long memberId, int pageNo, int pageSize);
    
    /**
     * 회원별 위시리스트 개수 조회
     * @param memberId 회원 ID
     * @return 위시리스트 개수
     */
    int getWishlistCountByMemberId(Long memberId);
    
    /**
     * 위시리스트 존재 여부 확인
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 존재 여부
     */
    boolean isInWishlist(Long memberId, Long productId);
    
    /**
     * 위시리스트에 상품 추가
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 성공 여부
     */
    boolean addToWishlist(Long memberId, Long productId);
    
    /**
     * 위시리스트에서 상품 제거
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 성공 여부
     */
    boolean removeFromWishlist(Long memberId, Long productId);
    
    /**
     * 위시리스트 토글 (있으면 제거, 없으면 추가)
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 추가되었으면 true, 제거되었으면 false
     */
    boolean toggleWishlist(Long memberId, Long productId);
    
    /**
     * 회원의 모든 위시리스트 제거
     * @param memberId 회원 ID
     * @return 제거된 항목 수
     */
    int clearWishlist(Long memberId);
    
    /**
     * 상품 관련 모든 위시리스트 제거 (상품 삭제 시 사용)
     * @param productId 상품 ID
     * @return 제거된 항목 수
     */
    int removeWishlistByProductId(Long productId);
} 