package com.kh.demo.domain.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 좋아요/싫어요 데이터 전송 객체
 */
@Data
public class LikeDislikeDTO {
    private Long likeDislikeId;
    private String targetType;    // 'BOARD' 또는 'REPLY'
    private Long targetId;        // 게시글 ID 또는 댓글 ID
    private Long memberId;        // 평가한 회원 ID
    private String likeType;      // 'LIKE' 또는 'DISLIKE'
    private LocalDateTime cdate;
    private LocalDateTime udate;
} 