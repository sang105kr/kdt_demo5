package com.kh.demo.web.product.controller.api.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateRequest {
    @NotBlank
    @Size(min = 1, max = 50)
    private String pname;

    @Size(max = 500)
    private String description;

    @NotNull
    @Positive
    @Min(value = 1000)
    @Max(value = 10000000)
    private Long price;

    @Min(value = 0)
    @Max(value = 5)
    private Double rating;

    @NotBlank
    @Size(max = 25)
    private String category;
} 