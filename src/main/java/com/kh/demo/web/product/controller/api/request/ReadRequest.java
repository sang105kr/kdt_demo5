package com.kh.demo.web.product.controller.api.request;

import lombok.Data;

@Data
public class ReadRequest {
    private Long productId;
    private String pname;
    private String description;
    private Long price;
    private Double rating;
    private String category;
} 