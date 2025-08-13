package com.kh.demo.domain.chat.dto;

import lombok.Data;

/**
 * 채팅 세션 생성 요청 DTO
 */
@Data
public class ChatSessionRequest {
    private Long memberId;              // 고객 ID
    private Long categoryId;            // 문의 카테고리
    private String title;               // 채팅 제목
}
