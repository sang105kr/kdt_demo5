package com.kh.demo.domain.product.search.document;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "product2")
@Setting(settingPath = "/elastic/product-settings.json")
public class ProductDocument {

    @Id
    private String id;  // Elasticsearch 내부 ID (String)
    
    @Field(type = FieldType.Long)
    private Long productId;  // Oracle의 product ID

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "products_pname_analyzer"),
        otherFields = {
            @InnerField(suffix = "auto_complete", type = FieldType.Search_As_You_Type, analyzer = "nori")
        }
    )
    private String pname;

    @Field(type = FieldType.Text, analyzer = "products_description_analyzer")
    private String description;

    @Field(type = FieldType.Integer)
    private Integer price;

    @Field(type = FieldType.Double)
    private Double rating;

    // 카테고리 ID (검색용)
    @Field(type = FieldType.Long)
    private Long categoryId;

    // 카테고리명 (표시용)
    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    /**
     * Products 엔티티를 ProductDocument로 변환 (카테고리명 포함)
     */
    public static ProductDocument from(Products products, CodeSVC codeSVC) {
        String categoryName = null;
        if (products.getCategoryId() != null) {
            categoryName = codeSVC.getCodeDecode("PRODUCT_CATEGORY", products.getCategoryId());
        }

        return ProductDocument.builder()
            .id(products.getProductId().toString())  // ✅ String ID 설정
            .productId(products.getProductId())
            .pname(products.getPname())
            .description(products.getDescription())
            .price(products.getPrice())
            .rating(products.getRating())
            .categoryId(products.getCategoryId())
            .categoryName(categoryName)
            .stockQuantity(products.getStockQuantity())
            .reviewCount(products.getReviewCount() != null ? products.getReviewCount() : 0)
            .build();
    }

    /**
     * ProductDocument를 Products 엔티티로 변환
     */
    public Products toEntity() {
        Products products = new Products();
        products.setProductId(this.productId);
        products.setPname(this.pname);
        products.setDescription(this.description);
        products.setPrice(this.price);
        products.setRating(this.rating);
        products.setCategoryId(this.categoryId);  // categoryId 사용
        products.setStockQuantity(this.stockQuantity);
        return products;
    }
}