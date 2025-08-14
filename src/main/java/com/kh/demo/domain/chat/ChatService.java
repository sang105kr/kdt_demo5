package com.kh.demo.domain.chat;

import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.dto.ChatSessionDto;
import com.kh.demo.domain.chat.dto.ChatSessionDetailDto;
import com.kh.demo.domain.chat.dto.ChatSessionRequest;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final CodeSVC codeSVC;
    
    // 시스템 메시지 관련 상수
    private static final Long SYSTEM_USER_ID = 3L; // 관리자1 ID (시스템 메시지용)
    
    // 채팅 세션 상태 코드 ID (동적 조회)
    private Long waitingStatusId;
    private Long activeStatusId;
    private Long completedStatusId;
    private Long systemMessageTypeId;
    
    /**
     * 초기화 - 코드 ID 조회
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        this.waitingStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "WAITING");
        this.activeStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "ACTIVE");
        this.completedStatusId = codeSVC.getCodeId("CHAT_SESSION_STATUS", "COMPLETED");
        this.systemMessageTypeId = codeSVC.getCodeId("CHAT_MESSAGE_TYPE", "SYSTEM");
        
        log.info("채팅 서비스 초기화 완료 - 대기: {}, 진행: {}, 완료: {}, 시스템메시지: {}", 
                waitingStatusId, activeStatusId, completedStatusId, systemMessageTypeId);
    }

    /**
     * 채팅 세션 생성
     */
    @Transactional
    public String startChatSession(ChatSessionRequest request) {
        String sessionId = generateSessionId();
        
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setMemberId(request.getMemberId() != null ? request.getMemberId() : 1L); // 임시로 1L 사용
        session.setCategoryId(request.getCategoryId() != null ? request.getCategoryId() : 1L);
        session.setStatusId(waitingStatusId); // 대기 상태
        session.setTitle(request.getTitle() != null ? request.getTitle() : "1:1 상담 문의");
        session.setStartTime(LocalDateTime.now());
        session.setMessageCount(0);
        
        sessionRepository.createSession(session);
        
        log.info("채팅 세션 생성: {}", sessionId);
        return sessionId;
    }

    /**
     * 메시지 전송
     */
    @Transactional
    public void sendMessage(ChatMessageDto messageDto) {
        // DB에 메시지 저장
        ChatMessage message = new ChatMessage();
        message.setSessionId(messageDto.getSessionId());
        message.setSenderId(messageDto.getSenderId());
        message.setSenderType(messageDto.getSenderType());
        message.setContent(messageDto.getContent());
        message.setMessageTypeId(messageDto.getMessageTypeId() != null ? messageDto.getMessageTypeId() : 1L);
        message.setCdate(LocalDateTime.now());
        
        Long messageId = messageRepository.saveMessage(message);
        
        // 세션의 메시지 수 업데이트
        updateSessionMessageCount(messageDto.getSessionId());
        
        // REST API 기반으로 메시지 저장만 수행
        // 실시간 전송은 클라이언트에서 폴링으로 처리
        
        log.info("메시지 전송: sessionId={}, senderId={}, content={}", 
                messageDto.getSessionId(), messageDto.getSenderId(), messageDto.getContent());
    }

    /**
     * 세션 메시지 목록 조회
     */
    public List<ChatMessageDto> getSessionMessages(String sessionId) {
        List<ChatMessage> messages = messageRepository.findBySessionId(sessionId);
        
        return messages.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 채팅 통계 조회
     */
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
    public List<ChatSessionDto> getWaitingSessions() {
        List<ChatSession> sessions = sessionRepository.findWaitingSessions();
        
        return sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
    }

    /**
     * 오늘 완료된 채팅 세션 목록 조회
     */
    public List<ChatSessionDto> getCompletedSessions() {
        List<ChatSession> sessions = sessionRepository.findTodayCompletedSessions();
        
        return sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
    }

    /**
     * 채팅 세션 히스토리 조회
     */
    public Map<String, Object> getSessionHistory(int page, int size, String dateFilter, String statusFilter, String search) {
        Map<String, Object> result = sessionRepository.findSessionHistory(page, size, dateFilter, statusFilter, search);
        
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
    public Optional<ChatSessionDto> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
            .map(this::convertToSessionDto);
    }

    /**
     * 채팅 세션 상세 정보 조회 (회원 정보 포함)
     */
    public Optional<ChatSessionDto> getSessionDetail(String sessionId) {
        try {
            // 먼저 기본 세션 정보 조회
            Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isEmpty()) {
                return Optional.empty();
            }
            
            ChatSession session = sessionOpt.get();
            ChatSessionDto dto = convertToSessionDto(session);
            
            // 회원 정보가 기본값이면 실제 데이터로 업데이트
            if ("고객".equals(dto.getMemberName()) || dto.getMemberName() == null) {
                // 실제 회원 정보 조회 (임시로 기본값 사용)
                dto.setMemberName("테스트 고객");
                dto.setMemberEmail("test@example.com");
                dto.setMemberPhone("010-1234-5678");
            }
            
            return Optional.of(dto);
        } catch (Exception e) {
            log.error("세션 상세 정보 조회 실패: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 채팅 세션 상태 업데이트
     */
    @Transactional
    public void updateSessionStatus(String sessionId, Long statusId) {
        sessionRepository.updateStatus(sessionId, statusId);
        
        // 상태 변경 알림 메시지 전송 (시스템 메시지)
        String statusMessage = getStatusMessage(statusId);
        if (statusMessage != null) {
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSessionId(sessionId);
            systemMessage.setSenderId(SYSTEM_USER_ID);
            systemMessage.setSenderType("A"); // 관리자 타입으로 처리
            systemMessage.setContent(statusMessage);
            systemMessage.setSenderName("시스템");
            systemMessage.setTimestamp(LocalDateTime.now());
            systemMessage.setMessageTypeId(systemMessageTypeId);
            
            sendMessage(systemMessage);
        }
    }

    /**
     * 채팅 세션 종료
     */
    @Transactional
    public void endSession(String sessionId) {
        try {
            sessionRepository.endSession(sessionId);
            
            // 종료 알림 메시지 전송 (시스템 메시지)
            ChatMessageDto endMessage = new ChatMessageDto();
            endMessage.setSessionId(sessionId);
            endMessage.setSenderId(SYSTEM_USER_ID);
            endMessage.setSenderType("A"); // 관리자 타입으로 처리
            endMessage.setContent("상담이 종료되었습니다. 감사합니다.");
            endMessage.setSenderName("시스템");
            endMessage.setTimestamp(LocalDateTime.now());
            endMessage.setMessageTypeId(systemMessageTypeId);
            
            sendMessage(endMessage);
            
        } catch (IllegalArgumentException e) {
            log.error("세션 종료 실패 - 잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("세션 종료 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("세션 종료에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessagesAsRead(String sessionId, Long receiverId) {
        messageRepository.markAllAsRead(sessionId, receiverId);
    }

    /**
     * 세션 ID 생성
     */
    private String generateSessionId() {
        return "CHAT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 세션 메시지 수 업데이트
     */
    private void updateSessionMessageCount(String sessionId) {
        List<ChatMessage> messages = messageRepository.findBySessionId(sessionId);
        sessionRepository.updateMessageCount(sessionId, messages.size());
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
        dto.setSessionId(session.getSessionId());
        dto.setMemberId(session.getMemberId());
        dto.setAdminId(session.getAdminId());
        dto.setCategoryId(session.getCategoryId());
        dto.setStatusId(session.getStatusId());
        dto.setTitle(session.getTitle());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setMessageCount(session.getMessageCount());
        dto.setCdate(session.getCdate());
        
        // 기본값 설정 (실제 회원 정보는 별도 조회 필요)
        dto.setMemberName("고객");
        dto.setMemberEmail(null);
        dto.setMemberPhone(null);
        dto.setAdminName("상담원");
        dto.setCategoryName("일반 문의");
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
        
        // 실제 회원 정보 설정
        dto.setMemberName(detailDto.getMemberName() != null ? detailDto.getMemberName() : "고객");
        dto.setMemberEmail(detailDto.getMemberEmail());
        dto.setMemberPhone(detailDto.getMemberPhone());
        dto.setAdminName(detailDto.getAdminName() != null ? detailDto.getAdminName() : "상담원");
        dto.setCategoryName(detailDto.getCategoryName() != null ? detailDto.getCategoryName() : "일반 문의");
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
}
