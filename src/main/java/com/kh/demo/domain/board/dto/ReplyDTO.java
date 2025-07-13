package com.kh.demo.domain.board.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 댓글 데이터 전송 객체
 */
@Data
public class ReplyDTO {
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