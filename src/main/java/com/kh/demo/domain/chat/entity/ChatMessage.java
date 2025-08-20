package com.kh.demo.domain.chat.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 * chat_message 테이블과 매핑
 */
@Data
public class ChatMessage {
    private Long messageId;             // 메시지 ID
    private Long sessionId;           // 채팅 세션 ID
    private Long senderId;              // 발신자 ID
    private String senderType;          // 발신자 타입 (M:고객, A:관리자)
    private Long messageTypeId;         // 메시지 타입
    private String content;             // 메시지 내용
    private String isRead;              // 읽음 여부 (Y: 읽음, N: 안읽음)
    private LocalDateTime cdate;        // 생성일시
}
