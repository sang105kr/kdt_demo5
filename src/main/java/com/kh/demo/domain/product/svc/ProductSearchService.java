package com.kh.demo.domain.product.svc;

import com.kh.demo.web.product.controller.page.dto.SearchCriteria;
import com.kh.demo.web.product.controller.page.dto.SearchResult;
import com.kh.demo.web.product.controller.page.dto.ProductListDTO;
import com.kh.demo.web.product.controller.page.dto.ProductDetailDTO;

import java.util.List;

/**
 * 상품 검색 서비스 인터페이스
 * - Elasticsearch 기반 검색
 * - 복합 조건 검색 지원
 * - 실시간 데이터 보완
 */
public interface ProductSearchService {
    
    /**
     * 통합 검색 (Elasticsearch + Oracle 보완)
     */
    SearchResult<ProductListDTO> search(SearchCriteria criteria);
    
    /**
     * 카테고리별 검색
     */
    SearchResult<ProductListDTO> searchByCategory(String category, int page, int size);
    
    /**
     * 키워드 검색
     */
    SearchResult<ProductListDTO> searchByKeyword(String keyword, int page, int size);
    
    /**
     * 가격 범위 검색
     */
    SearchResult<ProductListDTO> searchByPriceRange(Long minPrice, Long maxPrice, int page, int size);
    
    /**
     * 평점 검색
     */
    SearchResult<ProductListDTO> searchByRating(Double minRating, int page, int size);
    
    /**
     * 상품 상세 정보 조회
     */
    ProductDetailDTO getProductDetail(Long productId);
    
    /**
     * 자동완성
     */
    List<String> autocomplete(String prefix);
    
    /**
     * 인기 검색어
     */
    List<String> getPopularKeywords();
    
    /**
     * 검색 히스토리 저장
     */
    void saveSearchHistory(String keyword, Long memberId);
    
    /**
     * 검색 히스토리 조회
     */
    List<String> getSearchHistory(Long memberId);
} 