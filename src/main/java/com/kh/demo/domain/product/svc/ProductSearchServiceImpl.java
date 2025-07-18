package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.dao.ProductDocumentRepository;
import com.kh.demo.domain.product.search.document.ProductDocument;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.shared.exception.BusinessValidationException;
import com.kh.demo.web.dto.ProductDetailDTO;
import com.kh.demo.web.dto.ProductListDTO;
import com.kh.demo.web.dto.SearchCriteria;
import com.kh.demo.web.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
        
        // 3. Oracle fallback
        try {
            List<Products> products = searchFromOracle(criteria);
            List<ProductListDTO> productDTOs = products.stream()
                    .map(ProductListDTO::from)
                    .collect(Collectors.toList());
            
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
            if (product == null) {
                return null;
            }
            
            // 2. Elasticsearch에서 추가 정보 조회
            ProductDocument document = null;
            try {
                Optional<ProductDocument> docOpt = productDocumentRepository.findById(productId);
                document = docOpt.orElse(null);
            } catch (Exception e) {
                log.warn("Elasticsearch에서 상품 정보 조회 실패: {}", e.getMessage());
            }
            
            // 3. 데이터 통합
            ProductDetailDTO detailDTO = ProductDetailDTO.merge(product, document);
            
            // 4. 파일 정보 추가
            List<UploadFile> imageFiles = productService.findProductImages(productId);
            List<UploadFile> manualFiles = productService.findProductManuals(productId);
            
            detailDTO.setImageUrls(imageFiles.stream()
                    .map(UploadFile::getStoreFilename)
                    .collect(Collectors.toList()));
            detailDTO.setManualUrls(manualFiles.stream()
                    .map(UploadFile::getStoreFilename)
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
        // TODO: 인기 검색어 구현 (Redis 또는 별도 테이블 사용)
        return List.of("노트북", "스마트폰", "태블릿", "헤드폰", "키보드");
    }

    @Override
    public void saveSearchHistory(String keyword, Long memberId) {
        // TODO: 검색 히스토리 저장 구현
        log.info("검색 히스토리 저장: keyword={}, memberId={}", keyword, memberId);
    }

    @Override
    public List<String> getSearchHistory(Long memberId) {
        // TODO: 검색 히스토리 조회 구현
        return List.of();
    }

    // Private helper methods
    
    private List<ProductDocument> searchFromElasticsearch(SearchCriteria criteria) {
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            return productService.searchByPname(criteria.getKeyword().trim());
        } else if (criteria.getCategory() != null && !criteria.getCategory().trim().isEmpty()) {
            return productService.searchByCategory(criteria.getCategory().trim());
        } else if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            return productService.searchByPriceRange(criteria.getMinPrice(), criteria.getMaxPrice());
        } else if (criteria.getMinRating() != null) {
            return productService.searchByRating(criteria.getMinRating());
        } else {
            return productService.findAllProducts();
        }
    }
    
    private List<Products> searchFromOracle(SearchCriteria criteria) {
        // Oracle에서 기본 검색 (성능을 위해 제한적)
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            return productService.searchProductsByKeyword(criteria.getKeyword().trim(), 
                                                        criteria.getPage(), criteria.getSize());
        } else if (criteria.getCategory() != null && !criteria.getCategory().trim().isEmpty()) {
            return productService.getProductsByCategory(criteria.getCategory().trim(), 
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
                            dto.setImageUrl(imageFiles.get(0).getStoreFilename());
                        }
                    } catch (Exception e) {
                        log.warn("이미지 정보 추가 실패: productId={}", doc.getProductId());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 