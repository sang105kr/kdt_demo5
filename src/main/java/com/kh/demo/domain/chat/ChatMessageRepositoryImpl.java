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
            message.setSessionId(rs.getString("session_id"));
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
                session_id, sender_id, sender_type, message_type_id, 
                content, is_read, cdate
            ) VALUES (
                :sessionId, :senderId, :senderType, :messageTypeId,
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

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder);
        
        return keyHolder.getKey().longValue();
    }

    @Override
    public List<ChatMessage> findBySessionId(String sessionId) {
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
    public List<ChatMessage> findRecentMessages(String sessionId, int limit) {
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
    public List<ChatMessage> findUnreadMessages(String sessionId, Long receiverId) {
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
    public void markAllAsRead(String sessionId, Long receiverId) {
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
}
