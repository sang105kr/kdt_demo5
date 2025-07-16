package com.kh.demo.domain.review.vo;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewCommentDetailVO {
    private ReviewComment comment;
    private Member member;
    private Review review;
} 