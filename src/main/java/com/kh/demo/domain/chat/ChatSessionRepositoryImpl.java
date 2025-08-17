package com.kh.demo.domain.chat;

import com.kh.demo.domain.chat.dto.ChatSessionDetailDto;
import com.kh.demo.domain.chat.ChatSession;
import com.kh.demo.domain.chat.ChatMessage;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final NamedParameterJdbcTemplate template;
    private final CodeSVC codeSVC;
    
    // 채팅 세션 상태 코드 ID (동적 조회)
    private Long waitingStatusId;
    private Long activeStatusId;
    private Long completedStatusId;
    private Long disconnectedStatusId;
    
    /**
     * 초기화 - 코드 ID 조회
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        this.waitingStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "WAITING");
        this.activeStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "ACTIVE");
        this.completedStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "COMPLETED");
        this.disconnectedStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "DISCONNECTED");
        
        log.info("채팅 세션 Repository 초기화 완료 - 대기: {}, 진행: {}, 완료: {}", 
                waitingStatusId, activeStatusId, completedStatusId);
    }

    private final RowMapper<ChatSession> chatSessionRowMapper = new RowMapper<ChatSession>() {
        @Override
        public ChatSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatSession session = new ChatSession();
            session.setSessionId(rs.getString("session_id"));
            session.setMemberId(rs.getLong("member_id"));
            session.setAdminId(rs.getObject("admin_id", Long.class));
            session.setCategoryId(rs.getLong("category_id"));
            session.setStatusId(rs.getLong("status_id"));
            session.setTitle(rs.getString("title"));
            session.setStartTime(rs.getObject("start_time", LocalDateTime.class));
            session.setEndTime(rs.getObject("end_time", LocalDateTime.class));
            session.setMessageCount(rs.getInt("message_count"));
            session.setCdate(rs.getObject("cdate", LocalDateTime.class));
            session.setUdate(rs.getObject("udate", LocalDateTime.class));
            return session;
        }
    };

    private final RowMapper<ChatSessionDetailDto> chatSessionDetailRowMapper = new RowMapper<ChatSessionDetailDto>() {
        @Override
        public ChatSessionDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatSessionDetailDto dto = new ChatSessionDetailDto();
            dto.setSessionId(rs.getString("session_id"));
            dto.setMemberId(rs.getLong("member_id"));
            dto.setAdminId(rs.getObject("admin_id", Long.class));
            dto.setCategoryId(rs.getLong("category_id"));
            dto.setStatusId(rs.getLong("status_id"));
            dto.setTitle(rs.getString("title"));
            dto.setStartTime(rs.getObject("start_time", LocalDateTime.class));
            dto.setEndTime(rs.getObject("end_time", LocalDateTime.class));
            dto.setMessageCount(rs.getInt("message_count"));
            dto.setCdate(rs.getObject("cdate", LocalDateTime.class));
            dto.setUdate(rs.getObject("udate", LocalDateTime.class));
            
            // JOIN으로 가져온 회원 정보
            dto.setMemberName(rs.getString("member_name"));
            dto.setMemberEmail(rs.getString("member_email"));
            dto.setMemberPhone(rs.getString("member_phone"));
            dto.setAdminName(rs.getString("admin_name"));
            dto.setCategoryName(rs.getString("category_name"));
            
            return dto;
        }
    };

    @Override
    @Transactional
    public String createSession(ChatSession session) {
        String sql = """
            INSERT INTO chat_session (
                session_id, member_id, category_id, status_id, title, 
                start_time, message_count, cdate, udate
            ) VALUES (
                :sessionId, :memberId, :categoryId, :statusId, :title,
                :startTime, :messageCount, :cdate, :udate
            )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", session.getSessionId())
            .addValue("memberId", session.getMemberId())
            .addValue("categoryId", session.getCategoryId())
            .addValue("statusId", session.getStatusId())
            .addValue("title", session.getTitle())
            .addValue("startTime", session.getStartTime())
            .addValue("messageCount", session.getMessageCount() != null ? session.getMessageCount() : 0)
            .addValue("cdate", LocalDateTime.now())
            .addValue("udate", LocalDateTime.now());

        template.update(sql, params);
        return session.getSessionId();
    }

    @Override
    public Optional<ChatSession> findBySessionId(String sessionId) {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate
            FROM chat_session 
            WHERE session_id = :sessionId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId);

        List<ChatSession> results = template.query(sql, params, chatSessionRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    /**
     * 세션 ID로 채팅 세션 상세 정보 조회 (JOIN 포함)
     */
    public Optional<ChatSessionDetailDto> findDetailBySessionId(String sessionId) {
                    String sql = """
                SELECT cs.session_id, cs.member_id, cs.admin_id, cs.category_id, cs.status_id, 
                       cs.title, cs.start_time, cs.end_time, cs.message_count, cs.cdate, cs.udate,
                       m.nickname as member_name, m.email as member_email, m.tel as member_phone,
                       a.nickname as admin_name,
                       c.decode as category_name
                FROM chat_session cs
                LEFT JOIN member m ON cs.member_id = m.member_id
                LEFT JOIN member a ON cs.admin_id = a.member_id
                LEFT JOIN code c ON cs.category_id = c.code_id
                WHERE cs.session_id = :sessionId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId);

        List<ChatSessionDetailDto> results = template.query(sql, params, chatSessionDetailRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<ChatSession> findByMemberId(Long memberId) {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate
            FROM chat_session 
            WHERE member_id = :memberId
            ORDER BY cdate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("memberId", memberId);

        return template.query(sql, params, chatSessionRowMapper);
    }

    @Override
    public List<ChatSession> findByAdminId(Long adminId) {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate
            FROM chat_session 
            WHERE admin_id = :adminId
            ORDER BY cdate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("adminId", adminId);

        return template.query(sql, params, chatSessionRowMapper);
    }

    @Override
    public List<ChatSession> findByStatusId(Long statusId) {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate
            FROM chat_session 
            WHERE status_id = :statusId
            ORDER BY cdate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("statusId", statusId);

        return template.query(sql, params, chatSessionRowMapper);
    }

    @Override
    public List<ChatSession> findWaitingSessions() {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate
            FROM chat_session 
            WHERE status_id = :waitingStatusId
            ORDER BY cdate ASC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("waitingStatusId", waitingStatusId);

        return template.query(sql, params, chatSessionRowMapper);
    }

    @Override
    @Transactional
    public void updateStatus(String sessionId, Long statusId) {
        StringBuilder sql = new StringBuilder("UPDATE chat_session SET status_id = :statusId, udate = :udate");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("statusId", statusId)
            .addValue("udate", LocalDateTime.now());
        
        // ACTIVE 상태로 변경될 때 adminId도 설정
        if (statusId.equals(activeStatusId)) {
            // 현재 로그인한 관리자 ID를 가져와야 함
            // 임시로 세션에서 관리자 ID를 받도록 수정
            sql.append(", admin_id = :adminId");
            // 실제로는 세션에서 관리자 ID를 가져와야 함
            // 현재는 임시로 3번(admin1) 사용
            params.addValue("adminId", 3L); // TODO: 실제 로그인한 관리자 ID로 변경
        }
        
        sql.append(" WHERE session_id = :sessionId");
        template.update(sql.toString(), params);
    }
    
    @Override
    @Transactional
    public void updateStatusWithAdmin(String sessionId, Long statusId, Long adminId) {
        String sql = """
            UPDATE chat_session 
            SET status_id = :statusId, admin_id = :adminId, udate = :udate
            WHERE session_id = :sessionId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("statusId", statusId)
            .addValue("adminId", adminId)
            .addValue("udate", LocalDateTime.now());

        template.update(sql, params);
    }

    @Override
    @Transactional
    public void updatePresence(String sessionId, String side, String state, String reason, LocalDateTime graceUntil) {
        StringBuilder sb = new StringBuilder("UPDATE chat_session SET ");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("udate", LocalDateTime.now())
            .addValue("reason", reason)
            .addValue("graceUntil", graceUntil);

        if ("MEMBER".equalsIgnoreCase(side)) {
            sb.append("member_last_seen = :lastSeen, ");
            params.addValue("lastSeen", LocalDateTime.now());
        } else if ("ADMIN".equalsIgnoreCase(side)) {
            sb.append("admin_last_seen = :lastSeen, ");
            params.addValue("lastSeen", LocalDateTime.now());
        }

        if ("INACTIVE".equalsIgnoreCase(state)) {
            sb.append("status_id = :statusId, disconnect_reason = :reason, grace_until = :graceUntil, ");
            params.addValue("statusId", disconnectedStatusId);
        } else if ("ACTIVE".equalsIgnoreCase(state)) {
            // 현재 상태 조회 후, DISCONNECTED 였다면 ACTIVE로 복귀
            String statusSql = "SELECT status_id FROM chat_session WHERE session_id = :sessionId";
            Long currentStatus = template.queryForObject(statusSql, new MapSqlParameterSource().addValue("sessionId", sessionId), Long.class);
            if (currentStatus != null && currentStatus.equals(disconnectedStatusId)) {
                sb.append("status_id = :statusId, ");
                params.addValue("statusId", activeStatusId);
            }
            // 공통 정리 필드
            sb.append("disconnect_reason = NULL, grace_until = NULL, ");
        }

        sb.append("udate = :udate WHERE session_id = :sessionId");
        template.update(sb.toString(), params);
    }

    @Override
    public List<ChatSession> findResumableByMemberId(Long memberId, LocalDateTime now) {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate,
                   member_last_seen, admin_last_seen, disconnect_reason, grace_until
            FROM chat_session 
            WHERE member_id = :memberId
              AND status_id IN (:activeStatusId, :disconnectedStatusId)
              AND (grace_until IS NULL OR grace_until >= :now)
            ORDER BY udate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("activeStatusId", activeStatusId)
            .addValue("disconnectedStatusId", disconnectedStatusId)
            .addValue("now", now);

        return template.query(sql, params, chatSessionRowMapper);
    }

    @Override
    @Transactional
    public void endSession(String sessionId) {
        // 먼저 세션이 존재하는지 확인
        String checkSql = """
            SELECT COUNT(*) FROM chat_session WHERE session_id = :sessionId
            """;
        
        MapSqlParameterSource checkParams = new MapSqlParameterSource()
            .addValue("sessionId", sessionId);
        
        int count = template.queryForObject(checkSql, checkParams, Integer.class);
        
        if (count == 0) {
            throw new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId);
        }
        
        // 세션 상태 확인
        String statusSql = """
            SELECT status_id FROM chat_session WHERE session_id = :sessionId
            """;
        
        Long currentStatus = template.queryForObject(statusSql, checkParams, Long.class);
        
        if (currentStatus != null && currentStatus.equals(completedStatusId)) {
            throw new IllegalArgumentException("이미 종료된 세션입니다: " + sessionId);
        }
        
        // 세션 종료
        String updateSql = """
            UPDATE chat_session 
            SET status_id = :completedStatusId, end_time = :endTime, udate = :udate
            WHERE session_id = :sessionId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("completedStatusId", completedStatusId)
            .addValue("endTime", LocalDateTime.now())
            .addValue("udate", LocalDateTime.now());

        int updatedRows = template.update(updateSql, params);
        
        if (updatedRows == 0) {
            throw new RuntimeException("세션 종료에 실패했습니다: " + sessionId);
        }
    }

    @Override
    @Transactional
    public void updateMessageCount(String sessionId, Integer messageCount) {
        String sql = """
            UPDATE chat_session 
            SET message_count = :messageCount, udate = :udate
            WHERE session_id = :sessionId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("messageCount", messageCount)
            .addValue("udate", LocalDateTime.now());

        template.update(sql, params);
    }

    @Override
    public List<ChatSession> findActiveSessions() {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate
            FROM chat_session 
            WHERE status_id IN (:activeStatusId, :disconnectedStatusId)
              AND status_id != :completedStatusId
            ORDER BY cdate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("activeStatusId", activeStatusId)
            .addValue("disconnectedStatusId", disconnectedStatusId)
            .addValue("completedStatusId", completedStatusId);

        return template.query(sql, params, chatSessionRowMapper);
    }

    @Override
    public List<ChatSession> findTodayCompletedSessions() {
        String sql = """
            SELECT session_id, member_id, admin_id, category_id, status_id, 
                   title, start_time, end_time, message_count, cdate, udate
            FROM chat_session 
            WHERE status_id = :completedStatusId 
            AND TRUNC(end_time) = TRUNC(SYSDATE)
            ORDER BY end_time DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("completedStatusId", completedStatusId);

        return template.query(sql, params, chatSessionRowMapper);
    }

    @Override
    public Map<String, Object> findSessionHistory(int page, int size, String dateFilter, String statusFilter, String search) {
        // 기본 WHERE 조건
        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        
        // 날짜 필터
        if ("today".equals(dateFilter)) {
            whereClause.append(" AND TRUNC(cs.end_time) = TRUNC(SYSDATE)");
        } else if ("yesterday".equals(dateFilter)) {
            whereClause.append(" AND TRUNC(cs.end_time) = TRUNC(SYSDATE - 1)");
        } else if ("week".equals(dateFilter)) {
            whereClause.append(" AND cs.end_time >= TRUNC(SYSDATE, 'IW')");
        } else if ("month".equals(dateFilter)) {
            whereClause.append(" AND cs.end_time >= TRUNC(SYSDATE, 'MM')");
        }
        // "all"인 경우 날짜 조건 추가 안함
        
        // 상태 필터
        if ("completed".equals(statusFilter)) {
            whereClause.append(" AND cs.status_id = :completedStatusId");
            params.addValue("completedStatusId", completedStatusId);
        } else if ("cancelled".equals(statusFilter)) {
            // 취소 상태 코드가 있다면 추가 (현재는 없으므로 주석 처리)
            // whereClause.append(" AND cs.status_id = :cancelledStatusId");
            // params.addValue("cancelledStatusId", cancelledStatusId);
        }
        
        // 검색 조건
        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (m.nickname LIKE :search OR cs.title LIKE :search)");
            params.addValue("search", "%" + search.trim() + "%");
        }
        
        // 전체 개수 조회
        String countSql = """
            SELECT COUNT(*) 
            FROM chat_session cs
            LEFT JOIN member m ON cs.member_id = m.member_id
            """ + whereClause.toString();
        
        int totalCount = template.queryForObject(countSql, params, Integer.class);
        
        // 페이지네이션 계산
        int offset = (page - 1) * size;
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        // 데이터 조회
        String dataSql = """
            SELECT cs.session_id, cs.member_id, cs.admin_id, cs.category_id, cs.status_id, 
                   cs.title, cs.start_time, cs.end_time, cs.message_count, cs.cdate, cs.udate
            FROM chat_session cs
            LEFT JOIN member m ON cs.member_id = m.member_id
            """ + whereClause.toString() + """
            ORDER BY cs.end_time DESC
            OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
            """;
        
        params.addValue("offset", offset);
        params.addValue("size", size);
        
        List<ChatSession> sessions = template.query(dataSql, params, chatSessionRowMapper);
        
        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("sessions", sessions);
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        result.put("pageSize", size);
        
        return result;
    }
}
