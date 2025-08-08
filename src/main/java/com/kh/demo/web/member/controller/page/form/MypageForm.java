package com.kh.demo.web.member.controller.page.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MypageForm {
    
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "연락처를 입력해주세요")
    @Pattern(regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$", message = "올바른 연락처 형식이 아닙니다")
    private String tel;
    
    @NotBlank(message = "별칭을 입력해주세요")
    @Size(min = 2, max = 10, message = "별칭은 2~10자 사이로 입력해주세요")
    private String nickname;
    
    @NotBlank(message = "성별을 선택해주세요")
    private String gender;
    
    @NotNull(message = "생년월일을 입력해주세요")
    private LocalDate birthDate;
    
    private List<String> hobby;
    
    private Long region;
    
    // 주소 정보
    private String zipcode;         // 우편번호
    private String address;         // 기본주소
    private String addressDetail;   // 상세주소
    
    @NotNull(message = "비밀번호를 입력해주세요")
    private String currentPassword; // 현재 비밀번호
} 