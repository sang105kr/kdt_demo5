package com.kh.demo.web.chat.controller.page;

import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.svc.ChatSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 채팅 메시지 핸들러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatSVC chatSVC;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송 처리
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessage, 
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("=== 채팅 메시지 수신 ===");
            log.info("메시지 내용: {}", chatMessage);
            log.info("세션 ID: {}", chatMessage.getSessionId());
            log.info("발신자 ID: {}", chatMessage.getSenderId());
            log.info("발신자 타입: {}", chatMessage.getSenderType());
            log.info("메시지 내용: {}", chatMessage.getContent());
            log.info("메시지 타입 ID: {}", chatMessage.getMessageTypeId());
            
            // sender_type 매핑 (클라이언트 -> 데이터베이스)
            String mappedSenderType = mapSenderType(chatMessage.getSenderType());
            chatMessage.setSenderType(mappedSenderType);
            log.info("매핑된 발신자 타입: {}", mappedSenderType);
            
            // 메시지를 데이터베이스에 저장
            log.info("데이터베이스에 메시지 저장 시도...");
            chatSVC.sendMessage(chatMessage);
            log.info("데이터베이스에 메시지 저장 완료");
            
            // 메시지를 해당 세션의 모든 참가자에게 브로드캐스트
            try {
                messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getSessionId(), chatMessage);
                log.info("메시지 브로드캐스트 완료: sessionId={}", chatMessage.getSessionId());
            } catch (Exception e) {
                log.error("메시지 브로드캐스트 실패: sessionId={}", chatMessage.getSessionId(), e);
            }
            
        } catch (Exception e) {
            log.error("채팅 메시지 처리 실패", e);
            throw e;
        }
    }

    /**
     * 읽음 이벤트 처리: 클라이언트가 화면상 실제 열람했음을 WebSocket으로 통지
     */
    @MessageMapping("/chat.read")
    public void read(@Payload ChatMessageDto chatMessage) {
        try {
            // 알림 메시지(상대가 읽음)를 브로드캐스트
            ChatMessageDto readEvent = new ChatMessageDto();
            readEvent.setSessionId(chatMessage.getSessionId());
            readEvent.setSenderId(chatMessage.getSenderId());
            readEvent.setSenderType(chatMessage.getSenderType());
            readEvent.setSenderName("시스템");
            readEvent.setContent("READ_EVENT");
            readEvent.setTimestamp(java.time.LocalDateTime.now());
            readEvent.setMessageTypeId(4L);
            readEvent.setIsRead("Y");
            messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getSessionId(), readEvent);
        } catch (Exception e) {
            log.error("읽음 이벤트 처리 실패", e);
        }
    }

    /**
     * 채팅 세션 참가 처리
     */
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDto chatMessage, 
                        SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("=== 채팅 세션 참가 요청 ===");
            log.info("참가 메시지: {}", chatMessage);
            log.info("세션 ID: {}", chatMessage.getSessionId());
            log.info("발신자 ID: {}", chatMessage.getSenderId());
            log.info("발신자 타입: {}", chatMessage.getSenderType());
            log.info("발신자 이름: {}", chatMessage.getSenderName());
            
            // sender_type 매핑 (클라이언트 -> 데이터베이스)
            String mappedSenderType = mapSenderType(chatMessage.getSenderType());
            chatMessage.setSenderType(mappedSenderType);
            log.info("매핑된 발신자 타입: {}", mappedSenderType);
            
            // WebSocket 세션에 사용자 정보 추가 (null 체크 추가)
            if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
                headerAccessor.getSessionAttributes().put("sessionId", chatMessage.getSessionId());
                headerAccessor.getSessionAttributes().put("senderId", chatMessage.getSenderId());
                headerAccessor.getSessionAttributes().put("senderName", chatMessage.getSenderName());
                log.info("WebSocket 세션에 사용자 정보 추가 완료");
            } else {
                log.warn("WebSocket 세션 속성을 설정할 수 없습니다. headerAccessor 또는 sessionAttributes가 null입니다.");
            }
            
            log.info("채팅 세션 참가: sessionId={}, senderId={}, senderName={}", 
                    chatMessage.getSessionId(), chatMessage.getSenderId(), chatMessage.getSenderName());
            
            // 시스템 메시지는 ChatService를 통해 처리 (중복 방지)
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSessionId(chatMessage.getSessionId());
            systemMessage.setSenderId(0L);
            systemMessage.setSenderType("S");
            systemMessage.setSenderName("시스템");
            systemMessage.setContent(chatMessage.getSenderName() + "님이 상담에 참가했습니다.");
            systemMessage.setTimestamp(java.time.LocalDateTime.now());
            systemMessage.setMessageTypeId(4L); // 시스템 메시지
            
            // ChatService를 통해 시스템 메시지 처리 (브로드캐스트 포함)
            chatSVC.sendMessage(systemMessage);
            
        } catch (Exception e) {
            log.error("채팅 세션 참가 처리 실패", e);
            throw e;
        }
    }

    /**
     * 채팅 세션 퇴장 처리
     */
    @MessageMapping("/chat.removeUser")
    public void removeUser(@Payload ChatMessageDto chatMessage, 
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("채팅 세션 퇴장: sessionId={}, senderId={}, senderName={}", 
                    chatMessage.getSessionId(), chatMessage.getSenderId(), chatMessage.getSenderName());
            
            // 시스템 메시지는 ChatService를 통해 처리 (중복 방지)
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSessionId(chatMessage.getSessionId());
            systemMessage.setSenderId(0L);
            systemMessage.setSenderType("S");
            systemMessage.setSenderName("시스템");
            systemMessage.setContent(chatMessage.getSenderName() + "님이 상담을 종료했습니다.");
            systemMessage.setTimestamp(java.time.LocalDateTime.now());
            systemMessage.setMessageTypeId(4L); // 시스템 메시지
            
            // ChatService를 통해 시스템 메시지 처리 (브로드캐스트 포함)
            chatSVC.sendMessage(systemMessage);
            
        } catch (Exception e) {
            log.error("채팅 세션 퇴장 처리 실패", e);
            throw e;
        }
    }

    /**
     * 관리자에게 새 채팅 세션 알림
     */
    public void notifyNewChatSession(String sessionId, String customerName) {
        try {
            ChatMessageDto notification = new ChatMessageDto();
            notification.setSessionId(Long.valueOf(sessionId));
            notification.setSenderId(0L);
            notification.setSenderType("S");
            notification.setSenderName("시스템");
            notification.setContent("새로운 상담 요청: " + customerName);
            notification.setTimestamp(java.time.LocalDateTime.now());
            notification.setMessageTypeId(4L);
            
            // 관리자 전용 토픽으로 알림 전송
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
            
            log.info("새 채팅 세션 알림 전송: sessionId={}, customerName={}", sessionId, customerName);
        } catch (Exception e) {
            log.error("새 채팅 세션 알림 전송 실패", e);
        }
    }

    /**
     * 특정 사용자에게 메시지 전송
     */
    public void sendToUser(String userId, ChatMessageDto message) {
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
            log.info("사용자별 메시지 전송: userId={}, messageId={}", userId, message.getSessionId());
        } catch (Exception e) {
            log.error("사용자별 메시지 전송 실패", e);
        }
    }
    
    /**
     * 발신자 타입 매핑 (클라이언트 -> 데이터베이스)
     */
    private String mapSenderType(String clientSenderType) {
        if (clientSenderType == null) {
            return "M"; // 기본값: 고객
        }
        
        return switch (clientSenderType.toUpperCase()) {
            case "CUSTOMER", "M" -> "M";  // 고객
            case "ADMIN", "A" -> "A";     // 관리자
            case "SYSTEM", "S" -> "S";    // 시스템
            default -> "M";               // 기본값: 고객
        };
    }
}
