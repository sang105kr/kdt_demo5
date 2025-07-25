package com.kh.demo.domain.review.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewComment extends BaseEntity {
    private Long commentId;
    private Long reviewId;
    private Long memberId;
    private Long parentId;
    private String content;
    private Integer helpfulCount;
    private Integer reportCount;
    private Long status; // code_id
} 