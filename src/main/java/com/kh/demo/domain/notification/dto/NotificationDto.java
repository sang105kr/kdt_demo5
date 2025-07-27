package com.kh.demo.domain.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 알림 DTO
 * 
 * 프론트엔드에서 사용하는 알림 데이터 전송 객체입니다.
 * 엔티티와 달리 알림 타입명, 상대적 시간, 아이콘 등 UI에 필요한 추가 정보를 포함합니다.
 */
@Data
public class NotificationDto {
    
    /** 알림 고유 ID */
    private Long notificationId;
    
    /** 회원 ID */
    private Long memberId;
    
    /** 대상 타입 (CUSTOMER: 고객용, ADMIN: 관리자용) */
    private String targetType;
    
    /** 알림 타입 ID */
    private Long notificationTypeId;
    
    /** 알림 타입명 (code.decode에서 조회) */
    private String notificationTypeName;
    
    /** 알림 제목 */
    private String title;
    
    /** 알림 메시지 */
    private String message;
    
    /** 관련 URL */
    private String targetUrl;
    
    /** 관련 ID */
    private Long targetId;
    
    /** 읽음 여부 (0: 안읽음, 1: 읽음) */
    private Integer isRead;
    
    /** 생성일시 */
    private LocalDateTime createdDate;
    
    /** 읽음 일시 */
    private LocalDateTime readDate;
    
    /** 사용여부 */
    private String useYn;
    
    // ===== UI 전용 필드 =====
    
    /** 상대적 시간 (예: "10분 전", "1시간 전") */
    private String timeAgo;
    
    /** 알림 아이콘 (알림 타입별 아이콘) */
    private String notificationIcon;
} 