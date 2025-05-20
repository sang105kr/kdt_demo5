package com.kh.demo.domain.product.dao;

import com.kh.demo.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {
  //상품등록
  Long save(Product product);

  //상품목록
  List<Product> findAll();

  /**
   * 상품목록 페이징
   * @param pageNo 요청 페이지
   * @param numOfRows 요청 페이지 레코드 수
   * @return 상품목록
   */
  List<Product> findAll(int pageNo, int numOfRows);

  /**
   *  상품 총 건수
   * @return 총 건수
   */
  int getTotalCount();
  
  //상품조회
  Optional<Product> findById(Long id);
  
  //상품삭제(단건)
  int deleteById(Long id);

  //상품삭제(여러건)
  int deleteByIds(List<Long> ids);

  //상품수정
  int updateById(Long productId, Product product);

}
