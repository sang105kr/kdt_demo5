package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.common.dao.SearchLogDAO;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.dao.ProductDocumentRepository;
import com.kh.demo.domain.product.search.document.ProductDocument;
import com.kh.demo.web.product.controller.page.dto.ProductDetailDTO;
import com.kh.demo.web.product.controller.page.dto.ProductListDTO;
import com.kh.demo.web.product.controller.page.dto.SearchCriteria;
import com.kh.demo.web.product.controller.page.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 통합 상품 검색 서비스 구현체
 * Elasticsearch: 검색 및 목록 조회 (빠른 검색)
 * Oracle: 실시간 데이터 (재고, 가격 등 트랜잭션 데이터)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductService productService;
    private final ProductDAO productDAO;
    private final ProductDocumentRepository productDocumentRepository;
    private final SearchLogDAO searchLogDAO;
    private final CodeSVC codeSVC;  // CodeSVC 주입 추가

    @Override
    public SearchResult<ProductListDTO> search(SearchCriteria criteria) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Elasticsearch에서 검색 시도
            List<ProductDocument> documents = searchFromElasticsearch(criteria);
            
            if (documents != null && !documents.isEmpty()) {
                // 2. Oracle에서 실시간 데이터 보완
                List<ProductListDTO> enrichedProducts = enrichWithOracleData(documents);
                
                long searchTime = System.currentTimeMillis() - startTime;
                return SearchResult.of(enrichedProducts, documents.size(), 
                                     criteria.getPage(), criteria.getSize(), "elasticsearch", searchTime);
            }
        } catch (Exception e) {
            log.warn("Elasticsearch 검색 실패, Oracle fallback 사용: {}", e.getMessage());
        }
        
        // 3. Oracle fallback (카테고리명 포함)
        try {
            List<Products> products = searchFromOracle(criteria);
            log.info("🔍 Oracle에서 {}개 상품 조회됨", products.size());
            
            List<ProductListDTO> productDTOs = products.stream()
                    .map(product -> {
                        ProductListDTO dto = ProductListDTO.from(product, codeSVC);
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            log.info("🔍 첫 번째 상품 카테고리명: {}", 
            productDTOs.isEmpty() ? "N/A" : productDTOs.get(0).getCategoryName());
            
            long searchTime = System.currentTimeMillis() - startTime;
            return SearchResult.of(productDTOs, products.size(), 
                                 criteria.getPage(), criteria.getSize(), "oracle", searchTime);
        } catch (Exception e) {
            log.error("Oracle 검색도 실패: {}", e.getMessage());
            long searchTime = System.currentTimeMillis() - startTime;
            return SearchResult.empty(criteria.getSize());
        }
    }

    @Override
    public SearchResult<ProductListDTO> searchByCategory(String category, int page, int size) {
        SearchCriteria criteria = SearchCriteria.of(null, category, null, null, null, null, null, page, size);
        return search(criteria);
    }

    @Override
    public SearchResult<ProductListDTO> searchByKeyword(String keyword, int page, int size) {
        SearchCriteria criteria = SearchCriteria.of(keyword, null, null, null, null, null, null, page, size);
        return search(criteria);
    }

    @Override
    public SearchResult<ProductListDTO> searchByPriceRange(Long minPrice, Long maxPrice, int page, int size) {
        SearchCriteria criteria = SearchCriteria.of(null, null, minPrice, maxPrice, null, null, null, page, size);
        return search(criteria);
    }

    @Override
    public SearchResult<ProductListDTO> searchByRating(Double minRating, int page, int size) {
        SearchCriteria criteria = SearchCriteria.of(null, null, null, null, minRating, null, null, page, size);
        return search(criteria);
    }

    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        try {
            // 1. Oracle에서 실시간 데이터 조회
            Products product = productService.getProductById(productId);
            log.info("product={}", product);
            if (product == null) {
                return null;
            }
            
            // 2. Elasticsearch에서 추가 정보 조회
            ProductDocument document = null;
            try {
                document = productDocumentRepository.findByProductId(productId);
                log.info("document={}", product);
            } catch (Exception e) {
                log.warn("Elasticsearch에서 상품 정보 조회 실패: {}", e.getMessage());
            }
            
            // 3. 데이터 통합
            ProductDetailDTO detailDTO = ProductDetailDTO.merge(product, document);
            
            // 4. 카테고리 이름 설정 (Service 계층에서 처리)
            if (product.getCategoryId() != null) {
                detailDTO.setCategoryName(codeSVC.getCodeValue("PRODUCT_CATEGORY", product.getCategoryId()));
            }
            
            // 5. 파일 정보 추가
            List<UploadFile> imageFiles = productService.findProductImages(productId);
            List<UploadFile> manualFiles = productService.findProductManuals(productId);
            
            detailDTO.setImageUrls(imageFiles.stream()
                    .map(file -> "/uploads/" + file.getStoreFilename())
                    .collect(Collectors.toList()));
            detailDTO.setManualUrls(manualFiles.stream()
                    .map(file -> "/uploads/" + file.getStoreFilename())
                    .collect(Collectors.toList()));
            
            return detailDTO;
            
        } catch (Exception e) {
            log.error("상품 상세 정보 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> autocomplete(String prefix) {
        try {
            return productService.autocompletePname(prefix);
        } catch (Exception e) {
            log.warn("자동완성 검색 실패: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<String> getPopularKeywords() {
        try {
            // 1차: Elasticsearch에서 인기검색어 조회 (향후 구현)
            // return elasticsearchService.getPopularKeywords(5);
            
            // 현재는 Oracle 백업 로직만 사용 (Elasticsearch 구현 전까지)
            log.debug("Elasticsearch 미구현으로 Oracle에서 인기검색어 조회");
            return searchLogDAO.getPopularKeywordsFromOracle(5);
            
        } catch (Exception e) {
            log.warn("인기검색어 조회 실패, 기본값 반환", e);
            // 최종 백업: 하드코딩된 기본값
            return List.of("노트북", "스마트폰", "태블릿", "헤드폰", "키보드");
        }
    }

    @Override
    @Transactional
    public void saveSearchHistory(String keyword, Long memberId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("빈 검색어는 저장하지 않습니다: keyword={}", keyword);
            return;
        }
        
        try {
            // 1차: Oracle에 검색 히스토리 저장 (동기)
            searchLogDAO.saveSearchLog(memberId, keyword.trim(), "PRODUCT", null, null);
            log.debug("검색 히스토리 저장 완료: keyword={}, memberId={}", keyword, memberId);
            
            // 2차: Elasticsearch에 비동기 전송 (향후 구현)
            // CompletableFuture.runAsync(() -> {
            //     try {
            //         elasticsearchService.indexSearchEvent(keyword, memberId);
            //     } catch (Exception e) {
            //         log.warn("ES 인덱싱 실패 (Oracle 저장은 성공): keyword={}", keyword, e);
            //     }
            // });
            
        } catch (Exception e) {
            log.error("검색 히스토리 저장 실패: keyword={}, memberId={}", keyword, memberId, e);
        }
    }

    @Override
    public List<String> getSearchHistory(Long memberId) {
        if (memberId == null) {
            log.debug("로그인되지 않은 사용자의 검색 히스토리 요청");
            return List.of();
        }
        
        try {
            // 1차: Oracle에서 개인 검색 히스토리 조회
            List<String> history = searchLogDAO.getMemberSearchHistory(memberId, 5);
            log.debug("검색 히스토리 조회 완료: memberId={}, count={}", memberId, history.size());
            return history;
            
        } catch (Exception e) {
            log.error("검색 히스토리 조회 실패: memberId={}", memberId, e);
            // 장애 시 빈 목록 반환
            return List.of();
        }
    }

    @Override
    @Transactional
    public void clearSearchHistory(Long memberId) {
        if (memberId == null) {
            log.debug("로그인되지 않은 사용자의 검색 히스토리 삭제 요청");
            return;
        }
        
        try {
            // Oracle에서 해당 사용자의 모든 검색 히스토리 삭제
            searchLogDAO.clearMemberSearchHistory(memberId);
            log.debug("검색 히스토리 삭제 완료: memberId={}", memberId);
            
        } catch (Exception e) {
            log.error("검색 히스토리 삭제 실패: memberId={}", memberId, e);
            throw new RuntimeException("검색 히스토리 삭제에 실패했습니다.", e);
        }
    }
    
    @Override
    @Transactional
    public void deleteSearchHistoryItem(String keyword, Long memberId) {
        if (memberId == null) {
            log.debug("로그인되지 않은 사용자의 검색 히스토리 개별 삭제 요청");
            return;
        }
        
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("빈 검색어는 삭제하지 않습니다: keyword={}", keyword);
            return;
        }
        
        try {
            // Oracle에서 해당 사용자의 특정 검색 히스토리 삭제
            searchLogDAO.deleteMemberSearchHistoryItem(memberId, keyword.trim());
            log.debug("검색 히스토리 개별 삭제 완료: memberId={}, keyword={}", memberId, keyword);
            
        } catch (Exception e) {
            log.error("검색 히스토리 개별 삭제 실패: memberId={}, keyword={}", memberId, keyword, e);
            throw new RuntimeException("검색 히스토리 개별 삭제에 실패했습니다.", e);
        }
    }

    // Private helper methods
    
    private List<ProductDocument> searchFromElasticsearch(SearchCriteria criteria) {
        try {
            // 새로운 복합 검색 메서드 사용
            return productService.searchWithMultipleCriteria(
                criteria.getKeyword(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating(),
                criteria.getCategoryName()
            );
        } catch (Exception e) {
            log.error("Elasticsearch 복합 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private List<Products> searchFromOracle(SearchCriteria criteria) {
        // Oracle에서 기본 검색 (성능을 위해 제한적)
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            return productService.searchProductsByKeyword(criteria.getKeyword().trim(), 
                                                        criteria.getPage(), criteria.getSize());
        } else if (criteria.getCategoryName() != null && !criteria.getCategoryName().trim().isEmpty()) {
            return productService.getProductsByCategory(criteria.getCategoryName().trim(),
                                                      criteria.getPage(), criteria.getSize());
        } else {
            return productService.getAllProducts(criteria.getPage(), criteria.getSize());
        }
    }
    
    private List<ProductListDTO> enrichWithOracleData(List<ProductDocument> documents) {
        return documents.stream()
                .map(doc -> {
                    // ProductDocument로부터 DTO 생성 (하이라이팅 정보 포함)
                    ProductListDTO dto = ProductListDTO.from(doc);
                    
                    // Elasticsearch에서 가져온 categoryName이 없으면 CodeSVC로 보완
                    if (dto.getCategoryName() == null && doc.getCategoryId() != null) {
                        dto.setCategoryName(codeSVC.getCodeDecode("PRODUCT_CATEGORY", doc.getCategoryId()));
                    }
                    
                    // Oracle에서 실시간 데이터 보완
                    try {
                        Products realTimeProduct = productService.getProductById(doc.getProductId());
                        if (realTimeProduct != null) {
                            dto.setPrice(realTimeProduct.getPrice());
                            dto.setStockQuantity(realTimeProduct.getStockQuantity());
                        }
                    } catch (Exception e) {
                        log.warn("실시간 데이터 보완 실패: productId={}", doc.getProductId());
                    }
                    
                    // 이미지 정보 추가
                    try {
                        List<UploadFile> imageFiles = productService.findProductImages(doc.getProductId());
                        if (!imageFiles.isEmpty()) {
                            dto.setImageUrl("/uploads/" + imageFiles.get(0).getStoreFilename());
                        }
                    } catch (Exception e) {
                        log.warn("이미지 정보 추가 실패: productId={}", doc.getProductId());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 