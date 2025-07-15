package com.kh.demo.domain.board.svc;

import com.kh.demo.domain.board.entity.Replies;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 서비스 인터페이스
 */
public interface RboardSVC {
    
    /**
     * 댓글 등록
     * @param reply 댓글 정보
     * @return 등록된 댓글 ID
     */
    Long save(Replies reply);
    
    /**
     * 댓글 수정
     * @param reply 수정할 댓글 정보
     * @return 수정된 행 수
     */
    int update(Replies reply);
    
    /**
     * 댓글 삭제
     * @param replyId 댓글 ID
     * @return 삭제된 행 수
     */
    int deleteById(Long replyId);
    
    /**
     * 댓글 ID로 조회
     * @param replyId 댓글 ID
     * @return 댓글 정보
     */
    Optional<Replies> findById(Long replyId);
    
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
     * 댓글 좋아요
     * @param replyId 댓글 ID
     * @param email 사용자 이메일
     * @return 성공 여부
     */
    boolean likeReply(Long replyId, String email);
    
    /**
     * 댓글 싫어요
     * @param replyId 댓글 ID
     * @param email 사용자 이메일
     * @return 성공 여부
     */
    boolean dislikeReply(Long replyId, String email);
    
    /**
     * 댓글 좋아요 취소
     * @param replyId 댓글 ID
     * @param email 사용자 이메일
     * @return 성공 여부
     */
    boolean cancelReplyLike(Long replyId, String email);
    
    /**
     * 댓글 싫어요 취소
     * @param replyId 댓글 ID
     * @param email 사용자 이메일
     * @return 성공 여부
     */
    boolean cancelReplyDislike(Long replyId, String email);
} 