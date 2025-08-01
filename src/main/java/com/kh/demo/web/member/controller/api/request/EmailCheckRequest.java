package com.kh.demo.web.member.controller.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 중복 확인 요청 DTO
 */
@Getter
@Setter
@ToString
public class EmailCheckRequest {
    
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    /**
     * 이메일이 유효한지 확인
     */
    public boolean isValidEmail() {
        return email != null && email.contains("@") && email.length() > 5;
    }
} 