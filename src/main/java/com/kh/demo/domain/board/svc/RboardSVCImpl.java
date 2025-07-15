package com.kh.demo.domain.board.svc;

import com.kh.demo.domain.board.dao.RboardDAO;
import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 댓글 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RboardSVCImpl implements RboardSVC {

    private final RboardDAO rboardDAO;

    /**
     * 댓글 등록
     */
    @Override
    @Transactional
    public Long save(Replies reply) {
        // 비즈니스 로직: 댓글 내용 검증
        validateRcontent(reply.getRcontent());
        
        // 비즈니스 로직: 부모 댓글 존재 여부 확인 (답글인 경우)
        if (reply.getParentId() != null) {
            validateParentReply(reply.getParentId());
        }
        
        // 기본값 설정
        if (reply.getCdate() == null) {
            reply.setCdate(LocalDateTime.now());
        }
        if (reply.getUdate() == null) {
            reply.setUdate(LocalDateTime.now());
        }

        // 계층형 댓글 로직 처리
        if (reply.getParentId() == null) {
            // 최상위 댓글 등록
            saveOriginalReply(reply);
        } else {
            // 대댓글 등록
            saveReplyReply(reply);
        }

        return reply.getReplyId();
    }
    
    /**
     * 최상위 댓글 등록 처리
     */
    private void saveOriginalReply(Replies reply) {
        // 최상위 댓글은 reply_id를 rgroup으로 사용
        Long replyId = rboardDAO.save(reply);
        reply.setReplyId(replyId);
        reply.setRgroup(replyId);
        reply.setRstep(0);
        reply.setRindent(0);
        reply.setParentId(null);
        reply.setStatus("A");
        
        // rgroup 업데이트
        rboardDAO.updateRgroup(replyId, replyId);
    }
    
    /**
     * 대댓글 등록 처리
     */
    private void saveReplyReply(Replies reply) {
        // 부모 댓글 조회
        Replies parentReply = rboardDAO.findById(reply.getParentId())
                .orElseThrow(() -> new BusinessValidationException("부모 댓글을 찾을 수 없습니다."));
        
        // 계층 구조 설정
        reply.setRgroup(parentReply.getRgroup());
        reply.setRstep(parentReply.getRstep() + 1);
        reply.setRindent(parentReply.getRindent() + 1);
        reply.setStatus("A");
        
        // 대댓글 저장
        Long replyId = rboardDAO.save(reply);
        reply.setReplyId(replyId);
    }

    /**
     * 댓글 수정
     */
    @Override
    @Transactional
    public int update(Replies reply) {
        // 비즈니스 로직: 댓글 존재 여부 확인
        if (!rboardDAO.findById(reply.getReplyId()).isPresent()) {
            throw new BusinessValidationException("댓글번호: " + reply.getReplyId() + "를 찾을 수 없습니다.");
        }
        
        // 비즈니스 로직: 댓글 내용 검증
        validateRcontent(reply.getRcontent());
        
        reply.setUdate(LocalDateTime.now());
        return rboardDAO.updateById(reply.getReplyId(), reply);
    }

    /**
     * 댓글 삭제
     */
    @Override
    @Transactional
    public int deleteById(Long replyId) {
        // 비즈니스 로직: 댓글 존재 여부 확인
        if (!rboardDAO.findById(replyId).isPresent()) {
            throw new BusinessValidationException("댓글번호: " + replyId + "를 찾을 수 없습니다.");
        }
        
        return rboardDAO.deleteById(replyId);
    }

    /**
     * 댓글 ID로 조회
     */
    @Override
    public Optional<Replies> findById(Long replyId) {
        return rboardDAO.findById(replyId);
    }

    /**
     * 게시글별 댓글 조회
     */
    @Override
    public List<Replies> findByBoardId(Long boardId) {
        return rboardDAO.findByBoardId(boardId);
    }

    /**
     * 게시글별 댓글 페이징 조회
     */
    @Override
    public List<Replies> findByBoardIdWithPaging(Long boardId, int pageNo, int pageSize) {
        return rboardDAO.findByBoardIdWithPaging(boardId, pageNo, pageSize);
    }

    /**
     * 이메일별 댓글 조회
     */
    @Override
    public List<Replies> findByEmail(String email) {
        return rboardDAO.findByEmail(email);
    }

    /**
     * 이메일별 댓글 페이징 조회
     */
    @Override
    public List<Replies> findByEmailWithPaging(String email, int pageNo, int pageSize) {
        return rboardDAO.findByEmailWithPaging(email, pageNo, pageSize);
    }

    /**
     * 부모 댓글별 답글 조회
     */
    @Override
    public List<Replies> findByParentId(Long parentId) {
        return rboardDAO.findByParentId(parentId);
    }

    /**
     * 댓글 그룹 조회
     */
    @Override
    public List<Replies> findByRgroup(Long rgroup) {
        return rboardDAO.findByRgroup(rgroup);
    }

    /**
     * 댓글 내용 검색
     */
    @Override
    public List<Replies> findByRcontentContaining(String keyword) {
        return rboardDAO.findByRcontentContaining(keyword);
    }

    /**
     * 댓글 내용 검색 페이징 조회
     */
    @Override
    public List<Replies> findByRcontentContainingWithPaging(String keyword, int pageNo, int pageSize) {
        return rboardDAO.findByRcontentContainingWithPaging(keyword, pageNo, pageSize);
    }
    
    /**
     * 비즈니스 로직: 댓글 내용 검증
     */
    private void validateRcontent(String rcontent) {
        if (rcontent == null || rcontent.trim().isEmpty()) {
            throw new BusinessValidationException("댓글 내용은 필수입니다.");
        }
        if (rcontent.trim().length() < 2) {
            throw new BusinessValidationException("댓글 내용은 2자 이상이어야 합니다.");
        }
        if (rcontent.trim().length() > 500) {
            throw new BusinessValidationException("댓글 내용은 500자를 초과할 수 없습니다.");
        }
    }
    
    /**
     * 비즈니스 로직: 부모 댓글 존재 여부 확인
     */
    private void validateParentReply(Long parentId) {
        if (!rboardDAO.findById(parentId).isPresent()) {
            throw new BusinessValidationException("부모 댓글번호: " + parentId + "를 찾을 수 없습니다.");
        }
    }

    @Override
    @Transactional
    public boolean likeReply(Long replyId, String email) {
        try {
            // 댓글 존재 여부 확인
            if (!rboardDAO.findById(replyId).isPresent()) {
                throw new BusinessValidationException("존재하지 않는 댓글입니다.");
            }
            
            // 좋아요 수 증가
            int result = rboardDAO.incrementLikeCount(replyId);
            return result > 0;
        } catch (Exception e) {
            log.error("댓글 좋아요 처리 중 오류 발생: replyId={}, email={}", replyId, email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean dislikeReply(Long replyId, String email) {
        try {
            // 댓글 존재 여부 확인
            if (!rboardDAO.findById(replyId).isPresent()) {
                throw new BusinessValidationException("존재하지 않는 댓글입니다.");
            }
            
            // 싫어요 수 증가
            int result = rboardDAO.incrementDislikeCount(replyId);
            return result > 0;
        } catch (Exception e) {
            log.error("댓글 싫어요 처리 중 오류 발생: replyId={}, email={}", replyId, email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelReplyLike(Long replyId, String email) {
        try {
            // 댓글 존재 여부 확인
            if (!rboardDAO.findById(replyId).isPresent()) {
                throw new BusinessValidationException("존재하지 않는 댓글입니다.");
            }
            
            // 좋아요 수 감소
            int result = rboardDAO.decrementLikeCount(replyId);
            return result > 0;
        } catch (Exception e) {
            log.error("댓글 좋아요 취소 처리 중 오류 발생: replyId={}, email={}", replyId, email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelReplyDislike(Long replyId, String email) {
        try {
            // 댓글 존재 여부 확인
            if (!rboardDAO.findById(replyId).isPresent()) {
                throw new BusinessValidationException("존재하지 않는 댓글입니다.");
            }
            
            // 싫어요 수 감소
            int result = rboardDAO.decrementDislikeCount(replyId);
            return result > 0;
        } catch (Exception e) {
            log.error("댓글 싫어요 취소 처리 중 오류 발생: replyId={}, email={}", replyId, email, e);
            return false;
        }
    }
} 