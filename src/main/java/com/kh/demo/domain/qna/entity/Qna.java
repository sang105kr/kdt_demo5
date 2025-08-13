package com.kh.demo.domain.qna.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Qna extends BaseEntity {
    private Long qnaId;
    private Long productId; // nullable - 일반 Q&A의 경우 null
    private Long memberId;
    private Long categoryId; // code_id (QNA_CATEGORY)
    private String categoryName; // 카테고리명 (뷰 전용 필드)
    private String statusName; // 상태명 (뷰 전용 필드)
    private String nickname; // 작성자 닉네임 (뷰 전용 필드)
    private String title;
    private String content;
    private Integer helpfulCount;
    private Integer unhelpfulCount;
    private Integer viewCount;
    private Integer commentCount;
    private Long statusId; // code_id (QNA_STATUS)
    private Long adminId; // 답변한 관리자 ID (nullable)
    private String answer; // 관리자 답변 (nullable)
    private LocalDateTime answeredAt; // 답변일시 (nullable)
}
