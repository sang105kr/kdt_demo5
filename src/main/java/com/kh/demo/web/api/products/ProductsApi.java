package com.kh.demo.web.api.products;

import lombok.Data;

@Data
public class ProductsApi {
    private Long productId;
    private String pname;
    private String description;
    private Long price;
    private Double rating;
    private String category;
} 