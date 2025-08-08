package com.kh.demo.domain.notification.dao;

import com.kh.demo.domain.notification.entity.Notification;
import com.kh.demo.domain.notification.dto.NotificationDto;

import java.util.List;
import java.util.Optional;

/**
 * 알림 DAO 인터페이스
 * 
 * 알림 데이터에 대한 데이터베이스 접근을 담당합니다.
 * NamedParameterJdbcTemplate을 사용하여 SQL 인젝션을 방지하고
 * 파라미터 바인딩을 안전하게 처리합니다.
 */
public interface NotificationDAO {
    
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
     * @return 알림 엔티티 (Optional)
     */
    Optional<Notification> findById(Long notificationId);
    
    /**
     * 회원별 알림 목록 조회 (최신순)
     * 
     * @param memberId 회원 ID
     * @return 알림 목록
     */
    List<Notification> findByMemberId(Long memberId);
    
    /**
     * 회원별 대상 타입별 알림 목록 조회
     * 
     * @param memberId 회원 ID
     * @param targetTypeId 대상 타입 ID (code_id 참조)
     * @return 알림 목록
     */
    List<Notification> findByMemberIdAndTargetType(Long memberId, Long targetTypeId);
    
    /**
     * 회원별 읽지 않은 알림 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 읽지 않은 알림 목록
     */
    List<Notification> findUnreadByMemberId(Long memberId);
    
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
    
    /**
     * 회원별 알림 DTO 목록 조회 (UI용)
     * 
     * @param memberId 회원 ID
     * @param limit 조회할 개수 (0이면 전체)
     * @return 알림 DTO 목록
     */
    List<NotificationDto> findNotificationDtosByMemberId(Long memberId, int limit);
    
    /**
     * 오래된 알림 삭제 (배치 작업용)
     * 
     * @param days 삭제 기준 일수
     * @return 삭제된 행 수
     */
    int deleteOldNotifications(int days);
} 