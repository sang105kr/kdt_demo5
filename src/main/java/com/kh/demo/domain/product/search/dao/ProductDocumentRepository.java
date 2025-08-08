package com.kh.demo.domain.product.search.dao;

import com.kh.demo.domain.product.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch 상품 검색 Repository
 */
@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {
  /**
   * 카테고리로 검색
   */
  List<ProductDocument> findByCategoryName(String categoryName);
  
  /**
   * Oracle의 productId로 검색
   */
  ProductDocument findByProductId(Long productId);
} 