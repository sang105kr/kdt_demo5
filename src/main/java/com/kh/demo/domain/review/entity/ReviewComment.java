package com.kh.demo.domain.review.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
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
    private String status;
    
    // 조인을 위한 추가 필드
    private String memberNickname;
    private String reviewTitle;
} 