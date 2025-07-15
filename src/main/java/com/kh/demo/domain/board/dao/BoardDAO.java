package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.shared.base.BaseDAO;
import java.util.List;
import java.util.Optional;

/**
 * 게시판 데이터 접근 객체 인터페이스
 * 게시글의 CRUD 및 검색 기능을 제공합니다.
 * 
 * @author KDT
 * @since 2024
 */
public interface BoardDAO extends BaseDAO<Boards, Long> {
    
    /**
     * 페이징을 적용하여 게시글 목록을 조회합니다.
     * 
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 게시글 목록
     */
    List<Boards> findAllWithPaging(int offset, int limit);
    
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
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 게시글 목록
     */
    List<Boards> findByBcategoryWithPaging(Long bcategory, int offset, int limit);
    
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
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 게시글 목록
     */
    List<Boards> findByEmailWithPaging(String email, int offset, int limit);
    
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
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 게시글 목록
     */
    List<Boards> findByTitleContainingWithPaging(String keyword, int offset, int limit);
    
    /**
     * 게시글 조회수를 증가시킵니다.
     * 
     * @param boardId 게시글 ID
     * @return 업데이트된 행의 개수
     */
    int incrementHit(Long boardId);
    
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
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return 게시글 목록
     */
    List<Boards> findByBcategoryAndTitleContainingWithPaging(Long bcategory, String keyword, int offset, int limit);
    
    /**
     * 게시글 수정 (BaseDAO의 updateById와 별도로 제공)
     * 
     * @param board 수정할 게시글 정보
     * @return 수정된 행의 개수
     */
    int update(Boards board);
    
    /**
     * 게시글 내용 수정 (사용자가 수정 가능한 필드만 업데이트)
     * 
     * @param boardId 게시글 ID
     * @param bcategory 카테고리
     * @param title 제목
     * @param email 이메일
     * @param nickname 닉네임
     * @param bcontent 내용
     * @return 수정된 행의 개수
     */
    int updateContent(Long boardId, Long bcategory, String title, String email, String nickname, String bcontent);
    
    /**
     * 게시글 삭제 (BaseDAO의 deleteById와 별도로 제공)
     * 
     * @param boardId 게시글 ID
     * @return 삭제된 행의 개수
     */
    int delete(Long boardId);
    
    /**
     * 전체 게시글 수 조회 (BaseDAO의 getTotalCount와 별도로 제공)
     * 
     * @return 전체 게시글 수
     */
    int countAll();
    
    /**
     * 기존 답글들의 step 조정 (계층형 구조에서 답글 삽입 시 사용)
     * 
     * @param bgroup 답글 그룹 ID
     * @param newStep 새로 삽입될 답글의 step
     * @return 수정된 행의 개수
     */
    int adjustExistingSteps(Long bgroup, int newStep);
    
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