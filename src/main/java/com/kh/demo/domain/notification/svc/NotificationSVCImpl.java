package com.kh.demo.domain.notification.svc;

import com.kh.demo.domain.notification.dao.NotificationDAO;
import com.kh.demo.domain.notification.entity.Notification;
import com.kh.demo.domain.notification.dto.NotificationDto;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 서비스 구현체
 * 
 * 알림 관련 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * 
 * 주요 기능:
 * - 알림 CRUD 작업
 * - 읽음/안읽음 상태 관리
 * - 회원별 알림 조회
 * - 알림 타입별 생성 메서드
 * 
 * 알림 타입:
 * - ORDER: 주문 관련 알림
 * - PAYMENT: 결제 관련 알림
 * - DELIVERY: 배송 관련 알림
 * - REVIEW: 리뷰 관련 알림
 * - PRODUCT: 상품 관련 알림
 * - SYSTEM: 시스템 관련 알림
 * - ADMIN_ALERT: 관리자 알림
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSVCImpl implements NotificationSVC {

    private final NotificationDAO notificationDAO;
    private final CodeSVC codeSVC;

    /**
     * 알림 저장
     * 
     * 새로운 알림을 데이터베이스에 저장합니다.
     * 기본값(생성일시, 읽음여부, 사용여부)을 자동으로 설정합니다.
     * 
     * @param notification 저장할 알림 엔티티
     * @return 생성된 알림 ID
     */
    @Override
    @Transactional
    public Long save(Notification notification) {
        // 기본값 설정
        if (notification.getCreatedDate() == null) {
            notification.setCreatedDate(LocalDateTime.now());
        }
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        if (notification.getUseYn() == null) {
            notification.setUseYn("Y");
        }
        
        return notificationDAO.save(notification);
    }

    /**
     * 알림 수정
     * 
     * 기존 알림 정보를 업데이트합니다.
     * 
     * @param notification 수정할 알림 엔티티
     * @return 수정된 행 수
     */
    @Override
    @Transactional
    public int update(Notification notification) {
        return notificationDAO.update(notification);
    }

    /**
     * 알림 삭제
     * 
     * 지정된 알림을 데이터베이스에서 삭제합니다.
     * 
     * @param notificationId 삭제할 알림 ID
     * @return 삭제된 행 수
     */
    @Override
    @Transactional
    public int delete(Long notificationId) {
        return notificationDAO.delete(notificationId);
    }

    /**
     * 알림 ID로 조회
     * 
     * 지정된 알림 ID로 알림을 조회합니다.
     * 알림이 존재하지 않으면 RuntimeException을 발생시킵니다.
     * 
     * @param notificationId 조회할 알림 ID
     * @return 알림 엔티티
     * @throws RuntimeException 알림을 찾을 수 없는 경우
     */
    @Override
    public Notification findById(Long notificationId) {
        return notificationDAO.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + notificationId));
    }

    /**
     * 회원별 알림 목록 조회
     * 
     * 지정된 회원의 모든 알림을 최신순으로 조회합니다.
     * 
     * @param memberId 회원 ID
     * @return 알림 목록 (최신순)
     */
    @Override
    public List<Notification> findByMemberId(Long memberId) {
        return notificationDAO.findByMemberId(memberId);
    }

    /**
     * 회원별 대상 타입별 알림 목록 조회
     * 
     * 지정된 회원의 특정 대상 타입 알림을 최신순으로 조회합니다.
     * 
     * @param memberId 회원 ID
     * @param targetType 대상 타입 (CUSTOMER, ADMIN)
     * @return 알림 목록 (최신순)
     */
    @Override
    public List<Notification> findByMemberIdAndTargetType(Long memberId, String targetType) {
        return notificationDAO.findByMemberIdAndTargetType(memberId, targetType);
    }

    /**
     * 회원별 알림 DTO 목록 조회 (UI용)
     * 
     * 지정된 회원의 알림을 DTO 형태로 조회합니다.
     * UI에서 사용하기 위한 추가 정보(알림 타입명 등)를 포함합니다.
     * 
     * @param memberId 회원 ID
     * @param limit 조회할 개수 (0이면 전체)
     * @return 알림 DTO 목록 (최신순)
     */
    @Override
    public List<NotificationDto> findNotificationDtosByMemberId(Long memberId, int limit) {
        return notificationDAO.findNotificationDtosByMemberId(memberId, limit);
    }

    /**
     * 회원별 읽지 않은 알림 개수 조회
     * 
     * 지정된 회원의 읽지 않은 알림 개수를 조회합니다.
     * 
     * @param memberId 회원 ID
     * @return 읽지 않은 알림 개수
     */
    @Override
    public int countUnreadByMemberId(Long memberId) {
        return notificationDAO.countUnreadByMemberId(memberId);
    }

    /**
     * 알림 읽음 처리
     * 
     * 지정된 알림을 읽음 상태로 변경합니다.
     * 
     * @param notificationId 읽음 처리할 알림 ID
     * @return 수정된 행 수
     */
    @Override
    @Transactional
    public int markAsRead(Long notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    /**
     * 회원의 모든 알림 읽음 처리
     * 
     * 지정된 회원의 모든 읽지 않은 알림을 읽음 상태로 변경합니다.
     * 
     * @param memberId 회원 ID
     * @return 수정된 행 수
     */
    @Override
    @Transactional
    public int markAllAsRead(Long memberId) {
        return notificationDAO.markAllAsRead(memberId);
    }

    // ===== 알림 생성 메서드들 =====
    
    /**
     * 주문 알림 생성
     * 
     * 주문 관련 알림을 생성합니다.
     * 알림 타입은 ORDER로 설정되며, 대상 타입은 CUSTOMER입니다.
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param orderId 주문 ID
     */
    @Override
    @Transactional
    public void createOrderNotification(Long memberId, String title, String message, Long orderId) {
        Notification notification = new Notification();
        notification.setMemberId(memberId);
        notification.setTargetType("CUSTOMER");
        notification.setNotificationTypeId(codeSVC.getCodeId("NOTIFICATION_TYPE", "ORDER"));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl("/orders/" + orderId);
        notification.setTargetId(orderId);
        
        save(notification);
        log.info("주문 알림 생성: memberId={}, title={}", memberId, title);
    }

    /**
     * 결제 알림 생성
     * 
     * 결제 관련 알림을 생성합니다.
     * 알림 타입은 PAYMENT로 설정되며, 대상 타입은 CUSTOMER입니다.
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param orderId 주문 ID
     */
    @Override
    @Transactional
    public void createPaymentNotification(Long memberId, String title, String message, Long orderId) {
        Notification notification = new Notification();
        notification.setMemberId(memberId);
        notification.setTargetType("CUSTOMER");
        notification.setNotificationTypeId(codeSVC.getCodeId("NOTIFICATION_TYPE", "PAYMENT"));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl("/orders/" + orderId);
        notification.setTargetId(orderId);
        
        save(notification);
        log.info("결제 알림 생성: memberId={}, title={}", memberId, title);
    }

    /**
     * 배송 알림 생성
     * 
     * 배송 관련 알림을 생성합니다.
     * 알림 타입은 DELIVERY로 설정되며, 대상 타입은 CUSTOMER입니다.
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param orderId 주문 ID
     */
    @Override
    @Transactional
    public void createDeliveryNotification(Long memberId, String title, String message, Long orderId) {
        Notification notification = new Notification();
        notification.setMemberId(memberId);
        notification.setTargetType("CUSTOMER");
        notification.setNotificationTypeId(codeSVC.getCodeId("NOTIFICATION_TYPE", "DELIVERY"));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl("/orders/" + orderId);
        notification.setTargetId(orderId);
        
        save(notification);
        log.info("배송 알림 생성: memberId={}, title={}", memberId, title);
    }

    /**
     * 리뷰 알림 생성
     * 
     * 리뷰 관련 알림을 생성합니다.
     * 알림 타입은 REVIEW로 설정되며, 대상 타입은 CUSTOMER입니다.
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param productId 상품 ID
     */
    @Override
    @Transactional
    public void createReviewNotification(Long memberId, String title, String message, Long productId) {
        Notification notification = new Notification();
        notification.setMemberId(memberId);
        notification.setTargetType("CUSTOMER");
        notification.setNotificationTypeId(codeSVC.getCodeId("NOTIFICATION_TYPE", "REVIEW"));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl("/reviews/write?productId=" + productId);
        notification.setTargetId(productId);
        
        save(notification);
        log.info("리뷰 알림 생성: memberId={}, title={}", memberId, title);
    }

    /**
     * 상품 알림 생성
     * 
     * 상품 관련 알림을 생성합니다.
     * 알림 타입은 PRODUCT로 설정되며, 대상 타입은 CUSTOMER입니다.
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param productId 상품 ID
     */
    @Override
    @Transactional
    public void createProductNotification(Long memberId, String title, String message, Long productId) {
        Notification notification = new Notification();
        notification.setMemberId(memberId);
        notification.setTargetType("CUSTOMER");
        notification.setNotificationTypeId(codeSVC.getCodeId("NOTIFICATION_TYPE", "PRODUCT"));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl("/products/" + productId);
        notification.setTargetId(productId);
        
        save(notification);
        log.info("상품 알림 생성: memberId={}, title={}", memberId, title);
    }

    /**
     * 시스템 알림 생성
     * 
     * 시스템 관련 알림을 생성합니다.
     * 알림 타입은 SYSTEM으로 설정되며, 대상 타입은 CUSTOMER입니다.
     * 
     * @param memberId 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     */
    @Override
    @Transactional
    public void createSystemNotification(Long memberId, String title, String message) {
        Notification notification = new Notification();
        notification.setMemberId(memberId);
        notification.setTargetType("CUSTOMER");
        notification.setNotificationTypeId(codeSVC.getCodeId("NOTIFICATION_TYPE", "SYSTEM"));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl("/mypage");
        
        save(notification);
        log.info("시스템 알림 생성: memberId={}, title={}", memberId, title);
    }

    /**
     * 관리자 알림 생성
     * 
     * 관리자용 알림을 생성합니다.
     * 알림 타입은 ADMIN_ALERT로 설정되며, 대상 타입은 ADMIN입니다.
     * 
     * @param adminMemberId 관리자 회원 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param targetUrl 관련 URL
     */
    @Override
    @Transactional
    public void createAdminAlert(Long adminMemberId, String title, String message, String targetUrl) {
        Notification notification = new Notification();
        notification.setMemberId(adminMemberId);
        notification.setTargetType("ADMIN");
        notification.setNotificationTypeId(codeSVC.getCodeId("NOTIFICATION_TYPE", "ADMIN_ALERT"));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);
        
        save(notification);
        log.info("관리자 알림 생성: memberId={}, title={}", adminMemberId, title);
    }
} 