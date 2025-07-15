package com.kh.demo.web.controller.form.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DetailForm {
  private Long boardId;
  private Long bcategory;
  private String title;
  private String bcontent;
  private String nickname;
  private String email;
  private LocalDateTime cdate;
  private LocalDateTime udate;
  private Integer likeCount;
  private Integer dislikeCount;
}
