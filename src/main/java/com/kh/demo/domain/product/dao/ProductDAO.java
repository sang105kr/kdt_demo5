package com.kh.demo.domain.product.dao;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.common.base.BaseDAO;
import java.util.List;

/**
 * Products Repository 인터페이스
 */
public interface ProductDAO extends BaseDAO<Products, Long> {
    
    /**
     * 상품 삭제 (여러건)
     * @param productIds 상품번호 목록
     * @return 삭제 건수
     */
    int deleteByIds(List<Long> productIds);
    
    /**
     * 상품명으로 상품 검색
     * @param pname 상품명
     * @return 상품 목록
     */
    List<Products> findByPname(String pname);

    /**
     * 키워드로 상품 검색 (페이징)
     * @param keyword 검색 키워드
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 행 수
     * @return 상품 목록
     */
    List<Products> searchByKeyword(String keyword, int pageNo, int pageSize);

    /**
     * 키워드 검색 결과 개수
     * @param keyword 검색 키워드
     * @return 검색 결과 개수
     */
    int countByKeyword(String keyword);

    /**
     * 카테고리별 상품 조회 (페이징)
     * @param categoryId 카테고리 ID
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 행 수
     * @return 상품 목록
     */
    List<Products> findByCategory(Long categoryId, int pageNo, int pageSize);

    /**
     * 카테고리별 상품 개수
     * @param categoryId 카테고리 ID
     * @return 상품 개수
     */
    int countByCategory(Long categoryId);
    
    /**
     * 재고 관련 메서드들
     */
    
    /**
     * 재고 차감
     * @param productId 상품 ID
     * @param quantity 차감할 수량
     * @return 차감된 행 수
     */
    int decreaseStock(Long productId, Integer quantity);
    
    /**
     * 재고 증가
     * @param productId 상품 ID
     * @param quantity 증가할 수량
     * @return 증가된 행 수
     */
    int increaseStock(Long productId, Integer quantity);
    
    /**
     * 리뷰 개수 업데이트
     * @param productId 상품 ID
     * @return 업데이트된 행 수
     */
    int updateReviewCount(Long productId);
} 