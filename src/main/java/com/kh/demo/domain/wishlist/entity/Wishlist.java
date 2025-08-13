package com.kh.demo.domain.wishlist.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 위시리스트 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Wishlist extends BaseEntity {
    
    private Long wishlistId;     // 위시리스트 식별자
    private Long memberId;       // 회원 식별자
    private Long productId;      // 상품 식별자
} 