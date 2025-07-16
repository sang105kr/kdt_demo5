package com.kh.demo.web.page.form.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveForm {
    @NotNull
    private Long bcategory;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String bcontent;

    @NotBlank
    private String nickname;

    @NotBlank
    private String email;
}
