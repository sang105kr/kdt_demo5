package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.entity.Product;
import com.kh.demo.domain.product.dao.ProductDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSVCImpl implements ProductSVC{

  final private ProductDAO productDAO;

//  public ProductSVCImpl(ProductDAO productDAO) {
//    this.productDAO = productDAO;
//  }

  //상품등록
  @Override
  public Long save(Product product) {
    return productDAO.save(product);
  }

  //상품목록
  @Override
  public List<Product> findAll() {
    return productDAO.findAll();
  }

  //상품목록-페이징
  @Override
  public List<Product> findAll(int pageNo, int numOfRows) {
    return productDAO.findAll(pageNo, numOfRows);
  }

  //상품 총건수
  @Override
  public int getTotalCount() {
    return productDAO.getTotalCount();
  }

  //상품조회

  @Override
  public Optional<Product> findById(Long id) {
    return productDAO.findById(id);
  }

  //상품삭제(단건)
  @Override
  public int deleteById(Long id) {
    return productDAO.deleteById(id);
  }

  //상품삭제(여러건)
  @Override
  public int deleteByIds(List<Long> ids) {
    return productDAO.deleteByIds(ids);
  }

  //상품수정
  @Override
  public int updateById(Long productId, Product product) {
    return productDAO.updateById(productId, product);
  }
}
