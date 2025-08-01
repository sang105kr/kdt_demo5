package com.kh.demo.web.member.controller.page.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 이미지 업로드 폼
 */
@Getter
@Setter
@ToString
public class ProfileImageForm {
    
    @NotNull(message = "프로필 이미지를 선택해주세요.")
    private MultipartFile profileImage;
    
    /**
     * 업로드된 파일이 있는지 확인
     */
    public boolean hasFile() {
        return profileImage != null && !profileImage.isEmpty();
    }
    
    /**
     * 파일 크기 반환
     */
    public long getFileSize() {
        return profileImage != null ? profileImage.getSize() : 0;
    }
    
    /**
     * 파일 이름 반환
     */
    public String getFileName() {
        return profileImage != null ? profileImage.getOriginalFilename() : null;
    }
    
    /**
     * Content Type 반환
     */
    public String getContentType() {
        return profileImage != null ? profileImage.getContentType() : null;
    }
} 