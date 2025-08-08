package com.kh.demo.domain.review.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Review extends BaseEntity {
    private Long reviewId;
    private Long productId;
    private Long memberId;
    private Long orderId;
    private Integer rating; // 1~5 사이의 평점
    private String title;
    private String content;
    private Integer helpfulCount;
    private Integer reportCount;
    private Long statusId; // code_id
} 