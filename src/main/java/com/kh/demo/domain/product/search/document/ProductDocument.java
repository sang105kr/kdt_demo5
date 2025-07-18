package com.kh.demo.domain.product.search.document;

import com.kh.demo.domain.product.entity.Products;
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
    @Field(type = FieldType.Long)
    private Long productId;

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

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "products_cateogry_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 50)
        }
    )
    private String category;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /**
     * Products 엔티티를 ProductDocument로 변환
     */
    public static ProductDocument from(Products products) {
        return ProductDocument.builder()
            .productId(products.getProductId())
            .pname(products.getPname())
            .description(products.getDescription())
            .price(products.getPrice())
            .rating(products.getRating())
            .category(products.getCategory())
            .stockQuantity(products.getStockQuantity())
            .reviewCount(0)  // 기본값
            .viewCount(0)    // 기본값
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
        products.setCategory(this.category);
        products.setStockQuantity(this.stockQuantity);
        return products;
    }
}