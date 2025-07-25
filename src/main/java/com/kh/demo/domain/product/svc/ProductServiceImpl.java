package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.document.ProductDocument;
import com.kh.demo.domain.common.entity.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Product Service Implementation
 * 분리된 서비스들을 조합하여 전체 상품 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductOracleService productOracleService;
    private final ProductElasticsearchService productElasticsearchService;
    private final ProductFileService productFileService;

    /**
     * Save product with files (Oracle + Elasticsearch 동기화 + 파일 첨부)
     */
    @Override
    @Transactional
    public Long save(Products products, List<UploadFile> imageFiles, List<UploadFile> manualFiles) {
        // 1. Oracle에 상품 저장
        Long productId = productOracleService.save(products);
        
        // 2. 파일 저장
        productFileService.saveProductFiles(productId, imageFiles, manualFiles);
        
        // 3. Elasticsearch에 동기화
        try {
            products.setProductId(productId); // 생성된 ID 설정
            ProductDocument productDocument = ProductDocument.from(products);
            productElasticsearchService.save(productDocument);
        } catch (Exception e) {
            log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            // Elasticsearch 동기화 실패는 로그만 남기고 트랜잭션은 커밋
        }

        return productId;
    }

    @Override
    @Transactional
    public Long save(Products products) {
        Long productId = productOracleService.save(products);
        
        // Elasticsearch에 동기화
        try {
            products.setProductId(productId); // 생성된 ID 설정
            ProductDocument productDocument = ProductDocument.from(products);
            productElasticsearchService.save(productDocument);
        } catch (Exception e) {
            log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
        }

        return productId;
    }

    @Override
    public List<Products> findAll() {
        return productOracleService.findAll();
    }

    @Override
    public List<Products> findAll(int pageNo, int numOfRows) {
        return productOracleService.findAll(pageNo, numOfRows);
    }

    @Override
    public Optional<Products> findById(Long productId) {
        return productOracleService.findById(productId);
    }

    @Override
    @Transactional
    public int updateById(Long productId, Products products, List<UploadFile> imageFiles, List<UploadFile> manualFiles, 
                         List<Long> deleteImageIds, List<Long> deleteManualIds) {
        // 1. Oracle 업데이트
        int updatedRows = productOracleService.updateById(productId, products);
        
        // 2. 파일 처리
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            productFileService.deleteFiles(deleteImageIds);
        }
        if (deleteManualIds != null && !deleteManualIds.isEmpty()) {
            productFileService.deleteFiles(deleteManualIds);
        }
        
        if (imageFiles != null && !imageFiles.isEmpty()) {
            productFileService.saveProductFiles(productId, imageFiles, null);
        }
        if (manualFiles != null && !manualFiles.isEmpty()) {
            productFileService.saveProductFiles(productId, null, manualFiles);
        }
        
        // 3. Elasticsearch 동기화
        if (updatedRows > 0) {
            try {
                ProductDocument productDocument = ProductDocument.from(products);
                productElasticsearchService.update(productDocument);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            }
        }

        return updatedRows;
    }

    @Override
    @Transactional
    public int updateById(Long productId, Products products) {
        int updatedRows = productOracleService.updateById(productId, products);
        
        // Elasticsearch 동기화
        if (updatedRows > 0) {
            try {
                ProductDocument productDocument = ProductDocument.from(products);
                productElasticsearchService.update(productDocument);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            }
        }

        return updatedRows;
    }

    @Override
    @Transactional
    public int deleteById(Long productId) {
        // 1. 파일 삭제
        productFileService.deleteProductFiles(productId);
        
        // 2. Oracle 삭제
        int deletedRows = productOracleService.deleteById(productId);
        
        // 3. Elasticsearch 삭제
        if (deletedRows > 0) {
            try {
                productElasticsearchService.deleteById(productId);
            } catch (Exception e) {
                log.error("Elasticsearch 삭제 실패: {}", e.getMessage(), e);
            }
        }

        return deletedRows;
    }

    @Override
    @Transactional
    public int deleteByIds(List<Long> productIds) {
        int totalDeleted = 0;
        
        for (Long productId : productIds) {
            try {
                int deleted = deleteById(productId);
                totalDeleted += deleted;
            } catch (Exception e) {
                log.error("상품 삭제 실패 - productId: {}", productId, e);
            }
        }
        
        return totalDeleted;
    }

    @Override
    public int getTotalCount() {
        return productOracleService.getTotalCount();
    }

    @Override
    public List<UploadFile> findProductImages(Long productId) {
        return productFileService.findProductImages(productId);
    }

    @Override
    public List<UploadFile> findProductManuals(Long productId) {
        return productFileService.findProductManuals(productId);
    }

    @Override
    @Transactional
    public int deleteFiles(List<Long> fileIds) {
        return productFileService.deleteFiles(fileIds);
    }

    // Elasticsearch 검색 메서드들
    @Override
    public List<ProductDocument> searchByPname(String pname, int limit) {
        return productElasticsearchService.searchByPname(pname, limit);
    }

    @Override
    public List<ProductDocument> searchByDescription(String description) {
        return productElasticsearchService.searchByDescription(description);
    }

    @Override
    public List<ProductDocument> searchByCategory(String category) {
        return productElasticsearchService.searchByCategory(category);
    }

    @Override
    public List<ProductDocument> searchByPriceRange(Long minPrice, Long maxPrice) {
        return productElasticsearchService.searchByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<ProductDocument> searchByRating(Double minRating) {
        return productElasticsearchService.searchByRating(minRating);
    }

    @Override
    public List<ProductDocument> findAllProducts() {
        return productElasticsearchService.findAllProducts();
    }

    @Override
    public List<ProductDocument> searchWithMultipleCriteria(String keyword, Long minPrice, Long maxPrice, 
                                                          Double minRating, String category) {
        return productElasticsearchService.searchWithMultipleCriteria(keyword, minPrice, maxPrice, minRating, category);
    }

    public List<String> autocompletePname(String prefix) {
        return productElasticsearchService.autocompletePname(prefix);
    }

    @Transactional
    public void syncAllToElasticsearch() {
        List<Products> products = productOracleService.findAll();
        productElasticsearchService.syncAllToElasticsearch(products);
    }

    // Oracle 검색 메서드들
    @Override
    public List<Products> searchProductsByKeyword(String keyword, int page, int size) {
        return productOracleService.findAll(page, size); // 간단한 페이징으로 대체
    }

    @Override
    public int countProductsByKeyword(String keyword) {
        return productOracleService.getTotalCount();
    }

    @Override
    public List<Products> getProductsByCategory(String category, int page, int size) {
        log.info("카테고리별 상품 조회 - category: {}, page: {}, size: {}", category, page, size);
        return productOracleService.findByCategory(category, page, size);
    }

    @Override
    public int countProductsByCategory(String category) {
        log.info("카테고리별 상품 개수 조회 - category: {}", category);
        return productOracleService.countByCategory(category);
    }

    @Override
    public List<Products> getAllProducts(int page, int size) {
        return productOracleService.findAll(page, size);
    }

    @Override
    public int getTotalProductCount() {
        return productOracleService.getTotalCount();
    }

    @Override
    public Products getProductById(Long productId) {
        return productOracleService.findById(productId).orElse(null);
    }

    @Override
    @Transactional
    public int decreaseStock(Long productId, Integer quantity) {
        return productOracleService.decreaseStock(productId, quantity);
    }

    @Override
    @Transactional
    public int increaseStock(Long productId, Integer quantity) {
        return productOracleService.increaseStock(productId, quantity);
    }

    @Override
    public List<ProductDocument> highlightDescription(String keyword) {
        // 간단한 구현으로 대체
        return productElasticsearchService.searchByDescription(keyword);
    }

    @Override
    public void compareDataCount() {
        try {
            long oracleCount = productOracleService.getTotalCount();
            long elasticsearchCount = productElasticsearchService.findAllProducts().size();
            
            log.info("데이터 개수 비교 - Oracle: {}, Elasticsearch: {}", oracleCount, elasticsearchCount);
            
            if (oracleCount != elasticsearchCount) {
                log.warn("데이터 개수가 일치하지 않습니다! 동기화가 필요합니다.");
            } else {
                log.info("데이터 개수가 일치합니다.");
            }
        } catch (Exception e) {
            log.error("데이터 개수 비교 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
