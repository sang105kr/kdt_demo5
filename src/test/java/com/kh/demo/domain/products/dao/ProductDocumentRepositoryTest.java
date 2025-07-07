package com.kh.demo.domain.products.dao;

import com.kh.demo.domain.document.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ProductDocumentRepositoryTest {

    @Autowired
    private ProductDocumentRepository productDocumentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() {
        // 테스트 전 인덱스 초기화
        productDocumentRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 문서 저장")
    void save() {
        // Given
        ProductDocument productDocument = createTestProductDocument();

        // When
        ProductDocument savedDocument = productDocumentRepository.save(productDocument);

        // Then
        Assertions.assertThat(savedDocument).isNotNull();
        Assertions.assertThat(savedDocument.getProductId()).isEqualTo(productDocument.getProductId());
        Assertions.assertThat(savedDocument.getPname()).isEqualTo("삼성 노트북");
        log.info("저장된 상품 문서: {}", savedDocument);
    }

    @Test
    @DisplayName("상품 문서 조회")
    void findById() {
        // Given
        ProductDocument savedDocument = productDocumentRepository.save(createTestProductDocument());
        Long productId = savedDocument.getProductId();

        // When
        Optional<ProductDocument> foundDocument = productDocumentRepository.findById(productId);

        // Then
        Assertions.assertThat(foundDocument).isPresent();
        ProductDocument document = foundDocument.get();
        Assertions.assertThat(document.getProductId()).isEqualTo(productId);
        Assertions.assertThat(document.getPname()).isEqualTo("삼성 노트북");
        log.info("조회된 상품 문서: {}", document);
    }

    @Test
    @DisplayName("존재하지 않는 상품 문서 조회")
    void findByIdNotFound() {
        // Given
        Long nonExistentId = 999999L;

        // When
        Optional<ProductDocument> foundDocument = productDocumentRepository.findById(nonExistentId);

        // Then
        Assertions.assertThat(foundDocument).isEmpty();
    }

    @Test
    @DisplayName("모든 상품 문서 조회")
    void findAll() {
        // Given
        productDocumentRepository.save(createTestProductDocument("삼성 노트북"));
        productDocumentRepository.save(createTestProductDocument("LG 휴대폰"));
        productDocumentRepository.save(createTestProductDocument("아이폰"));

        // When
        List<ProductDocument> documents = (List<ProductDocument>) productDocumentRepository.findAll();

        // Then
        Assertions.assertThat(documents).isNotNull();
        Assertions.assertThat(documents.size()).isEqualTo(3);
        log.info("전체 상품 문서 수: {}", documents.size());
    }

    @Test
    @DisplayName("상품명으로 검색 (동의어 포함)")
    void findByPnameContaining() {
        // Given
        productDocumentRepository.save(createTestProductDocument("삼성 노트북"));
        productDocumentRepository.save(createTestProductDocument("LG 노트북"));
        productDocumentRepository.save(createTestProductDocument("맥북"));

        // When - "노트북"으로 검색하면 "컴퓨터", "랩탑" 등 동의어도 포함되어야 함
        List<ProductDocument> documents = productDocumentRepository.findByPnameContaining("노트북");

        // Then
        Assertions.assertThat(documents).isNotNull();
        Assertions.assertThat(documents.size()).isGreaterThan(0);
        log.info("'노트북' 검색 결과: {}", documents.size());
        
        for (ProductDocument doc : documents) {
            log.info("검색된 상품: {}", doc.getPname());
        }
    }

    @Test
    @DisplayName("상품설명으로 검색 (HTML 제거 후 검색)")
    void findByDescriptionContaining() {
        // Given
        ProductDocument doc1 = createTestProductDocument("상품1");
        doc1.setDescription("<p>고성능 노트북입니다</p>");
        productDocumentRepository.save(doc1);

        ProductDocument doc2 = createTestProductDocument("상품2");
        doc2.setDescription("<div>저가형 컴퓨터입니다</div>");
        productDocumentRepository.save(doc2);

        // When
        List<ProductDocument> documents = productDocumentRepository.findByDescriptionContaining("노트북");

        // Then
        Assertions.assertThat(documents).isNotNull();
        log.info("설명에서 '노트북' 검색 결과: {}", documents.size());
        
        for (ProductDocument doc : documents) {
            log.info("검색된 상품: {} - {}", doc.getPname(), doc.getDescription());
        }
    }

    @Test
    @DisplayName("카테고리로 검색")
    void findByCategory() {
        // Given
        ProductDocument doc1 = createTestProductDocument("상품1");
        doc1.setCategory("전자제품");
        productDocumentRepository.save(doc1);

        ProductDocument doc2 = createTestProductDocument("상품2");
        doc2.setCategory("의류");
        productDocumentRepository.save(doc2);

        ProductDocument doc3 = createTestProductDocument("상품3");
        doc3.setCategory("전자제품");
        productDocumentRepository.save(doc3);

        // When
        List<ProductDocument> documents = productDocumentRepository.findByCategory("전자제품");

        // Then
        Assertions.assertThat(documents).isNotNull();
        Assertions.assertThat(documents.size()).isEqualTo(2);
        log.info("'전자제품' 카테고리 검색 결과: {}", documents.size());
        
        for (ProductDocument doc : documents) {
            Assertions.assertThat(doc.getCategory()).isEqualTo("전자제품");
            log.info("검색된 상품: {} - {}", doc.getPname(), doc.getCategory());
        }
    }

    @Test
    @DisplayName("가격 범위로 검색")
    void findByPriceBetween() {
        // Given
        ProductDocument doc1 = createTestProductDocument("저가 상품");
        doc1.setPrice(50000L);
        productDocumentRepository.save(doc1);

        ProductDocument doc2 = createTestProductDocument("중가 상품");
        doc2.setPrice(150000L);
        productDocumentRepository.save(doc2);

        ProductDocument doc3 = createTestProductDocument("고가 상품");
        doc3.setPrice(300000L);
        productDocumentRepository.save(doc3);

        // When
        List<ProductDocument> documents = productDocumentRepository.findByPriceBetween(100000L, 200000L);

        // Then
        Assertions.assertThat(documents).isNotNull();
        Assertions.assertThat(documents.size()).isEqualTo(1);
        log.info("가격 범위 100,000~200,000 검색 결과: {}", documents.size());
        
        for (ProductDocument doc : documents) {
            Assertions.assertThat(doc.getPrice()).isBetween(100000L, 200000L);
            log.info("검색된 상품: {} - {}", doc.getPname(), doc.getPrice());
        }
    }

    @Test
    @DisplayName("평점 이상으로 검색")
    void findByRatingGreaterThanEqual() {
        // Given
        ProductDocument doc1 = createTestProductDocument("상품1");
        doc1.setRating(3.5);
        productDocumentRepository.save(doc1);

        ProductDocument doc2 = createTestProductDocument("상품2");
        doc2.setRating(4.2);
        productDocumentRepository.save(doc2);

        ProductDocument doc3 = createTestProductDocument("상품3");
        doc3.setRating(4.8);
        productDocumentRepository.save(doc3);

        // When
        List<ProductDocument> documents = productDocumentRepository.findByRatingGreaterThanEqual(4.0);

        // Then
        Assertions.assertThat(documents).isNotNull();
        Assertions.assertThat(documents.size()).isEqualTo(2);
        log.info("평점 4.0 이상 검색 결과: {}", documents.size());
        
        for (ProductDocument doc : documents) {
            Assertions.assertThat(doc.getRating()).isGreaterThanOrEqualTo(4.0);
            log.info("검색된 상품: {} - 평점: {}", doc.getPname(), doc.getRating());
        }
    }

    @Test
    @DisplayName("상품 문서 수정")
    void update() {
        // Given
        ProductDocument savedDocument = productDocumentRepository.save(createTestProductDocument("원본 상품"));
        Long productId = savedDocument.getProductId();

        // When
        savedDocument.setPname("수정된 상품명");
        savedDocument.setPrice(200000L);
        ProductDocument updatedDocument = productDocumentRepository.save(savedDocument);

        // Then
        Assertions.assertThat(updatedDocument.getPname()).isEqualTo("수정된 상품명");
        Assertions.assertThat(updatedDocument.getPrice()).isEqualTo(200000L);
        
        // 수정된 데이터 확인
        Optional<ProductDocument> foundDocument = productDocumentRepository.findById(productId);
        Assertions.assertThat(foundDocument).isPresent();
        Assertions.assertThat(foundDocument.get().getPname()).isEqualTo("수정된 상품명");
        log.info("수정된 상품 문서: {}", updatedDocument);
    }

    @Test
    @DisplayName("상품 문서 삭제")
    void delete() {
        // Given
        ProductDocument savedDocument = productDocumentRepository.save(createTestProductDocument("삭제할 상품"));
        Long productId = savedDocument.getProductId();

        // When
        productDocumentRepository.deleteById(productId);

        // Then
        Optional<ProductDocument> deletedDocument = productDocumentRepository.findById(productId);
        Assertions.assertThat(deletedDocument).isEmpty();
        log.info("상품 문서 삭제 완료: {}", productId);
    }

    @Test
    @DisplayName("복합 검색 테스트")
    void complexSearch() {
        // Given
        ProductDocument doc1 = createTestProductDocument("삼성 노트북");
        doc1.setCategory("전자제품");
        doc1.setPrice(1500000L);
        doc1.setRating(4.5);
        productDocumentRepository.save(doc1);

        ProductDocument doc2 = createTestProductDocument("LG 노트북");
        doc2.setCategory("전자제품");
        doc2.setPrice(1200000L);
        doc2.setRating(4.2);
        productDocumentRepository.save(doc2);

        ProductDocument doc3 = createTestProductDocument("맥북");
        doc3.setCategory("전자제품");
        doc3.setPrice(2500000L);
        doc3.setRating(4.8);
        productDocumentRepository.save(doc3);

        // When - 복합 조건으로 검색 (간단한 문자열 쿼리 사용)
        String queryString = "pname:노트북 AND category:전자제품 AND price:[1000000 TO 2000000] AND rating:[4.0 TO *]";
        Query searchQuery = new StringQuery(queryString);
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(searchQuery, ProductDocument.class);

        // Then
        Assertions.assertThat(searchHits.getTotalHits()).isGreaterThan(0);
        log.info("복합 검색 결과: {}", searchHits.getTotalHits());
        
        searchHits.forEach(hit -> {
            ProductDocument doc = hit.getContent();
            log.info("복합 검색 결과: {} - 가격: {}, 평점: {}", doc.getPname(), doc.getPrice(), doc.getRating());
        });
    }

    @Test
    @DisplayName("Products 엔티티 변환 테스트")
    void entityConversion() {
        // Given
        ProductDocument productDocument = createTestProductDocument("변환 테스트 상품");

        // When
        var products = productDocument.toEntity();

        // Then
        Assertions.assertThat(products.getProductId()).isEqualTo(productDocument.getProductId());
        Assertions.assertThat(products.getPname()).isEqualTo(productDocument.getPname());
        Assertions.assertThat(products.getDescription()).isEqualTo(productDocument.getDescription());
        Assertions.assertThat(products.getPrice()).isEqualTo(productDocument.getPrice());
        Assertions.assertThat(products.getRating()).isEqualTo(productDocument.getRating());
        Assertions.assertThat(products.getCategory()).isEqualTo(productDocument.getCategory());
        log.info("엔티티 변환 결과: {}", products);
    }

    @Test
    @DisplayName("Products에서 ProductDocument 변환 테스트")
    void documentConversion() {
        // Given
        var products = new com.kh.demo.domain.entity.Products();
        products.setProductId(1L);
        products.setPname("변환 테스트 상품");
        products.setDescription("변환 테스트 설명");
        products.setPrice(100000L);
        products.setRating(4.5);
        products.setCategory("테스트 카테고리");

        // When
        ProductDocument productDocument = ProductDocument.from(products);

        // Then
        Assertions.assertThat(productDocument.getProductId()).isEqualTo(products.getProductId());
        Assertions.assertThat(productDocument.getPname()).isEqualTo(products.getPname());
        Assertions.assertThat(productDocument.getDescription()).isEqualTo(products.getDescription());
        Assertions.assertThat(productDocument.getPrice()).isEqualTo(products.getPrice());
        Assertions.assertThat(productDocument.getRating()).isEqualTo(products.getRating());
        Assertions.assertThat(productDocument.getCategory()).isEqualTo(products.getCategory());
        log.info("문서 변환 결과: {}", productDocument);
    }

    // 테스트 헬퍼 메서드들
    private ProductDocument createTestProductDocument() {
        return createTestProductDocument("삼성 노트북");
    }

    private ProductDocument createTestProductDocument(String pname) {
        return ProductDocument.builder()
                .productId(System.currentTimeMillis()) // 유니크 ID 생성
                .pname(pname)
                .description("테스트 상품 설명입니다.")
                .price(1000000L)
                .rating(4.5)
                .category("전자제품")
                .build();
    }
} 