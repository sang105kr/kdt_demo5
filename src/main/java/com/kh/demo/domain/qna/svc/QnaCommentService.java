package com.kh.demo.domain.qna.svc;

import com.kh.demo.domain.qna.entity.QnaComment;
import com.kh.demo.domain.common.base.BaseSVC;

import java.util.List;

public interface QnaCommentService extends BaseSVC<QnaComment, Long> {
    
    // Q&A별 댓글 목록 조회
    List<QnaComment> findByQnaId(Long qnaId);
    
    // Q&A별 댓글 개수 조회
    int countByQnaId(Long qnaId);
    
    // 회원별 댓글 목록 조회
    List<QnaComment> findByMemberId(Long memberId);
    
    // 관리자별 댓글 목록 조회
    List<QnaComment> findByAdminId(Long adminId);
    
    // 댓글 타입별 목록 조회
    List<QnaComment> findByCommentTypeId(Long commentTypeId);
    
    // 댓글 작성 (권한 검증 포함)
    QnaComment createComment(QnaComment comment, Long memberId, Long adminId);
    
    // 댓글 수정 (작성자 본인만)
    int updateComment(Long commentId, QnaComment comment, Long memberId, Long adminId);
    
    // 댓글 삭제 (작성자 본인 또는 관리자만)
    int deleteComment(Long commentId, Long memberId, Long adminId, boolean isAdmin);
    
    // 댓글 상태 업데이트
    int updateStatus(Long commentId, Long statusId);
    
    // 도움됨 수 증가
    int incrementHelpfulCount(Long commentId);
    
    // 도움안됨 수 증가
    int incrementUnhelpfulCount(Long commentId);
    
    // 전체 댓글 목록 조회 (페이징) - 관리자용
    List<QnaComment> findAllWithPaging(int offset, int limit);
    
    // 전체 댓글 개수 조회 - 관리자용
    int countAll();
}
