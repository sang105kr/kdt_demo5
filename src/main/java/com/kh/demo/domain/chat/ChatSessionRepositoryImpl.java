package com.kh.demo.domain.chat;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final NamedParameterJdbcTemplate template;

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
            WHERE status_id = 1
            ORDER BY cdate ASC
            """;

        return template.query(sql, chatSessionRowMapper);
    }

    @Override
    @Transactional
    public void updateStatus(String sessionId, Long statusId) {
        String sql = """
            UPDATE chat_session 
            SET status_id = :statusId, udate = :udate
            WHERE session_id = :sessionId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("statusId", statusId)
            .addValue("udate", LocalDateTime.now());

        template.update(sql, params);
    }

    @Override
    @Transactional
    public void endSession(String sessionId) {
        String sql = """
            UPDATE chat_session 
            SET status_id = 3, end_time = :endTime, udate = :udate
            WHERE session_id = :sessionId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("endTime", LocalDateTime.now())
            .addValue("udate", LocalDateTime.now());

        template.update(sql, params);
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
}
