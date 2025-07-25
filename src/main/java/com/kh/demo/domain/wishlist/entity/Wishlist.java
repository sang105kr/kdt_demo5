package com.kh.demo.domain.wishlist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 위시리스트 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {
    
    private Long wishlistId;     // 위시리스트 식별자
    private Long memberId;       // 회원 식별자
    private Long productId;      // 상품 식별자
    private LocalDateTime cdate; // 생성일시
    private LocalDateTime udate; // 수정일시
} 