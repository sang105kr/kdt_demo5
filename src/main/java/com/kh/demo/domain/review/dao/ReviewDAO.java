package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.common.base.BaseDAO;

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
    Optional<Review> findByOrderIdAndProductId(Long orderId, Long productId);
    
    // 주문별 활성 리뷰 조회 (주문 내역에서 리뷰 작성 여부 확인용)
    Optional<Review> findActiveByOrderIdAndProductId(Long orderId, Long productId);
    
    // 평점별 리뷰 목록 조회
    List<Review> findByProductIdAndRating(Long productId, Double rating, int offset, int limit);
    
    // 리뷰 댓글 목록 조회
    List<ReviewComment> findCommentsByReviewId(Long reviewId);
    
    // 도움됨 수 증가
    int incrementHelpfulCount(Long reviewId);
    
    // 도움안됨 수 증가
    int incrementUnhelpfulCount(Long reviewId);
    
    // 신고 수 증가
    int incrementReportCount(Long reviewId);
    
    // 리뷰 상태 업데이트
    int updateStatus(Long reviewId, Long statusCodeId);
    
    // 상품 평균 평점 업데이트
    int updateProductRating(Long productId);
    
    // 상품별 공개 리뷰 목록 조회
    List<Review> findByProductIdAndStatus(Long productId, Long statusCodeId);
    
    // 공개 상태의 리뷰 상세 조회
    Optional<Review> findByIdAndStatus(Long reviewId, Long statusCodeId);
    
    // 사용자가 특정 상품에 대해 리뷰를 작성했는지 확인
    List<Review> findByMemberIdAndProductIdAndStatus(Long memberId, Long productId, Long statusCodeId);
    
    // === 관리자용 페이징 및 검색 메서드들 ===
    
    // 전체 리뷰 목록 조회 (페이징)
    List<Review> findAllWithPaging(int offset, int limit);
    
    // 상태별 리뷰 목록 조회 (페이징)
    List<Review> findByStatusWithPaging(Long statusId, int offset, int limit);
    
    // 키워드 검색 리뷰 목록 조회 (페이징)
    List<Review> findByKeywordWithPaging(String keyword, int offset, int limit);
    
    // 날짜 범위별 리뷰 목록 조회 (페이징)
    List<Review> findByDateRangeWithPaging(String startDate, String endDate, int offset, int limit);
    
    // === 카운트 메서드들 ===
    
    // 전체 리뷰 개수
    int countAll();
    
    // 상태별 리뷰 개수
    int countByStatus(Long statusId);
    
    // 키워드 검색 결과 개수
    int countByKeyword(String keyword);
    
    // 날짜 범위별 리뷰 개수
    int countByDateRange(String startDate, String endDate);
} 