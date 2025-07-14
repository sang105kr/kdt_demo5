package com.kh.demo.domain.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LikeDislike {
    private Long likeDislikeId;
    private String targetType; // "BOARD" or "REPLY"
    private Long targetId;
    private Long memberId;
    private String likeType; // "LIKE" or "DISLIKE"
    private LocalDateTime cdate;
    private LocalDateTime udate;
} 