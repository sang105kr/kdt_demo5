package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.entity.LikeDislike;
import java.util.Optional;

public interface LikeDislikeSVC {
    Long like(String targetType, Long targetId, Long memberId);
    Long dislike(String targetType, Long targetId, Long memberId);
    int cancel(String targetType, Long targetId, Long memberId);
    int countLikes(String targetType, Long targetId);
    int countDislikes(String targetType, Long targetId);
    Optional<LikeDislike> getStatus(String targetType, Long targetId, Long memberId);
} 