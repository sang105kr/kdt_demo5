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
} 