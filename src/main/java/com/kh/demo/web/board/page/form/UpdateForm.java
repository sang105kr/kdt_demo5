package com.kh.demo.web.board.page.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateForm {
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

    private LocalDateTime cdate;
    private LocalDateTime udate;
}
