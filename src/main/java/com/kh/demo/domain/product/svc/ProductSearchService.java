package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.document.ProductDocument;
import com.kh.demo.web.dto.ProductListDTO;
import com.kh.demo.web.dto.SearchCriteria;
import com.kh.demo.web.dto.SearchResult;
import com.kh.demo.web.dto.ProductDetailDTO;

import java.util.List;

/**
 * 통합 상품 검색 서비스
 * Elasticsearch: 검색 및 목록 조회
 * Oracle: 실시간 데이터 (재고, 가격 등)
 */
public interface ProductSearchService {
    
    /**
     * 통합 검색 (Elasticsearch 우선, 실패 시 Oracle fallback)
     */
    SearchResult<ProductListDTO> search(SearchCriteria criteria);
    
    /**
     * 카테고리별 상품 조회
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
     * 평점 이상 검색
     */
    SearchResult<ProductListDTO> searchByRating(Double minRating, int page, int size);
    
    /**
     * 상품 상세 정보 조회 (Oracle + Elasticsearch 통합)
     */
    ProductDetailDTO getProductDetail(Long productId);
    
    /**
     * 자동완성 검색
     */
    List<String> autocomplete(String prefix);
    
    /**
     * 인기 검색어 조회
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