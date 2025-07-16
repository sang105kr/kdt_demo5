package com.kh.demo.domain.product.svc;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.dao.ProductDocumentRepository;
import com.kh.demo.domain.product.search.document.ProductDocument;
import com.kh.demo.domain.common.dao.UploadFileDAO;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Product Service Implementation
 * Oracle 트랜잭션 처리 + Elasticsearch 동기화 + 고급 검색 기능 + 파일 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;
    private final ProductDocumentRepository productDocumentRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;
    private final UploadFileDAO uploadFileDAO;

    @Value("${file.upload.path}")
    private String uploadPath;

    // 파일 타입 코드 상수
    private static final Long PRODUCT_IMAGE_CODE = 7L; // PRODUCT_IMAGE
    private static final Long PRODUCT_MANUAL_CODE = 8L; // PRODUCT_MANUAL

    /**
     * Save product with files (Oracle + Elasticsearch 동기화 + 파일 첨부)
     */
    @Override
    @Transactional
    public Long save(Products products, List<UploadFile> imageFiles, List<UploadFile> manualFiles) {
        // 비즈니스 로직 검증
        validateProduct(products);
        
        // Set default values
        if (products.getCdate() == null) {
            products.setCdate(LocalDateTime.now());
        }
        if (products.getUdate() == null) {
            products.setUdate(LocalDateTime.now());
        }

        // 1. Oracle에 상품 저장
        Long productId = productDAO.save(products);
        log.info("상품 등록 완료 - Oracle: {}", productId);
        
        // 2. 파일 저장
        saveProductFiles(productId, imageFiles, manualFiles);
        
        // 3. Elasticsearch에 동기화
        try {
            products.setProductId(productId); // 생성된 ID 설정
            ProductDocument productDocument = ProductDocument.from(products);
            productDocumentRepository.save(productDocument);
            log.info("상품 등록 완료 - Elasticsearch: {}", productId);
        } catch (Exception e) {
            log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            // Elasticsearch 동기화 실패는 로그만 남기고 트랜잭션은 커밋
        }

        return productId;
    }

    /**
     * Save product (Oracle + Elasticsearch 동기화) - 기존 메서드
     */
    @Override
    @Transactional
    public Long save(Products products) {
        // 비즈니스 로직 검증
        validateProduct(products);
        
        // Set default values
        if (products.getCdate() == null) {
            products.setCdate(LocalDateTime.now());
        }
        if (products.getUdate() == null) {
            products.setUdate(LocalDateTime.now());
        }

        // 1. Oracle에 저장
        Long productId = productDAO.save(products);
        log.info("상품 등록 완료 - Oracle: {}", productId);
        
        // 2. Elasticsearch에 동기화
        try {
            products.setProductId(productId); // 생성된 ID 설정
            ProductDocument productDocument = ProductDocument.from(products);
            productDocumentRepository.save(productDocument);
            log.info("상품 등록 완료 - Elasticsearch: {}", productId);
        } catch (Exception e) {
            log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            // Elasticsearch 동기화 실패는 로그만 남기고 트랜잭션은 커밋
        }

        return productId;
    }

    /**
     * Find all products (Oracle)
     */
    @Override
    public List<Products> findAll() {
        return productDAO.findAll();
    }

    /**
     * Find products with paging (Oracle)
     */
    @Override
    public List<Products> findAll(int pageNo, int numOfRows) {
        return productDAO.findAllWithPaging(pageNo, numOfRows);
    }

    /**
     * Find product by ID (Oracle)
     */
    @Override
    public Optional<Products> findById(Long productId) {
        return productDAO.findById(productId);
    }

    /**
     * Update product with files (Oracle + Elasticsearch 동기화 + 파일 첨부)
     */
    @Override
    @Transactional
    public int updateById(Long productId, Products products, List<UploadFile> imageFiles, List<UploadFile> manualFiles, 
                         List<Long> deleteImageIds, List<Long> deleteManualIds) {
        // 비즈니스 로직 검증
        validateProduct(products);
        
        // 비즈니스 로직: 상품 존재 여부 확인
        if (!productDAO.findById(productId).isPresent()) {
            throw new BusinessValidationException("상품번호: " + productId + "를 찾을 수 없습니다.");
        }
        
        products.setUdate(LocalDateTime.now());
        
        // 1. Oracle 업데이트
        int updatedRows = productDAO.updateById(productId, products);
        log.info("상품 수정 완료 - Oracle: {}", productId);
        
        // 2. 파일 처리
        // 2-1. 삭제할 파일 처리
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            deleteFiles(deleteImageIds);
        }
        if (deleteManualIds != null && !deleteManualIds.isEmpty()) {
            deleteFiles(deleteManualIds);
        }
        
        // 2-2. 새로 추가할 파일 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            saveProductFiles(productId, imageFiles, null);
        }
        if (manualFiles != null && !manualFiles.isEmpty()) {
            saveProductFiles(productId, null, manualFiles);
        }
        
        // 3. Elasticsearch 동기화
        if (updatedRows > 0) {
            try {
                products.setProductId(productId);
                ProductDocument productDocument = ProductDocument.from(products);
                productDocumentRepository.save(productDocument);
                log.info("상품 수정 완료 - Elasticsearch: {}", productId);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
                // Elasticsearch 동기화 실패는 로그만 남기고 트랜잭션은 커밋
            }
        }
        
        return updatedRows;
    }

    /**
     * Update product (Oracle + Elasticsearch 동기화) - 기존 메서드
     */
    @Override
    @Transactional
    public int updateById(Long productId, Products products) {
        // 비즈니스 로직 검증
        validateProduct(products);
        
        // 비즈니스 로직: 상품 존재 여부 확인
        if (!productDAO.findById(productId).isPresent()) {
            throw new BusinessValidationException("상품번호: " + productId + "를 찾을 수 없습니다.");
        }
        
        products.setUdate(LocalDateTime.now());
        
        // 1. Oracle 업데이트
        int updatedRows = productDAO.updateById(productId, products);
        log.info("상품 수정 완료 - Oracle: {}", productId);
        
        // 2. Elasticsearch 동기화
        if (updatedRows > 0) {
            try {
                products.setProductId(productId);
                ProductDocument productDocument = ProductDocument.from(products);
                productDocumentRepository.save(productDocument);
                log.info("상품 수정 완료 - Elasticsearch: {}", productId);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
                // Elasticsearch 동기화 실패는 로그만 남기고 트랜잭션은 커밋
            }
        }
        
        return updatedRows;
    }

    /**
     * Delete product by ID (Oracle + Elasticsearch 동기화 + 파일 삭제)
     */
    @Override
    @Transactional
    public int deleteById(Long productId) {
        // 비즈니스 로직: 상품 존재 여부 확인
        if (!productDAO.findById(productId).isPresent()) {
            throw new BusinessValidationException("상품번호: " + productId + "를 찾을 수 없습니다.");
        }
        
        // 1. 상품 관련 파일 삭제
        deleteProductFiles(productId);
        
        // 2. Oracle 삭제
        int deletedRows = productDAO.deleteById(productId);
        log.info("상품 삭제 완료 - Oracle: {}", productId);
        
        // 3. Elasticsearch 동기화
        if (deletedRows > 0) {
            try {
                productDocumentRepository.deleteById(productId);
                log.info("상품 삭제 완료 - Elasticsearch: {}", productId);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
                // Elasticsearch 삭제 실패는 로그만 남기고 트랜잭션은 커밋
            }
        }
        
        return deletedRows;
    }

    /**
     * Delete products by IDs (Oracle + Elasticsearch 동기화 + 파일 삭제)
     */
    @Override
    @Transactional
    public int deleteByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessValidationException("삭제할 상품 ID 목록이 비어있습니다.");
        }
        
        // 1. 상품 관련 파일들 삭제
        for (Long productId : productIds) {
            deleteProductFiles(productId);
        }
        
        // 2. Oracle 삭제
        int deletedRows = productDAO.deleteByIds(productIds);
        log.info("상품 삭제 완료 - Oracle: {}건", deletedRows);
        
        // 3. Elasticsearch 동기화
        if (deletedRows > 0) {
            try {
                productDocumentRepository.deleteAllById(productIds);
                log.info("상품 삭제 완료 - Elasticsearch: {}건", deletedRows);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
                // Elasticsearch 삭제 실패는 로그만 남기고 트랜잭션은 커밋
            }
        }
        
        return deletedRows;
    }

    /**
     * Get total count (Oracle)
     */
    @Override
    public int getTotalCount() {
        return productDAO.getTotalCount();
    }

    /**
     * 파일 관련 메서드들
     */
    
    /**
     * 상품의 이미지 파일 목록 조회
     */
    @Override
    public List<UploadFile> findProductImages(Long productId) {
        return uploadFileDAO.findByCodeAndRid(PRODUCT_IMAGE_CODE, productId.toString());
    }
    
    /**
     * 상품의 설명서 파일 목록 조회
     */
    @Override
    public List<UploadFile> findProductManuals(Long productId) {
        return uploadFileDAO.findByCodeAndRid(PRODUCT_MANUAL_CODE, productId.toString());
    }
    
    /**
     * 파일 삭제 (물리적 파일 + DB 레코드)
     */
    @Override
    @Transactional
    public int deleteFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return 0;
        }
        
        int deletedCount = 0;
        for (Long fileId : fileIds) {
            try {
                // 1. 파일 정보 조회
                Optional<UploadFile> fileOpt = uploadFileDAO.findById(fileId);
                if (fileOpt.isPresent()) {
                    UploadFile file = fileOpt.get();
                    
                    // 2. 물리적 파일 삭제
                    deletePhysicalFile(file.getStoreFilename());
                    
                    // 3. DB 레코드 삭제
                    uploadFileDAO.delete(fileId);
                    deletedCount++;
                    log.info("파일 삭제 완료: {}", file.getUploadFilename());
                }
            } catch (Exception e) {
                log.error("파일 삭제 실패 - fileId: {}, error: {}", fileId, e.getMessage(), e);
            }
        }
        
        return deletedCount;
    }

    /**
     * 상품 파일 저장 (이미지 + 설명서)
     */
    private void saveProductFiles(Long productId, List<UploadFile> imageFiles, List<UploadFile> manualFiles) {
        String productIdStr = productId.toString();
        
        // 이미지 파일 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (UploadFile file : imageFiles) {
                file.setCode(PRODUCT_IMAGE_CODE);
                file.setRid(productIdStr);
                uploadFileDAO.save(file);
                log.info("상품 이미지 파일 저장 완료: {}", file.getUploadFilename());
            }
        }
        
        // 설명서 파일 저장
        if (manualFiles != null && !manualFiles.isEmpty()) {
            for (UploadFile file : manualFiles) {
                file.setCode(PRODUCT_MANUAL_CODE);
                file.setRid(productIdStr);
                uploadFileDAO.save(file);
                log.info("상품 설명서 파일 저장 완료: {}", file.getUploadFilename());
            }
        }
    }
    
    /**
     * 상품 관련 모든 파일 삭제
     */
    private void deleteProductFiles(Long productId) {
        String productIdStr = productId.toString();
        
        // 이미지 파일들 삭제
        List<UploadFile> imageFiles = uploadFileDAO.findByCodeAndRid(PRODUCT_IMAGE_CODE, productIdStr);
        for (UploadFile file : imageFiles) {
            deletePhysicalFile(file.getStoreFilename());
            uploadFileDAO.delete(file.getUploadfileId());
        }
        
        // 설명서 파일들 삭제
        List<UploadFile> manualFiles = uploadFileDAO.findByCodeAndRid(PRODUCT_MANUAL_CODE, productIdStr);
        for (UploadFile file : manualFiles) {
            deletePhysicalFile(file.getStoreFilename());
            uploadFileDAO.delete(file.getUploadfileId());
        }
        
        log.info("상품 관련 파일 삭제 완료 - productId: {}", productId);
    }
    
    /**
     * 물리적 파일 삭제
     */
    private void deletePhysicalFile(String storeFilename) {
        try {
            Path filePath = Paths.get(uploadPath, storeFilename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("물리적 파일 삭제 완료: {}", storeFilename);
            }
        } catch (IOException e) {
            log.error("물리적 파일 삭제 실패: {}, error: {}", storeFilename, e.getMessage(), e);
        }
    }

    /**
     * Elasticsearch 검색 메서드들 (고급 기능 포함)
     */
    
    /**
     * 상품명으로 검색 (Elasticsearch) - 하이라이팅 포함
     */
    @Override
    public List<ProductDocument> searchByPname(String pname) {
        log.info("상품명 검색 요청 - pname: {}", pname);
        
        Query multiMatchQuery = MultiMatchQuery.of(m -> m
            .query(pname)
            .fields("pname")
            .fuzziness("AUTO")
        )._toQuery();

        // 하이라이팅 설정
        HighlightFieldParameters highlightFieldParameters = HighlightFieldParameters.builder()
            .withPreTags("<b>")
            .withPostTags("</b>")
            .build();
        
        HighlightField highlightField = new HighlightField("pname", highlightFieldParameters);
        Highlight highlight = new Highlight(List.of(highlightField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(multiMatchQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(PageRequest.of(0, 10))
            .build();

        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);
        
        log.info("상품명 검색 결과 - 총 hits: {}", searchHits.getTotalHits());

        return searchHits.getSearchHits().stream()
            .map(hit -> {
                ProductDocument doc = hit.getContent();
                // 하이라이팅 결과 적용
                if (hit.getHighlightFields() != null && hit.getHighlightFields().containsKey("pname")) {
                    String highlightedPname = String.join(" ", hit.getHighlightFields().get("pname"));
                    doc.setPname(highlightedPname);
                }
                return doc;
            })
            .toList();
    }

    /**
     * 상품설명으로 검색 (Elasticsearch) - 하이라이팅 포함
     */
    @Override
    public List<ProductDocument> searchByDescription(String description) {
        log.info("상품설명 검색 요청 - description: {}", description);
        
        Query multiMatchQuery = MultiMatchQuery.of(m -> m
            .query(description)
            .fields("description")
            .fuzziness("AUTO")
        )._toQuery();

        // 하이라이팅 설정
        HighlightFieldParameters highlightFieldParameters = HighlightFieldParameters.builder()
            .withPreTags("<b>")
            .withPostTags("</b>")
            .build();
        
        HighlightField highlightField = new HighlightField("description", highlightFieldParameters);
        Highlight highlight = new Highlight(List.of(highlightField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(multiMatchQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(PageRequest.of(0, 10))
            .build();

        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);
        
        log.info("상품설명 검색 결과 - 총 hits: {}", searchHits.getTotalHits());

        return searchHits.getSearchHits().stream()
            .map(hit -> {
                ProductDocument doc = hit.getContent();
                // 하이라이팅 결과 적용
                if (hit.getHighlightFields() != null && hit.getHighlightFields().containsKey("description")) {
                    String highlightedDescription = String.join(" ", hit.getHighlightFields().get("description"));
                    doc.setDescription(highlightedDescription);
                }
                return doc;
            })
            .toList();
    }

    /**
     * 카테고리로 검색 (Elasticsearch) - 하이라이팅 포함
     */
    @Override
    public List<ProductDocument> searchByCategory(String category) {
        log.info("카테고리 검색 요청 - category: {}", category);
        
        Query multiMatchQuery = MultiMatchQuery.of(m -> m
            .query(category)
            .fields("category")
            .fuzziness("AUTO")
        )._toQuery();

        // 하이라이팅 설정
        HighlightFieldParameters highlightFieldParameters = HighlightFieldParameters.builder()
            .withPreTags("<b>")
            .withPostTags("</b>")
            .build();
        
        HighlightField highlightField = new HighlightField("category", highlightFieldParameters);
        Highlight highlight = new Highlight(List.of(highlightField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(multiMatchQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(PageRequest.of(0, 10))
            .build();

        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);
        
        log.info("카테고리 검색 결과 - 총 hits: {}", searchHits.getTotalHits());

        return searchHits.getSearchHits().stream()
            .map(hit -> {
                ProductDocument doc = hit.getContent();
                // 하이라이팅 결과 적용
                if (hit.getHighlightFields() != null && hit.getHighlightFields().containsKey("category")) {
                    String highlightedCategory = String.join(" ", hit.getHighlightFields().get("category"));
                    doc.setCategory(highlightedCategory);
                }
                return doc;
            })
            .toList();
    }

    /**
     * 가격 범위로 검색 (Elasticsearch)
     */
    @Override
    public List<ProductDocument> searchByPriceRange(Long minPrice, Long maxPrice) {
        return productDocumentRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * 평점 이상으로 검색 (Elasticsearch)
     */
    @Override
    public List<ProductDocument> searchByRating(Double minRating) {
        return productDocumentRepository.findByRatingGreaterThanEqual(minRating);
    }

    /**
     * 하이라이팅된 설명 검색
     */
    public List<ProductDocument> highlightDescription(String keyword) {
        Query multiMatchQuery = MultiMatchQuery.of(m -> m
            .query(keyword)
            .fields("description")
            .fuzziness("AUTO")
        )._toQuery();

        // description 필드에 하이라이팅 적용
        HighlightFieldParameters highlightFieldParameters = HighlightFieldParameters.builder()
            .withPreTags("<b>")
            .withPostTags("</b>")
            .build();
        
        HighlightField highlightField = new HighlightField("description", highlightFieldParameters);
        Highlight highlight = new Highlight(List.of(highlightField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(multiMatchQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(PageRequest.of(0, 10))
            .build();

        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        return searchHits.getSearchHits().stream()
            .map(hit -> {
                ProductDocument doc = hit.getContent();
                // 하이라이팅 결과 적용
                if (hit.getHighlightFields() != null && hit.getHighlightFields().containsKey("description")) {
                    String highlightedDescription = String.join(" ", hit.getHighlightFields().get("description"));
                    doc.setDescription(highlightedDescription);
                }
                return doc;
            })
            .toList();
    }

    /**
     * 상품명 자동완성
     */
    public List<String> autocompletePname(String prefix) {
        log.info("자동완성 요청 - prefix: {}", prefix);
        
        // 메인 pname 필드에서 prefix 검색
        Query prefixQuery = co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery.of(p -> p
            .field("pname")
            .value(prefix)
        )._toQuery();

        log.info("생성된 쿼리: {}", prefixQuery.toString());

        // 하이라이팅 설정 - 메인 pname 필드에 하이라이팅 적용
        HighlightFieldParameters highlightFieldParameters = HighlightFieldParameters.builder()
            .withPreTags("<b>")
            .withPostTags("</b>")
            .build();
        
        HighlightField highlightField = new HighlightField("pname", highlightFieldParameters);
        Highlight highlight = new Highlight(List.of(highlightField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(prefixQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(PageRequest.of(0, 10))
            .build();

        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);
        
        log.info("검색 결과 - 총 hits: {}", searchHits.getTotalHits());

        List<String> results = searchHits.getSearchHits().stream()
            .map(hit -> {
                ProductDocument productDocument = hit.getContent();
                String pname = productDocument.getPname();
                
                log.info("=== 검색 결과 상세 ===");
                log.info("원본 상품명: {}", pname);
                log.info("하이라이팅 필드 존재 여부: {}", hit.getHighlightFields() != null);
                
                if (hit.getHighlightFields() != null) {
                    log.info("하이라이팅 필드 목록: {}", hit.getHighlightFields().keySet());
                    
                    // pname 필드에서 하이라이팅 확인
                    if (hit.getHighlightFields().containsKey("pname")) {
                        String highlightedPname = String.join(" ", hit.getHighlightFields().get("pname"));
                        pname = highlightedPname;
                        log.info("하이라이팅된 상품명: {}", pname);
                    } else {
                        log.info("검색된 상품 (하이라이팅 없음): {}", pname);
                    }
                } else {
                    log.info("검색된 상품: {}", pname);
                }
                
                return pname;
            })
            .toList();
            
        log.info("자동완성 결과 ({}개): {}", results.size(), results);
        return results;
    }

    /**
     * 고급 상품명 검색 (하이라이팅 포함)
     */
    public List<ProductDocument> searchProductsByPname(String pname) {
        log.info("상품명 검색 요청 - pname: {}", pname);
        
        // 상품명으로 검색 (fuzzy 검색 포함)
        Query multiMatchQuery = MultiMatchQuery.of(m -> m
            .query(pname)
            .fields("pname")
            .fuzziness("AUTO")
        )._toQuery();

        // 하이라이팅 설정
        HighlightFieldParameters highlightFieldParameters = HighlightFieldParameters.builder()
            .withPreTags("<b>")
            .withPostTags("</b>")
            .build();
        
        HighlightField highlightField = new HighlightField("pname", highlightFieldParameters);
        Highlight highlight = new Highlight(List.of(highlightField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(multiMatchQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(PageRequest.of(0, 20))  // 상품 목록은 20개까지
            .build();

        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);
        
        log.info("상품명 검색 결과 - 총 hits: {}", searchHits.getTotalHits());

        return searchHits.getSearchHits().stream()
            .map(hit -> {
                ProductDocument doc = hit.getContent();
                // 하이라이팅 결과 적용
                if (hit.getHighlightFields() != null && hit.getHighlightFields().containsKey("pname")) {
                    String highlightedPname = String.join(" ", hit.getHighlightFields().get("pname"));
                    doc.setPname(highlightedPname);
                }
                return doc;
            })
            .toList();
    }

    /**
     * 전체 데이터 동기화 (Oracle → Elasticsearch)
     */
    @Transactional
    public void syncAllToElasticsearch() {
        log.info("=== 전체 데이터 Elasticsearch 동기화 시작 ===");
        
        try {
            // 1. Elasticsearch 인덱스 초기화 (기존 데이터 삭제)
            productDocumentRepository.deleteAll();
            log.info("Elasticsearch 기존 데이터 삭제 완료");
            
            // 2. Oracle에서 모든 상품 데이터 조회
            List<Products> productsList = productDAO.findAll();
            log.info("Oracle에서 {}개의 상품 데이터 조회 완료", productsList.size());
            
            // 3. Products 엔티티를 ProductDocument로 변환
            List<ProductDocument> documents = productsList.stream()
                    .map(ProductDocument::from)
                    .collect(Collectors.toList());
            
            // 4. Elasticsearch에 일괄 저장
            productDocumentRepository.saveAll(documents);
            log.info("Elasticsearch에 {}개의 상품 데이터 저장 완료", documents.size());
            
            log.info("=== 전체 데이터 Elasticsearch 동기화 완료 ===");
            
        } catch (Exception e) {
            log.error("전체 데이터 Elasticsearch 동기화 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("전체 데이터 동기화 실패", e);
        }
    }

    /**
     * 데이터 개수 비교
     */
    public void compareDataCount() {
        long oracleCount = productDAO.getTotalCount();
        long elasticsearchCount = productDocumentRepository.count();
        
        log.info("데이터 개수 비교 - Oracle: {}, Elasticsearch: {}", oracleCount, elasticsearchCount);
        
        if (oracleCount != elasticsearchCount) {
            log.warn("데이터 개수가 일치하지 않습니다! 동기화가 필요합니다.");
        } else {
            log.info("데이터 개수가 일치합니다.");
        }
    }

    /**
     * 상품 전체 검증 로직
     */
    private void validateProduct(Products products) {
        // 상품명 검증
        validatePname(products.getPname());
        
        // 설명 검증
        validateDescription(products.getDescription());
        
        // 카테고리 검증
        validateCategory(products.getCategory());
        
        // 가격 검증
        validatePrice(products.getPrice());
        
        // 평점 검증
        validateRating(products.getRating());
        
        // 중복 상품명 검사 (신규 등록 시에만)
        if (products.getProductId() == null) {
            validateDuplicatePname(products.getPname());
        }
    }
    
    /**
     * 상품명 검증
     */
    private void validatePname(String pname) {
        if (pname == null || pname.trim().isEmpty()) {
            throw new BusinessValidationException("상품명은 필수입니다.");
        }
        if (pname.trim().length() < 2) {
            throw new BusinessValidationException("상품명은 2자 이상이어야 합니다.");
        }
        if (pname.trim().length() > 100) {
            throw new BusinessValidationException("상품명은 100자 이하여야 합니다.");
        }
    }
    
    /**
     * 설명 검증
     */
    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new BusinessValidationException("상품 설명은 필수입니다.");
        }
        if (description.trim().length() < 10) {
            throw new BusinessValidationException("상품 설명은 10자 이상이어야 합니다.");
        }
        if (description.trim().length() > 1000) {
            throw new BusinessValidationException("상품 설명은 1000자 이하여야 합니다.");
        }
    }
    
    /**
     * 카테고리 검증
     */
    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new BusinessValidationException("카테고리는 필수입니다.");
        }
        // 허용된 카테고리 목록 검증
        List<String> allowedCategories = List.of("전자제품", "의류", "도서", "식품", "스포츠", "뷰티", "가구", "기타");
        if (!allowedCategories.contains(category.trim())) {
            throw new BusinessValidationException("유효하지 않은 카테고리입니다. 허용된 카테고리: " + allowedCategories);
        }
    }
    
    /**
     * 평점 검증
     */
    private void validateRating(Double rating) {
        if (rating != null) {
            if (rating < 0.0 || rating > 5.0) {
                throw new BusinessValidationException("평점은 0.0 ~ 5.0 사이여야 합니다.");
            }
        }
    }
    
    /**
     * 중복 상품명 검사
     */
    private void validateDuplicatePname(String pname) {
        List<Products> existingProducts = productDAO.findByPname(pname.trim());
        if (!existingProducts.isEmpty()) {
            throw new BusinessValidationException("이미 존재하는 상품명입니다: " + pname);
        }
    }
    
    /**
     * 비즈니스 로직: 가격 검증
     */
    private void validatePrice(Integer price) {
        if (price == null || price <= 0) {
            throw new BusinessValidationException("가격은 0보다 커야 합니다.");
        }
        if (price > 10_000_000) {
            throw new BusinessValidationException("상품의 가격이 천만원을 초과할 수 없습니다.");
        }
    }

    // ==================== 고객용 검색 메서드들 ====================

    @Override
    public List<Products> searchProductsByKeyword(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        return productDAO.searchByKeyword(keyword, offset, size);
    }

    @Override
    public int countProductsByKeyword(String keyword) {
        return productDAO.countByKeyword(keyword);
    }

    @Override
    public List<Products> getProductsByCategory(String category, int page, int size) {
        int offset = (page - 1) * size;
        return productDAO.findByCategory(category, offset, size);
    }

    @Override
    public int countProductsByCategory(String category) {
        return productDAO.countByCategory(category);
    }

    @Override
    public List<Products> getAllProducts(int page, int size) {
        int offset = (page - 1) * size;
        return productDAO.findAllWithPaging(offset, size);
    }

    @Override
    public int getTotalProductCount() {
        return productDAO.getTotalCount();
    }

    @Override
    public Products getProductById(Long productId) {
        return productDAO.findById(productId).orElse(null);
    }
    
    /**
     * 재고 차감
     */
    @Override
    @Transactional
    public int decreaseStock(Long productId, Integer quantity) {
        log.info("재고 차감 - productId: {}, quantity: {}", productId, quantity);
        
        // 상품 존재 여부 확인
        Optional<Products> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다. productId: " + productId);
        }
        
        Products product = productOpt.get();
        
        // 재고 확인
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + product.getStockQuantity() + ", 요청 수량: " + quantity);
        }
        
        // 재고 차감
        int updatedRows = productDAO.decreaseStock(productId, quantity);
        
        if (updatedRows > 0) {
            log.info("재고 차감 완료 - productId: {}, 차감 수량: {}, 남은 재고: {}", 
                    productId, quantity, product.getStockQuantity() - quantity);
        }
        
        return updatedRows;
    }
    
    /**
     * 재고 증가
     */
    @Override
    @Transactional
    public int increaseStock(Long productId, Integer quantity) {
        log.info("재고 증가 - productId: {}, quantity: {}", productId, quantity);
        
        // 상품 존재 여부 확인
        Optional<Products> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다. productId: " + productId);
        }
        
        // 재고 증가
        int updatedRows = productDAO.increaseStock(productId, quantity);
        
        if (updatedRows > 0) {
            log.info("재고 증가 완료 - productId: {}, 증가 수량: {}", productId, quantity);
        }
        
        return updatedRows;
    }
}
