package com.kh.demo.web.member.controller.page.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginForm {
  @NotBlank
  private String email;
  @NotBlank
  private String passwd;
}
