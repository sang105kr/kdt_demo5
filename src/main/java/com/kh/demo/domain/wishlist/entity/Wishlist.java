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
    
    // 조인용 필드 (실제 DB 컬럼이 아님)
    private String productName;  // 상품명
    private Integer productPrice; // 상품가격
    private String productCategory; // 상품카테고리
    private String memberNickname; // 회원 닉네임
} 