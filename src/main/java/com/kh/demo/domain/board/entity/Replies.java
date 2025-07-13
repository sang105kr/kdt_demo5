package com.kh.demo.domain.board.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Replies extends BaseEntity {
    private Long replyId;       // 댓글 번호
    private Long boardId;       // 원글 번호 (boards 테이블 참조)
    private String email;       // 작성자 이메일
    private String nickname;    // 작성자 별칭
    private String rcontent;    // 댓글 내용
    private Long parentId;      // 부모 댓글 번호 (대댓글용, NULL이면 최상위 댓글)
    private Long rgroup;        // 댓글 그룹 (같은 그룹 내에서 정렬)
    private Integer rstep;      // 댓글 단계 (대댓글 깊이)
    private Integer rindent;    // 들여쓰기 레벨
    private String status;      // 댓글 상태 (활성: 'A', 삭제: 'D', 숨김: 'H')
} 