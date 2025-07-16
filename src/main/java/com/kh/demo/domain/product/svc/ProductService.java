package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.document.ProductDocument;
import com.kh.demo.domain.common.entity.UploadFile;

import java.util.List;
import java.util.Optional;

/**
 * 상품 서비스 인터페이스
 * Oracle 트랜잭션 처리 + Elasticsearch 검색 + 고급 검색 기능
 */
public interface ProductService {
    
    /**
     * 상품 등록 (Oracle + Elasticsearch 동기화 + 파일 첨부)
     * @param products 상품 정보
     * @param imageFiles 이미지 파일 목록
     * @param manualFiles 설명서 파일 목록
     * @return 등록된 상품 ID
     */
    Long save(Products products, List<UploadFile> imageFiles, List<UploadFile> manualFiles);
    
    /**
     * 상품 등록 (파일 없이)
     * @param products 상품 정보
     * @return 등록된 상품 ID
     */
    Long save(Products products);
    
    /**
     * 모든 상품 목록 조회 (Oracle)
     * @return 상품 목록
     */
    List<Products> findAll();
    
    /**
     * 상품 목록 페이징 조회 (Oracle)
     * @param pageNo 페이지 번호
     * @param numOfRows 페이지당 행 수
     * @return 상품 목록
     */
    List<Products> findAll(int pageNo, int numOfRows);
    
    /**
     * 상품 ID로 조회 (Oracle)
     * @param productId 상품 ID
     * @return 상품 정보
     */
    Optional<Products> findById(Long productId);
    
    /**
     * 상품 수정 (Oracle + Elasticsearch 동기화 + 파일 첨부)
     * @param productId 상품 ID
     * @param products 수정할 상품 정보
     * @param imageFiles 새로 추가할 이미지 파일 목록
     * @param manualFiles 새로 추가할 설명서 파일 목록
     * @param deleteImageIds 삭제할 이미지 파일 ID 목록
     * @param deleteManualIds 삭제할 설명서 파일 ID 목록
     * @return 수정된 행 수
     */
    int updateById(Long productId, Products products, List<UploadFile> imageFiles, List<UploadFile> manualFiles, 
                   List<Long> deleteImageIds, List<Long> deleteManualIds);
    
    /**
     * 상품 수정 (파일 없이)
     * @param productId 상품 ID
     * @param products 수정할 상품 정보
     * @return 수정된 행 수
     */
    int updateById(Long productId, Products products);
    
    /**
     * 상품 삭제 (단건, Oracle + Elasticsearch 동기화 + 파일 삭제)
     * @param productId 상품 ID
     * @return 삭제된 행 수
     */
    int deleteById(Long productId);
    
    /**
     * 상품 삭제 (여러건, Oracle + Elasticsearch 동기화 + 파일 삭제)
     * @param productIds 상품 ID 목록
     * @return 삭제된 행 수
     */
    int deleteByIds(List<Long> productIds);
    
    /**
     * 상품 총 건수 조회 (Oracle)
     * @return 총 건수
     */
    int getTotalCount();

    /**
     * 파일 관련 메서드들
     */
    
    /**
     * 상품의 이미지 파일 목록 조회
     * @param productId 상품 ID
     * @return 이미지 파일 목록
     */
    List<UploadFile> findProductImages(Long productId);
    
    /**
     * 상품의 설명서 파일 목록 조회
     * @param productId 상품 ID
     * @return 설명서 파일 목록
     */
    List<UploadFile> findProductManuals(Long productId);
    
    /**
     * 파일 삭제 (물리적 파일 + DB 레코드)
     * @param fileIds 삭제할 파일 ID 목록
     * @return 삭제된 파일 수
     */
    int deleteFiles(List<Long> fileIds);

    /**
     * Elasticsearch 검색 메서드들
     */
    
    /**
     * 상품명으로 검색 (Elasticsearch) - 하이라이팅 포함
     * @param pname 상품명
     * @return 검색 결과
     */
    List<ProductDocument> searchByPname(String pname);

    /**
     * 상품설명으로 검색 (Elasticsearch) - 하이라이팅 포함
     * @param description 상품설명
     * @return 검색 결과
     */
    List<ProductDocument> searchByDescription(String description);

    /**
     * 카테고리로 검색 (Elasticsearch) - 하이라이팅 포함
     * @param category 카테고리
     * @return 검색 결과
     */
    List<ProductDocument> searchByCategory(String category);

    /**
     * 가격 범위로 검색 (Elasticsearch)
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return 검색 결과
     */
    List<ProductDocument> searchByPriceRange(Long minPrice, Long maxPrice);

    /**
     * 평점 이상으로 검색 (Elasticsearch)
     * @param minRating 최소 평점
     * @return 검색 결과
     */
    List<ProductDocument> searchByRating(Double minRating);

    /**
     * 고급 검색 및 관리 메서드들
     */
    
    /**
     * 하이라이팅된 설명 검색
     * @param keyword 검색 키워드
     * @return 하이라이팅된 검색 결과
     */
    List<ProductDocument> highlightDescription(String keyword);

    /**
     * 상품명 자동완성
     * @param prefix 접두사
     * @return 자동완성 결과 목록
     */
    List<String> autocompletePname(String prefix);

    /**
     * 고급 상품명 검색 (하이라이팅 포함)
     * @param pname 상품명
     * @return 검색 결과
     */
    List<ProductDocument> searchProductsByPname(String pname);

    /**
     * 전체 데이터 동기화 (Oracle → Elasticsearch)
     */
    void syncAllToElasticsearch();

    /**
     * 데이터 개수 비교 (Oracle vs Elasticsearch)
     */
    void compareDataCount();

    /**
     * 고객용 검색 메서드들
     */
    
    /**
     * 키워드로 상품 검색 (Oracle)
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지당 크기
     * @return 검색 결과
     */
    List<Products> searchProductsByKeyword(String keyword, int page, int size);
    
    /**
     * 키워드 검색 결과 개수 (Oracle)
     * @param keyword 검색 키워드
     * @return 검색 결과 개수
     */
    int countProductsByKeyword(String keyword);
    
    /**
     * 카테고리별 상품 조회 (Oracle)
     * @param category 카테고리
     * @param page 페이지 번호
     * @param size 페이지당 크기
     * @return 상품 목록
     */
    List<Products> getProductsByCategory(String category, int page, int size);
    
    /**
     * 카테고리별 상품 개수 (Oracle)
     * @param category 카테고리
     * @return 상품 개수
     */
    int countProductsByCategory(String category);
    
    /**
     * 전체 상품 조회 (페이징)
     * @param page 페이지 번호
     * @param size 페이지당 크기
     * @return 상품 목록
     */
    List<Products> getAllProducts(int page, int size);
    
    /**
     * 전체 상품 개수
     * @return 전체 상품 개수
     */
    int getTotalProductCount();
    
    /**
     * 상품 ID로 조회 (null 반환 가능)
     * @param productId 상품 ID
     * @return 상품 정보 (없으면 null)
     */
    Products getProductById(Long productId);
    
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
