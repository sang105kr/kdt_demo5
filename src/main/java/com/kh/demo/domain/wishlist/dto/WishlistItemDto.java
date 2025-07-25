package com.kh.demo.domain.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 위시리스트 목록 조회용 DTO
 * 위시리스트 + 상품 + 회원 정보 조인 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemDto {
    
    // 위시리스트 정보
    private Long wishlistId;     // 위시리스트 식별자
    private Long memberId;       // 회원 식별자
    private Long productId;      // 상품 식별자
    private LocalDateTime cdate; // 생성일시
    private LocalDateTime udate; // 수정일시
    
    // 상품 정보 (조인)
    private String productName;     // 상품명
    private Integer productPrice;   // 상품가격
    private String productCategory; // 상품카테고리
    
    // 회원 정보 (조인)
    private String memberNickname;  // 회원 닉네임
} 