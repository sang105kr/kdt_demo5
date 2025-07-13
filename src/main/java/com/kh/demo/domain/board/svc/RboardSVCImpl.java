package com.kh.demo.domain.board.svc;

import com.kh.demo.domain.board.dao.RboardDAO;
import com.kh.demo.domain.entity.Replies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 서비스 구현체
 * 댓글의 비즈니스 로직을 처리합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RboardSVCImpl implements RboardSVC {

    private final RboardDAO rboardDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long save(Replies reply) {
        log.info("댓글 등록 시작: boardId={}", reply.getBoardId());
        Long replyId = rboardDAO.save(reply);
        log.info("댓글 등록 완료: replyId={}", replyId);
        return replyId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int update(Replies reply) {
        log.info("댓글 수정 시작: replyId={}", reply.getReplyId());
        int result = rboardDAO.update(reply);
        log.info("댓글 수정 완료: replyId={}, result={}", reply.getReplyId(), result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int delete(Long replyId) {
        log.info("댓글 삭제 시작: replyId={}", replyId);
        int result = rboardDAO.delete(replyId);
        log.info("댓글 삭제 완료: replyId={}, result={}", replyId, result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Replies> findById(Long replyId) {
        log.debug("댓글 조회: replyId={}", replyId);
        return rboardDAO.findById(replyId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByBoardId(Long boardId) {
        log.debug("게시글별 댓글 목록 조회: boardId={}", boardId);
        return rboardDAO.findByBoardId(boardId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByBoardIdWithPaging(Long boardId, int pageNo, int pageSize) {
        log.debug("게시글별 페이징 댓글 목록 조회: boardId={}, pageNo={}, pageSize={}", boardId, pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return rboardDAO.findByBoardIdWithPaging(boardId, offset, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByEmail(String email) {
        log.debug("작성자별 댓글 목록 조회: email={}", email);
        return rboardDAO.findByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByEmailWithPaging(String email, int pageNo, int pageSize) {
        log.debug("작성자별 페이징 댓글 목록 조회: email={}, pageNo={}, pageSize={}", email, pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return rboardDAO.findByEmailWithPaging(email, offset, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByParentId(Long parentId) {
        log.debug("부모 댓글별 답글 목록 조회: parentId={}", parentId);
        return rboardDAO.findByParentId(parentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByRgroup(Long rgroup) {
        log.debug("댓글 그룹별 댓글 목록 조회: rgroup={}", rgroup);
        return rboardDAO.findByRgroup(rgroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByRcontentContaining(String keyword) {
        log.debug("댓글 내용 검색: keyword={}", keyword);
        return rboardDAO.findByRcontentContaining(keyword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByRcontentContainingWithPaging(String keyword, int pageNo, int pageSize) {
        log.debug("댓글 내용 검색 페이징: keyword={}, pageNo={}, pageSize={}", keyword, pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return rboardDAO.findByRcontentContainingWithPaging(keyword, offset, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByReplyId(Long replyId) {
        log.debug("댓글 존재 여부 확인: replyId={}", replyId);
        return rboardDAO.existsByReplyId(replyId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByBoardId(Long boardId) {
        log.debug("게시글별 댓글 수 조회: boardId={}", boardId);
        return rboardDAO.countByBoardId(boardId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByEmail(String email) {
        log.debug("작성자별 댓글 수 조회: email={}", email);
        return rboardDAO.countByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByParentId(Long parentId) {
        log.debug("부모 댓글별 답글 수 조회: parentId={}", parentId);
        return rboardDAO.countByParentId(parentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByRcontentContaining(String keyword) {
        log.debug("댓글 내용 검색 결과 수 조회: keyword={}", keyword);
        return rboardDAO.countByRcontentContaining(keyword);
    }
} 