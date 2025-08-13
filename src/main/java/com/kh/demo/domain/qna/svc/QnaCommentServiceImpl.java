package com.kh.demo.domain.qna.svc;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.qna.dao.QnaCommentDAO;
import com.kh.demo.domain.qna.dao.QnaDAO;
import com.kh.demo.domain.qna.entity.QnaComment;
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
public class QnaCommentServiceImpl implements QnaCommentService {

    private final QnaCommentDAO qnaCommentDAO;
    private final QnaDAO qnaDAO;
    private final CodeSVC codeSVC;

    @Override
    @Transactional
    public Long save(QnaComment comment) {
        return qnaCommentDAO.save(comment);
    }

    @Override
    public Optional<QnaComment> findById(Long id) {
        return qnaCommentDAO.findById(id);
    }

    @Override
    public List<QnaComment> findAll() {
        return qnaCommentDAO.findAll();
    }

    @Override
    public List<QnaComment> findAll(int pageNo, int numOfRows) {
        int offset = (pageNo - 1) * numOfRows;
        return qnaCommentDAO.findAllWithPaging(offset, numOfRows);
    }

    @Override
    @Transactional
    public int updateById(Long id, QnaComment comment) {
        return qnaCommentDAO.updateById(id, comment);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        return qnaCommentDAO.deleteById(id);
    }

    @Override
    public int getTotalCount() {
        return qnaCommentDAO.getTotalCount();
    }

    @Override
    public List<QnaComment> findByQnaId(Long qnaId) {
        return qnaCommentDAO.findByQnaId(qnaId);
    }

    @Override
    public int countByQnaId(Long qnaId) {
        return qnaCommentDAO.countByQnaId(qnaId);
    }

    @Override
    public List<QnaComment> findByMemberId(Long memberId) {
        return qnaCommentDAO.findByMemberId(memberId);
    }

    @Override
    public List<QnaComment> findByAdminId(Long adminId) {
        return qnaCommentDAO.findByAdminId(adminId);
    }

    @Override
    public List<QnaComment> findByCommentTypeId(Long commentTypeId) {
        return qnaCommentDAO.findByCommentTypeId(commentTypeId);
    }

    @Override
    @Transactional
    public QnaComment createComment(QnaComment comment, Long memberId, Long adminId) {
        // 권한 검증: 로그인한 사용자 또는 관리자만 댓글 작성 가능
        if (memberId == null && adminId == null) {
            throw new BusinessValidationException("로그인이 필요한 서비스입니다.");
        }

        // Q&A 존재 여부 확인
        Optional<com.kh.demo.domain.qna.entity.Qna> qnaOpt = qnaDAO.findById(comment.getQnaId());
        if (qnaOpt.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 Q&A입니다.");
        }

        comment.setMemberId(memberId);
        comment.setAdminId(adminId);
        
        // statusId가 null이면 ACTIVE 상태로 설정
        if (comment.getStatusId() == null) {
            Long activeStatusId = codeSVC.getCodeId("QNA_COMMENT_STATUS", "ACTIVE");
            comment.setStatusId(activeStatusId);
        }

        // commentTypeId가 null이면 COMMENT 타입으로 설정
        if (comment.getCommentTypeId() == null) {
            Long commentTypeId = codeSVC.getCodeId("QNA_COMMENT_TYPE", "COMMENT");
            comment.setCommentTypeId(commentTypeId);
        }

        // 초기값 설정
        if (comment.getHelpfulCount() == null) {
            comment.setHelpfulCount(0);
        }
        if (comment.getUnhelpfulCount() == null) {
            comment.setUnhelpfulCount(0);
        }

        Long commentId = qnaCommentDAO.save(comment);
        
        // Q&A의 댓글 수 증가
        qnaDAO.incrementCommentCount(comment.getQnaId());
        
        return qnaCommentDAO.findById(commentId)
            .orElseThrow(() -> new BusinessValidationException("댓글 저장 중 오류가 발생했습니다."));
    }

    @Override
    @Transactional
    public int updateComment(Long commentId, QnaComment comment, Long memberId, Long adminId) {
        // 권한 검증: 본인이 작성한 댓글만 수정 가능
        Optional<QnaComment> existingComment = qnaCommentDAO.findById(commentId);
        if (existingComment.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 댓글입니다.");
        }

        QnaComment foundComment = existingComment.get();
        boolean isOwner = false;
        
        if (memberId != null && foundComment.getMemberId() != null && foundComment.getMemberId().equals(memberId)) {
            isOwner = true;
        }
        if (adminId != null && foundComment.getAdminId() != null && foundComment.getAdminId().equals(adminId)) {
            isOwner = true;
        }
        
        if (!isOwner) {
            throw new BusinessValidationException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 수정 불가능한 상태인지 확인
        Long activeStatusId = codeSVC.getCodeId("QNA_COMMENT_STATUS", "ACTIVE");
        if (!foundComment.getStatusId().equals(activeStatusId)) {
            throw new BusinessValidationException("수정할 수 없는 상태의 댓글입니다.");
        }

        comment.setCommentId(commentId);
        comment.setQnaId(foundComment.getQnaId());
        comment.setMemberId(foundComment.getMemberId());
        comment.setAdminId(foundComment.getAdminId());
        comment.setStatusId(activeStatusId);

        return qnaCommentDAO.updateById(commentId, comment);
    }

    @Override
    @Transactional
    public int deleteComment(Long commentId, Long memberId, Long adminId, boolean isAdmin) {
        // 권한 검증: 본인이 작성한 댓글 또는 관리자만 삭제 가능
        Optional<QnaComment> existingComment = qnaCommentDAO.findById(commentId);
        if (existingComment.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 댓글입니다.");
        }

        QnaComment foundComment = existingComment.get();
        boolean isOwner = false;
        
        if (memberId != null && foundComment.getMemberId() != null && foundComment.getMemberId().equals(memberId)) {
            isOwner = true;
        }
        if (adminId != null && foundComment.getAdminId() != null && foundComment.getAdminId().equals(adminId)) {
            isOwner = true;
        }
        
        if (!isAdmin && !isOwner) {
            throw new BusinessValidationException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        int result = qnaCommentDAO.deleteById(commentId);
        
        if (result > 0) {
            // Q&A의 댓글 수 감소
            qnaDAO.decrementCommentCount(foundComment.getQnaId());
        }
        
        return result;
    }

    @Override
    @Transactional
    public int updateStatus(Long commentId, Long statusId) {
        return qnaCommentDAO.updateStatus(commentId, statusId);
    }

    @Override
    @Transactional
    public int incrementHelpfulCount(Long commentId) {
        return qnaCommentDAO.incrementHelpfulCount(commentId);
    }

    @Override
    @Transactional
    public int incrementUnhelpfulCount(Long commentId) {
        return qnaCommentDAO.incrementUnhelpfulCount(commentId);
    }

    @Override
    public List<QnaComment> findAllWithPaging(int offset, int limit) {
        return qnaCommentDAO.findAllWithPaging(offset, limit);
    }

    @Override
    public int countAll() {
        return qnaCommentDAO.countAll();
    }
}
