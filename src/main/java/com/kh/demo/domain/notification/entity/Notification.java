package com.kh.demo.domain.notification.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 * 
 * 고객용과 관리자용 알림을 통합 관리하는 엔티티입니다.
 * code 테이블과 연계하여 알림 타입을 관리합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    
    /** 알림 고유 ID */
    private Long notificationId;
    
    /** 회원 ID (member 테이블 참조) */
    private Long memberId;
    
    /** 대상 타입 (code_id 참조, gcode='NOTIFICATION_TARGET_TYPE') */
    private Long targetType;
    
    /** 알림 타입 ID (code 테이블의 NOTIFICATION_TYPE 참조) */
    private Long notificationTypeId;
    
    /** 알림 제목 */
    private String title;
    
    /** 알림 메시지 */
    private String message;
    
    /** 관련 URL (클릭 시 이동할 페이지) */
    private String targetUrl;
    
    /** 관련 ID (주문ID, 상품ID, 리뷰ID 등) */
    private Long targetId;
    
    /** 읽음 여부 (N: 안읽음, Y: 읽음) */
    private String isRead;
    
    /** 생성일시 */
    private LocalDateTime createdDate;
    
    /** 읽음 일시 */
    private LocalDateTime readDate;
    
    /** 사용여부 (Y: 사용, N: 삭제) */
    private String useYn;
} 