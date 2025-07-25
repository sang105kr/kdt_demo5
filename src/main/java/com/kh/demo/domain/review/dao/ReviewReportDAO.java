package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.review.entity.ReviewReport;
import com.kh.demo.domain.common.base.BaseDAO;

import java.util.List;

public interface ReviewReportDAO extends BaseDAO<ReviewReport, Long> {
    
    // 리뷰별 신고 목록 조회
    List<ReviewReport> findByReviewId(Long reviewId);
    
    // 댓글별 신고 목록 조회
    List<ReviewReport> findByCommentId(Long commentId);
    
    // 신고자별 신고 목록 조회
    List<ReviewReport> findByReporterId(Long reporterId);
    
    // 상태별 신고 목록 조회
    List<ReviewReport> findByStatus(String status);
    
    // 신고 상태 업데이트
    int updateStatus(Long reportId, String status, String adminMemo);
} 