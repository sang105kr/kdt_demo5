package com.kh.demo.domain.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 채팅 세션 상세 정보 DTO
 * JOIN 결과를 담기 위한 DTO
 */
@Data
public class ChatSessionDetailDto {
    // 기본 세션 정보
    private Long sessionId;
    private Long memberId;
    private Long adminId;
    private Long categoryId;
    private Long statusId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer messageCount;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    // 새로 추가된 필드들
    private LocalDateTime memberLastSeen;  // 고객 마지막 접속 시간
    private LocalDateTime adminLastSeen;   // 상담원 마지막 접속 시간
    private Long disconnectReasonId;       // 이탈 사유 (code_id 참조)
    private Long exitReasonId;             // 상담 종료 사유 (code_id 참조)
    private String endedBy;                // 종료자 타입 (M:고객, A:관리자)
    private LocalDateTime graceUntil;      // 유예 만료 시간(재접속 허용 기한)
    
    // JOIN으로 가져온 회원 정보
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private LocalDateTime memberJoinDate;
    private String adminName;
    private String categoryName;
    
    // 코드 정보 (이름으로 표시)
    private String disconnectReasonName;   // 이탈 사유명
    private String exitReasonName;         // 종료 사유명
    private String statusName;             // 상태명
}
