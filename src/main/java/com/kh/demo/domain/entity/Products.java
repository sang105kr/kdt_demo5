package com.kh.demo.domain.entity;
import lombok.Data;

@Data
public class Products {
    private Long productId;   // 상품아이디 (PRODUCT_ID)
    private String pname;     // 상품명 (PNAME)
    private String description; // 상품설명 (DESCRIPTION)
    private Long price;       // 상품가격 (PRICE)
    private Double rating;    // 상품평점 (RATING)
    private String category;  // 상품카테고리 (CATEGORY)
}
