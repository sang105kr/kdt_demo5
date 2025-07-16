package com.kh.demo.domain.review.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Review extends BaseEntity {
    private Long reviewId;
    private Long productId;
    private Long memberId;
    private Long orderId;
    private BigDecimal rating;
    private String title;
    private String content;
    private Integer helpfulCount;
    private Integer reportCount;
    private String status;
    
    // 조인을 위한 추가 필드
    private String memberNickname;
    private String productName;
    private String orderNumber;
} 