package com.kh.demo.domain.chat.dto;

import lombok.Data;

/**
 * 채팅 메시지 타입 DTO
 */
@Data
public class ChatMessageTypeDto {
    private Long codeId;    // 코드 ID
    private String code;    // 코드 값
    private String decode;  // 코드 설명
}
