package com.kh.demo.domain.products.dao;

import com.kh.demo.domain.entity.Products;

import java.util.List;
import java.util.Optional;

/**
 * Products Repository 인터페이스
 */
public interface ProductRepository {
    
    /**
     * 상품 등록
     * @param products 상품 정보
     * @return 상품번호
     */
    Long save(Products products);
    
    /**
     * 상품 목록 조회
     * @return 상품 목록
     */
    List<Products> findAll();
    
    /**
     * 상품 목록 조회 (페이징)
     * @param pageNo 페이지 번호
     * @param numOfRows 페이지당 행 수
     * @return 상품 목록
     */
    List<Products> findAll(int pageNo, int numOfRows);
    
    /**
     * 상품 총 건수
     * @return 총 건수
     */
    int getTotalCount();
    
    /**
     * 상품 조회
     * @param productId 상품번호
     * @return 상품정보
     */
    Optional<Products> findById(Long productId);
    
    /**
     * 상품 수정
     * @param productId 상품번호
     * @param products 상품정보
     * @return 수정 건수
     */
    int updateById(Long productId, Products products);
    
    /**
     * 상품 삭제
     * @param productId 상품번호
     * @return 삭제 건수
     */
    int deleteById(Long productId);
    
    /**
     * 상품 삭제 (여러건)
     * @param productIds 상품번호 목록
     * @return 삭제 건수
     */
    int deleteByIds(List<Long> productIds);
}
