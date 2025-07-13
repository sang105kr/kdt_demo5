package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.entity.LikeDislike;
import com.kh.demo.domain.dto.LikeDislikeDTO;
import java.util.Optional;

/**
 * 호감/비호감 데이터 접근 객체 인터페이스
 * 호감/비호감의 CRUD 및 통계 기능을 제공합니다.
 * 
 * @author KDT
 * @since 2024
 */
public interface LikeDislikeDAO {
    
    /**
     * 호감/비호감을 등록합니다.
     * 
     * @param likeDislike 등록할 호감/비호감 정보
     * @return 등록된 호감/비호감의 ID
     */
    Long save(LikeDislike likeDislike);
    
    /**
     * 호감/비호감을 수정합니다.
     * 
     * @param likeDislike 수정할 호감/비호감 정보
     * @return 수정된 행의 개수
     */
    int update(LikeDislike likeDislike);
    
    /**
     * 호감/비호감을 삭제합니다.
     * 
     * @param likeDislikeId 삭제할 호감/비호감 ID
     * @return 삭제된 행의 개수
     */
    int delete(Long likeDislikeId);
    
    /**
     * 호감/비호감 ID로 단건 조회합니다.
     * 
     * @param likeDislikeId 조회할 호감/비호감 ID
     * @return 호감/비호감 정보 (Optional)
     */
    Optional<LikeDislike> findById(Long likeDislikeId);
    
    /**
     * 특정 대상에 대한 회원의 평가를 조회합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @return 호감/비호감 정보 (Optional)
     */
    Optional<LikeDislike> findByTargetAndMember(String targetType, Long targetId, Long memberId);
    
    /**
     * 특정 대상에 대한 호감/비호감 통계를 조회합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @param memberId 현재 회원 ID (null 가능)
     * @return 호감/비호감 통계 정보
     */
    LikeDislikeDTO getLikeDislikeStats(String targetType, Long targetId, Long memberId);
    
    /**
     * 특정 대상에 대한 호감/비호감을 삭제합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @param memberId 회원 ID
     * @return 삭제된 행의 개수
     */
    int deleteByTargetAndMember(String targetType, Long targetId, Long memberId);
    
    /**
     * 특정 대상에 대한 모든 호감/비호감을 삭제합니다.
     * 
     * @param targetType 대상 타입 (BOARD, REPLY)
     * @param targetId 대상 ID
     * @return 삭제된 행의 개수
     */
    int deleteByTarget(String targetType, Long targetId);
} 