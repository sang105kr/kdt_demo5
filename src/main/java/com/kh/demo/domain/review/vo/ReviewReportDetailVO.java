package com.kh.demo.domain.review.vo;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.entity.ReviewReport;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewReportDetailVO {
    private ReviewReport report;
    private Member reporter;
    private Review review;
    private ReviewComment comment;
} 