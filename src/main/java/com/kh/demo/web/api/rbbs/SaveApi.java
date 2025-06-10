package com.kh.demo.web.api.rbbs;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaveApi {
  @NotNull(message="원글ID는 필수 입니다.")
  private Long bbsId;
  @NotBlank(message = "내용은 필수 입력 항목입니다.")
  private String content;
  @NotBlank(message = "작성자는 필수 입력 항목입니다.")
  @Size(min = 1, max=10, message="작성자는 최소1자 최대10자 이내로 입력해 주세요.")
  private String writer;
}
