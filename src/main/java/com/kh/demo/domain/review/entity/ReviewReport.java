package com.kh.demo.domain.review.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewReport extends BaseEntity {
    private Long reportId;
    private Long reviewId;
    private Long commentId;
    private Long reporterId;
    private String reportType;
    private String reportReason;
    private String status;
    private String adminMemo;
} 