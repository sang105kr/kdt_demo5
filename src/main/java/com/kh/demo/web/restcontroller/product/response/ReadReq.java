package com.kh.demo.web.restcontroller.product.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 상품 조회 응답 DTO
 */
@Data
public class ReadReq {
    private Long productId;         // 상품 식별자
    private String pname;           // 상품명
    private String description;     // 상품설명
    private Integer price;          // 상품가격
    private Double rating;          // 상품평점
    private String category;        // 상품카테고리
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
} 