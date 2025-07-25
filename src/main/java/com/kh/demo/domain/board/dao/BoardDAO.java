package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.common.base.BaseDAO;
import java.util.List;

/**
 * 게시판 데이터 접근 객체 인터페이스
 * 게시글의 CRUD 및 검색 기능을 제공합니다.
 * 
 * @author KDT
 * @since 2024
 */
public interface BoardDAO extends BaseDAO<Boards, Long> {
    
    /**
     * 카테고리별 게시글 목록 조회
     * @param bcategory 카테고리 ID
     * @return 게시글 목록
     */
    List<Boards> findByBcategory(Long bcategory);
    
    /**
     * 카테고리별 페이징 게시글 목록을 조회합니다.
     * 
     * @param bcategory 게시판 카테고리 ID
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 행 수
     * @return 게시글 목록
     */
    List<Boards> findByBcategoryWithPaging(Long bcategory, int pageNo, int pageSize);
    
    /**
     * 작성자별 게시글 목록을 조회합니다.
     * 
     * @param email 작성자 이메일
     * @return 게시글 목록
     */
    List<Boards> findByEmail(String email);
    
    /**
     * 작성자별 페이징 게시글 목록을 조회합니다.
     * 
     * @param email 작성자 이메일
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 행 수
     * @return 게시글 목록
     */
    List<Boards> findByEmailWithPaging(String email, int pageNo, int pageSize);
    
    /**
     * 제목에 키워드가 포함된 게시글을 검색합니다.
     * 
     * @param keyword 검색 키워드
     * @return 게시글 목록
     */
    List<Boards> findByTitleContaining(String keyword);
    
    /**
     * 제목에 키워드가 포함된 게시글을 페이징하여 검색합니다.
     * 
     * @param keyword 검색 키워드
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 행 수
     * @return 게시글 목록
     */
    List<Boards> findByTitleContainingWithPaging(String keyword, int pageNo, int pageSize);
    
    /**
     * 게시글 내용 수정 (사용자가 수정 가능한 필드만 업데이트)
     * @param boardId 게시글 ID
     * @param bcategory 카테고리
     * @param title 제목
     * @param email 이메일
     * @param nickname 닉네임
     * @param bcontent 내용
     * @return 수정된 행 수
     */
    int updateContent(Long boardId, Long bcategory, String title, String email, String nickname, String bcontent);
    
    /**
     * 기존 답글들의 step 조정
     * @param bgroup 게시글 그룹
     * @param newStep 새로운 step 값
     * @return 수정된 행 수
     */
    int adjustExistingSteps(Long bgroup, int newStep);
    
    /**
     * 답글 그룹별 게시글 목록을 조회합니다 (계층형 구조).
     * 
     * @param bgroup 답글 그룹 ID
     * @return 게시글 목록
     */
    List<Boards> findByBgroup(Long bgroup);
    
    /**
     * 게시글 존재 여부를 확인합니다.
     * 
     * @param boardId 게시글 ID
     * @return 존재 여부
     */
    boolean existsByBoardId(Long boardId);
    
    /**
     * 카테고리별 게시글 수를 조회합니다.
     * 
     * @param bcategory 게시판 카테고리 ID
     * @return 게시글 개수
     */
    int countByBcategory(Long bcategory);
    
    /**
     * 작성자별 게시글 수를 조회합니다.
     * 
     * @param email 작성자 이메일
     * @return 게시글 개수
     */
    int countByEmail(String email);
    
    /**
     * 제목 검색 결과 수를 조회합니다.
     * 
     * @param keyword 검색 키워드
     * @return 검색 결과 개수
     */
    int countByTitleContaining(String keyword);
    
    /**
     * 카테고리별 제목 검색 결과 수를 조회합니다.
     * 
     * @param bcategory 카테고리 ID
     * @param keyword 검색 키워드
     * @return 검색 결과 개수
     */
    int countByBcategoryAndTitleContaining(Long bcategory, String keyword);
    
    /**
     * 카테고리별 제목 검색 페이징 조회
     * @param bcategory 카테고리 ID
     * @param keyword 검색 키워드
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 행 수
     * @return 게시글 목록
     */
    List<Boards> findByBcategoryAndTitleContainingWithPaging(Long bcategory, String keyword, int pageNo, int pageSize);
    
    /**
     * 게시글 좋아요 수 증가
     * 
     * @param boardId 게시글 ID
     * @return 수정된 행의 개수
     */
    int incrementLikeCount(Long boardId);
    
    /**
     * 게시글 좋아요 수 감소
     * 
     * @param boardId 게시글 ID
     * @return 수정된 행의 개수
     */
    int decrementLikeCount(Long boardId);
    
    /**
     * 게시글 비호감 수 증가
     * 
     * @param boardId 게시글 ID
     * @return 수정된 행의 개수
     */
    int incrementDislikeCount(Long boardId);
    
    /**
     * 게시글 비호감 수 감소
     * 
     * @param boardId 게시글 ID
     * @return 수정된 행의 개수
     */
    int decrementDislikeCount(Long boardId);
} 