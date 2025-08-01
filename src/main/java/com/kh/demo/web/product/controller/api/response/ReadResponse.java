package com.kh.demo.web.product.controller.api.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 상품 조회 응답 DTO
 */
@Data
public class ReadResponse {
    private Long productId;         // 상품 식별자
    private String pname;           // 상품명
    private String description;     // 상품설명
    private Integer price;          // 상품가격
    private Double rating;          // 상품평점
    private String category;        // 상품카테고리
    private String imageUrl;        // 대표 이미지 URL
    private String manualUrl;       // 설명서 URL
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
} 