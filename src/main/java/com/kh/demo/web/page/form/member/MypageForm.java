package com.kh.demo.web.page.form.member;

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
    
    private List<Long> hobby;
    
    private Long region;
} 