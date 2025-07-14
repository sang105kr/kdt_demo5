package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.LikeDislike;
import java.util.Optional;
import java.util.List;

public interface LikeDislikeDAO {
    Long save(LikeDislike likeDislike);
    int delete(Long likeDislikeId);
    Optional<LikeDislike> findById(Long likeDislikeId);
    int countByTarget(String targetType, Long targetId, String likeType);
    Optional<LikeDislike> findByTargetAndMember(String targetType, Long targetId, Long memberId);
    List<LikeDislike> findAllByTarget(String targetType, Long targetId);
} 