package com.kh.demo.web.restcontroller.product.request;

import lombok.Data;

@Data
public class ReadReq {
    private Long productId;
    private String pname;
    private String description;
    private Long price;
    private Double rating;
    private String category;
} 