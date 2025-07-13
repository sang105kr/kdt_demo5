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

        return rboardDAO.save(reply);
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
} 