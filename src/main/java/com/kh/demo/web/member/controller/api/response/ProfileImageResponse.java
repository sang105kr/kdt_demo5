package com.kh.demo.web.member.controller.api.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 프로필 이미지 응답 DTO
 */
@Getter
@Builder
@ToString
public class ProfileImageResponse {
    
    /**
     * 성공 메시지
     */
    private String message;
    
    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;
    
    /**
     * 프로필 이미지 존재 여부
     */
    private boolean hasProfileImage;
    
    /**
     * 파일 정보 (업로드 시)
     */
    private FileInfo fileInfo;
    
    @Getter
    @Builder
    @ToString
    public static class FileInfo {
        private String fileName;
        private long fileSize;
        private String contentType;
    }
} 