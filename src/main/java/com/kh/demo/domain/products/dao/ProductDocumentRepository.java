package com.kh.demo.domain.products.dao;

import com.kh.demo.domain.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch 상품 검색 Repository
 */
@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {
  /**
   * 상품명으로 검색 (동의어 포함)
   */
  List<ProductDocument> findByPnameContaining(String pname);

  /**
   * 상품설명으로 검색 (HTML 제거 후 검색)
   */
  List<ProductDocument> findByDescriptionContaining(String description);

  /**
   * 카테고리로 검색
   */
  List<ProductDocument> findByCategory(String category);

  /**
   * 가격 범위로 검색
   */
  List<ProductDocument> findByPriceBetween(Long minPrice, Long maxPrice);

  /**
   * 평점 이상으로 검색
   */
  List<ProductDocument> findByRatingGreaterThanEqual(Double rating);
} 