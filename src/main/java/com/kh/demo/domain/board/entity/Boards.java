package com.kh.demo.domain.board.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.sql.Clob;

@Data
@EqualsAndHashCode(callSuper = true)
public class Boards extends BaseEntity {
    private Long boardId;           // 게시글 번호
    private Long bcategory;         // 분류카테고리 (code_id 참조)
    private String title;           // 제목
    private String email;           // email
    private String nickname;        // 별칭
    private Integer hit;            // 조회수
    private String bcontent;        // 본문
    private Long pboardId;          // 부모 게시글번호
    private Long bgroup;            // 답글그룹
    private Integer step;           // 답글단계
    private Integer bindent;        // 답글들여쓰기
    private String status;          // 답글상태
} 