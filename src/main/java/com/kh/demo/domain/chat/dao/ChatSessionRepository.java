package com.kh.demo.domain.chat.dao;

import com.kh.demo.domain.chat.dto.ChatSessionDetailDto;
import com.kh.demo.domain.chat.entity.ChatSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 채팅 세션 Repository 인터페이스
 */
public interface ChatSessionRepository {
    
    /**
     * 채팅 세션 생성
     */
    Long createSession(ChatSession session);
    
    /**
     * 세션 ID로 채팅 세션 조회
     */
    Optional<ChatSession> findBySessionId(Long sessionId);
    
    /**
     * 세션 ID로 채팅 세션 상세 정보 조회 (JOIN 포함)
     */
    Optional<ChatSessionDetailDto> findDetailBySessionId(Long sessionId);
    
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
    void updateStatus(Long sessionId, Long statusId);
    
    /**
     * 채팅 세션 상태 업데이트 (관리자 ID 포함)
     */
    void updateStatusWithAdmin(Long sessionId, Long statusId, Long adminId);
    
    /**
     * 채팅 세션 종료
     */
    void endSession(Long sessionId);
    
    /**
     * 메시지 수 업데이트
     */
    void updateMessageCount(Long sessionId, Integer messageCount);
    
    /**
     * 진행 중인 채팅 세션 목록 조회
     */
    List<ChatSession> findActiveSessions();
    
    /**
     * 오늘 완료된 채팅 세션 목록 조회
     */
    List<ChatSession> findTodayCompletedSessions();

    /**
     * 최근 완료된 채팅 세션 목록 조회 (최근 7일)
     */
    List<ChatSession> findRecentCompletedSessions();
    
    /**
     * 채팅 세션 히스토리 조회 (페이지네이션, 필터링 포함)
     */
    Map<String, Object> findSessionHistory(int page, int size, String dateFilter, String exitReasonFilter, String search);

        /**
     * presence 업데이트 (이탈/재접속 처리)
     */
    void updatePresence(Long sessionId, String side, String state, String reason, java.time.LocalDateTime graceUntil);
    
    /**
     * 재개 가능한 세션 조회 (유예시간 내 ACTIVE/DISCONNECTED)
     */
    List<ChatSession> findResumableByMemberId(Long memberId, java.time.LocalDateTime now);
    
    /**
     * 다음 세션 ID 조회 (Oracle Sequence 사용)
     */
    Long getNextSessionId();

    /**
     * 채팅 세션 이탈 처리
     */
    void updateDisconnectReason(Long sessionId, Long disconnectReasonId, LocalDateTime graceUntil);

    /**
     * 채팅 세션 종료 처리 (종료 사유 포함)
     */
    void endSessionWithReason(Long sessionId, Long exitReasonId, String endedBy);

    /**
     * 채팅 세션 재접속 처리
     */
    void updateReconnect(Long sessionId);

    /**
     * 마지막 접속 시간 업데이트
     */
    void updateLastSeen(Long sessionId, String side, LocalDateTime lastSeen);
}
