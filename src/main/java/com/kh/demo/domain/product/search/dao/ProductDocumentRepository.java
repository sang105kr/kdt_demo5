package com.kh.demo.domain.product.search.dao;

import com.kh.demo.domain.product.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch 상품 검색 Repository
 */
@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {
  /**
   * 가격 범위로 검색
   */
  List<ProductDocument> findByPriceBetween(Long minPrice, Long maxPrice);

  /**
   * 평점 이상으로 검색
   */
  List<ProductDocument> findByRatingGreaterThanEqual(Double rating);
} 