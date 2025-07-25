package com.kh.demo.domain.common.enums;

/**
 * 상품 카테고리 열거형
 */
public enum ProductCategory {
    ELECTRONICS("ELECTRONICS", "전자제품"),
    CLOTHING("CLOTHING", "의류"),
    BOOKS("BOOKS", "도서"),
    FOOD("FOOD", "식품"),
    SPORTS("SPORTS", "스포츠용품"),
    BEAUTY("BEAUTY", "뷰티"),
    HOME("HOME", "홈&리빙"),
    AUTOMOTIVE("AUTOMOTIVE", "자동차용품"),
    TOYS("TOYS", "장난감"),
    OTHER("OTHER", "기타");
    
    private final String code;
    private final String description;
    
    ProductCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ProductCategory fromCode(String code) {
        for (ProductCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown product category code: " + code);
    }
} 