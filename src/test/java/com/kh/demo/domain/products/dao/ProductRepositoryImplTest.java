package com.kh.demo.domain.products.dao;

import com.kh.demo.domain.entity.Products;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootTest
@Transactional
class ProductRepositoryImplTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    @DisplayName("상품 등록")
    void save() {
        // Given
        Products products = new Products();
        products.setPname("테스트 상품");
        products.setDescription("테스트 상품 설명");
        products.setPrice(10000L);
        products.setRating(4.5);
        products.setCategory("전자제품");

        // When
        Long productId = productRepository.save(products);

        // Then
        log.info("생성된 상품 ID: {}", productId);
        Assertions.assertThat(productId).isNotNull();
        Assertions.assertThat(productId).isPositive();
    }

    @Test
    @DisplayName("상품 목록 조회")
    void findAll() {
        // Given
        // 테스트 데이터 생성
        createTestProducts();

        // When
        List<Products> productsList = productRepository.findAll();

        // Then
        log.info("상품 목록 크기: {}", productsList.size());
        Assertions.assertThat(productsList).isNotNull();
        Assertions.assertThat(productsList.size()).isGreaterThan(0);
        
        for (Products product : productsList) {
            log.info("상품: {}", product);
            Assertions.assertThat(product.getProductId()).isNotNull();
            Assertions.assertThat(product.getPname()).isNotNull();
        }
    }

    @Test
    @DisplayName("상품 목록 조회 (페이징)")
    void findAllWithPaging() {
        // Given
        createTestProducts();
        int pageNo = 1;
        int numOfRows = 5;

        // When
        List<Products> productsList = productRepository.findAll(pageNo, numOfRows);

        // Then
        log.info("페이징된 상품 목록 크기: {}", productsList.size());
        Assertions.assertThat(productsList).isNotNull();
        Assertions.assertThat(productsList.size()).isLessThanOrEqualTo(numOfRows);
    }

    @Test
    @DisplayName("상품 조회")
    void findById() {
        // Given
        Products savedProduct = createTestProduct();
        Long productId = savedProduct.getProductId();

        // When
        Optional<Products> foundProduct = productRepository.findById(productId);

        // Then
        Assertions.assertThat(foundProduct).isPresent();
        Products product = foundProduct.get();
        Assertions.assertThat(product.getProductId()).isEqualTo(productId);
        Assertions.assertThat(product.getPname()).isEqualTo("테스트 상품");
        log.info("조회된 상품: {}", product);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회")
    void findByIdNotFound() {
        // Given
        Long nonExistentId = 999999L;

        // When
        Optional<Products> foundProduct = productRepository.findById(nonExistentId);

        // Then
        Assertions.assertThat(foundProduct).isEmpty();
    }

    @Test
    @DisplayName("상품 수정")
    void updateById() {
        // Given
        Products savedProduct = createTestProduct();
        Long productId = savedProduct.getProductId();
        
        Products updateData = new Products();
        updateData.setPname("수정된 상품명");
        updateData.setDescription("수정된 상품 설명");
        updateData.setPrice(20000L);
        updateData.setRating(4.8);
        updateData.setCategory("가전제품");

        // When
        int updatedRows = productRepository.updateById(productId, updateData);

        // Then
        Assertions.assertThat(updatedRows).isEqualTo(1);
        
        // 수정된 데이터 확인
        Optional<Products> updatedProduct = productRepository.findById(productId);
        Assertions.assertThat(updatedProduct).isPresent();
        Products product = updatedProduct.get();
        Assertions.assertThat(product.getPname()).isEqualTo("수정된 상품명");
        Assertions.assertThat(product.getPrice()).isEqualTo(20000L);
        log.info("수정된 상품: {}", product);
    }

    @Test
    @DisplayName("상품 삭제 (단건)")
    void deleteById() {
        // Given
        Products savedProduct = createTestProduct();
        Long productId = savedProduct.getProductId();

        // When
        int deletedRows = productRepository.deleteById(productId);

        // Then
        Assertions.assertThat(deletedRows).isEqualTo(1);
        
        // 삭제 확인
        Optional<Products> deletedProduct = productRepository.findById(productId);
        Assertions.assertThat(deletedProduct).isEmpty();
    }

    @Test
    @DisplayName("상품 삭제 (여러건)")
    void deleteByIds() {
        // Given
        Products product1 = createTestProduct();
        Products product2 = createTestProduct();
        Products product3 = createTestProduct();
        
        List<Long> productIds = List.of(product1.getProductId(), product2.getProductId(), product3.getProductId());

        // When
        int deletedRows = productRepository.deleteByIds(productIds);

        // Then
        Assertions.assertThat(deletedRows).isEqualTo(3);
        
        // 삭제 확인
        for (Long productId : productIds) {
            Optional<Products> deletedProduct = productRepository.findById(productId);
            Assertions.assertThat(deletedProduct).isEmpty();
        }
    }

    @Test
    @DisplayName("상품 총 건수")
    void getTotalCount() {
        // Given
        createTestProducts();

        // When
        int totalCount = productRepository.getTotalCount();

        // Then
        log.info("총 상품 건수: {}", totalCount);
        Assertions.assertThat(totalCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("상품 등록 후 총 건수 확인")
    void saveAndGetTotalCount() {
        // Given
        int initialCount = productRepository.getTotalCount();
        log.info("초기 상품 건수: {}", initialCount);

        // When
        Products newProduct = new Products();
        newProduct.setPname("새로운 상품");
        newProduct.setDescription("새로운 상품 설명");
        newProduct.setPrice(15000L);
        newProduct.setRating(4.2);
        newProduct.setCategory("의류");
        
        Long productId = productRepository.save(newProduct);
        int afterSaveCount = productRepository.getTotalCount();

        // Then
        Assertions.assertThat(productId).isNotNull();
        Assertions.assertThat(afterSaveCount).isEqualTo(initialCount + 1);
        log.info("저장 후 상품 건수: {}", afterSaveCount);
    }

    // 테스트 헬퍼 메서드들
    private Products createTestProduct() {
        Products products = new Products();
        products.setPname("테스트 상품");
        products.setDescription("테스트 상품 설명");
        products.setPrice(10000L);
        products.setRating(4.5);
        products.setCategory("전자제품");
        
        Long productId = productRepository.save(products);
        products.setProductId(productId);
        return products;
    }

    private void createTestProducts() {
        for (int i = 1; i <= 10; i++) {
            Products products = new Products();
            products.setPname("테스트 상품 " + i);
            products.setDescription("테스트 상품 설명 " + i);
            products.setPrice(10000L + (i * 1000));
            products.setRating(4.0 + (i * 0.1));
            products.setCategory("카테고리 " + (i % 3 + 1));
            
            productRepository.save(products);
        }
    }
} 