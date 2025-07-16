package com.kh.demo.web.api.product.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateReq {
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(min = 1, max = 50, message = "상품명은 50자를 초과할 수 없습니다.")
    private String pname;

    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    @Min(value = 1000, message = "가격은 1000원 이상이어야 합니다.")
    @Max(value = 10000000, message = "가격은 1천만원 이하여야 합니다.")
    private Long price;

    @Min(value = 0, message = "평점은 0 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하여야 합니다.")
    private Double rating;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 25, message = "카테고리는 25자를 초과할 수 없습니다.")
    private String category;
} 