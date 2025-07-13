package com.kh.demo.domain.product.dto;

import lombok.Data;

/**
 * 상품 검색 조건 DTO
 */
@Data
public class ProductSearchDTO {
    private String pname;
    private String category;
    private Integer minPrice;
    private Integer maxPrice;
    private Double minRating;
    private String searchType; // pname, description, category, all
    private String searchKeyword;
    private String sortBy; // price, rating, cdate
    private String sortOrder; // asc, desc
} 