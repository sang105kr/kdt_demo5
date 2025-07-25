package com.kh.demo.web.product.controller.page.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 검색 조건 DTO
 */
@Data
@Builder
public class SearchCriteria {
    private String keyword;           // 검색 키워드
    private String category;          // 카테고리
    private Long minPrice;            // 최소 가격
    private Long maxPrice;            // 최대 가격
    private Double minRating;         // 최소 평점
    private String sortBy;            // 정렬 기준 (price, rating, name, date)
    private String sortOrder;         // 정렬 순서 (asc, desc)
    private int page;                 // 페이지 번호
    private int size;                 // 페이지 크기
    
    public static SearchCriteria of(String keyword, String category, Long minPrice, Long maxPrice, 
                                   Double minRating, String sortBy, String sortOrder, int page, int size) {
        return SearchCriteria.builder()
                .keyword(keyword)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .sortBy(sortBy != null ? sortBy : "date")
                .sortOrder(sortOrder != null ? sortOrder : "desc")
                .page(page > 0 ? page : 1)
                .size(size > 0 ? size : 12)
                .build();
    }
} 