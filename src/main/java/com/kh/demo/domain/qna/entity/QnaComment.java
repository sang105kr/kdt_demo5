package com.kh.demo.domain.qna.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QnaComment extends BaseEntity {
    private Long commentId;
    private Long qnaId;
    private Long memberId;
    private Long adminId; // 관리자 댓글인 경우
    private String content;
    private Long commentTypeId; // code_id (QNA_COMMENT_TYPE)
    private Integer helpfulCount;
    private Integer unhelpfulCount;
    private Long statusId; // code_id
    private String memberNickname; // 회원 닉네임 (JOIN으로 가져온 데이터)
    private String adminNickname; // 관리자 닉네임 (JOIN으로 가져온 데이터)
}
