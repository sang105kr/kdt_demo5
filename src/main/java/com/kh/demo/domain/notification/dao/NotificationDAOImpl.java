package com.kh.demo.domain.notification.dao;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.notification.dto.NotificationDto;
import com.kh.demo.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 DAO 구현체
 * 
 * NamedParameterJdbcTemplate을 사용하여 알림 데이터에 대한
 * 데이터베이스 접근을 구현합니다.
 * 
 * 주요 기능:
 * - 알림 CRUD 작업
 * - 읽음/안읽음 상태 관리
 * - 회원별 알림 조회
 * - 알림 타입별 조회
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationDAOImpl implements NotificationDAO {

    private final NamedParameterJdbcTemplate template;
    private final CodeSVC codeSVC;  // CodeCache 대신 CodeSVC 사용

    // ===== RowMapper 정의 =====
    
    /** 알림 엔티티 RowMapper */
    private final RowMapper<Notification> notificationRowMapper = BeanPropertyRowMapper.newInstance(Notification.class);
    
    /** 알림 DTO RowMapper */
    private final RowMapper<NotificationDto> notificationDtoRowMapper = BeanPropertyRowMapper.newInstance(NotificationDto.class);

    /**
     * 알림 저장
     * 
     * 새로운 알림을 데이터베이스에 저장하고 생성된 알림 ID를 반환합니다.
     * 
     * @param notification 저장할 알림 엔티티
     * @return 생성된 알림 ID
     */
    @Override
    public Long save(Notification notification) {
        String sql = """
            INSERT INTO notifications (
                member_id, target_type, notification_type_id, title, message, 
                target_url, target_id, is_read, created_date, use_yn
            ) VALUES (
                :memberId, :targetType, :notificationTypeId, :title, :message,
                :targetUrl, :targetId, :isRead, :createdDate, :useYn
            )
            """;
        
        SqlParameterSource param = new BeanPropertySqlParameterSource(notification);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        template.update(sql, param, keyHolder);
        return keyHolder.getKey().longValue();
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
    public int update(Notification notification) {
        String sql = """
            UPDATE notifications SET
                member_id = :memberId,
                target_type = :targetType,
                notification_type_id = :notificationTypeId,
                title = :title,
                message = :message,
                target_url = :targetUrl,
                target_id = :targetId,
                is_read = :isRead,
                read_date = :readDate,
                use_yn = :useYn,
                udate = CURRENT_TIMESTAMP
            WHERE notification_id = :notificationId
            """;
        
        SqlParameterSource param = new BeanPropertySqlParameterSource(notification);
        return template.update(sql, param);
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
    public int delete(Long notificationId) {
        String sql = "DELETE FROM notifications WHERE notification_id = :notificationId";
        SqlParameterSource param = new MapSqlParameterSource("notificationId", notificationId);
        return template.update(sql, param);
    }

    /**
     * 알림 ID로 조회
     * 
     * 지정된 알림 ID로 알림을 조회합니다.
     * 
     * @param notificationId 조회할 알림 ID
     * @return 알림 엔티티 (Optional)
     */
    @Override
    public Optional<Notification> findById(Long notificationId) {
        String sql = "SELECT * FROM notifications WHERE notification_id = :notificationId";
        SqlParameterSource param = new MapSqlParameterSource("notificationId", notificationId);
        
        try {
            Notification notification = template.queryForObject(sql, param, notificationRowMapper);
            return Optional.ofNullable(notification);
        } catch (Exception e) {
            return Optional.empty();
        }
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
        String sql = """
            SELECT * FROM notifications 
            WHERE member_id = :memberId AND use_yn = 'Y'
            ORDER BY created_date DESC
            """;
        SqlParameterSource param = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, param, notificationRowMapper);
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
        String sql = """
            SELECT * FROM notifications 
            WHERE member_id = :memberId AND target_type = :targetType AND use_yn = 'Y'
            ORDER BY created_date DESC
            """;
        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("targetType", targetType);
        return template.query(sql, param, notificationRowMapper);
    }

    /**
     * 회원별 읽지 않은 알림 목록 조회
     * 
     * 지정된 회원의 읽지 않은 알림을 최신순으로 조회합니다.
     * 
     * @param memberId 회원 ID
     * @return 읽지 않은 알림 목록 (최신순)
     */
    @Override
    public List<Notification> findUnreadByMemberId(Long memberId) {
        String sql = """
            SELECT * FROM notifications 
            WHERE member_id = :memberId AND is_read = 'N' AND use_yn = 'Y'
            ORDER BY created_date DESC
            """;
        SqlParameterSource param = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, param, notificationRowMapper);
    }

    /**
     * 회원의 읽지 않은 알림 개수 조회
     * 
     * @param memberId 회원 ID
     * @return 읽지 않은 알림 개수
     */
    @Override
    public int countUnreadByMemberId(Long memberId) {
        String sql = """
            SELECT COUNT(*) FROM notifications 
            WHERE member_id = :memberId AND is_read = 'N' AND use_yn = 'Y'
            """;
        SqlParameterSource param = new MapSqlParameterSource("memberId", memberId);
        return template.queryForObject(sql, param, Integer.class);
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
    public int markAsRead(Long notificationId) {
        String sql = """
            UPDATE notifications SET 
                is_read = 'Y', 
                read_date = CURRENT_TIMESTAMP,
                udate = CURRENT_TIMESTAMP
            WHERE notification_id = :notificationId
            """;
        SqlParameterSource param = new MapSqlParameterSource("notificationId", notificationId);
        return template.update(sql, param);
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
    public int markAllAsRead(Long memberId) {
        String sql = """
            UPDATE notifications SET 
                is_read = 'Y', 
                read_date = CURRENT_TIMESTAMP,
                udate = CURRENT_TIMESTAMP
            WHERE member_id = :memberId AND is_read = 'N'
            """;
        SqlParameterSource param = new MapSqlParameterSource("memberId", memberId);
        return template.update(sql, param);
    }

    /**
     * 회원별 알림 DTO 목록 조회 (UI용)
     * 
     * @param memberId 회원 ID
     * @param limit 조회할 개수 (0이면 전체)
     * @return 알림 DTO 목록 (최신순)
     */
    @Override
    public List<NotificationDto> findNotificationDtosByMemberId(Long memberId, int limit) {
        String baseSql = """
            SELECT 
                n.notification_id,
                n.member_id,
                n.target_type,
                n.notification_type_id,
                n.title,
                n.message,
                n.target_url,
                n.target_id,
                CASE WHEN n.is_read = 'Y' THEN 1 ELSE 0 END as is_read,
                n.created_date,
                n.read_date,
                n.use_yn,
                c.decode as notificationTypeName
            FROM notifications n
            LEFT JOIN code c ON n.notification_type_id = c.code_id
            WHERE n.member_id = :memberId
              AND n.use_yn = 'Y'
            ORDER BY n.created_date DESC 
            """;
        
        String sql;
        MapSqlParameterSource param = new MapSqlParameterSource("memberId", memberId);
        
        if (limit > 0) {
            sql = baseSql + " FETCH FIRST :limit ROWS ONLY";
            param.addValue("limit", limit);
        } else {
            sql = baseSql;
        }
        
        return template.query(sql, param, notificationDtoRowMapper);
    }

    /**
     * 오래된 알림 삭제 (배치 작업용)
     * 
     * 지정된 일수보다 오래된 읽은 알림을 삭제합니다.
     * 배치 작업에서 사용됩니다.
     * 
     * @param days 삭제 기준 일수
     * @return 삭제된 행 수
     */
    @Override
    public int deleteOldNotifications(int days) {
        String sql = """
            DELETE FROM notifications 
            WHERE created_date < CURRENT_TIMESTAMP - INTERVAL ':days' DAY
            AND is_read = 'Y'
            """;
        SqlParameterSource param = new MapSqlParameterSource("days", days);
        return template.update(sql, param);
    }
} 