package com.kh.demo.domain.product.dao;


import lombok.extern.slf4j.Slf4j;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootTest
class ProductDAOImplTest {
//  private static final Logger logger = LoggerFactory.getLogger(ProductDAOImplTest.class);

  @Autowired  // 스프링 컨테이너에 동일 타입의 객체를 찾아와 주입해줌
  ProductDAO productDAO;

  @Test
  void testLog() {
//    System.out.println("hi");logging:
//    logger.info("info");
//    logger.warn("warn");
//    logger.error("error");
//    logger.debug("debug");
//    logger.trace("debug");
    log.info("hi");
    log.warn("warn");
    log.error("error");
  }
  @Test
  @DisplayName("상품목록")
  void findAll(){
    List<Product> list = productDAO.findAll();
//    log.info("상품목록={}", list);
    for (Product product : list) {
      log.info("product={}",product);
    }
  }
  
  @Test
  @DisplayName("상품등록")
  void save(){
    Product product = new Product();
    product.setPname("테스트2");
    product.setQuantity(10L);
    product.setPrice(1000000L);

    Long pid = productDAO.save(product);
    log.info("상품번호={}", pid);
  }

  @Test
  @DisplayName("상품등록(여러건)")
  void saveProducts(){

    for (int i = 1; i <= 235; i++) {
      Product product = new Product();
      product.setPname("상품명_" + i);
      product.setQuantity(Long.valueOf(i));
      product.setPrice( Long.valueOf(i * 10));
      Long pid = productDAO.save(product);
    }

  }

  @Test
  @DisplayName("상품조회")
  void findById() {
    Long productId = 20L;
    Optional<Product> optionalProduct = productDAO.findById(productId);
    Product findedProduct = optionalProduct.orElseThrow();// 값이 없으면 예외 발생
    log.info("findedProduct={}", findedProduct);
  }

  @Test
  @DisplayName("상품삭제(단건)")
  void deleteById(){
    Long id = 21L;
    int rows = productDAO.deleteById(id);
    log.info("rows={}",rows);
    Assertions.assertThat(rows).isEqualTo(1);
  }

  @Test
  @DisplayName("상품삭제(여러건)")
  void deleteByIds() {
    List<Long> ids = List.of(2L,3L);
    int rows = productDAO.deleteByIds(ids);
    Assertions.assertThat(rows).isEqualTo(2);
  }

  @Test
  @DisplayName("상품수정")
  void updateById() {
    Long id = 22L;
    Product product = new Product();
    product.setPname("본체");
    product.setQuantity(1L);
    product.setPrice(3_000_000L);

    int rows = productDAO.updateById(id, product);

    Optional<Product> optProduct = productDAO.findById(id);
    // optProduct.orElseThrow() : optional 컨테이너 객체에 product객체가 존재하면 반환 존재하지 않으면 예외발생
    Product modifiedProduct = optProduct.orElseThrow();

    Assertions.assertThat(modifiedProduct.getPname()).isEqualTo("본체");
    Assertions.assertThat(modifiedProduct.getQuantity()).isEqualTo(1L);
    Assertions.assertThat(modifiedProduct.getPrice()).isEqualTo(3_000_000L);
  }

  @Test
  @DisplayName("상품목록-페이징")
  void findAllPaging() {

    List<Product> list = productDAO.findAll(1, 10);
    for (Product product : list) {
      log.info("product_{}",product);
    }
  }

  @Test
  @DisplayName("총 상품 건수")
  void getTotalCount() {

    int totalCount = productDAO.getTotalCount();
    log.info("totalCount={}", totalCount);
  }
}