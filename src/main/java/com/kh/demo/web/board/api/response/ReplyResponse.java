package com.kh.demo.web.board.api.response;

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
    private String parentNickname;  // 부모 댓글 작성자 닉네임
    private Long rgroup;
    private Integer rstep;
    private Integer rindent;
    private Integer likeCount;
    private Integer dislikeCount;
    private String status;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    private String profileImageUrl; // 작성자 프로필 이미지 URL (없으면 null)
} 