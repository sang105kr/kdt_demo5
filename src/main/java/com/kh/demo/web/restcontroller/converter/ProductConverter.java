package com.kh.demo.web.restcontroller.converter;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.web.restcontroller.product.request.CreateReq;
import com.kh.demo.web.restcontroller.product.request.UpdateReq;
import com.kh.demo.web.restcontroller.product.response.ReadReq;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 도메인 변환 로직
 * DTO ↔ Entity 변환을 담당
 */
@Component
public class ProductConverter {

    /**
     * CreateReq → Products 엔티티 변환
     */
    public Products toEntity(CreateReq request) {
        Products product = new Products();
        BeanUtils.copyProperties(request, product);
        // Long → Integer 타입 변환 (필드명이 같아도 타입이 다르면 수동 처리)
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice().intValue());
        }
        return product;
    }

    /**
     * UpdateReq → Products 엔티티 변환
     */
    public Products toEntity(UpdateReq request) {
        Products product = new Products();
        BeanUtils.copyProperties(request, product);
        // Long → Integer 타입 변환
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice().intValue());
        }
        return product;
    }

    /**
     * Products 엔티티 → ReadReq 변환
     */
    public ReadReq toResponse(Products product) {
        ReadReq response = new ReadReq();
        BeanUtils.copyProperties(product, response);
        // 필드명이 다른 경우 수동 매핑
        response.setProductId(product.getProductId());
        response.setCreatedAt(product.getCdate());
        response.setUpdatedAt(product.getUdate());
        return response;
    }

    /**
     * Products 리스트 → ReadReq 리스트 변환
     */
    public List<ReadReq> toResponseList(List<Products> products) {
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 엔티티 업데이트 (기존 엔티티에 요청 데이터 반영)
     * 부분 업데이트는 NamedJdbcTemplate에서 동적 쿼리로 처리하므로
     * 여기서는 단순히 BeanUtils로 복사
     */
    public void updateEntity(Products existingProduct, UpdateReq request) {
        BeanUtils.copyProperties(request, existingProduct);
        // Long → Integer 타입 변환
        if (request.getPrice() != null) {
            existingProduct.setPrice(request.getPrice().intValue());
        }
    }
} 