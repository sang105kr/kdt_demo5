package com.kh.demo.domain.product.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Elasticsearch 상품 문서
 */
@Data
@Document(indexName = "products")
public class ProductDocument {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Long)
    private Long productId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String pname;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Integer)
    private Integer price;
    
    @Field(type = FieldType.Double)
    private Double rating;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Date)
    private String cdate;
    
    @Field(type = FieldType.Date)
    private String udate;
} 