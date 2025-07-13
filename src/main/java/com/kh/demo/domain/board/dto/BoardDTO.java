package com.kh.demo.domain.board.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 게시판 데이터 전송 객체
 */
@Data
public class BoardDTO {
    private Long boardId;
    private Long bcategory;
    private String title;
    private String email;
    private String nickname;
    private Integer hit;
    private String bcontent;
    private Long pboardId;
    private Long bgroup;
    private Integer step;
    private Integer bindent;
    private String status;
    private LocalDateTime cdate;
    private LocalDateTime udate;
} 