package com.kh.demo.domain.chat.svc;

import com.kh.demo.domain.chat.dao.ChatMessageRepository;
import com.kh.demo.domain.chat.dao.ChatSessionRepository;
import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.dto.ChatSessionDetailDto;
import com.kh.demo.domain.chat.dto.ChatSessionDto;
import com.kh.demo.domain.chat.dto.ChatSessionRequest;
import com.kh.demo.domain.chat.entity.ChatMessage;
import com.kh.demo.domain.chat.entity.ChatSession;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.svc.MemberSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSVCImpl implements ChatSVC {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final CodeSVC codeSVC;
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberSVC memberSVC;
    
    // 시스템 메시지 관련 상수
    private static final Long SYSTEM_USER_ID = 3L; // 관리자1 ID (시스템 메시지용)
    
    // 채팅 세션 상태 코드 ID (동적 조회)
    private Long waitingStatusId;
    private Long activeStatusId;
    private Long completedStatusId;
    private Long disconnectedStatusId;
    private Long systemMessageTypeId;
    private Long textMessageTypeId; // TEXT 메시지 타입 ID 추가
    
    /**
     * 초기화 - 코드 ID 조회 (캐시 활용)
     */
    @jakarta.annotation.PostConstruct
    @Override
    public void init() {
        log.info("채팅 서비스 초기화 시작");
        
        // CodeSVC 캐시에서 CHAT_SESSION_STATUS 그룹의 모든 코드를 한 번에 가져옴
        List<Code> chatStatusCodes = codeSVC.getCodeList("CHAT_SESSION_STATUS");
        List<Code> chatMessageTypeCodes = codeSVC.getCodeList("CHAT_MESSAGE_TYPE");
        
        if (chatStatusCodes == null || chatStatusCodes.isEmpty()) {
            log.warn("CHAT_SESSION_STATUS 캐시가 비어있음. 캐시 새로고침 시도...");
            codeSVC.refreshCache();
            chatStatusCodes = codeSVC.getCodeList("CHAT_SESSION_STATUS");
            chatMessageTypeCodes = codeSVC.getCodeList("CHAT_MESSAGE_TYPE");
        }
        
        log.info("CHAT_SESSION_STATUS 코드 개수: {}", chatStatusCodes != null ? chatStatusCodes.size() : 0);
        log.info("CHAT_MESSAGE_TYPE 코드 개수: {}", chatMessageTypeCodes != null ? chatMessageTypeCodes.size() : 0);
        
        // 캐시된 코드 리스트에서 직접 매핑 (캐시 활용)
        if (chatStatusCodes != null) {
            for (Code code : chatStatusCodes) {
                switch (code.getCode()) {
                    case "WAITING":
                        this.waitingStatusId = code.getCodeId();
                        break;
                    case "ACTIVE":
                        this.activeStatusId = code.getCodeId();
                        break;
                    case "COMPLETED":
                        this.completedStatusId = code.getCodeId();
                        break;
                    case "DISCONNECTED":
                        this.disconnectedStatusId = code.getCodeId();
                        break;
                }
            }
        }
        
        // CHAT_MESSAGE_TYPE 코드 매핑
        if (chatMessageTypeCodes != null) {
            for (Code code : chatMessageTypeCodes) {
                if ("SYSTEM".equals(code.getCode())) {
                    this.systemMessageTypeId = code.getCodeId();
                } else if ("TEXT".equals(code.getCode())) {
                    this.textMessageTypeId = code.getCodeId();
                }
            }
        }
        
        log.info("채팅 서비스 초기화 완료:");
        log.info("- 대기 상태 ID: {} (WAITING)", waitingStatusId);
        log.info("- 진행 상태 ID: {} (ACTIVE)", activeStatusId);
        log.info("- 완료 상태 ID: {} (COMPLETED)", completedStatusId);
        log.info("- 연결해제 상태 ID: {} (DISCONNECTED)", disconnectedStatusId);
        log.info("- 시스템 메시지 타입 ID: {} (SYSTEM)", systemMessageTypeId);
        log.info("- 텍스트 메시지 타입 ID: {} (TEXT)", textMessageTypeId);
    }

    /**
     * 채팅 세션 생성
     */
    @Transactional
    @Override
    public Long startChatSession(ChatSessionRequest request) {
        Long sessionId = generateSessionId();
        
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setMemberId(request.getMemberId() != null ? request.getMemberId() : 1L); // 임시로 1L 사용
        // categoryId는 필수이며 code_id(FAQ_CATEGORY) 여야 함
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리 ID(code_id)가 누락되었습니다.");
        }
        session.setCategoryId(request.getCategoryId());
        // 대기 상태 코드 보장 (초기 캐시 타이밍 이슈 대비)
        if (waitingStatusId == null) {
            waitingStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "WAITING");
            if (waitingStatusId == null) {
                throw new IllegalStateException("대기 상태 코드(WAITING)를 찾을 수 없습니다.");
            }
        }
        session.setStatusId(waitingStatusId); // 대기 상태
        // 카테고리명을 기반으로 제목 생성
        String categoryName = "일반 문의";
        if (session.getCategoryId() != null) {
            try {
                categoryName = codeSVC.getCodeDecode("FAQ_CATEGORY", session.getCategoryId());
            } catch (Exception e) {
                log.warn("카테고리명 조회 실패, 기본값 사용: categoryId={}", session.getCategoryId(), e);
            }
        }
        session.setTitle(request.getTitle() != null ? request.getTitle() : categoryName + " 상담");
        session.setStartTime(LocalDateTime.now());
        
        sessionRepository.createSession(session);
        
        // 새로운 세션 생성 시 관리자 대시보드에 WebSocket 알림 전송
        notifyNewSession(session);
        
        log.info("채팅 세션 생성: {}", sessionId);
        return sessionId;
    }

    /**
     * 새로운 세션 생성 시 관리자 대시보드에 알림
     */
    private void notifyNewSession(ChatSession session) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("sessionId", session.getSessionId());
            notification.put("memberId", session.getMemberId());
            notification.put("categoryId", session.getCategoryId());
            notification.put("title", session.getTitle());
            notification.put("startTime", session.getStartTime());
            notification.put("statusId", session.getStatusId());
            
            // 관리자 대시보드에 새로운 세션 알림 전송
            messagingTemplate.convertAndSend("/topic/chat/new-session", notification);
            
            log.info("새로운 세션 알림 전송: sessionId={}", session.getSessionId());
        } catch (Exception e) {
            log.error("새로운 세션 알림 전송 실패: sessionId={}", session.getSessionId(), e);
        }
    }
    
    /**
     * 메시지 전송 (DB 저장만 수행, WebSocket 전송은 클라이언트에서 처리)
     */
    @Transactional
    @Override
    public void sendMessage(ChatMessageDto messageDto) {
        log.info("=== 메시지 전송 시작 ===");
        log.info("입력 메시지: sessionId={}, senderId={}, senderType={}, content={}", 
                messageDto.getSessionId(), messageDto.getSenderId(), messageDto.getSenderType(), messageDto.getContent());
        
        // DB에 메시지 저장
        ChatMessage message = new ChatMessage();
        message.setSessionId(Long.valueOf(messageDto.getSessionId()));
        message.setSenderId(messageDto.getSenderId());
        message.setSenderType(messageDto.getSenderType());
        message.setContent(messageDto.getContent());
        
        // messageTypeId 설정 (클라이언트에서 전송한 값이 있으면 사용, 없으면 TEXT 타입 사용)
        Long messageTypeId = messageDto.getMessageTypeId();
        if (messageTypeId == null) {
            // TEXT 메시지 타입의 code_id 조회
            messageTypeId = codeSVC.getCodeId("CHAT_MESSAGE_TYPE", "TEXT");
            if (messageTypeId == null) {
                log.warn("TEXT 메시지 타입 코드를 찾을 수 없음. 기본값 1 사용");
                messageTypeId = 1L;
            }
        }
        message.setMessageTypeId(messageTypeId);
        message.setCdate(LocalDateTime.now());
        
        log.info("저장할 메시지: {}", message);
        
        Long messageId = messageRepository.saveMessage(message);
        
        log.info("메시지 저장 완료: messageId={}", messageId);
        
        // 메시지 송신 시 양쪽 모두 안읽음으로 설정
        setMessageAsUnread(messageDto.getSessionId(), messageId, messageDto.getSenderId());
        
        // 메시지 정보 추가 (클라이언트에서 사용할 수 있도록)
        messageDto.setMessageId(messageId);
        messageDto.setTimestamp(LocalDateTime.now());
        
        // 송신자 정보 설정
        if ("M".equals(messageDto.getSenderType())) {
            // 고객 메시지인 경우
            var member = memberSVC.findByMemberId(messageDto.getSenderId());
            if (member.isPresent()) {
                messageDto.setSenderName(member.get().getNickname() != null ? member.get().getNickname() : "고객");
            } else {
                messageDto.setSenderName("고객");
            }
        } else if ("A".equals(messageDto.getSenderType())) {
            // 관리자 메시지인 경우
            messageDto.setSenderName("상담원");
        }
        
        // WebSocket 브로드캐스트는 ChatWebSocketHandler에서 처리하므로 여기서는 제거
        // (중복 브로드캐스트 방지)
        
        log.info("메시지 전송 완료: sessionId={}, messageId={}, senderId={}, senderType={}, content={}, messageTypeId={}", 
                messageDto.getSessionId(), messageId, messageDto.getSenderId(), messageDto.getSenderType(), messageDto.getContent(), messageTypeId);
    }

    /**
     * 세션 메시지 목록 조회
     */
    @Override
    public List<ChatMessageDto> getSessionMessages(Long sessionId) {
        List<ChatMessage> messages = messageRepository.findBySessionId(sessionId);
        
        return messages.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 채팅 통계 조회
     */
    @Override
    public Map<String, Object> getChatStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 대기 중인 세션 수
        List<ChatSession> waitingSessions = sessionRepository.findWaitingSessions();
        int waitingCount = waitingSessions.size();
        stats.put("waiting", waitingCount);
        log.info("대기 중인 세션 수: {}", waitingCount);
        
        // 진행 중인 세션 수
        List<ChatSession> activeSessions = sessionRepository.findActiveSessions();
        int activeCount = activeSessions.size();
        stats.put("active", activeCount);
        log.info("진행 중인 세션 수: {}", activeCount);
        
        // 오늘 완료된 세션 수
        List<ChatSession> todayCompletedSessions = sessionRepository.findTodayCompletedSessions();
        int completedCount = todayCompletedSessions.size();
        stats.put("completed", completedCount);
        log.info("오늘 완료된 세션 수: {}", completedCount);
        
        // 총 메시지 수
        Long totalMessages = messageRepository.getTotalMessageCount();
        long totalCount = totalMessages != null ? totalMessages : 0L;
        stats.put("totalMessages", totalCount);
        log.info("총 메시지 수: {}", totalCount);
        
        log.info("채팅 통계 조회 완료: {}", stats);
        return stats;
    }

    /**
     * 대기 중인 채팅 세션 목록 조회
     */
    @Override
    public List<ChatSessionDto> getWaitingSessions() {
        List<ChatSession> sessions = sessionRepository.findWaitingSessions();
        
        return sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
    }

    /**
     * 진행중인 채팅 세션 목록 조회
     */
    @Override
    public List<ChatSessionDto> getActiveSessions() {
        List<ChatSession> sessions = sessionRepository.findActiveSessions();
        
        return sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
    }

    /**
     * 오늘 완료된 채팅 세션 목록 조회
     */
    @Override
    public List<ChatSessionDto> getCompletedSessions() {
        log.info("오늘 완료된 채팅 세션 목록 조회 시작");
        
        List<ChatSession> sessions = sessionRepository.findTodayCompletedSessions();
        log.info("데이터베이스에서 조회된 세션 수: {}", sessions.size());
        
        List<ChatSessionDto> sessionDtos = sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
        
        log.info("변환된 DTO 수: {}", sessionDtos.size());
        return sessionDtos;
    }

    /**
     * 채팅 세션 히스토리 조회
     */
    @Override
    public Map<String, Object> getSessionHistory(int page, int size, String dateFilter, String exitReasonFilter, String search) {
        Map<String, Object> result = sessionRepository.findSessionHistory(page, size, dateFilter, exitReasonFilter, search);
        
        // ChatSession을 ChatSessionDto로 변환
        @SuppressWarnings("unchecked")
        List<ChatSession> sessions = (List<ChatSession>) result.get("sessions");
        List<ChatSessionDto> sessionDtos = sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
        
        result.put("sessions", sessionDtos);
        return result;
    }

    /**
     * 채팅 세션 조회
     */
    @Override
    public Optional<ChatSessionDto> getSession(Long sessionId) {
        return sessionRepository.findBySessionId(sessionId)
            .map(this::convertToSessionDto);
    }

    /**
     * 채팅 세션 상세 정보 조회
     */
    @Override
    public Optional<ChatSessionDto> getSessionDetail(Long sessionId) {
        try {
            log.info("세션 상세 정보 조회 시작: sessionId={}", sessionId);
            
            // 상세 정보 조회 (JOIN 포함)
            Optional<ChatSessionDetailDto> detailOpt = sessionRepository.findDetailBySessionId(sessionId);
            if (detailOpt.isEmpty()) {
                log.warn("세션 상세 정보를 찾을 수 없음: sessionId={}", sessionId);
                return Optional.empty();
            }
            
            ChatSessionDetailDto detail = detailOpt.get();
            ChatSessionDto dto = convertDetailToSessionDto(detail);
            
            log.info("세션 상세 정보 조회 완료: sessionId={}, dto={}", sessionId, dto);
            return Optional.of(dto);
        } catch (Exception e) {
            log.error("세션 상세 정보 조회 실패: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 채팅 세션 상태 업데이트
     */
    @Transactional
    @Override
    public void updateSessionStatus(Long sessionId, Long statusId, Long adminId) {
        // ACTIVE 상태로 변경될 때 adminId도 함께 설정
        if (statusId.equals(activeStatusId) && adminId != null) {
            sessionRepository.updateStatusWithAdmin(sessionId, statusId, adminId);
        } else {
            sessionRepository.updateStatus(sessionId, statusId);
        }
        
        // ACTIVE 상태로 변경될 때 (상담원이 상담 시작) 고객 메시지들을 읽음 처리
        if (statusId.equals(activeStatusId) && adminId != null) {
            // 해당 세션의 모든 고객 메시지를 읽음 처리
            messageRepository.markAllAsRead(sessionId, adminId);
            
            // 읽음 이벤트를 WebSocket으로 전송하여 고객에게 알림
            try {
                            ChatMessageDto readEvent = new ChatMessageDto();
            readEvent.setSessionId(sessionId);
                readEvent.setSenderId(adminId);
                readEvent.setSenderType("A");
                readEvent.setContent("READ_EVENT");
                readEvent.setSenderName("상담원");
                readEvent.setTimestamp(LocalDateTime.now());
                readEvent.setMessageTypeId(systemMessageTypeId);
                
                messagingTemplate.convertAndSend("/topic/chat/" + sessionId, readEvent);
                log.info("읽음 이벤트 전송 완료: sessionId={}, adminId={}", sessionId, adminId);
            } catch (Exception e) {
                log.warn("읽음 이벤트 전송 실패: {}", e.getMessage());
            }
        }
        
        // 상태 변경 알림 메시지 전송 (시스템 메시지)
        String statusMessage = getStatusMessage(statusId);
        if (statusMessage != null) {
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSessionId(sessionId);
            systemMessage.setSenderId(SYSTEM_USER_ID);
            systemMessage.setSenderType("S");
            systemMessage.setContent(statusMessage);
            systemMessage.setSenderName("시스템");
            systemMessage.setTimestamp(LocalDateTime.now());
            systemMessage.setMessageTypeId(systemMessageTypeId);
            
            sendMessage(systemMessage);
        }
        
        // 세션 상태 변경을 대시보드에 WebSocket 알림 전송
        try {
            // 업데이트된 세션 정보 조회
            Optional<ChatSessionDto> updatedSession = getSession(sessionId);
            if (updatedSession.isPresent()) {
                Map<String, Object> sessionUpdate = new HashMap<>();
                sessionUpdate.put("sessionId", sessionId);
                sessionUpdate.put("statusId", statusId);
                sessionUpdate.put("statusName", getStatusName(statusId));
                sessionUpdate.put("adminId", adminId);
                sessionUpdate.put("updatedAt", LocalDateTime.now());
                sessionUpdate.put("session", updatedSession.get());
                
                // 대시보드에 세션 상태 변경 알림 전송
                messagingTemplate.convertAndSend("/topic/chat/sessions", sessionUpdate);
                log.info("세션 상태 변경 알림 전송: sessionId={}, statusId={}", sessionId, statusId);
            }
        } catch (Exception e) {
            log.error("세션 상태 변경 알림 전송 실패: sessionId={}", sessionId, e);
        }
    }

    /**
     * presence 업데이트 (이탈/재접속)
     */
    @Transactional
    @Override
    public void updatePresence(Long sessionId, String side, String state, String reason, Long graceSeconds) {
        // 업데이트 이전 상태를 확인해 복귀 메시지 오발송 방지
        Long prevStatusId = sessionRepository.findBySessionId(sessionId)
            .map(ChatSession::getStatusId)
            .orElse(null);

        LocalDateTime graceUntil = null;
        if ("INACTIVE".equalsIgnoreCase(state)) {
            long seconds = graceSeconds != null ? graceSeconds : 300L; // 기본 5분
            graceUntil = LocalDateTime.now().plusSeconds(seconds);
        }
        sessionRepository.updatePresence(sessionId, side, state, reason, graceUntil);

        // 상태 변경 시스템 메시지 (선택)
        String msg = null;
        if ("INACTIVE".equalsIgnoreCase(state)) {
            // 이전 상태가 DISCONNECTED가 아닐 때에만 일시 이탈 메시지 전송 (중복 방지)
            if (prevStatusId == null || !prevStatusId.equals(disconnectedStatusId)) {
                msg = "MEMBER".equalsIgnoreCase(side) ? "고객이 잠시 이탈했습니다." : "상담원이 잠시 이탈했습니다.";
            }
        } else if ("ACTIVE".equalsIgnoreCase(state)) {
            // 이전 상태가 DISCONNECTED일 때에만 복귀 메시지 전송
            if (prevStatusId != null && prevStatusId.equals(disconnectedStatusId)) {
                msg = "MEMBER".equalsIgnoreCase(side) ? "고객이 다시 접속했습니다." : "상담원이 다시 접속했습니다.";
            }
        }
        if (msg != null) {
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSessionId(sessionId);
            systemMessage.setSenderId(SYSTEM_USER_ID);
            systemMessage.setSenderType("S");
            systemMessage.setContent(msg);
            systemMessage.setSenderName("시스템");
            systemMessage.setTimestamp(LocalDateTime.now());
            systemMessage.setMessageTypeId(systemMessageTypeId);
            sendMessage(systemMessage);
        }
    }

    /**
     * 채팅 세션 종료 (종료 사유 포함)
     */
    @Transactional
    @Override
    public void endSessionWithReason(Long sessionId, Long exitReasonId, String endedBy) {
        log.info("채팅 세션 종료 (사유 포함): sessionId={}, exitReasonId={}, endedBy={}", sessionId, exitReasonId, endedBy);
        
        // 세션 존재 확인
        Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId);
        }
        
        ChatSession session = sessionOpt.get();
        if (session.getStatusId().equals(completedStatusId)) {
            throw new IllegalArgumentException("이미 종료된 세션입니다: " + sessionId);
        }
        
        // 세션 종료 처리
        sessionRepository.endSessionWithReason(sessionId, exitReasonId, endedBy);
        
        // 시스템 메시지 전송
        String exitReasonName = getExitReasonName(exitReasonId);
        String systemMessage = String.format("상담이 종료되었습니다. (종료 사유: %s)", exitReasonName);
        sendSystemMessage(sessionId, systemMessage);
        
        log.info("채팅 세션 종료 완료: sessionId={}", sessionId);
    }

    /**
     * 채팅 세션 이탈 처리
     */
    @Transactional
    @Override
    public void handleDisconnect(Long sessionId, Long disconnectReasonId, LocalDateTime graceUntil) {
        log.info("채팅 세션 이탈 처리: sessionId={}, disconnectReasonId={}, graceUntil={}", sessionId, disconnectReasonId, graceUntil);
        
        // 세션 존재 확인
        Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId);
        }
        
        // 이탈 처리
        sessionRepository.updateDisconnectReason(sessionId, disconnectReasonId, graceUntil);
        
        // 시스템 메시지 전송
        String disconnectReasonName = getDisconnectReasonName(disconnectReasonId);
        String systemMessage = String.format("고객이 일시적으로 이탈했습니다. (이탈 사유: %s)", disconnectReasonName);
        sendSystemMessage(sessionId, systemMessage);
        
        log.info("채팅 세션 이탈 처리 완료: sessionId={}", sessionId);
    }

    /**
     * 채팅 세션 재접속 처리
     */
    @Transactional
    @Override
    public void handleReconnect(Long sessionId) {
        log.info("채팅 세션 재접속 처리: sessionId={}", sessionId);
        
        // 세션 존재 확인
        Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId);
        }
        
        // 재접속 처리
        sessionRepository.updateReconnect(sessionId);
        
        // 시스템 메시지 전송
        sendSystemMessage(sessionId, "고객이 다시 접속했습니다.");
        
        log.info("채팅 세션 재접속 처리 완료: sessionId={}", sessionId);
    }

    /**
     * 마지막 접속 시간 업데이트
     */
    @Transactional
    @Override
    public void updateLastSeen(Long sessionId, String side) {
        sessionRepository.updateLastSeen(sessionId, side, LocalDateTime.now());
    }

    /**
     * 재개 가능한 세션 조회
     */
    @Override
    public List<ChatSessionDto> getResumableSessions(Long memberId) {
        List<ChatSession> sessions = sessionRepository.findResumableByMemberId(memberId, LocalDateTime.now());
        
        return sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
    }

    /**
     * 이탈 사유 코드 목록 조회
     */
    @Override
    public List<Code> getDisconnectReasons() {
        return codeSVC.getCodeList("CHAT_DISCONNECT_REASON");
    }

    /**
     * 종료 사유 코드 목록 조회
     */
    @Override
    public List<Code> getExitReasons() {
        List<Code> reasons = codeSVC.getCodeList("CHAT_EXIT_REASON");
        
        if (reasons == null || reasons.isEmpty()) {
            log.warn("CHAT_EXIT_REASON 캐시가 비어있음. 캐시 새로고침 시도...");
            codeSVC.refreshCache();
            reasons = codeSVC.getCodeList("CHAT_EXIT_REASON");
            
            if (reasons == null || reasons.isEmpty()) {
                log.error("CHAT_EXIT_REASON 코드를 찾을 수 없습니다. 데이터베이스 확인 필요.");
                // 빈 리스트 반환하여 NPE 방지
                return new ArrayList<>();
            }
        }
        
        log.info("CHAT_EXIT_REASON 코드 조회 완료: {} 개", reasons.size());
        return reasons;
    }

    /**
     * 이탈 사유명 조회
     */
    private String getDisconnectReasonName(Long disconnectReasonId) {
        if (disconnectReasonId == null) return "알 수 없음";
        
        return codeSVC.getCodeDecode("CHAT_DISCONNECT_REASON", disconnectReasonId);
    }

    /**
     * 종료 사유명 조회
     */
    private String getExitReasonName(Long exitReasonId) {
        if (exitReasonId == null) return "알 수 없음";
        
        return codeSVC.getCodeDecode("CHAT_EXIT_REASON", exitReasonId);
    }

    /**
     * 진행중 상태 ID 조회
     */
    @Override
    public Long getActiveStatusId() {
        return this.activeStatusId;
    }
    
    /**
     * 완료 상태 ID 조회
     */
    @Override
    public Long getCompletedStatusId() {
        return this.completedStatusId;
    }
    
    /**
     * 상태명으로 상태 ID 조회
     */
    @Override
    public Long getStatusIdByStatus(String status) {
        if ("WAITING".equalsIgnoreCase(status)) {
            return this.waitingStatusId;
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            return this.activeStatusId;
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            return this.completedStatusId;
        } else if ("DISCONNECTED".equalsIgnoreCase(status)) {
            return this.disconnectedStatusId;
        }
        throw new IllegalArgumentException("알 수 없는 상태명: " + status);
    }



    /**
     * 메시지 읽음 처리
     */
    @Transactional
    @Override
    public void markMessagesAsRead(Long sessionId, Long receiverId) {
        messageRepository.markAllAsRead(sessionId, receiverId);
    }

    /**
     * 세션 ID 생성 (Oracle Sequence 사용)
     */
    private Long generateSessionId() {
        // Oracle Sequence를 사용하여 숫자 ID 생성
        return sessionRepository.getNextSessionId();
    }

    /**
     * ChatMessage를 ChatMessageDto로 변환
     */
    private ChatMessageDto convertToDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setSessionId(message.getSessionId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderType(message.getSenderType());
        dto.setContent(message.getContent());
        dto.setMessageTypeId(message.getMessageTypeId());
        dto.setTimestamp(message.getCdate());
        dto.setIsRead(message.getIsRead());
        
        // 발신자 이름 설정 (실제로는 회원 정보에서 가져와야 함)
        if ("M".equals(message.getSenderType())) {
            dto.setSenderName("고객");
        } else if ("A".equals(message.getSenderType())) {
            // 시스템 메시지인지 확인 (message_type_id가 SYSTEM인 경우)
            if (message.getMessageTypeId() != null && message.getMessageTypeId().equals(systemMessageTypeId)) {
                dto.setSenderName("시스템");
            } else {
                dto.setSenderName("상담원");
            }
        } else {
            dto.setSenderName("시스템");
        }
        
        return dto;
    }

    /**
     * ChatSession을 ChatSessionDto로 변환
     */
    private ChatSessionDto convertToSessionDto(ChatSession session) {
        ChatSessionDto dto = new ChatSessionDto();
        dto.setSessionId(session.getSessionId()); // 이미 Long 타입
        dto.setMemberId(session.getMemberId());
        dto.setAdminId(session.getAdminId());
        dto.setCategoryId(session.getCategoryId());
        dto.setStatusId(session.getStatusId());
        dto.setTitle(session.getTitle());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setCdate(session.getCdate());

        // exit_reason_id 추가
        dto.setExitReasonId(session.getExitReasonId());
        log.info("세션 {}의 exit_reason_id: {}", session.getSessionId(), session.getExitReasonId());
        
        // 회원 정보 조회
        String memberName = "고객";
        String memberEmail = null;
        try {
            var member = memberSVC.findByMemberId(session.getMemberId());
            if (member.isPresent()) {
                memberName = member.get().getNickname() != null ? member.get().getNickname() : "고객";
                memberEmail = member.get().getEmail();
            }
        } catch (Exception e) {
            log.warn("회원 정보 조회 실패: memberId={}, error={}", session.getMemberId(), e.getMessage());
        }
        
        dto.setMemberName(memberName);
        dto.setMemberEmail(memberEmail);
        dto.setMemberPhone(null);
        dto.setAdminName("상담원");
        // 카테고리명을 code 테이블에서 조회
        dto.setCategoryName(codeSVC.getCodeDecode("FAQ_CATEGORY", session.getCategoryId()));
        dto.setStatusName(getStatusName(session.getStatusId()));
        
        return dto;
    }

    /**
     * ChatSessionDetailDto를 ChatSessionDto로 변환
     */
    private ChatSessionDto convertDetailToSessionDto(ChatSessionDetailDto detailDto) {
        ChatSessionDto dto = new ChatSessionDto();
        dto.setSessionId(detailDto.getSessionId());
        dto.setMemberId(detailDto.getMemberId());
        dto.setAdminId(detailDto.getAdminId());
        dto.setCategoryId(detailDto.getCategoryId());
        dto.setStatusId(detailDto.getStatusId());
        dto.setTitle(detailDto.getTitle());
        dto.setStartTime(detailDto.getStartTime());
        dto.setEndTime(detailDto.getEndTime());
        dto.setMessageCount(detailDto.getMessageCount());
        dto.setCdate(detailDto.getCdate());
        
        // exit_reason_id 추가 (ChatSessionDetailDto에 있다면)
        if (detailDto.getExitReasonId() != null) {
            dto.setExitReasonId(detailDto.getExitReasonId());
        }
        
        // 실제 회원 정보 설정
        dto.setMemberName(detailDto.getMemberName() != null ? detailDto.getMemberName() : "고객");
        dto.setMemberEmail(detailDto.getMemberEmail());
        dto.setMemberPhone(detailDto.getMemberPhone());
        dto.setMemberJoinDate(detailDto.getMemberJoinDate());
        dto.setAdminName(detailDto.getAdminName() != null ? detailDto.getAdminName() : "상담원");
        // 카테고리명을 code 테이블에서 조회 (detailDto에 없으면 기본값 사용)
        dto.setCategoryName(detailDto.getCategoryName() != null ? detailDto.getCategoryName() : 
                          codeSVC.getCodeDecode("FAQ_CATEGORY", detailDto.getCategoryId()));
        dto.setStatusName(getStatusName(detailDto.getStatusId()));
        
        return dto;
    }

    /**
     * 상태 메시지 반환
     */
    private String getStatusMessage(Long statusId) {
        if (statusId == null) return null;
        
        String statusName = codeSVC.getCodeDecode("CHAT_SESSION_STATUS", statusId);
        if (statusName == null) return null;
        
        switch (statusName) {
            case "대기중": return "상담 대기 중입니다.";
            case "진행중": return "상담이 시작되었습니다.";
            case "완료": return "상담이 종료되었습니다.";
            default: return null;
        }
    }

    /**
     * 상태 이름 반환
     */
    private String getStatusName(Long statusId) {
        if (statusId == null) return "알 수 없음";
        
        String statusName = codeSVC.getCodeDecode("CHAT_SESSION_STATUS", statusId);
        return statusName != null ? statusName : "알 수 없음";
    }

    /**
     * 메시지를 안읽음으로 설정 (양쪽 모두)
     */
    private void setMessageAsUnread(Long sessionId, Long messageId, Long senderId) {
        try {
            log.info("=== 메시지 안읽음 설정 시작 ===");
            log.info("입력 파라미터: sessionId={}, messageId={}, senderId={}", sessionId, messageId, senderId);
            
            // 세션 정보 조회
            Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isEmpty()) {
                log.warn("세션을 찾을 수 없습니다: sessionId={}", sessionId);
                return;
            }
            
            ChatSession session = sessionOpt.get();
            Long memberId = session.getMemberId();
            Long adminId = session.getAdminId();
            
            log.info("세션 정보: memberId={}, adminId={}", memberId, adminId);
            
            // 송신자가 고객인지 관리자인지 확인
            boolean isCustomerMessage = memberId.equals(senderId);
            
            log.info("메시지 타입: isCustomerMessage={}", isCustomerMessage);
            
            if (isCustomerMessage) {
                // 고객 메시지인 경우 - 관리자가 읽지 않음
                if (adminId != null) {
                    log.info("고객 메시지 - 관리자 안읽음 설정: messageId={}, adminId={}", messageId, adminId);
                    messageRepository.markAsUnread(messageId, adminId);
                }
            } else {
                // 관리자 메시지인 경우 - 고객이 읽지 않음
                log.info("관리자 메시지 - 고객 안읽음 설정: messageId={}, memberId={}", messageId, memberId);
                messageRepository.markAsUnread(messageId, memberId);
            }
            
            log.info("메시지 안읽음 설정 완료: messageId={}, senderId={}, sessionId={}", messageId, senderId, sessionId);
        } catch (Exception e) {
            log.error("메시지 안읽음 설정 실패: messageId={}, sessionId={}", messageId, sessionId, e);
        }
    }

    /**
     * 상담 종료
     */
    @Transactional
    @Override
    public void endChatSession(Long sessionId, String exitReason, String endedBy, Long memberId) {
        log.info("상담 종료 시작: sessionId={}, reason={}, endedBy={}, memberId={}", 
                sessionId, exitReason, endedBy, memberId);
        
        // 세션 조회
        Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId);
        }
        
        ChatSession session = sessionOpt.get();
        
        // 이미 종료된 세션인지 확인
        if (completedStatusId.equals(session.getStatusId())) {
            log.warn("이미 종료된 세션입니다: sessionId={}", sessionId);
            return;
        }
        
        // 종료 시간 설정
        session.setEndTime(LocalDateTime.now());
        session.setStatusId(completedStatusId);
        
        // 종료 사유 및 종료자 정보 저장 (추후 확장 가능)
        // TODO: 종료 사유를 별도 테이블에 저장하는 로직 추가
        
        // 세션 업데이트
        sessionRepository.updateStatus(sessionId, completedStatusId);
        
        // 시스템 메시지 전송
        String endMessage = getEndMessage(exitReason, endedBy);
        sendSystemMessage(sessionId, endMessage);
        
        // 관리자 대시보드에 WebSocket 알림 전송
        notifySessionEnd(session);
        
        log.info("상담 종료 완료: sessionId={}", sessionId);
    }
    
    /**
     * 시스템 메시지 전송
     */
    private void sendSystemMessage(Long sessionId, String content) {
        try {
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSessionId(sessionId);
            systemMessage.setSenderId(SYSTEM_USER_ID);
            systemMessage.setSenderType("S");
            systemMessage.setContent(content);
            systemMessage.setSenderName("시스템");
            systemMessage.setTimestamp(LocalDateTime.now());
            
            // 시스템 메시지 타입의 code_id 조회
            Long systemMessageTypeId = codeSVC.getCodeId("CHAT_MESSAGE_TYPE", "SYSTEM");
            if (systemMessageTypeId == null) {
                log.warn("SYSTEM 메시지 타입 코드를 찾을 수 없음. 기본값 4 사용");
                systemMessageTypeId = 4L;
            }
            systemMessage.setMessageTypeId(systemMessageTypeId);
            
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId, systemMessage);
            log.info("시스템 메시지 전송 완료: sessionId={}, content={}, messageTypeId={}", sessionId, content, systemMessageTypeId);
        } catch (Exception e) {
            log.error("시스템 메시지 전송 실패: sessionId={}", sessionId, e);
        }
    }
    
    /**
     * 종료 메시지 생성
     */
    private String getEndMessage(String exitReason, String endedBy) {
        String reasonText = getExitReasonText(exitReason);
        String enderText = getEndedByText(endedBy);
        
        return String.format("상담이 %s에 의해 종료되었습니다. (사유: %s)", enderText, reasonText);
    }
    
    /**
     * 종료자 텍스트 반환
     */
    private String getEndedByText(String endedBy) {
        if (endedBy == null) return "시스템";
        
        // endedBy가 코드값인지 확인하고 codeSVC에서 조회
        try {
            Long endedById = Long.valueOf(endedBy);
            return codeSVC.getCodeDecode("CHAT_ENDED_BY", endedById);
        } catch (NumberFormatException e) {
            // endedBy가 문자열인 경우 직접 매핑
            switch (endedBy) {
                case "MEMBER": return "고객";
                case "ADMIN": return "상담원";
                case "SYSTEM": return "시스템";
                default: return "시스템";
            }
        }
    }
    
    /**
     * 종료 사유 텍스트 반환
     */
    private String getExitReasonText(String exitReason) {
        if (exitReason == null) return "기타";
        
        // exitReason이 코드값인지 확인하고 codeSVC에서 조회
        try {
            Long exitReasonId = Long.valueOf(exitReason);
            return codeSVC.getCodeDecode("CHAT_EXIT_REASON", exitReasonId);
        } catch (NumberFormatException e) {
            // exitReason이 문자열인 경우 직접 매핑
            switch (exitReason) {
                case "SOLVED": return "해결됨";
                case "UNSATISFIED": return "불만족";
                case "OTHER_METHOD": return "다른 방법으로 문의";
                case "LATER": return "나중에 다시 문의";
                case "PHONE": return "전화 상담 희망";
                case "JUST_EXIT": return "그냥 종료";
                default: return "기타";
            }
        }
    }
    
    /**
     * 세션 종료 알림 전송
     */
    private void notifySessionEnd(ChatSession session) {
        try {
            ChatSessionDto sessionDto = convertToSessionDto(session);
            messagingTemplate.convertAndSend("/topic/chat/sessions", Map.of(
                "type", "SESSION_ENDED",
                "sessionId", session.getSessionId(),
                "session", sessionDto
            ));
            log.info("세션 종료 알림 전송 완료: sessionId={}", session.getSessionId());
        } catch (Exception e) {
            log.error("세션 종료 알림 전송 실패: sessionId={}", session.getSessionId(), e);
        }
    }
    
    @Override
    public Long getMessageCountBySessionId(Long sessionId) {
        return messageRepository.getMessageCountBySessionId(sessionId);
    }
    
    @Override
    public Map<Long, Long> getMessageCountBySessionIds(List<Long> sessionIds) {
        return messageRepository.getMessageCountBySessionIds(sessionIds);
    }
}
