package com.kh.demo.domain.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 채팅 세션 상세 정보 DTO (JOIN 데이터 포함)
 */
@Data
public class ChatSessionDetailDto {
    // ChatSession 엔티티 필드
    private String sessionId;           // 채팅 세션 ID
    private Long memberId;              // 고객 ID
    private Long adminId;               // 상담원 ID
    private Long categoryId;            // 문의 카테고리
    private Long statusId;              // 상태 (1:대기, 2:진행중, 3:완료)
    private String title;               // 채팅 제목
    private LocalDateTime startTime;    // 시작시간
    private LocalDateTime endTime;      // 종료시간
    private Integer messageCount;       // 메시지 수
    private LocalDateTime cdate;        // 생성일시
    private LocalDateTime udate;        // 수정일시
    
    // JOIN으로 가져올 회원 정보
    private String memberName;          // 고객 이름
    private String memberEmail;         // 고객 이메일
    private String memberPhone;         // 고객 전화번호
    private String adminName;           // 상담원 이름
    private String categoryName;        // 카테고리 이름
    private String statusName;          // 상태 이름
}
