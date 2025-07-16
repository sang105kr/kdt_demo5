package com.kh.demo.domain.product.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Products extends BaseEntity {
    private Long productId;         // 상품 식별자
    private String pname;           // 상품명
    private String description;     // 상품설명
    private Integer price;          // 상품가격
    private Double rating;          // 상품평점
    private String category;        // 상품카테고리
    private Integer stockQuantity;  // 재고 수량
} 