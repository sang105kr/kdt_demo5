package com.kh.demo.domain.chat.dao;

import com.kh.demo.domain.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final NamedParameterJdbcTemplate template;

    private final RowMapper<ChatMessage> chatMessageRowMapper = new RowMapper<ChatMessage>() {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatMessage message = new ChatMessage();
            message.setMessageId(rs.getLong("message_id"));
            message.setSessionId(rs.getLong("session_id"));
            message.setSenderId(rs.getLong("sender_id"));
            message.setSenderType(rs.getString("sender_type"));
            message.setMessageTypeId(rs.getLong("message_type_id"));
            message.setContent(rs.getString("content"));
            message.setIsRead(rs.getString("is_read"));
            message.setCdate(rs.getObject("cdate", LocalDateTime.class));
            return message;
        }
    };

    @Override
    @Transactional
    public Long saveMessage(ChatMessage message) {
        String sql = """
            INSERT INTO chat_message (
                message_id, session_id, sender_id, sender_type, message_type_id, 
                content, is_read, cdate
            ) VALUES (
                seq_chat_message_id.NEXTVAL, :sessionId, :senderId, :senderType, :messageTypeId,
                :content, :isRead, :cdate
            )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", message.getSessionId())
            .addValue("senderId", message.getSenderId())
            .addValue("senderType", message.getSenderType())
            .addValue("messageTypeId", message.getMessageTypeId())
            .addValue("content", message.getContent())
            .addValue("isRead", message.getIsRead() != null ? message.getIsRead() : "N")
            .addValue("cdate", LocalDateTime.now());

        template.update(sql, params);
        
        // 생성된 ID를 가져오기 위해 시퀀스의 현재 값 조회
        String selectIdSql = "SELECT seq_chat_message_id.CURRVAL FROM DUAL";
        return template.queryForObject(selectIdSql, new MapSqlParameterSource(), Long.class);
    }

    @Override
    public List<ChatMessage> findBySessionId(Long sessionId) {
        String sql = """
            SELECT message_id, session_id, sender_id, sender_type, 
                   message_type_id, content, is_read, cdate
            FROM chat_message 
            WHERE session_id = :sessionId
            ORDER BY cdate ASC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId);

        return template.query(sql, params, chatMessageRowMapper);
    }

    @Override
    public List<ChatMessage> findRecentMessages(Long sessionId, int limit) {
        String sql = """
            SELECT message_id, session_id, sender_id, sender_type, 
                   message_type_id, content, is_read, cdate
            FROM chat_message 
            WHERE session_id = :sessionId
            ORDER BY cdate DESC
            FETCH FIRST :limit ROWS ONLY
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("limit", limit);

        List<ChatMessage> messages = template.query(sql, params, chatMessageRowMapper);
        // 최신순으로 가져온 메시지를 시간순으로 정렬
        messages.sort((m1, m2) -> m1.getCdate().compareTo(m2.getCdate()));
        return messages;
    }

    @Override
    public List<ChatMessage> findBySenderId(Long senderId) {
        String sql = """
            SELECT message_id, session_id, sender_id, sender_type, 
                   message_type_id, content, is_read, cdate
            FROM chat_message 
            WHERE sender_id = :senderId
            ORDER BY cdate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("senderId", senderId);

        return template.query(sql, params, chatMessageRowMapper);
    }

    @Override
    public List<ChatMessage> findUnreadMessages(Long sessionId, Long receiverId) {
        String sql = """
            SELECT message_id, session_id, sender_id, sender_type, 
                   message_type_id, content, is_read, cdate
            FROM chat_message 
            WHERE session_id = :sessionId 
            AND sender_id != :receiverId 
            AND is_read = 'N'
            ORDER BY cdate ASC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("receiverId", receiverId);

        return template.query(sql, params, chatMessageRowMapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        String sql = """
            UPDATE chat_message 
            SET is_read = 'Y'
            WHERE message_id = :messageId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("messageId", messageId);

        template.update(sql, params);
    }

    @Override
    @Transactional
    public void markAsUnread(Long messageId, Long receiverId) {
        // 메시지 읽음 상태를 안읽음으로 설정하는 로직
        // 현재 구조에서는 메시지 송신 시 기본적으로 안읽음으로 저장되므로
        // 별도 처리 없이 로그만 남김
        log.info("메시지 안읽음 설정 호출: messageId={}, receiverId={}", messageId, receiverId);
        
        // 실제로는 메시지가 이미 'N'으로 저장되어 있으므로 추가 처리 불필요
        // 하지만 혹시 모르니 확인용 쿼리 실행
        String sql = """
            SELECT is_read FROM chat_message WHERE message_id = :messageId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("messageId", messageId);
        
        try {
            String isRead = template.queryForObject(sql, params, String.class);
            log.info("메시지 현재 읽음 상태: messageId={}, isRead={}", messageId, isRead);
        } catch (Exception e) {
            log.warn("메시지 읽음 상태 조회 실패: messageId={}", messageId, e);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long sessionId, Long receiverId) {
        String sql = """
            UPDATE chat_message 
            SET is_read = 'Y'
            WHERE session_id = :sessionId 
            AND sender_id != :receiverId 
            AND is_read = 'N'
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId)
            .addValue("receiverId", receiverId);

        template.update(sql, params);
    }

    @Override
    public Long getTotalMessageCount() {
        String sql = "SELECT COUNT(*) FROM chat_message";
        return template.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }
    
    @Override
    public Long getMessageCountBySessionId(Long sessionId) {
        String sql = "SELECT COUNT(*) FROM chat_message WHERE session_id = :sessionId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", sessionId);
        return template.queryForObject(sql, params, Long.class);
    }
    
    @Override
    public Map<Long, Long> getMessageCountBySessionIds(List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return new HashMap<>();
        }
        
        String sql = """
            SELECT session_id, COUNT(*) as message_count 
            FROM chat_message 
            WHERE session_id IN (:sessionIds)
            GROUP BY session_id
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionIds", sessionIds);
        
        return template.query(sql, params, (rs, rowNum) -> {
            Long sessionId = rs.getLong("session_id");
            Long messageCount = rs.getLong("message_count");
            return new AbstractMap.SimpleEntry<>(sessionId, messageCount);
        }).stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
    }
}
