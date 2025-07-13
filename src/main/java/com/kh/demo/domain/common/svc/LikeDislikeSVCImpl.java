package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dao.LikeDislikeDAO;
import com.kh.demo.domain.dto.LikeDislikeDTO;
import com.kh.demo.domain.entity.LikeDislike;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 호감/비호감 서비스 구현체
 * 호감/비호감의 비즈니스 로직을 처리합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeDislikeSVCImpl implements LikeDislikeSVC {

    private final LikeDislikeDAO likeDislikeDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean evaluate(String targetType, Long targetId, Long memberId, String likeType) {
        log.info("호감/비호감 평가 시작: targetType={}, targetId={}, memberId={}, likeType={}", 
                targetType, targetId, memberId, likeType);
        
        try {
            // 기존 평가 확인
            Optional<LikeDislike> existingLike = likeDislikeDAO.findByTargetAndMember(targetType, targetId, memberId);
            
            if (existingLike.isPresent()) {
                // 기존 평가가 있는 경우 수정
                LikeDislike likeDislike = existingLike.get();
                likeDislike.setLikeType(likeType);
                int result = likeDislikeDAO.update(likeDislike);
                log.info("호감/비호감 수정 완료: result={}", result);
                return result > 0;
            } else {
                // 새로운 평가 등록
                LikeDislike likeDislike = new LikeDislike();
                likeDislike.setTargetType(targetType);
                likeDislike.setTargetId(targetId);
                likeDislike.setMemberId(memberId);
                likeDislike.setLikeType(likeType);
                
                Long likeDislikeId = likeDislikeDAO.save(likeDislike);
                log.info("호감/비호감 등록 완료: likeDislikeId={}", likeDislikeId);
                return likeDislikeId != null;
            }
        } catch (Exception e) {
            log.error("호감/비호감 평가 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean cancel(String targetType, Long targetId, Long memberId) {
        log.info("호감/비호감 취소 시작: targetType={}, targetId={}, memberId={}", targetType, targetId, memberId);
        
        try {
            int result = likeDislikeDAO.deleteByTargetAndMember(targetType, targetId, memberId);
            log.info("호감/비호감 취소 완료: result={}", result);
            return result > 0;
        } catch (Exception e) {
            log.error("호감/비호감 취소 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LikeDislikeDTO getStats(String targetType, Long targetId, Long memberId) {
        log.debug("호감/비호감 통계 조회: targetType={}, targetId={}, memberId={}", targetType, targetId, memberId);
        
        try {
            LikeDislikeDTO stats = likeDislikeDAO.getLikeDislikeStats(targetType, targetId, memberId);
            log.debug("호감/비호감 통계 조회 완료: likeCount={}, dislikeCount={}", 
                    stats.getLikeCount(), stats.getDislikeCount());
            return stats;
        } catch (Exception e) {
            log.error("호감/비호감 통계 조회 실패: {}", e.getMessage(), e);
            // 기본값 반환
            LikeDislikeDTO defaultStats = new LikeDislikeDTO();
            defaultStats.setTargetType(targetType);
            defaultStats.setTargetId(targetId);
            defaultStats.setLikeCount(0L);
            defaultStats.setDislikeCount(0L);
            defaultStats.setUserLikeType(null);
            return defaultStats;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserLikeType(String targetType, Long targetId, Long memberId) {
        log.debug("사용자 평가 타입 조회: targetType={}, targetId={}, memberId={}", targetType, targetId, memberId);
        
        try {
            Optional<LikeDislike> likeDislike = likeDislikeDAO.findByTargetAndMember(targetType, targetId, memberId);
            String likeType = likeDislike.map(LikeDislike::getLikeType).orElse(null);
            log.debug("사용자 평가 타입 조회 완료: likeType={}", likeType);
            return likeType;
        } catch (Exception e) {
            log.error("사용자 평가 타입 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }
} 