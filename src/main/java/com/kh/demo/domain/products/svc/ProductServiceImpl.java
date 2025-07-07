package com.kh.demo.domain.products.svc;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.kh.demo.domain.document.ProductDocument;
import com.kh.demo.domain.entity.Products;
import com.kh.demo.domain.products.dao.ProductDocumentRepository;
import com.kh.demo.domain.products.dao.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품 서비스 구현체 (Oracle + Elasticsearch 동기화)
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 상품 등록 (Oracle + Elasticsearch 동기화)
     */
    @Override
    @Transactional
    public Long save(Products products) {
        // 1. Oracle에 저장
        Long productId = productRepository.save(products);
        log.info("상품 등록 완료 - Oracle: {}", productId);
        
        // 2. Elasticsearch에 동기화
        try {
            products.setProductId(productId); // 생성된 ID 설정
            ProductDocument document = ProductDocument.from(products);
            productDocumentRepository.save(document);
            log.info("상품 등록 완료 - Elasticsearch: {}", productId);
        } catch (Exception e) {
            log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
        }
        
        return productId;
    }

    /**
     * 상품 목록 조회 (Oracle)
     */
    @Override
    public List<Products> findAll() {
        return productRepository.findAll();
    }

    /**
     * 상품 목록 조회 (페이징, Oracle)
     */
    @Override
    public List<Products> findAll(int pageNo, int numOfRows) {
        return productRepository.findAll(pageNo, numOfRows);
    }

    /**
     * 상품 총 건수 (Oracle)
     */
    @Override
    public int getTotalCount() {
        return productRepository.getTotalCount();
    }

    /**
     * 상품 조회 (Oracle)
     */
    @Override
    public Optional<Products> findById(Long productId) {
        return productRepository.findById(productId);
    }

    /**
     * 상품 수정 (Oracle + Elasticsearch 동기화)
     */
    @Override
    @Transactional
    public int updateById(Long productId, Products products) {
        // 1. Oracle 수정
        int result = productRepository.updateById(productId, products);
        log.info("상품 수정 완료 - Oracle: {}", productId);
        
        // 2. Elasticsearch 동기화
        if (result > 0) {
            try {
                products.setProductId(productId);
                ProductDocument document = ProductDocument.from(products);
                productDocumentRepository.save(document);
                log.info("상품 수정 완료 - Elasticsearch: {}", productId);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            }
        }
        
        return result;
    }

    /**
     * 상품 삭제 (Oracle + Elasticsearch 동기화)
     */
    @Override
    @Transactional
    public int deleteById(Long productId) {
        // 1. Oracle 삭제
        int result = productRepository.deleteById(productId);
        log.info("상품 삭제 완료 - Oracle: {}", productId);
        
        // 2. Elasticsearch 동기화
        if (result > 0) {
            try {
                productDocumentRepository.deleteById(productId);
                log.info("상품 삭제 완료 - Elasticsearch: {}", productId);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            }
        }
        
        return result;
    }

    /**
     * 상품 삭제 (여러건, Oracle + Elasticsearch 동기화)
     */
    @Override
    @Transactional
    public int deleteByIds(List<Long> productIds) {
        // 1. Oracle 삭제
        int result = productRepository.deleteByIds(productIds);
        log.info("상품 삭제 완료 - Oracle: {}건", result);
        
        // 2. Elasticsearch 동기화
        if (result > 0) {
            try {
                productDocumentRepository.deleteAllById(productIds);
                log.info("상품 삭제 완료 - Elasticsearch: {}건", result);
            } catch (Exception e) {
                log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            }
        }
        
        return result;
    }

    /**
     * Elasticsearch 검색 - 상품명으로 검색
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
     * Elasticsearch 검색 - 상품설명으로 검색
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
     * Elasticsearch 검색 - 카테고리로 검색
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
     * Elasticsearch 검색 - 가격 범위로 검색
     */
    @Override
    public List<ProductDocument> searchByPriceRange(Long minPrice, Long maxPrice) {
        return productDocumentRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Elasticsearch 검색 - 평점 이상으로 검색
     */
    @Override
    public List<ProductDocument> searchByRating(Double rating) {
        return productDocumentRepository.findByRatingGreaterThanEqual(rating);
    }

    /**
     * 전체 데이터 동기화 (Oracle → Elasticsearch)
     */
    @Override
    @Transactional
    public void syncAllToElasticsearch() {
        log.info("=== 전체 데이터 Elasticsearch 동기화 시작 ===");
        
        try {
            // 1. Elasticsearch 인덱스 초기화 (기존 데이터 삭제)
            productDocumentRepository.deleteAll();
            log.info("Elasticsearch 기존 데이터 삭제 완료");
            
            // 2. Oracle에서 모든 상품 데이터 조회
            List<Products> productsList = productRepository.findAll();
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
    @Override
    public void compareDataCount() {
        long oracleCount = productRepository.getTotalCount();
        long elasticsearchCount = productDocumentRepository.count();
        
        log.info("데이터 개수 비교 - Oracle: {}, Elasticsearch: {}", oracleCount, elasticsearchCount);
        
        if (oracleCount != elasticsearchCount) {
            log.warn("데이터 개수가 일치하지 않습니다! 동기화가 필요합니다.");
        } else {
            log.info("데이터 개수가 일치합니다.");
        }
    }

    @Override
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
                // description 필드의 하이라이팅 결과를 가져와서 별도 필드에 저장
                if (hit.getHighlightFields() != null && hit.getHighlightFields().containsKey("description")) {
                    String highlightedDescription = String.join(" ", hit.getHighlightFields().get("description"));
                    // 하이라이팅된 결과를 별도 필드에 저장 (예: descriptionHighlighted)
                    // ProductDocument에 descriptionHighlighted 필드가 있다면:
                    // doc.setDescriptionHighlighted(highlightedDescription);
                    // 또는 원본 description을 하이라이팅된 버전으로 교체:
                    doc.setDescription(highlightedDescription);
                }
                return doc;
            })
            .toList();
    }

    @Override
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
            .withPageable(PageRequest.of(0, 10))  // 10개 결과로 변경
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

    @Override
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
}
