package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dto.LikeDislikeDTO;

/**
 * 호감/비호감 서비스 인터페이스
 * 호감/비호감의 비즈니스 로직을 처리합니다.
 * 
 * @author KDT
 * @since 2024
 */
public interface LikeDislikeSVC {
    
    /**
     * 호감/비호감을 평가합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @param likeType 평가 타입 (LIKE, DISLIKE)
     * @return 평가 결과 (성공 여부)
     */
    boolean evaluate(String targetType, Long targetId, Long memberId, String likeType);
    
    /**
     * 호감/비호감을 취소합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @return 취소 결과 (성공 여부)
     */
    boolean cancel(String targetType, Long targetId, Long memberId);
    
    /**
     * 특정 대상에 대한 호감/비호감 통계를 조회합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @param memberId 현재 회원 ID (null 가능)
     * @return 호감/비호감 통계 정보
     */
    LikeDislikeDTO getStats(String targetType, Long targetId, Long memberId);
    
    /**
     * 회원이 특정 대상에 평가한 타입을 조회합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @return 평가 타입 (LIKE, DISLIKE, null)
     */
    String getUserLikeType(String targetType, Long targetId, Long memberId);
} 