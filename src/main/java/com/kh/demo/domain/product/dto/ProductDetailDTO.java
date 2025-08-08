package com.kh.demo.domain.product.dto;

import com.kh.demo.domain.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 상품 상세 정보 DTO
 * Products 엔티티와 Code 테이블을 조인한 결과
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailDTO extends BaseDTO {
    
    // Products 기본 정보
    private Long productId;
    private String pname;
    private String description;
    private Integer price;
    private Double rating;
    private Integer stockQuantity;
    
    // 코드 참조 필드
    private Long categoryId;        // 카테고리 코드 ID
    
    // 코드 decode 값 (조인으로 조회)
    private String categoryCode;    // 카테고리 코드 (ELECTRONICS, CLOTHING, etc.)
    private String categoryName;    // 카테고리명 (전자제품, 의류, etc.)
    
    /**
     * 재고 상태 확인
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
    
    /**
     * 재고 부족 여부 확인 (10개 미만)
     */
    public boolean isLowStock() {
        return stockQuantity != null && stockQuantity > 0 && stockQuantity < 10;
    }
    
    /**
     * 평점을 별표로 표시 (5점 만점)
     */
    public String getRatingStars() {
        if (rating == null) return "☆☆☆☆☆";
        int stars = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < stars ? "★" : "☆");
        }
        return sb.toString();
    }
}