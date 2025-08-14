package com.kh.demo.domain.chat;

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
    List<ChatMessage> findBySessionId(String sessionId);
    
    /**
     * 세션 ID로 최근 메시지 N개 조회
     */
    List<ChatMessage> findRecentMessages(String sessionId, int limit);
    
    /**
     * 발신자 ID로 메시지 목록 조회
     */
    List<ChatMessage> findBySenderId(Long senderId);
    
    /**
     * 읽지 않은 메시지 목록 조회
     */
    List<ChatMessage> findUnreadMessages(String sessionId, Long receiverId);
    
    /**
     * 메시지 읽음 처리
     */
    void markAsRead(Long messageId);
    
    /**
     * 세션의 모든 메시지 읽음 처리
     */
    void markAllAsRead(String sessionId, Long receiverId);
    
    /**
     * 총 메시지 수 조회
     */
    Long getTotalMessageCount();
}
