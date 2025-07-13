package com.kh.demo.web.controller.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateForm {
  @NotBlank(message = "제목은 필수 입력 항목입니다.")
  @Size(min = 3, max=50, message="제목은 최소3자 최대50자 이내로 입력해 주세요.")
  private String title;
  @NotBlank(message = "내용은 필수 입력 항목입니다.")
  private String content;
  @NotBlank(message = "작성자는 필수 입력 항목입니다.")
  @Size(min = 1, max=10, message="작성자는 최소1자 최대10자 이내로 입력해 주세요.")
  private String writer;
}
