package com.kh.demo.web.page.form.member;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FindIdForm {
    
    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String tel;
    
    @NotNull(message = "생년월일은 필수입니다")
    private LocalDate birthDate;
} 