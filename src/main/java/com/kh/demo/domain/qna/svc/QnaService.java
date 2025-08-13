package com.kh.demo.domain.qna.svc;

import com.kh.demo.domain.qna.entity.Qna;
import com.kh.demo.domain.qna.entity.QnaComment;
import com.kh.demo.domain.common.base.BaseSVC;

import java.util.List;
import java.util.Optional;

public interface QnaService extends BaseSVC<Qna, Long> {
    
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
    
    // Q&A 상세 조회 (조회수 증가 포함)
    Optional<Qna> findByIdWithViewCount(Long qnaId);
    
    // 조회수 증가
    int incrementViewCount(Long qnaId);
    
    // 도움됨 수 증가
    int incrementHelpfulCount(Long qnaId);
    
    // 도움안됨 수 증가
    int incrementUnhelpfulCount(Long qnaId);
    
    // Q&A 상태 업데이트
    int updateStatus(Long qnaId, Long statusId);
    
    // 관리자 답변 업데이트
    int updateAnswer(Long qnaId, String answer, Long adminId);
    
    // Q&A 작성 (권한 검증 포함)
    Qna createQna(Qna qna, Long memberId);
    
    // Q&A 수정 (작성자 본인만)
    int updateQna(Long qnaId, Qna qna, Long memberId);
    
    // Q&A 삭제 (작성자 본인 또는 관리자만)
    int deleteQna(Long qnaId, Long memberId, boolean isAdmin);
    
    // Q&A 댓글 목록 조회
    List<QnaComment> findCommentsByQnaId(Long qnaId);
    
    // Q&A 댓글 개수 조회
    int countCommentsByQnaId(Long qnaId);
    
    // 전체 Q&A 목록 조회 (페이징) - 관리자용
    List<Qna> findAllWithPaging(int offset, int limit);
    
    // 전체 Q&A 개수 조회 - 관리자용
    int countAll();
    
    // 관리자용 Q&A 목록 조회 (필터링 포함)
    List<Qna> findAll(String category, String keyword, String status, int page, int size);
    
    // 관리자용 Q&A 개수 조회 (필터링 포함)
    int countAll(String category, String keyword, String status);
}
