package com.kh.demo.web.restcontroller.board.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyResponse {
    private Long replyId;
    private Long boardId;
    private String email;
    private String nickname;
    private String rcontent;
    private Long parentId;
    private Long rgroup;
    private Integer rstep;
    private Integer rindent;
    private String status;
    private LocalDateTime cdate;
    private LocalDateTime udate;
} 