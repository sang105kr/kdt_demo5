package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.entity.Replies;
import java.util.List;
import java.util.Optional;

/**
 * 댓글 데이터 접근 객체 인터페이스
 * 댓글의 CRUD 및 검색 기능을 제공합니다.
 * 
 * @author KDT
 * @since 2024
 */
public interface RboardDAO {
    
    /**
     * 댓글을 등록합니다.
     * 
     * @param reply 등록할 댓글 정보
     * @return 등록된 댓글의 ID
     */
    Long save(Replies reply);
    
    /**
     * 댓글을 수정합니다.
     * 
     * @param reply 수정할 댓글 정보
     * @return 수정된 행의 개수
     */
    int update(Replies reply);
    
    /**
     * 댓글을 삭제합니다.
     * 
     * @param replyId 삭제할 댓글 ID
     * @return 삭제된 행의 개수
     */
    int delete(Long replyId);
    
    /**
     * 댓글 ID로 단건 조회합니다.
     * 
     * @param replyId 조회할 댓글 ID
     * @return 댓글 정보 (Optional)
     */
    Optional<Replies> findById(Long replyId);
    
    /**
     * 게시글별 댓글 목록을 조회합니다.
     * 
     * @param boardId 게시글 ID
     * @return 댓글 목록
     */
    List<Replies> findByBoardId(Long boardId);
    
    /**
     * 게시글별 페이징 댓글 목록을 조회합니다.
     * 
     * @param boardId 게시글 ID
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 댓글 목록
     */
    List<Replies> findByBoardIdWithPaging(Long boardId, int offset, int limit);
    
    /**
     * 작성자별 댓글 목록을 조회합니다.
     * 
     * @param email 작성자 이메일
     * @return 댓글 목록
     */
    List<Replies> findByEmail(String email);
    
    /**
     * 작성자별 페이징 댓글 목록을 조회합니다.
     * 
     * @param email 작성자 이메일
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 댓글 목록
     */
    List<Replies> findByEmailWithPaging(String email, int offset, int limit);
    
    /**
     * 부모 댓글별 하위 댓글 목록을 조회합니다 (대댓글).
     * 
     * @param parentId 부모 댓글 ID
     * @return 하위 댓글 목록
     */
    List<Replies> findByParentId(Long parentId);
    
    /**
     * 댓글 그룹별 댓글 목록을 조회합니다 (계층형 구조).
     * 
     * @param rgroup 댓글 그룹 ID
     * @return 댓글 목록
     */
    List<Replies> findByRgroup(Long rgroup);
    
    /**
     * 댓글 내용에 키워드가 포함된 댓글을 검색합니다.
     * 
     * @param keyword 검색 키워드
     * @return 댓글 목록
     */
    List<Replies> findByRcontentContaining(String keyword);
    
    /**
     * 댓글 내용에 키워드가 포함된 댓글을 페이징하여 검색합니다.
     * 
     * @param keyword 검색 키워드
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 댓글 목록
     */
    List<Replies> findByRcontentContainingWithPaging(String keyword, int offset, int limit);
    
    /**
     * 댓글 존재 여부를 확인합니다.
     * 
     * @param replyId 댓글 ID
     * @return 존재 여부
     */
    boolean existsByReplyId(Long replyId);
    
    /**
     * 게시글별 댓글 수를 조회합니다.
     * 
     * @param boardId 게시글 ID
     * @return 댓글 개수
     */
    int countByBoardId(Long boardId);
    
    /**
     * 작성자별 댓글 수를 조회합니다.
     * 
     * @param email 작성자 이메일
     * @return 댓글 개수
     */
    int countByEmail(String email);
    
    /**
     * 부모 댓글별 하위 댓글 수를 조회합니다.
     * 
     * @param parentId 부모 댓글 ID
     * @return 하위 댓글 개수
     */
    int countByParentId(Long parentId);
    
    /**
     * 댓글 내용 검색 결과 수를 조회합니다.
     * 
     * @param keyword 검색 키워드
     * @return 검색 결과 개수
     */
    int countByRcontentContaining(String keyword);
} 