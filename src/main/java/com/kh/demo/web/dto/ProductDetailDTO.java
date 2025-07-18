package com.kh.demo.web.dto;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.search.document.ProductDocument;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 상세 정보 DTO (Oracle + Elasticsearch 통합)
 */
@Data
@Builder
public class ProductDetailDTO {
    // 기본 정보 (Oracle)
    private Long productId;
    private String pname;
    private String description;
    private Integer price;
    private String category;
    private Integer stockQuantity;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    // 검색 관련 정보 (Elasticsearch)
    private Double rating;
    private Integer reviewCount;
    private Integer viewCount;
    
    // 파일 정보
    private List<String> imageUrls;
    private List<String> manualUrls;
    
    // 관련 상품
    private List<ProductListDTO> relatedProducts;
    
    // 하이라이팅 필드 (Elasticsearch 검색 결과용)
    private String highlightedPname;
    private String highlightedDescription;
    
    /**
     * Oracle Products와 Elasticsearch ProductDocument를 통합
     */
    public static ProductDetailDTO merge(Products product, ProductDocument document) {
        return ProductDetailDTO.builder()
                .productId(product.getProductId())
                .pname(product.getPname())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .cdate(product.getCdate())
                .udate(product.getUdate())
                .rating(document != null ? document.getRating() : product.getRating())
                .reviewCount(document != null ? document.getReviewCount() : 0)
                .viewCount(document != null ? document.getViewCount() : 0)
                .build();
    }
    
    /**
     * Oracle Products만으로 생성 (Elasticsearch 실패 시)
     */
    public static ProductDetailDTO from(Products product) {
        return ProductDetailDTO.builder()
                .productId(product.getProductId())
                .pname(product.getPname())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .cdate(product.getCdate())
                .udate(product.getUdate())
                .rating(product.getRating())
                .reviewCount(0)
                .viewCount(0)
                .build();
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