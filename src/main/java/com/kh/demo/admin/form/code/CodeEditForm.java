package com.kh.demo.admin.form.code;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 코드 수정 폼
 */
@Data
public class CodeEditForm {

    @NotNull(message = "코드 ID는 필수입니다")
    private Long codeId;

    @NotBlank(message = "그룹코드는 필수입니다")
    @Size(max = 30, message = "그룹코드는 30자 이하여야 합니다")
    private String gcode;

    @NotBlank(message = "코드값은 필수입니다")
    @Size(max = 30, message = "코드값은 30자 이하여야 합니다")
    private String code;

    @NotBlank(message = "코드명은 필수입니다")
    @Size(max = 100, message = "코드명은 100자 이하여야 합니다")
    private String decode;

    private Long pcode; // 상위코드 (선택사항)

    @NotNull(message = "정렬순서는 필수입니다")
    private Integer sortOrder = 1;

    @Pattern(regexp = "^[YN]$", message = "사용여부는 Y 또는 N이어야 합니다")
    private String useYn = "Y";
} 