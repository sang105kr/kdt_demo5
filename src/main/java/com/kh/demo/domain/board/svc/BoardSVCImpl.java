package com.kh.demo.domain.board.svc;

import com.kh.demo.domain.board.dao.BoardDAO;
import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * 게시판 서비스 구현체
 * 게시판 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardSVCImpl implements BoardSVC {
    private final BoardDAO boardDAO;

    @Override
    @Transactional
    public Long save(Boards boards) {
        // 비즈니스 로직: 제목 검증
        validateTitle(boards.getTitle());
        
        // 비즈니스 로직: 내용 길이 검증
        validateContent(boards.getBcontent());
        
        // 기본값 설정
        if (boards.getCdate() == null) {
            boards.setCdate(LocalDateTime.now());
        }
        if (boards.getUdate() == null) {
            boards.setUdate(LocalDateTime.now());
        }
        
        // 계층형 게시글 로직 처리
        if (boards.getPboardId() == null) {
            // 원글 등록
            saveOriginalPost(boards);
        } else {
            // 답글 등록
            saveReplyPost(boards);
        }
        
        return boards.getBoardId();
    }
    
    /**
     * 원글 등록 처리
     */
    private void saveOriginalPost(Boards boards) {
        // 원글은 board_id를 bgroup으로 사용
        Long boardId = boardDAO.save(boards);
        boards.setBoardId(boardId);
        boards.setBgroup(boardId);
        boards.setStep(0);
        boards.setBindent(0);
        boards.setPboardId(null);
        boards.setStatus("A");
        
        // bgroup 업데이트
        boardDAO.updateById(boardId, boards);
    }
    
    /**
     * 답글 등록 처리
     */
    private void saveReplyPost(Boards boards) {
        // 부모 게시글 조회
        Boards parentBoard = boardDAO.findById(boards.getPboardId())
                .orElseThrow(() -> new BusinessValidationException("부모 게시글을 찾을 수 없습니다."));
        
        // 계층 구조 설정
        boards.setBgroup(parentBoard.getBgroup());
        boards.setStep(parentBoard.getStep() + 1);
        boards.setBindent(parentBoard.getBindent() + 1);
        boards.setStatus("A");
        
        // 기존 답글들의 step 조정 (새 답글보다 step이 크거나 같은 것들을 +1)
        adjustExistingSteps(boards.getBgroup(), boards.getStep());
        
        // 답글 저장
        Long boardId = boardDAO.save(boards);
        boards.setBoardId(boardId);
    }
    
    /**
     * 기존 답글들의 step 조정
     */
    private void adjustExistingSteps(Long bgroup, int newStep) {
        // 같은 그룹에서 step이 새 답글의 step보다 크거나 같은 게시글들의 step을 +1 증가
        boardDAO.adjustExistingSteps(bgroup, newStep);
    }

    @Override
    @Transactional
    public int update(Boards boards) {
        // 비즈니스 로직: 게시글 존재 여부 확인
        if (!boardDAO.findById(boards.getBoardId()).isPresent()) {
            throw new BusinessValidationException("게시글번호: " + boards.getBoardId() + "를 찾을 수 없습니다.");
        }
        
        // 비즈니스 로직: 제목 검증
        validateTitle(boards.getTitle());
        
        // 비즈니스 로직: 내용 길이 검증
        validateContent(boards.getBcontent());
        
        boards.setUdate(LocalDateTime.now());
        return boardDAO.updateById(boards.getBoardId(), boards);
    }
    
    @Override
    @Transactional
    public int updateContent(Long boardId, Long bcategory, String title, String email, String nickname, String bcontent) {
        // 비즈니스 로직: 게시글 존재 여부 확인
        if (!boardDAO.findById(boardId).isPresent()) {
            throw new BusinessValidationException("게시글번호: " + boardId + "를 찾을 수 없습니다.");
        }
        
        // 비즈니스 로직: 제목 검증
        validateTitle(title);
        
        // 비즈니스 로직: 내용 길이 검증
        validateContent(bcontent);
        
        return boardDAO.updateContent(boardId, bcategory, title, email, nickname, bcontent);
    }

    @Override
    @Transactional
    public int deleteById(Long boardId) {
        // 비즈니스 로직: 게시글 존재 여부 확인
        if (!boardDAO.findById(boardId).isPresent()) {
            throw new BusinessValidationException("게시글번호: " + boardId + "를 찾을 수 없습니다.");
        }
        
        return boardDAO.deleteById(boardId);
    }

    @Override
    public Optional<Boards> findById(Long boardId) {
        return boardDAO.findById(boardId);
    }

    @Override
    public List<Boards> findAll() {
        return boardDAO.findAll();
    }

    @Override
    public List<Boards> findAllWithPaging(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findAllWithPaging(offset, pageSize);
    }

    @Override
    public List<Boards> findByBcategory(Long bcategory) {
        return boardDAO.findByBcategory(bcategory);
    }

    @Override
    public List<Boards> findByBcategoryWithPaging(Long bcategory, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findByBcategoryWithPaging(bcategory, offset, pageSize);
    }

    @Override
    public List<Boards> findByEmail(String email) {
        return boardDAO.findByEmail(email);
    }

    @Override
    public List<Boards> findByEmailWithPaging(String email, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findByEmailWithPaging(email, offset, pageSize);
    }

    @Override
    public List<Boards> findByTitleContaining(String keyword) {
        return boardDAO.findByTitleContaining(keyword);
    }

    @Override
    public List<Boards> findByTitleContainingWithPaging(String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findByTitleContainingWithPaging(keyword, offset, pageSize);
    }

    @Override
    public List<Boards> findByBgroup(Long bgroup) {
        return boardDAO.findByBgroup(bgroup);
    }

    @Override
    public int countAll() {
        return boardDAO.countAll();
    }

    @Override
    public int countByBcategory(Long bcategory) {
        return boardDAO.countByBcategory(bcategory);
    }

    @Override
    public int countByTitleContaining(String keyword) {
        return boardDAO.countByTitleContaining(keyword);
    }

    @Override
    public int countByBcategoryAndTitleContaining(Long bcategory, String keyword) {
        return boardDAO.countByBcategoryAndTitleContaining(bcategory, keyword);
    }

    @Override
    public List<Boards> findByBcategoryAndTitleContainingWithPaging(Long bcategory, String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return boardDAO.findByBcategoryAndTitleContainingWithPaging(bcategory, keyword, offset, pageSize);
    }

    /**
     * 게시글의 좋아요 수 조회
     * @param boardId 게시글 ID
     * @return 좋아요 수
     */
    public int getLikeCount(Long boardId) {
        Optional<Boards> board = boardDAO.findById(boardId);
        return board.map(Boards::getLikeCount).orElse(0);
    }

    /**
     * 게시글의 싫어요 수 조회
     * @param boardId 게시글 ID
     * @return 싫어요 수
     */
    public int getDislikeCount(Long boardId) {
        Optional<Boards> board = boardDAO.findById(boardId);
        return board.map(Boards::getDislikeCount).orElse(0);
    }

    @Override
    @Transactional
    public boolean likeBoard(Long boardId, String email) {
        try {
            // 게시글 존재 여부 확인
            if (!boardDAO.existsByBoardId(boardId)) {
                throw new BusinessValidationException("존재하지 않는 게시글입니다.");
            }
            
            // 좋아요 수 증가
            int result = boardDAO.incrementLikeCount(boardId);
            return result > 0;
        } catch (Exception e) {
            log.error("게시글 좋아요 처리 중 오류 발생: boardId={}, email={}", boardId, email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean dislikeBoard(Long boardId, String email) {
        try {
            // 게시글 존재 여부 확인
            if (!boardDAO.existsByBoardId(boardId)) {
                throw new BusinessValidationException("존재하지 않는 게시글입니다.");
            }
            
            // 싫어요 수 증가
            int result = boardDAO.incrementDislikeCount(boardId);
            return result > 0;
        } catch (Exception e) {
            log.error("게시글 싫어요 처리 중 오류 발생: boardId={}, email={}", boardId, email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelBoardLike(Long boardId, String email) {
        try {
            // 게시글 존재 여부 확인
            if (!boardDAO.existsByBoardId(boardId)) {
                throw new BusinessValidationException("존재하지 않는 게시글입니다.");
            }
            
            // 좋아요 수 감소
            int result = boardDAO.decrementLikeCount(boardId);
            return result > 0;
        } catch (Exception e) {
            log.error("게시글 좋아요 취소 처리 중 오류 발생: boardId={}, email={}", boardId, email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelBoardDislike(Long boardId, String email) {
        try {
            // 게시글 존재 여부 확인
            if (!boardDAO.existsByBoardId(boardId)) {
                throw new BusinessValidationException("존재하지 않는 게시글입니다.");
            }
            
            // 싫어요 수 감소
            int result = boardDAO.decrementDislikeCount(boardId);
            return result > 0;
        } catch (Exception e) {
            log.error("게시글 싫어요 취소 처리 중 오류 발생: boardId={}, email={}", boardId, email, e);
            return false;
        }
    }

    /**
     * 비즈니스 로직: 제목 검증
     */
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessValidationException("제목은 필수입니다.");
        }
        if (title.length() > 100) {
            throw new BusinessValidationException("제목은 100자를 초과할 수 없습니다.");
        }
    }

    /**
     * 비즈니스 로직: 내용 길이 검증
     */
    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessValidationException("내용은 필수입니다.");
        }
        if (content.length() > 4000) {
            throw new BusinessValidationException("내용은 4000자를 초과할 수 없습니다.");
        }
    }
} 