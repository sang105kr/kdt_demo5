package com.kh.demo.domain.chat;

import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.dto.ChatSessionDto;
import com.kh.demo.domain.chat.dto.ChatSessionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    /**
     * 채팅 세션 생성
     */
    @Transactional
    public String startChatSession(ChatSessionRequest request) {
        String sessionId = generateSessionId();
        
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setMemberId(request.getMemberId());
        session.setCategoryId(request.getCategoryId());
        session.setStatusId(1L); // 대기 상태
        session.setTitle(request.getTitle());
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
     * 대기 중인 채팅 세션 목록 조회
     */
    public List<ChatSessionDto> getWaitingSessions() {
        List<ChatSession> sessions = sessionRepository.findWaitingSessions();
        
        return sessions.stream()
            .map(this::convertToSessionDto)
            .collect(Collectors.toList());
    }

    /**
     * 채팅 세션 조회
     */
    public Optional<ChatSessionDto> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
            .map(this::convertToSessionDto);
    }

    /**
     * 채팅 세션 상태 업데이트
     */
    @Transactional
    public void updateSessionStatus(String sessionId, Long statusId) {
        sessionRepository.updateStatus(sessionId, statusId);
        
        // 상태 변경 알림 메시지 전송
        String statusMessage = getStatusMessage(statusId);
        if (statusMessage != null) {
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSessionId(sessionId);
            systemMessage.setSenderId(0L); // 시스템
            systemMessage.setSenderType("S"); // 시스템
            systemMessage.setContent(statusMessage);
            systemMessage.setSenderName("시스템");
            systemMessage.setTimestamp(LocalDateTime.now());
            systemMessage.setMessageTypeId(2L); // 시스템 메시지
            
            sendMessage(systemMessage);
        }
    }

    /**
     * 채팅 세션 종료
     */
    @Transactional
    public void endSession(String sessionId) {
        sessionRepository.endSession(sessionId);
        
        // 종료 알림 메시지 전송
        ChatMessageDto endMessage = new ChatMessageDto();
        endMessage.setSessionId(sessionId);
        endMessage.setSenderId(0L);
        endMessage.setSenderType("S");
        endMessage.setContent("상담이 종료되었습니다. 감사합니다.");
        endMessage.setSenderName("시스템");
        endMessage.setTimestamp(LocalDateTime.now());
        endMessage.setMessageTypeId(2L);
        
        sendMessage(endMessage);
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
            dto.setSenderName("상담원");
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
        
        // 이름 설정 (실제로는 회원 정보에서 가져와야 함)
        dto.setMemberName("고객");
        dto.setAdminName(session.getAdminId() != null ? "상담원" : null);
        dto.setCategoryName("일반 문의");
        dto.setStatusName(getStatusName(session.getStatusId()));
        
        return dto;
    }

    /**
     * 상태 메시지 반환
     */
    private String getStatusMessage(Long statusId) {
        if (statusId == null) return null;
        
        switch (statusId.intValue()) {
            case 1: return "상담 대기 중입니다.";
            case 2: return "상담이 시작되었습니다.";
            case 3: return "상담이 종료되었습니다.";
            default: return null;
        }
    }

    /**
     * 상태 이름 반환
     */
    private String getStatusName(Long statusId) {
        if (statusId == null) return "알 수 없음";
        
        switch (statusId.intValue()) {
            case 1: return "대기";
            case 2: return "진행중";
            case 3: return "완료";
            default: return "알 수 없음";
        }
    }
}
