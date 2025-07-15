package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.domain.shared.base.BaseDAO;
import java.util.List;
import java.util.Optional;

/**
 * 댓글 데이터 접근 객체 인터페이스
 */
public interface RboardDAO extends BaseDAO<Replies, Long> {
    
    /**
     * 게시글별 댓글 조회
     * @param boardId 게시글 ID
     * @return 댓글 목록
     */
    List<Replies> findByBoardId(Long boardId);
    
    /**
     * 게시글별 댓글 페이징 조회
     * @param boardId 게시글 ID
     * @param pageNo 페이지 번호
     * @param pageSize 페이지 크기
     * @return 댓글 목록
     */
    List<Replies> findByBoardIdWithPaging(Long boardId, int pageNo, int pageSize);
    
    /**
     * 이메일별 댓글 조회
     * @param email 이메일
     * @return 댓글 목록
     */
    List<Replies> findByEmail(String email);
    
    /**
     * 이메일별 댓글 페이징 조회
     * @param email 이메일
     * @param pageNo 페이지 번호
     * @param pageSize 페이지 크기
     * @return 댓글 목록
     */
    List<Replies> findByEmailWithPaging(String email, int pageNo, int pageSize);
    
    /**
     * 부모 댓글별 답글 조회
     * @param parentId 부모 댓글 ID
     * @return 댓글 목록
     */
    List<Replies> findByParentId(Long parentId);
    
    /**
     * 댓글 그룹 조회
     * @param rgroup 댓글 그룹 ID
     * @return 댓글 목록
     */
    List<Replies> findByRgroup(Long rgroup);
    
    /**
     * 댓글 내용 검색
     * @param keyword 검색 키워드
     * @return 댓글 목록
     */
    List<Replies> findByRcontentContaining(String keyword);
    
    /**
     * 댓글 내용 검색 페이징 조회
     * @param keyword 검색 키워드
     * @param pageNo 페이지 번호
     * @param pageSize 페이지 크기
     * @return 댓글 목록
     */
    List<Replies> findByRcontentContainingWithPaging(String keyword, int pageNo, int pageSize);
    
    /**
     * 댓글 좋아요 수 증가
     * 
     * @param replyId 댓글 ID
     * @return 수정된 행의 개수
     */
    int incrementLikeCount(Long replyId);
    
    /**
     * 댓글 좋아요 수 감소
     * 
     * @param replyId 댓글 ID
     * @return 수정된 행의 개수
     */
    int decrementLikeCount(Long replyId);
    
    /**
     * 댓글 비호감 수 증가
     * 
     * @param replyId 댓글 ID
     * @return 수정된 행의 개수
     */
    int incrementDislikeCount(Long replyId);
    
    /**
     * 댓글 비호감 수 감소
     * 
     * @param replyId 댓글 ID
     * @return 수정된 행의 개수
     */
    int decrementDislikeCount(Long replyId);
    
    /**
     * 댓글 그룹 업데이트
     * 
     * @param replyId 댓글 ID
     * @param rgroup 댓글 그룹 ID
     * @return 수정된 행의 개수
     */
    int updateRgroup(Long replyId, Long rgroup);
} 