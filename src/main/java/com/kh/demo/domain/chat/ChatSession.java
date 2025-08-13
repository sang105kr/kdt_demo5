package com.kh.demo.domain.chat;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 채팅 세션 엔티티
 * chat_session 테이블과 매핑
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSession extends BaseEntity {
    private String sessionId;           // 채팅 세션 ID
    private Long memberId;              // 고객 ID
    private Long adminId;               // 상담원 ID
    private Long categoryId;            // 문의 카테고리
    private Long statusId;              // 상태 (1:대기, 2:진행중, 3:완료)
    private String title;               // 채팅 제목
    private LocalDateTime startTime;    // 시작시간
    private LocalDateTime endTime;      // 종료시간
    private Integer messageCount;       // 메시지 수
}
