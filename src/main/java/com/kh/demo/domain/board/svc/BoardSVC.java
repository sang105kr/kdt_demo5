package com.kh.demo.domain.board.svc;

import com.kh.demo.domain.board.entity.Boards;

import java.util.List;
import java.util.Optional;

/**
 * 게시판 서비스 인터페이스
 */
public interface BoardSVC {
    
    /**
     * 게시글 등록
     * @param board 게시글 정보
     * @return 등록된 게시글 ID
     */
    Long save(Boards board);
    
    /**
     * 게시글 수정
     * @param board 수정할 게시글 정보
     * @return 수정된 행 수
     */
    int update(Boards board);
    
    /**
     * 게시글 삭제
     * @param boardId 게시글 ID
     * @return 삭제된 행 수
     */
    int deleteById(Long boardId);
    
    /**
     * 게시글 ID로 조회
     * @param boardId 게시글 ID
     * @return 게시글 정보
     */
    Optional<Boards> findById(Long boardId);
    
    /**
     * 모든 게시글 조회
     * @return 게시글 목록
     */
    List<Boards> findAll();
    
    /**
     * 게시글 목록 페이징 조회
     * @param pageNo 페이지 번호
     * @param pageSize 페이지 크기
     * @return 게시글 목록
     */
    List<Boards> findAllWithPaging(int pageNo, int pageSize);
    
    /**
     * 카테고리별 게시글 조회
     * @param bcategory 카테고리 ID
     * @return 게시글 목록
     */
    List<Boards> findByBcategory(Long bcategory);
    
    /**
     * 카테고리별 게시글 페이징 조회
     * @param bcategory 카테고리 ID
     * @param pageNo 페이지 번호
     * @param pageSize 페이지 크기
     * @return 게시글 목록
     */
    List<Boards> findByBcategoryWithPaging(Long bcategory, int pageNo, int pageSize);
    
    /**
     * 이메일별 게시글 조회
     * @param email 이메일
     * @return 게시글 목록
     */
    List<Boards> findByEmail(String email);
    
    /**
     * 이메일별 게시글 페이징 조회
     * @param email 이메일
     * @param pageNo 페이지 번호
     * @param pageSize 페이지 크기
     * @return 게시글 목록
     */
    List<Boards> findByEmailWithPaging(String email, int pageNo, int pageSize);
    
    /**
     * 제목 검색
     * @param keyword 검색 키워드
     * @return 게시글 목록
     */
    List<Boards> findByTitleContaining(String keyword);
    
    /**
     * 제목 검색 페이징 조회
     * @param keyword 검색 키워드
     * @param pageNo 페이지 번호
     * @param pageSize 페이지 크기
     * @return 게시글 목록
     */
    List<Boards> findByTitleContainingWithPaging(String keyword, int pageNo, int pageSize);
    
    /**
     * 게시글 그룹 조회
     * @param bgroup 게시글 그룹 ID
     * @return 게시글 목록
     */
    List<Boards> findByBgroup(Long bgroup);
} 