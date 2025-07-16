package com.kh.demo.web.page.form.product;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateForm {
    private Long productId;

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
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double rating;

    @NotBlank
    @Size(max = 50)
    private String category;
}
