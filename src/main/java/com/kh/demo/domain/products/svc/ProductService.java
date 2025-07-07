package com.kh.demo.domain.products.svc;

import com.kh.demo.domain.entity.Products;
import com.kh.demo.domain.document.ProductDocument;

import java.util.List;
import java.util.Optional;

/**
 * 상품 서비스 인터페이스 (Oracle + Elasticsearch 동기화)
 */
public interface ProductService {
    
    /**
     * 상품 등록 (Oracle + Elasticsearch 동기화)
     * @param products 상품 정보
     * @return 상품번호
     */
    Long save(Products products);
    
    /**
     * 상품 목록 조회 (Oracle)
     * @return 상품 목록
     */
    List<Products> findAll();
    
    /**
     * 상품 목록 조회 (페이징, Oracle)
     * @param pageNo 페이지 번호
     * @param numOfRows 페이지당 행 수
     * @return 상품 목록
     */
    List<Products> findAll(int pageNo, int numOfRows);
    
    /**
     * 상품 총 건수 (Oracle)
     * @return 총 건수
     */
    int getTotalCount();
    
    /**
     * 상품 조회 (Oracle)
     * @param productId 상품번호
     * @return 상품정보
     */
    Optional<Products> findById(Long productId);
    
    /**
     * 상품 수정 (Oracle + Elasticsearch 동기화)
     * @param productId 상품번호
     * @param products 상품정보
     * @return 수정 건수
     */
    int updateById(Long productId, Products products);
    
    /**
     * 상품 삭제 (Oracle + Elasticsearch 동기화)
     * @param productId 상품번호
     * @return 삭제 건수
     */
    int deleteById(Long productId);
    
    /**
     * 상품 삭제 (여러건, Oracle + Elasticsearch 동기화)
     * @param productIds 상품번호 목록
     * @return 삭제 건수
     */
    int deleteByIds(List<Long> productIds);
    
    /**
     * Elasticsearch 검색 - 상품명으로 검색
     * @param pname 상품명
     * @return 검색 결과
     */
    List<ProductDocument> searchByPname(String pname);
    
    /**
     * Elasticsearch 검색 - 상품설명으로 검색
     * @param description 상품설명
     * @return 검색 결과
     */
    List<ProductDocument> searchByDescription(String description);
    
    /**
     * Elasticsearch 검색 - 카테고리로 검색
     * @param category 카테고리
     * @return 검색 결과
     */
    List<ProductDocument> searchByCategory(String category);
    
    /**
     * Elasticsearch 검색 - 가격 범위로 검색
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return 검색 결과
     */
    List<ProductDocument> searchByPriceRange(Long minPrice, Long maxPrice);
    
    /**
     * Elasticsearch 검색 - 평점 이상으로 검색
     * @param rating 평점
     * @return 검색 결과
     */
    List<ProductDocument> searchByRating(Double rating);
    
    /**
     * 전체 데이터 동기화 (Oracle → Elasticsearch)
     */
    void syncAllToElasticsearch();
    
    /**
     * 데이터 개수 비교
     */
    void compareDataCount();
    
    /**
     * Elasticsearch 검색 - 상품설명 하이라이팅 검색
     * @param keyword 검색어
     * @return 하이라이팅 결과 포함 검색 결과
     */
    List<ProductDocument> highlightDescription(String keyword);
    
    /**
     * Elasticsearch 자동완성 - 상품명 prefix 기반 자동완성
     * @param prefix 입력값
     * @return 자동완성 후보 리스트
     */
    List<String> autocompletePname(String prefix);
    
    /**
     * Elasticsearch 검색 - 상품명으로 검색하여 상품 목록 반환
     * @param pname 검색할 상품명
     * @return 검색된 상품 목록
     */
    List<ProductDocument> searchProductsByPname(String pname);
}
