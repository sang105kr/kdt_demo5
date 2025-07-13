package com.kh.demo.web.form.board;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DetailForm {
  private Long bbsId;
  private String title;
  private String content;
  private String writer;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
