package com.kh.demo.domain.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * WebSocket 메시지 전송용 DTO
 */
@Data
public class ChatMessageDto {
    private String sessionId;           // 채팅 세션 ID
    private Long senderId;              // 발신자 ID
    private String senderType;          // 발신자 타입 (M:고객, A:관리자)
    private String content;             // 메시지 내용
    private String senderName;          // 발신자 이름
    private LocalDateTime timestamp;    // 전송 시간
    private Long messageTypeId;         // 메시지 타입 (1: 일반, 2: 시스템)
    private String isRead;              // 읽음 여부 (Y/N)
}
