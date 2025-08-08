package com.kh.demo.domain.product.search.svc;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.json.JsonData;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.dao.ProductDocumentRepository;
import com.kh.demo.domain.product.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Elasticsearch 관련 상품 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductElasticsearchService {

    private final ProductDocumentRepository productDocumentRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;
    private final CodeSVC codeSVC;

    /**
     * 상품 저장 (Elasticsearch)
     */
    public void save(ProductDocument productDocument) {
        try {
            productDocumentRepository.save(productDocument);
            log.info("상품 등록 완료 - Elasticsearch: {}", productDocument.getProductId());
        } catch (Exception e) {
            log.error("Elasticsearch 저장 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 상품 수정 (Elasticsearch)
     */
    public void update(ProductDocument productDocument) {
        try {
            productDocumentRepository.save(productDocument);
            log.info("상품 수정 완료 - Elasticsearch: {}", productDocument.getProductId());
        } catch (Exception e) {
            log.error("Elasticsearch 수정 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 상품 삭제 (Elasticsearch)
     */
    public void deleteById(String productId) {
        try {
            productDocumentRepository.deleteById(productId);
            log.info("상품 삭제 완료 - Elasticsearch: {}", productId);
        } catch (Exception e) {
            log.error("Elasticsearch 삭제 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 상품명으로 검색
     */
    public List<ProductDocument> searchByPname(String pname, int limit) {
        try {
            Query query = MultiMatchQuery.of(m -> m
                    .fields("pname^3", "pname.ngram^2", "pname.edge_ngram")
                    .query(pname)
            )._toQuery();

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(limit)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("상품명 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 상품 설명으로 검색
     */
    public List<ProductDocument> searchByDescription(String description) {
        try {
            Query query = MultiMatchQuery.of(m -> m
                    .fields("description^2", "description.ngram")
                    .query(description)
            )._toQuery();

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(100)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("상품 설명 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 카테고리로 검색
     */
    public List<ProductDocument> searchByCategoryName(String categoryName) {
        try {
            Query query = TermQuery.of(t -> t
                    .field("categoryName")
                    .value(categoryName)
            )._toQuery();

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(100)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("카테고리 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 가격 범위로 검색
     */
    public List<ProductDocument> searchByPriceRange(Long minPrice, Long maxPrice) {
        try {
            Query query;
            if (minPrice != null && maxPrice != null) {
                query = RangeQuery.of(r -> r
                        .field("price")
                        .gte(JsonData.of(minPrice))
                        .lte(JsonData.of(maxPrice))
                )._toQuery();
            } else if (minPrice != null) {
                query = RangeQuery.of(r -> r
                        .field("price")
                        .gte(JsonData.of(minPrice))
                )._toQuery();
            } else if (maxPrice != null) {
                query = RangeQuery.of(r -> r
                        .field("price")
                        .lte(JsonData.of(maxPrice))
                )._toQuery();
            } else {
                query = MatchAllQuery.of(m -> m)._toQuery();
            }

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(100)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("가격 범위 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 평점으로 검색
     */
    public List<ProductDocument> searchByRating(Double minRating) {
        try {
            Query query = RangeQuery.of(r -> r
                    .field("rating")
                    .gte(JsonData.of(minRating))
            )._toQuery();

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(100)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("평점 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 전체 상품 조회
     */
    public List<ProductDocument> findAllProducts() {
        try {
            Query query = MatchAllQuery.of(m -> m)._toQuery();
            
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(1000)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("전체 상품 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 복합 검색
     */
    public List<ProductDocument> searchWithMultipleCriteria(String keyword, Long minPrice, Long maxPrice, 
                                                          Double minRating, String categoryName) {
        try {
            List<ProductDocument> results = null;

            // 1. 키워드 검색
            if (keyword != null && !keyword.trim().isEmpty()) {
                log.info("키워드 필터 실행: {}", keyword);
                List<ProductDocument> keywordResults = searchByPname(keyword, 100);
                log.info("키워드 필터 결과: {}건", keywordResults.size());
                results = keywordResults;
            }

            // 2. 카테고리 필터
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                log.info("카테고리 필터 실행: {}", categoryName);
                List<ProductDocument> categoryNameResults = searchByCategoryName(categoryName);
                log.info("카테고리 필터 결과: {}건", categoryNameResults.size());
                results = filterResults(results, categoryNameResults);
                log.info("카테고리 필터 적용 후 결과: {}건", results.size());
            }

            // 3. 가격 범위 필터
            if (minPrice != null || maxPrice != null) {
                log.info("가격 범위 필터 실행: {} ~ {}", minPrice, maxPrice);
                List<ProductDocument> priceResults = searchByPriceRange(minPrice, maxPrice);
                log.info("가격 범위 필터 결과: {}건", priceResults.size());
                results = filterResults(results, priceResults);
                log.info("가격 범위 필터 적용 후 결과: {}건", results.size());
            }

            // 4. 평점 필터
            if (minRating != null && minRating > 0) {
                log.info("평점 필터 실행: {} 이상", minRating);
                List<ProductDocument> ratingResults = searchByRating(minRating);
                log.info("평점 필터 결과: {}건", ratingResults.size());
                results = filterResults(results, ratingResults);
                log.info("평점 필터 적용 후 결과: {}건", results.size());
            }

            // 5. 모든 조건이 없으면 전체 상품 반환
            if (results == null) {
                log.info("검색 조건 없음, 전체 상품 반환");
                results = findAllProducts();
                log.info("전체 상품 결과: {}건", results.size());
            }

            log.info("최종 검색 결과: {}건", results.size());
            return results;

        } catch (Exception e) {
            log.error("복합 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 검색 결과 필터링 (교집합)
     */
    private List<ProductDocument> filterResults(List<ProductDocument> baseResults, List<ProductDocument> filterResults) {
        if (baseResults == null || baseResults.isEmpty()) {
            return filterResults;
        }

        if (filterResults == null || filterResults.isEmpty()) {
            return baseResults;
        }

        // 상품 ID 기준으로 교집합 계산
        Set<Long> baseIds = baseResults.stream()
                .map(ProductDocument::getProductId)
                .collect(Collectors.toSet());

        return filterResults.stream()
                .filter(doc -> baseIds.contains(doc.getProductId()))
                .collect(Collectors.toList());
    }

    /**
     * 자동완성
     */
    public List<String> autocompletePname(String prefix) {
        try {
            Query query = PrefixQuery.of(p -> p
                    .field("pname")
                    .value(prefix)
            )._toQuery();

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withMaxResults(10)
                    .build();

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getPname())
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("자동완성 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 전체 데이터 동기화 (기존 인덱스 삭제 후 재생성)
     */
    public void syncAllToElasticsearch(List<Products> products) {
        try {
            log.info("Elasticsearch 전체 동기화 시작: {}개 상품", products.size());
            
            // 1. 기존 인덱스 삭제
            log.info("기존 product2 인덱스 삭제 시작");
            boolean indexExists = elasticsearchClient.indices().exists(e -> e.index("product2")).value();
            if (indexExists) {
                elasticsearchClient.indices().delete(d -> d.index("product2"));
                log.info("기존 product2 인덱스 삭제 완료");
            } else {
                log.info("기존 product2 인덱스가 존재하지 않음");
            }
            
            // 2. 인덱스 재생성 (설정 파일 기반)
            log.info("새로운 product2 인덱스 생성 시작");
            try (var inputStream = getClass().getResourceAsStream("/elastic/product-settings.json")) {
                if (inputStream == null) {
                    throw new RuntimeException("product-settings.json 파일을 찾을 수 없습니다.");
                }
                String settingsJson = new String(inputStream.readAllBytes());
                elasticsearchClient.indices().create(c -> c
                    .index("product2")
                    .settings(s -> s.withJson(new StringReader(settingsJson)))
                );
                log.info("새로운 product2 인덱스 생성 완료");
            } catch (IOException e) {
                log.error("설정 파일 읽기 실패: {}", e.getMessage(), e);
                throw new RuntimeException("설정 파일 읽기 실패", e);
            }
            
            // 3. 상품 데이터를 ProductDocument로 변환 (카테고리명 포함)
            log.info("상품 데이터를 ProductDocument로 변환 시작");
            List<ProductDocument> documents = products.stream()
                    .map(product -> ProductDocument.from(product, codeSVC))
                    .collect(Collectors.toList());
            log.info("ProductDocument 변환 완료: {}개", documents.size());
            
            // 4. Elasticsearch에 저장
            log.info("Elasticsearch에 데이터 저장 시작");
            productDocumentRepository.saveAll(documents);
            log.info("Elasticsearch에 {}개의 상품 데이터 저장 완료", documents.size());
            
            log.info("Elasticsearch 전체 동기화 완료");

        } catch (Exception e) {
            log.error("Elasticsearch 동기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Elasticsearch 동기화 실패", e);
        }
    }
} 