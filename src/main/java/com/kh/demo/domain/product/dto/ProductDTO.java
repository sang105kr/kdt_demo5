package com.kh.demo.domain.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 상품 데이터 전송 객체
 */
@Data
public class ProductDTO {
    private Long productId;
    private String pname;
    private String description;
    private Integer price;
    private Double rating;
    private String category;
    private LocalDateTime cdate;
    private LocalDateTime udate;
} 