package com.kh.demo.web.member.controller.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 닉네임 중복 확인 요청 DTO
 */
@Getter
@Setter
@ToString
public class NicknameCheckRequest {
    
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이로 입력해주세요.")
    private String nickname;
    
    private String currentEmail; // 현재 사용자 이메일 (선택사항)
    
    /**
     * 닉네임이 유효한지 확인
     */
    public boolean isValidNickname() {
        return nickname != null && nickname.length() >= 2 && nickname.length() <= 20;
    }
    
    /**
     * 현재 사용자 이메일이 있는지 확인
     */
    public boolean hasCurrentEmail() {
        return currentEmail != null && !currentEmail.trim().isEmpty();
    }
} 