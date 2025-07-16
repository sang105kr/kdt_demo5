package com.kh.demo.domain.review.svc;

import com.kh.demo.domain.review.dao.ReviewCommentDAO;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewCommentServiceImpl implements ReviewCommentService {
    
    private final ReviewCommentDAO reviewCommentDAO;
    
    @Override
    @Transactional
    public Long save(ReviewComment comment) {
        return reviewCommentDAO.save(comment);
    }
    
    @Override
    public Optional<ReviewComment> findById(Long id) {
        return reviewCommentDAO.findById(id);
    }
    
    @Override
    public List<ReviewComment> findAll() {
        return reviewCommentDAO.findAll();
    }
    
    @Override
    public List<ReviewComment> findAll(int pageNo, int numOfRows) {
        // 페이징 구현 (간단한 방식)
        List<ReviewComment> allComments = reviewCommentDAO.findAll();
        int startIndex = (pageNo - 1) * numOfRows;
        int endIndex = Math.min(startIndex + numOfRows, allComments.size());
        
        if (startIndex >= allComments.size()) {
            return List.of();
        }
        
        return allComments.subList(startIndex, endIndex);
    }
    
    @Override
    @Transactional
    public int updateById(Long id, ReviewComment comment) {
        return reviewCommentDAO.updateById(id, comment);
    }
    
    @Override
    @Transactional
    public int deleteById(Long id) {
        return reviewCommentDAO.deleteById(id);
    }
    
    @Override
    public int getTotalCount() {
        return reviewCommentDAO.getTotalCount();
    }
    
    @Override
    public List<ReviewComment> findByReviewId(Long reviewId) {
        return reviewCommentDAO.findByReviewId(reviewId);
    }
    
    @Override
    public int countByReviewId(Long reviewId) {
        return reviewCommentDAO.countByReviewId(reviewId);
    }
    
    @Override
    public List<ReviewComment> findByMemberId(Long memberId) {
        return reviewCommentDAO.findByMemberId(memberId);
    }
    
    @Override
    @Transactional
    public ReviewComment createComment(ReviewComment comment, Long memberId) {
        // 권한 검증: 로그인한 사용자만 댓글 작성 가능
        if (memberId == null) {
            throw new BusinessValidationException("로그인이 필요한 서비스입니다.");
        }
        
        comment.setMemberId(memberId);
        comment.setStatus("ACTIVE");
        
        Long commentId = reviewCommentDAO.save(comment);
        comment.setCommentId(commentId);
        
        return comment;
    }
    
    @Override
    @Transactional
    public int updateComment(Long commentId, ReviewComment comment, Long memberId) {
        // 권한 검증: 본인이 작성한 댓글만 수정 가능
        Optional<ReviewComment> existingComment = reviewCommentDAO.findById(commentId);
        if (existingComment.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 댓글입니다.");
        }
        
        ReviewComment foundComment = existingComment.get();
        if (!foundComment.getMemberId().equals(memberId)) {
            throw new BusinessValidationException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        
        // 수정 불가능한 상태인지 확인
        if (!"ACTIVE".equals(foundComment.getStatus())) {
            throw new BusinessValidationException("수정할 수 없는 상태의 댓글입니다.");
        }
        
        comment.setCommentId(commentId);
        comment.setReviewId(foundComment.getReviewId());
        comment.setMemberId(memberId);
        comment.setStatus("ACTIVE");
        
        return reviewCommentDAO.updateById(commentId, comment);
    }
    
    @Override
    @Transactional
    public int deleteComment(Long commentId, Long memberId, boolean isAdmin) {
        // 권한 검증: 본인이 작성한 댓글이거나 관리자만 삭제 가능
        Optional<ReviewComment> existingComment = reviewCommentDAO.findById(commentId);
        if (existingComment.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 댓글입니다.");
        }
        
        ReviewComment foundComment = existingComment.get();
        if (!isAdmin && !foundComment.getMemberId().equals(memberId)) {
            throw new BusinessValidationException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
        
        // 소프트 삭제 (상태 변경)
        return reviewCommentDAO.updateStatus(commentId, "DELETED");
    }
    
    @Override
    @Transactional
    public int updateStatus(Long commentId, String status) {
        return reviewCommentDAO.updateStatus(commentId, status);
    }
} 