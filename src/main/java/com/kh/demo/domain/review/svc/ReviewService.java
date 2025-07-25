package com.kh.demo.domain.review.svc;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.vo.ReviewDetailVO;
import com.kh.demo.domain.common.base.BaseSVC;

import java.util.List;
import java.util.Optional;

public interface ReviewService extends BaseSVC<Review, Long> {
    
    // 리뷰 상세 정보 조회 (관련 엔티티들과 함께)
    Optional<ReviewDetailVO> findReviewDetailById(Long reviewId);
    
    // 상품별 리뷰 목록 조회 (페이징)
    List<ReviewDetailVO> findReviewDetailsByProductId(Long productId, int offset, int limit);
    
    // 회원별 리뷰 목록 조회
    List<ReviewDetailVO> findReviewDetailsByMemberId(Long memberId);
    
    // 회원별 리뷰 목록 조회 (기본 엔티티)
    List<Review> findByMemberId(Long memberId);
    
    // 주문별 리뷰 조회 (구매 인증용)
    Optional<ReviewDetailVO> findReviewDetailByOrderId(Long orderId);
    
    // 평점별 리뷰 목록 조회
    List<ReviewDetailVO> findReviewDetailsByProductIdAndRating(Long productId, Double rating, int offset, int limit);
    
    // 리뷰 댓글 목록 조회
    List<ReviewComment> findCommentsByReviewId(Long reviewId);
    
    // 주문별 리뷰 조회
    Optional<Review> findByOrderId(Long orderId);
    Optional<Review> findByOrderIdAndProductId(Long orderId, Long productId);
    
    // 도움됨 수 증가
    int incrementHelpfulCount(Long reviewId);
    
    // 신고 수 증가
    int incrementReportCount(Long reviewId);
    
    // 리뷰 상태 업데이트
    int updateStatus(Long reviewId, Long statusCodeId);
    
    // 상품 평균 평점 업데이트
    int updateProductRating(Long productId);
    
    // 리뷰 작성 (구매 인증 포함)
    Review createReview(Review review);
    
    // 리뷰 수정 (작성자 본인만)
    int updateReview(Long reviewId, Review review, Long memberId);
    
    // 리뷰 삭제 (작성자 본인 또는 관리자만)
    int deleteReview(Long reviewId, Long memberId, boolean isAdmin);
    
    // 상품별 공개 리뷰 목록 조회
    List<Review> findByProductIdAndStatus(Long productId, Long statusCodeId);
    
    // 공개 상태의 리뷰 상세 조회
    Optional<Review> findByIdAndStatus(Long reviewId, Long statusCodeId);
} 