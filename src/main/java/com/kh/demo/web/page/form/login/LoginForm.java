package com.kh.demo.web.page.form.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginForm {
  @NotBlank
  private String email;
  @NotBlank
  private String passwd;
}
