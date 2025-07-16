package com.kh.demo.domain.product.dao;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.shared.base.BaseDAO;
import java.util.List;
import java.util.Optional;

/**
 * Products Repository 인터페이스
 */
public interface ProductDAO extends BaseDAO<Products, Long> {
    
    /**
     * 상품 목록 페이징 조회
     * @param pageNo 페이지 번호
     * @param numOfRows 페이지당 행 수
     * @return 상품 목록
     */
    List<Products> findAllWithPaging(int pageNo, int numOfRows);
    
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
     * @param offset 오프셋
     * @param limit 제한 개수
     * @return 상품 목록
     */
    List<Products> searchByKeyword(String keyword, int offset, int limit);

    /**
     * 키워드 검색 결과 개수
     * @param keyword 검색 키워드
     * @return 검색 결과 개수
     */
    int countByKeyword(String keyword);

    /**
     * 카테고리별 상품 조회 (페이징)
     * @param category 카테고리
     * @param offset 오프셋
     * @param limit 제한 개수
     * @return 상품 목록
     */
    List<Products> findByCategory(String category, int offset, int limit);

    /**
     * 카테고리별 상품 개수
     * @param category 카테고리
     * @return 상품 개수
     */
    int countByCategory(String category);
    
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
} 