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
        
        return boardDAO.save(boards);
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
    private void validateContent(java.sql.Clob content) {
        if (content == null) {
            throw new BusinessValidationException("내용은 필수입니다.");
        }
        // Clob의 경우 실제 길이 검증은 데이터베이스에서 처리
    }
} 