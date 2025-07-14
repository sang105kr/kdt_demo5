package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dao.LikeDislikeDAO;
import com.kh.demo.domain.common.entity.LikeDislike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeDislikeSVCImpl implements LikeDislikeSVC {
    private final LikeDislikeDAO likeDislikeDAO;

    @Override
    @Transactional
    public Long like(String targetType, Long targetId, Long memberId) {
        Optional<LikeDislike> existing = likeDislikeDAO.findByTargetAndMember(targetType, targetId, memberId);
        if (existing.isPresent()) {
            LikeDislike ld = existing.get();
            if ("LIKE".equals(ld.getLikeType())) {
                return ld.getLikeDislikeId(); // 이미 좋아요
            } else {
                // DISLIKE → LIKE로 변경
                likeDislikeDAO.delete(ld.getLikeDislikeId());
            }
        }
        LikeDislike newLike = new LikeDislike();
        newLike.setTargetType(targetType);
        newLike.setTargetId(targetId);
        newLike.setMemberId(memberId);
        newLike.setLikeType("LIKE");
        newLike.setCdate(LocalDateTime.now());
        newLike.setUdate(LocalDateTime.now());
        return likeDislikeDAO.save(newLike);
    }

    @Override
    @Transactional
    public Long dislike(String targetType, Long targetId, Long memberId) {
        Optional<LikeDislike> existing = likeDislikeDAO.findByTargetAndMember(targetType, targetId, memberId);
        if (existing.isPresent()) {
            LikeDislike ld = existing.get();
            if ("DISLIKE".equals(ld.getLikeType())) {
                return ld.getLikeDislikeId(); // 이미 비호감
            } else {
                // LIKE → DISLIKE로 변경
                likeDislikeDAO.delete(ld.getLikeDislikeId());
            }
        }
        LikeDislike newDislike = new LikeDislike();
        newDislike.setTargetType(targetType);
        newDislike.setTargetId(targetId);
        newDislike.setMemberId(memberId);
        newDislike.setLikeType("DISLIKE");
        newDislike.setCdate(LocalDateTime.now());
        newDislike.setUdate(LocalDateTime.now());
        return likeDislikeDAO.save(newDislike);
    }

    @Override
    @Transactional
    public int cancel(String targetType, Long targetId, Long memberId) {
        Optional<LikeDislike> existing = likeDislikeDAO.findByTargetAndMember(targetType, targetId, memberId);
        if (existing.isPresent()) {
            return likeDislikeDAO.delete(existing.get().getLikeDislikeId());
        }
        return 0;
    }

    @Override
    public int countLikes(String targetType, Long targetId) {
        return likeDislikeDAO.countByTarget(targetType, targetId, "LIKE");
    }

    @Override
    public int countDislikes(String targetType, Long targetId) {
        return likeDislikeDAO.countByTarget(targetType, targetId, "DISLIKE");
    }

    @Override
    public Optional<LikeDislike> getStatus(String targetType, Long targetId, Long memberId) {
        return likeDislikeDAO.findByTargetAndMember(targetType, targetId, memberId);
    }
} 