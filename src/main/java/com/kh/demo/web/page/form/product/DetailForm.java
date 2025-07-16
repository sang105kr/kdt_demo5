package com.kh.demo.web.page.form.product;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DetailForm {
    private Long productId;
    private String pname;
    private String description;
    private Integer price;
    private Double rating;
    private String category;
    private LocalDateTime cdate;
    private LocalDateTime udate;
}
