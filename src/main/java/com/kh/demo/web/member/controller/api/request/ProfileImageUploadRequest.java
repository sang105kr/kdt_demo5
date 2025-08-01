package com.kh.demo.web.member.controller.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

/**
 * 프로필 이미지 업로드 요청 DTO
 */
@Getter
@Setter
@ToString
public class ProfileImageUploadRequest {
    
    @NotNull(message = "프로필 이미지를 선택해주세요.")
    private MultipartFile profileImage;
    
    /**
     * 파일이 있는지 확인
     */
    public boolean hasFile() {
        return profileImage != null && !profileImage.isEmpty();
    }
    
    /**
     * 파일 크기 반환 (bytes)
     */
    public long getFileSize() {
        return profileImage != null ? profileImage.getSize() : 0;
    }
    
    /**
     * 파일명 반환
     */
    public String getFileName() {
        return profileImage != null ? profileImage.getOriginalFilename() : null;
    }
    
    /**
     * Content-Type 반환
     */
    public String getContentType() {
        return profileImage != null ? profileImage.getContentType() : null;
    }
} 