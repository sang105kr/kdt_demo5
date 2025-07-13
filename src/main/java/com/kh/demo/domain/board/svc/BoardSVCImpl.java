package com.kh.demo.domain.board.svc;

import com.kh.demo.domain.board.dao.BoardDAO;
import com.kh.demo.domain.entity.Boards;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 게시판 서비스 구현체
 * 게시글의 비즈니스 로직을 처리합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardSVCImpl implements BoardSVC {

    private final BoardDAO boardDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long save(Boards board) {
        log.info("게시글 등록 시작: {}", board.getTitle());
        Long boardId = boardDAO.save(board);
        log.info("게시글 등록 완료: boardId={}", boardId);
        return boardId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int update(Boards board) {
        log.info("게시글 수정 시작: boardId={}", board.getBoardId());
        int result = boardDAO.update(board);
        log.info("게시글 수정 완료: boardId={}, result={}", board.getBoardId(), result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int delete(Long boardId) {
        log.info("게시글 삭제 시작: boardId={}", boardId);
        int result = boardDAO.delete(boardId);
        log.info("게시글 삭제 완료: boardId={}, result={}", boardId, result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boards> findById(Long boardId) {
        log.debug("게시글 조회: boardId={}", boardId);
        Optional<Boards> board = boardDAO.findById(boardId);
        
        // 조회수 증가
        if (board.isPresent()) {
            boardDAO.incrementHit(boardId);
            log.debug("조회수 증가: boardId={}", boardId);
        }
        
        return board;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findAll() {
        log.debug("전체 게시글 목록 조회");
        return boardDAO.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findAllWithPaging(int pageNo, int pageSize) {
        log.debug("페이징 게시글 목록 조회: pageNo={}, pageSize={}", pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findAllWithPaging(offset, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBcategory(Long bcategory) {
        log.debug("카테고리별 게시글 목록 조회: bcategory={}", bcategory);
        return boardDAO.findByBcategory(bcategory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBcategoryWithPaging(Long bcategory, int pageNo, int pageSize) {
        log.debug("카테고리별 페이징 게시글 목록 조회: bcategory={}, pageNo={}, pageSize={}", bcategory, pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findByBcategoryWithPaging(bcategory, offset, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByEmail(String email) {
        log.debug("작성자별 게시글 목록 조회: email={}", email);
        return boardDAO.findByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByEmailWithPaging(String email, int pageNo, int pageSize) {
        log.debug("작성자별 페이징 게시글 목록 조회: email={}, pageNo={}, pageSize={}", email, pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findByEmailWithPaging(email, offset, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByTitleContaining(String keyword) {
        log.debug("제목 검색: keyword={}", keyword);
        return boardDAO.findByTitleContaining(keyword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByTitleContainingWithPaging(String keyword, int pageNo, int pageSize) {
        log.debug("제목 검색 페이징: keyword={}, pageNo={}, pageSize={}", keyword, pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findByTitleContainingWithPaging(keyword, offset, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int incrementHit(Long boardId) {
        log.debug("조회수 증가: boardId={}", boardId);
        return boardDAO.incrementHit(boardId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBgroup(Long bgroup) {
        log.debug("답글 그룹별 게시글 목록 조회: bgroup={}", bgroup);
        return boardDAO.findByBgroup(bgroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByBoardId(Long boardId) {
        log.debug("게시글 존재 여부 확인: boardId={}", boardId);
        return boardDAO.existsByBoardId(boardId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAll() {
        log.debug("전체 게시글 수 조회");
        return boardDAO.countAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByBcategory(Long bcategory) {
        log.debug("카테고리별 게시글 수 조회: bcategory={}", bcategory);
        return boardDAO.countByBcategory(bcategory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByEmail(String email) {
        log.debug("작성자별 게시글 수 조회: email={}", email);
        return boardDAO.countByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByTitleContaining(String keyword) {
        log.debug("제목 검색 결과 수 조회: keyword={}", keyword);
        return boardDAO.countByTitleContaining(keyword);
    }
} 