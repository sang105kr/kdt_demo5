package com.kh.demo.web.board.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyUpdateRequest {
    @NotNull
    private Long replyId;
    @NotBlank
    private String rcontent;
} 