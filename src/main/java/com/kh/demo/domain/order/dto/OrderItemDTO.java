package com.kh.demo.domain.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 주문 상품 정보 DTO (View용)
 */
@Data
public class OrderItemDTO {
    
    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
    private Integer subtotal;
    private LocalDateTime cdate;
    private LocalDateTime udate;

    // 리뷰 관련
    private Boolean reviewed; // 리뷰 작성 여부
    private Long reviewId;    // 작성된 리뷰의 id (없으면 null)
} 