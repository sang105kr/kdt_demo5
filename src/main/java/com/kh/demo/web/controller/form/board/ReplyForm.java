package com.kh.demo.web.controller.form.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyForm {
    
    @NotNull(message = "게시글 번호는 필수입니다.")
    private Long boardId;
    
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String rcontent;
    
    private Long parentId;  // 대댓글인 경우 부모 댓글 ID
    
    private Long rgroup;    // 댓글 그룹
    
    private Integer rstep;  // 댓글 단계
    
    private Integer rindent; // 들여쓰기 레벨
} 