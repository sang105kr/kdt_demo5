package com.kh.demo.domain.chat.entity;

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
    private Long sessionId;           // 채팅 세션 ID
    private Long memberId;              // 고객 ID
    private Long adminId;               // 상담원 ID
    private Long categoryId;            // 문의 카테고리 (code_id 참조)
    private Long statusId;              // 상태 (code_id 참조)
    private String title;               // 채팅 제목
    private LocalDateTime startTime;    // 시작시간
    private LocalDateTime endTime;      // 종료시간
    private LocalDateTime memberLastSeen;  // 고객 마지막 접속 시간
    private LocalDateTime adminLastSeen;   // 상담원 마지막 접속 시간
    private Long disconnectReasonId;    // 이탈 사유 (code_id 참조)
    private Long exitReasonId;          // 상담 종료 사유 (code_id 참조)
    private String endedBy;             // 종료자 타입 (M:고객, A:관리자)
    private LocalDateTime graceUntil;   // 유예 만료 시간(재접속 허용 기한)
}
