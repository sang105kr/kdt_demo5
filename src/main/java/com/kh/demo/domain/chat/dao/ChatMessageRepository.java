package com.kh.demo.domain.chat.dao;

import com.kh.demo.domain.chat.entity.ChatMessage;

import java.util.List;

/**
 * 채팅 메시지 Repository 인터페이스
 */
public interface ChatMessageRepository {
    
    /**
     * 메시지 저장
     */
    Long saveMessage(ChatMessage message);
    
    /**
     * 세션 ID로 메시지 목록 조회
     */
    List<ChatMessage> findBySessionId(Long sessionId);
    
    /**
     * 세션 ID로 최근 메시지 N개 조회
     */
    List<ChatMessage> findRecentMessages(Long sessionId, int limit);
    
    /**
     * 발신자 ID로 메시지 목록 조회
     */
    List<ChatMessage> findBySenderId(Long senderId);
    
    /**
     * 읽지 않은 메시지 목록 조회
     */
    List<ChatMessage> findUnreadMessages(Long sessionId, Long receiverId);
    
    /**
     * 메시지 읽음 처리
     */
    void markAsRead(Long messageId);
    
    /**
     * 메시지 안읽음 처리 (특정 사용자)
     */
    void markAsUnread(Long messageId, Long receiverId);
    
    /**
     * 세션의 모든 메시지 읽음 처리
     */
    void markAllAsRead(Long sessionId, Long receiverId);
    
    /**
     * 총 메시지 수 조회
     */
    Long getTotalMessageCount();
    
    /**
     * 세션별 메시지 수 조회
     */
    Long getMessageCountBySessionId(Long sessionId);
    
    /**
     * 여러 세션의 메시지 수 조회 (세션 ID 목록으로)
     */
    java.util.Map<Long, Long> getMessageCountBySessionIds(java.util.List<Long> sessionIds);
}
