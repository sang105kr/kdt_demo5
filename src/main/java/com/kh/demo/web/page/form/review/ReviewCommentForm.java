package com.kh.demo.web.page.form.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewCommentForm {
    
    @NotNull(message = "리뷰 ID는 필수입니다")
    private Long reviewId;
    
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(min = 1, max = 500, message = "댓글 내용은 {min}~{max}자 이내여야 합니다")
    private String content;
} 