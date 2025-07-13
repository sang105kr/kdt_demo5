package com.kh.demo.web.controller.form.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginForm {
  @NotBlank(message = "이메일은 필수!")
  private String email;
  @NotBlank(message = "비밀번호는 필수!")
  private String passwd;
}
