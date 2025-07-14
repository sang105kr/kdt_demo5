package com.kh.demo.web.restcontroller.board.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyCreateRequest {
    @NotNull
    private Long boardId;
    private Long parentId; // null이면 댓글, 값 있으면 대댓글
    @NotBlank
    private String rcontent;
} 