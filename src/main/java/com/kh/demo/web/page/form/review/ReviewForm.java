package com.kh.demo.web.page.form.review;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewForm {
    
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;
    
    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;
    
    @NotBlank(message = "리뷰 제목은 필수입니다")
    @Size(min = 2, max = 100, message = "리뷰 제목은 {min}~{max}자 이내여야 합니다")
    private String title;
    
    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(min = 10, max = 2000, message = "리뷰 내용은 {min}~{max}자 이내여야 합니다")
    private String content;
    
    @NotNull(message = "평점은 필수입니다")
    @DecimalMin(value = "0.5", message = "평점은 최소 0.5점 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "평점은 최대 5.0점까지 가능합니다")
    private BigDecimal rating;
} 