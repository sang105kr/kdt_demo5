package com.kh.demo.domain.chat.svc;

import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.dto.ChatSessionDto;
import com.kh.demo.domain.chat.dto.ChatSessionRequest;
import com.kh.demo.domain.common.entity.Code;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChatSVC {
  @jakarta.annotation.PostConstruct
  void init();

  @Transactional
  Long startChatSession(ChatSessionRequest request);

  @Transactional
  void sendMessage(ChatMessageDto messageDto);

  List<ChatMessageDto> getSessionMessages(Long sessionId);

  Map<String, Object> getChatStatistics();

  List<ChatSessionDto> getWaitingSessions();

  List<ChatSessionDto> getActiveSessions();

  List<ChatSessionDto> getCompletedSessions();

  Map<String, Object> getSessionHistory(int page, int size, String dateFilter, String exitReasonFilter, String search);

  Optional<ChatSessionDto> getSession(Long sessionId);

  Optional<ChatSessionDto> getSessionDetail(Long sessionId);

  @Transactional
  void updateSessionStatus(Long sessionId, Long statusId, Long adminId);

  @Transactional
  void updatePresence(Long sessionId, String side, String state, String reason, Long graceSeconds);

  @Transactional
  void endSessionWithReason(Long sessionId, Long exitReasonId, String endedBy);

  /**
   * 채팅 세션 종료 (이탈 사유 포함)
   */
  void handleDisconnect(Long sessionId, Long disconnectReasonId, LocalDateTime graceUntil);

  /**
   * 채팅 세션 재접속 처리
   */
  void handleReconnect(Long sessionId);

  @Transactional
  void updateLastSeen(Long sessionId, String side);

  List<ChatSessionDto> getResumableSessions(Long memberId);

  List<Code> getDisconnectReasons();

  List<Code> getExitReasons();

  Long getActiveStatusId();

  Long getCompletedStatusId();

  Long getStatusIdByStatus(String status);

  @Transactional
  void markMessagesAsRead(Long sessionId, Long receiverId);

  @Transactional
  void endChatSession(Long sessionId, String exitReason, String endedBy, Long memberId);
  
  /**
   * 세션별 메시지 수 조회
   */
  Long getMessageCountBySessionId(Long sessionId);
  
  /**
   * 여러 세션의 메시지 수 조회
   */
  Map<Long, Long> getMessageCountBySessionIds(List<Long> sessionIds);
}
