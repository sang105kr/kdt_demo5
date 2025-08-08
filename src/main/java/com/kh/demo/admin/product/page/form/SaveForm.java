package com.kh.demo.admin.product.page.form;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaveForm {

    @NotBlank
    @Size(min = 2, max = 100)
    private String pname;

    @NotBlank
    @Size(min = 10, max = 1000)
    private String description;

    @NotNull
    @Positive
    @Min(value = 0)
    @Max(value = 999999999)
    private Integer price;

    @NotNull
    @Positive
    @Max(value = 9999999999L)
    private Long categoryId;

    @NotNull
    @Min(value = 0)
    @Max(value = 999999)
    private Integer stockQuantity;
}
