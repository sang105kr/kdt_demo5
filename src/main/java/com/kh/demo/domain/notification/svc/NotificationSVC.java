package com.kh.demo.domain.notification.svc;

import com.kh.demo.domain.notification.entity.Notification;
import com.kh.demo.domain.notification.dto.NotificationDto;

import java.util.List;

/**
 * 알림 서비스 인터페이스
 * 
 * 알림 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 
 * 주요 기능:
 * - 알림 CRUD 작업
 * - 읽음/안읽음 상태 관리
 * - 회원별 알림 조회
 * - 알림 타입별 생성 메서드
 */
public interface NotificationSVC {
    
    // ===== 기본 CRUD 메서드 =====
    
    /**
     * 알림 저장
     * 
     * @param notification 저장할 알림 엔티티
     * @return 생성된 알림 ID
     */
    Long save(Notification notification);
    
    /**
     * 알림 수정
     * 
     * @param notification 수정할 알림 엔티티
     * @return 수정된 행 수
     */
    int update(Notification notification);
    
    /**
     * 알림 삭제
     * 
     * @param notificationId 삭제할 알림 ID
     * @return 삭제된 행 수
     */
    int delete(Long notificationId);
    
    /**
     * 알림 ID로 조회
     * 
     * @param notificationId 조회할 알림 ID
     * @return 알림 엔티티
     */
    Notification findById(Long notificationId);
    
    /**
     * 회원별 알림 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 알림 목록
     */
    List<Notification> findByMemberId(Long memberId);
    
    /**
     * 회원별 대상 타입별 알림 목록 조회
     * 
     * @param memberId 회원 ID
     * @param targetType 대상 타입 (CUSTOMER, ADMIN)
     * @return 알림 목록
     */
    List<Notification> findByMemberIdAndTargetType(Long memberId, String targetType);
    
    /**
     * 회원별 알림 DTO 목록 조회 (UI용)
     * 
     * @param memberId 회원 ID
     * @param limit 조회할 개수 (0이면 전체)
     * @return 알림 DTO 목록
     */
    List<NotificationDto> findNotificationDtosByMemberId(Long memberId, int limit);
    
    /**
     * 회원별 읽지 않은 알림 개수 조회
     * 
     * @param memberId 회원 ID
     * @return 읽지 않은 알림 개수
     */
    int countUnreadByMemberId(Long memberId);
    
    /**
     * 알림 읽음 처리
     * 
     * @param notificationId 읽음 처리할 알림 ID
     * @return 수정된 행 수
     */
    int markAsRead(Long notificationId);
    
    /**
     * 회원의 모든 알림 읽음 처리
     * 
     * @param memberId 회원 ID
     * @return 수정된 행 수
     */
    int markAllAsRead(Long memberId);
    
    // ===== 알림 생성 메서드들 =====
    
    /**
     * 주문 알림 생성
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param orderId 주문 ID
     */
    void createOrderNotification(Long memberId, String title, String message, Long orderId);
    
    /**
     * 결제 알림 생성
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param orderId 주문 ID
     */
    void createPaymentNotification(Long memberId, String title, String message, Long orderId);
    
    /**
     * 배송 알림 생성
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param orderId 주문 ID
     */
    void createDeliveryNotification(Long memberId, String title, String message, Long orderId);
    
    /**
     * 리뷰 알림 생성
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param productId 상품 ID
     */
    void createReviewNotification(Long memberId, String title, String message, Long productId);
    
    /**
     * 상품 알림 생성
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param productId 상품 ID
     */
    void createProductNotification(Long memberId, String title, String message, Long productId);
    
    /**
     * 시스템 알림 생성
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     */
    void createSystemNotification(Long memberId, String title, String message);
    
    /**
     * 관리자 알림 생성
     * 
     * @param adminMemberId 관리자 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param targetUrl 관련 URL
     */
    void createAdminAlert(Long adminMemberId, String title, String message, String targetUrl);
} 