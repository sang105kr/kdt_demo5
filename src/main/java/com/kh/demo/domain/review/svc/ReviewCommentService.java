package com.kh.demo.domain.review.svc;

import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.shared.base.BaseSVC;

import java.util.List;
import java.util.Optional;

public interface ReviewCommentService extends BaseSVC<ReviewComment, Long> {
    
    // 리뷰별 댓글 목록 조회
    List<ReviewComment> findByReviewId(Long reviewId);
    
    // 리뷰별 댓글 개수 조회
    int countByReviewId(Long reviewId);
    
    // 회원별 댓글 목록 조회
    List<ReviewComment> findByMemberId(Long memberId);
    
    // 댓글 작성 (권한 검증 포함)
    ReviewComment createComment(ReviewComment comment, Long memberId);
    
    // 댓글 수정 (작성자 본인만)
    int updateComment(Long commentId, ReviewComment comment, Long memberId);
    
    // 댓글 삭제 (작성자 본인 또는 관리자만)
    int deleteComment(Long commentId, Long memberId, boolean isAdmin);
    
    // 댓글 상태 업데이트
    int updateStatus(Long commentId, String status);
} 