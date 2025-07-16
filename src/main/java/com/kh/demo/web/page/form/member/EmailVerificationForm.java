package com.kh.demo.web.page.form.member;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EmailVerificationForm {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "인증 코드는 필수입니다")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 숫자 6자리여야 합니다")
    private String verificationCode;
} 