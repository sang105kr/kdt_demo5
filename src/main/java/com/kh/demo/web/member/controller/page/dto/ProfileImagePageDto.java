package com.kh.demo.web.member.controller.page.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 프로필 이미지 페이지 DTO (SSR용)
 */
@Getter
@Builder
@ToString
public class ProfileImagePageDto {
    
    /**
     * 회원 ID
     */
    private Long memberId;
    
    /**
     * 회원 닉네임
     */
    private String nickname;
    
    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;
    
    /**
     * 프로필 이미지 존재 여부
     */
    private boolean hasProfileImage;
    
    /**
     * 에러 메시지
     */
    private String errorMessage;
    
    /**
     * 성공 메시지
     */
    private String successMessage;
} 