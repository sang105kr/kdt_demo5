package com.kh.demo.domain.wishlist.dao;

import com.kh.demo.domain.wishlist.entity.Wishlist;
import com.kh.demo.domain.wishlist.dto.WishlistItemDto;
import com.kh.demo.domain.common.base.BaseDAO;

import java.util.List;

/**
 * 위시리스트 Repository 인터페이스
 */
public interface WishlistDAO extends BaseDAO<Wishlist, Long> {
    
    /**
     * 회원별 위시리스트 조회 (조인 정보 포함)
     * @param memberId 회원 ID
     * @return 위시리스트 목록 (상품 정보 포함)
     */
    List<WishlistItemDto> findWishlistItemsByMemberId(Long memberId);
    
    /**
     * 회원별 위시리스트 조회 (페이징, 조인 정보 포함)
     * @param memberId 회원 ID
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 항목 수
     * @return 위시리스트 목록 (상품 정보 포함)
     */
    List<WishlistItemDto> findWishlistItemsByMemberId(Long memberId, int pageNo, int pageSize);
    
    /**
     * 회원별 위시리스트 조회 (기본 엔티티만)
     * @param memberId 회원 ID
     * @return 위시리스트 목록
     */
    List<Wishlist> findByMemberId(Long memberId);
    
    /**
     * 회원별 위시리스트 조회 (페이징, 기본 엔티티만)
     * @param memberId 회원 ID
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 항목 수
     * @return 위시리스트 목록
     */
    List<Wishlist> findByMemberId(Long memberId, int pageNo, int pageSize);
    
    /**
     * 회원별 위시리스트 개수 조회
     * @param memberId 회원 ID
     * @return 위시리스트 개수
     */
    int countByMemberId(Long memberId);
    
    /**
     * 특정 회원의 특정 상품 위시리스트 조회
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 위시리스트 (존재하지 않으면 null)
     */
    Wishlist findByMemberIdAndProductId(Long memberId, Long productId);
    
    /**
     * 위시리스트 존재 여부 확인
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 존재 여부
     */
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
    
    /**
     * 위시리스트 추가
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 생성된 위시리스트 ID
     */
    Long addWishlist(Long memberId, Long productId);
    
    /**
     * 위시리스트 제거
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 삭제된 행 수
     */
    int removeWishlist(Long memberId, Long productId);
    
    /**
     * 회원의 모든 위시리스트 제거
     * @param memberId 회원 ID
     * @return 삭제된 행 수
     */
    int removeAllByMemberId(Long memberId);
    
    /**
     * 상품 관련 모든 위시리스트 제거 (상품 삭제 시 사용)
     * @param productId 상품 ID
     * @return 삭제된 행 수
     */
    int removeAllByProductId(Long productId);
} 