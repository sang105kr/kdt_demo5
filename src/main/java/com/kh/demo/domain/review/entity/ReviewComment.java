package com.kh.demo.domain.review.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewComment extends BaseEntity {
    private Long commentId;
    private Long reviewId;
    private Long memberId;
    private Long parentId;
    private String content;
    private Integer helpfulCount;
    private Integer unhelpfulCount; // 도움안됨 수 추가
    private Integer reportCount;
    private Long statusId; // code_id
    private String memberNickname; // 회원 닉네임 (JOIN으로 가져온 데이터)
} 