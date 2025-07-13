package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.LikeDislike;
import com.kh.demo.domain.common.dto.LikeDislikeDTO;
import com.kh.demo.domain.shared.base.BaseDAO;
import java.util.List;
import java.util.Optional;

/**
 * 좋아요/싫어요 데이터 접근 객체 인터페이스
 */
public interface LikeDislikeDAO extends BaseDAO<LikeDislike, Long> {
    
    /**
     * 대상과 회원으로 조회
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @return 좋아요/싫어요 정보
     */
    Optional<LikeDislike> findByTargetAndMember(String targetType, Long targetId, Long memberId);
    
    /**
     * 좋아요/싫어요 통계 조회
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @return 통계 정보
     */
    LikeDislikeDTO getLikeDislikeStats(String targetType, Long targetId, Long memberId);
    
    /**
     * 대상과 회원으로 삭제
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @return 삭제 건수
     */
    int deleteByTargetAndMember(String targetType, Long targetId, Long memberId);
    
    /**
     * 좋아요/싫어요 수정
     * @param likeDislike 수정할 좋아요/싫어요 정보
     * @return 수정 건수
     */
    int update(LikeDislike likeDislike);
} 