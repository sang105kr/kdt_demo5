package com.kh.demo.web.product.controller.page.dto;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.document.ProductDocument;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 목록 표시용 DTO
 * 엔티티와 뷰 사이의 데이터 전달 객체
 */
@Data
@NoArgsConstructor
public class ProductListDTO {
    
    // 상품 기본 정보
    private Long productId;
    private String pname;
    private String description;
    private Integer price;
    private Double rating;
    private String category;
    private Integer stockQuantity;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    // 뷰 전용 필드
    private String imageUrl;
    private String manualUrl;
    
    // 하이라이팅 필드 (Elasticsearch 검색 결과용)
    private String highlightedPname;
    private String highlightedDescription;
    
    /**
     * Products 엔티티로부터 DTO 생성
     */
    public static ProductListDTO from(Products product) {
        ProductListDTO dto = new ProductListDTO();
        dto.setProductId(product.getProductId());
        dto.setPname(product.getPname());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setRating(product.getRating());
        dto.setCategory(product.getCategory());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCdate(product.getCdate());
        dto.setUdate(product.getUdate());
        return dto;
    }
    
    /**
     * ProductDocument로부터 DTO 생성 (하이라이팅 정보 포함)
     */
    public static ProductListDTO from(ProductDocument document) {
        ProductListDTO dto = new ProductListDTO();
        dto.setProductId(document.getProductId());
        dto.setPname(document.getPname());
        dto.setDescription(document.getDescription());
        dto.setPrice(document.getPrice());
        dto.setRating(document.getRating());
        dto.setCategory(document.getCategory());
        dto.setStockQuantity(document.getStockQuantity());
        
        // 하이라이팅 정보 설정 (Elasticsearch에서 하이라이팅된 필드가 원본 필드를 덮어씀)
        // 하이라이팅이 적용된 경우 pname, description에 하이라이팅 태그가 포함됨
        dto.setHighlightedPname(document.getPname());
        dto.setHighlightedDescription(document.getDescription());
        
        return dto;
    }
    
    /**
     * 하이라이팅된 상품명 반환 (하이라이팅이 있으면 하이라이팅된 버전, 없으면 원본)
     */
    public String getDisplayPname() {
        return highlightedPname != null ? highlightedPname : pname;
    }
    
    /**
     * 하이라이팅된 설명 반환 (하이라이팅이 있으면 하이라이팅된 버전, 없으면 원본)
     */
    public String getDisplayDescription() {
        return highlightedDescription != null ? highlightedDescription : description;
    }
} 