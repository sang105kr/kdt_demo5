package com.kh.demo.domain.review.vo;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.review.entity.Review;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDetailVO {
    private Review review;
    private Member member;
    private Products product;
    private Order order;
    private Integer commentCount; // 댓글 개수 추가
} 