package com.kh.demo.web.form.bbs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateForm {
  private Long bbsId;
  private String title;
  private String content;
  private String writer;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
