package com.kh.demo.web.member.controller.page.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileImageForm {
    
    @NotNull(message = "프로필 사진을 선택해주세요")
    private MultipartFile profileImage;
} 