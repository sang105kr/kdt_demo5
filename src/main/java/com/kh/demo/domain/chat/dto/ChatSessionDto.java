package com.kh.demo.domain.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 채팅 세션 응답 DTO
 */
@Data
public class ChatSessionDto {
    private Long sessionId;           // 채팅 세션 ID
    private Long memberId;              // 고객 ID
    private String memberName;          // 고객 이름
    private String memberEmail;         // 고객 이메일
    private String memberPhone;         // 고객 전화번호
    private LocalDateTime memberJoinDate; // 고객 가입일
    private Long adminId;               // 상담원 ID
    private String adminName;           // 상담원 이름
    private Long categoryId;            // 문의 카테고리
    private String categoryName;        // 카테고리 이름
    private Long statusId;              // 상태
    private String statusName;          // 상태 이름
    private String title;               // 채팅 제목
    private LocalDateTime startTime;    // 시작시간
    private LocalDateTime endTime;      // 종료시간
    private Integer messageCount;       // 메시지 수
    private Long exitReasonId;          // 종료 사유 ID
    private LocalDateTime cdate;        // 생성일시
}
