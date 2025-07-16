package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.shared.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface ReviewCommentDAO extends BaseDAO<ReviewComment, Long> {
    
    // 리뷰별 댓글 목록 조회
    List<ReviewComment> findByReviewId(Long reviewId);
    
    // 리뷰별 댓글 개수 조회
    int countByReviewId(Long reviewId);
    
    // 회원별 댓글 목록 조회
    List<ReviewComment> findByMemberId(Long memberId);
    
    // 댓글 상태 업데이트
    int updateStatus(Long commentId, String status);
} 