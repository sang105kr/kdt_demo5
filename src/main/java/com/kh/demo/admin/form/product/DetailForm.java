package com.kh.demo.admin.form.product;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DetailForm {
    private Long productId;
    private String pname;
    private String productName;  // pname과 동일하지만 view에서 사용
    private String description;
    private Integer price;
    private Double rating;
    private String category;
    private Integer stockQuantity;
    private String imageUrl;     // 첫 번째 이미지 URL (view용)
    private String manualUrl;    // 첫 번째 매뉴얼 URL (view용)
    private LocalDateTime cdate;
    private LocalDateTime udate;
}
