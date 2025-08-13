package com.kh.demo.domain.qna.dao;

import com.kh.demo.domain.qna.entity.Qna;
import com.kh.demo.domain.qna.entity.QnaComment;
import com.kh.demo.domain.common.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface QnaDAO extends BaseDAO<Qna, Long> {
    
    // 상품별 Q&A 목록 조회 (페이징)
    List<Qna> findByProductId(Long productId, int offset, int limit);
    
    // 상품별 Q&A 개수 조회
    int countByProductId(Long productId);
    
    // 회원별 Q&A 목록 조회 (활성 상태만)
    List<Qna> findByMemberId(Long memberId);
    
    // 회원별 Q&A 목록 조회 (모든 상태)
    List<Qna> findByMemberIdAllStatus(Long memberId);
    
    // 카테고리별 Q&A 목록 조회 (페이징)
    List<Qna> findByCategoryId(Long categoryId, int offset, int limit);
    
    // 카테고리별 Q&A 개수 조회
    int countByCategoryId(Long categoryId);
    
    // 상태별 Q&A 목록 조회 (페이징)
    List<Qna> findByStatusId(Long statusId, int offset, int limit);
    
    // 상태별 Q&A 개수 조회
    int countByStatusId(Long statusId);
    
    // 키워드 검색 Q&A 목록 조회 (페이징)
    List<Qna> findByKeyword(String keyword, int offset, int limit);
    
    // 키워드 검색 Q&A 개수 조회
    int countByKeyword(String keyword);
    
    // 조회수 증가
    int incrementViewCount(Long qnaId);
    
    // 도움됨 수 증가
    int incrementHelpfulCount(Long qnaId);
    
    // 도움안됨 수 증가
    int incrementUnhelpfulCount(Long qnaId);
    
    // 댓글 수 증가
    int incrementCommentCount(Long qnaId);
    
    // 댓글 수 감소
    int decrementCommentCount(Long qnaId);
    
    // Q&A 상태 업데이트
    int updateStatus(Long qnaId, Long statusId);
    
    // 관리자 답변 업데이트
    int updateAnswer(Long qnaId, String answer, Long adminId);
    
    // Q&A 댓글 목록 조회
    List<QnaComment> findCommentsByQnaId(Long qnaId);
    
    // Q&A 댓글 개수 조회
    int countCommentsByQnaId(Long qnaId);
    
    // 전체 Q&A 목록 조회 (페이징) - 관리자용
    List<Qna> findAllWithPaging(int offset, int limit);
    
    // 전체 Q&A 개수 조회 - 관리자용
    int countAll();
}
