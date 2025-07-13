package com.kh.demo.domain.common.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LikeDislike extends BaseEntity {
    private Long likeDislikeId;   // 좋아요/싫어요 ID
    private String targetType;    // 'BOARD' 또는 'REPLY'
    private Long targetId;        // 게시글 ID 또는 댓글 ID
    private Long memberId;        // 평가한 회원 ID
    private String likeType;      // 'LIKE' 또는 'DISLIKE'
} 