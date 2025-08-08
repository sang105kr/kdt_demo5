package com.kh.demo.admin.member.page.form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 회원 수정 폼
 */
@Data
public class AdminMemberForm {
    
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;
    
    @NotBlank(message = "전화번호는 필수입니다.")
    @Size(min = 10, max = 15, message = "전화번호는 10~15자 사이여야 합니다.")
    private String tel;
    
    @NotNull(message = "성별은 필수입니다.")
    private Long gender;
    
    @NotNull(message = "생년월일은 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    @NotNull(message = "회원 구분은 필수입니다.")
    private Long gubun;
    
    @NotNull(message = "회원 상태는 필수입니다.")
    private Long status;
    
    // 취미 리스트 (1:N 관계)
    private List<Long> hobbies;
    
    // 기타 정보
    private Long region;
    private String address;
    private String addressDetail;
    private String zipcode;
} 