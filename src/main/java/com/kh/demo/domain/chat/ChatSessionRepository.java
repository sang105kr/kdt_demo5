package com.kh.demo.domain.chat;

import java.util.List;
import java.util.Optional;

/**
 * 채팅 세션 Repository 인터페이스
 */
public interface ChatSessionRepository {
    
    /**
     * 채팅 세션 생성
     */
    String createSession(ChatSession session);
    
    /**
     * 세션 ID로 채팅 세션 조회
     */
    Optional<ChatSession> findBySessionId(String sessionId);
    
    /**
     * 회원 ID로 채팅 세션 목록 조회
     */
    List<ChatSession> findByMemberId(Long memberId);
    
    /**
     * 관리자 ID로 채팅 세션 목록 조회
     */
    List<ChatSession> findByAdminId(Long adminId);
    
    /**
     * 상태별 채팅 세션 목록 조회
     */
    List<ChatSession> findByStatusId(Long statusId);
    
    /**
     * 대기 중인 채팅 세션 목록 조회
     */
    List<ChatSession> findWaitingSessions();
    
    /**
     * 채팅 세션 상태 업데이트
     */
    void updateStatus(String sessionId, Long statusId);
    
    /**
     * 채팅 세션 종료
     */
    void endSession(String sessionId);
    
    /**
     * 메시지 수 업데이트
     */
    void updateMessageCount(String sessionId, Integer messageCount);
}
