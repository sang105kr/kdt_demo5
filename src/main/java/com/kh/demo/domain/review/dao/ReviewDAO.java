package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.shared.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface ReviewDAO extends BaseDAO<Review, Long> {
    
    // 상품별 리뷰 목록 조회 (페이징)
    List<Review> findByProductId(Long productId, int offset, int limit);
    
    // 상품별 리뷰 개수 조회
    int countByProductId(Long productId);
    
    // 회원별 리뷰 목록 조회
    List<Review> findByMemberId(Long memberId);
    
    // 주문별 리뷰 조회 (구매 인증용)
    Optional<Review> findByOrderId(Long orderId);
    
    // 평점별 리뷰 목록 조회
    List<Review> findByProductIdAndRating(Long productId, Double rating, int offset, int limit);
    
    // 리뷰 댓글 목록 조회
    List<ReviewComment> findCommentsByReviewId(Long reviewId);
    
    // 도움됨 수 증가
    int incrementHelpfulCount(Long reviewId);
    
    // 신고 수 증가
    int incrementReportCount(Long reviewId);
    
    // 리뷰 상태 업데이트
    int updateStatus(Long reviewId, String status);
    
    // 상품 평균 평점 업데이트
    int updateProductRating(Long productId);
} 