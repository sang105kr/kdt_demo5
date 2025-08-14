package com.kh.demo.web.chat;

import com.kh.demo.domain.chat.ChatService;
import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.dto.ChatSessionDto;
import com.kh.demo.domain.chat.dto.ChatSessionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 채팅 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatService chatService;

    /**
     * 채팅 세션 생성
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody ChatSessionRequest request) {
        try {
            String sessionId = chatService.startChatSession(request);
            
            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "message", "채팅 세션이 생성되었습니다."
            ));
        } catch (Exception e) {
            log.error("채팅 세션 생성 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "채팅 세션 생성에 실패했습니다."
            ));
        }
    }

    /**
     * 채팅 세션 조회
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        try {
            var session = chatService.getSessionDetail(sessionId);
            
            if (session.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "code", "00",
                    "message", "채팅 세션을 조회했습니다.",
                    "data", session.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("채팅 세션 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "채팅 세션 조회에 실패했습니다."
            ));
        }
    }

    /**
     * 채팅 세션 메시지 목록 조회
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Map<String, Object>> getSessionMessages(@PathVariable String sessionId) {
        try {
            List<ChatMessageDto> messages = chatService.getSessionMessages(sessionId);
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "메시지 목록을 조회했습니다.",
                "data", messages
            ));
        } catch (Exception e) {
            log.error("메시지 목록 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "메시지 목록 조회에 실패했습니다."
            ));
        }
    }

    /**
     * 채팅 통계 조회 (관리자용)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getChatStatistics() {
        try {
            Map<String, Object> stats = chatService.getChatStatistics();
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "채팅 통계를 조회했습니다.",
                "data", stats
            ));
        } catch (Exception e) {
            log.error("채팅 통계 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "채팅 통계 조회에 실패했습니다."
            ));
        }
    }

    /**
     * 대기 중인 채팅 세션 목록 조회 (관리자용)
     */
    @GetMapping("/sessions/waiting")
    public ResponseEntity<Map<String, Object>> getWaitingSessions() {
        try {
            List<ChatSessionDto> sessions = chatService.getWaitingSessions();
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "대기 중인 채팅 세션 목록을 조회했습니다.",
                "data", sessions
            ));
        } catch (Exception e) {
            log.error("대기 중인 채팅 세션 목록 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "대기 중인 채팅 세션 목록 조회에 실패했습니다."
            ));
        }
    }

    /**
     * 오늘 완료된 채팅 세션 목록 조회 (관리자용)
     */
    @GetMapping("/sessions/completed")
    public ResponseEntity<Map<String, Object>> getCompletedSessions() {
        try {
            List<ChatSessionDto> sessions = chatService.getCompletedSessions();
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "완료된 채팅 세션 목록을 조회했습니다.",
                "data", sessions
            ));
        } catch (Exception e) {
            log.error("완료된 채팅 세션 목록 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "완료된 채팅 세션 목록 조회에 실패했습니다."
            ));
        }
    }

    /**
     * 채팅 세션 히스토리 조회 (관리자용)
     */
    @GetMapping("/sessions/history")
    public ResponseEntity<Map<String, Object>> getSessionHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "today") String dateFilter,
            @RequestParam(defaultValue = "all") String statusFilter,
            @RequestParam(defaultValue = "") String search) {
        try {
            Map<String, Object> result = chatService.getSessionHistory(page, size, dateFilter, statusFilter, search);
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "채팅 세션 히스토리를 조회했습니다.",
                "data", result
            ));
        } catch (Exception e) {
            log.error("채팅 세션 히스토리 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "채팅 세션 히스토리 조회에 실패했습니다."
            ));
        }
    }



    /**
     * 채팅 세션 상태 업데이트
     */
    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> updateSessionStatus(
            @PathVariable String sessionId, 
            @RequestBody Map<String, Long> request) {
        try {
            Long statusId = request.get("statusId");
            if (statusId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", "99",
                    "message", "상태 ID가 필요합니다."
                ));
            }
            
            chatService.updateSessionStatus(sessionId, statusId);
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "채팅 세션 상태가 업데이트되었습니다."
            ));
        } catch (Exception e) {
            log.error("채팅 세션 상태 업데이트 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "채팅 세션 상태 업데이트에 실패했습니다."
            ));
        }
    }

    /**
     * 채팅 세션 종료
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> endSession(@PathVariable String sessionId) {
        try {
            log.info("채팅 세션 종료 요청: sessionId={}", sessionId);
            
            chatService.endSession(sessionId);
            
            log.info("채팅 세션 종료 성공: sessionId={}", sessionId);
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "채팅 세션이 종료되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.error("채팅 세션 종료 실패 - 잘못된 요청: sessionId={}, error={}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("채팅 세션 종료 실패 - 예상치 못한 오류: sessionId={}", sessionId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "채팅 세션 종료에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 메시지 전송
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable String sessionId,
            @RequestBody ChatMessageDto messageDto) {
        try {
            messageDto.setSessionId(sessionId);
            chatService.sendMessage(messageDto);
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "메시지가 전송되었습니다."
            ));
        } catch (Exception e) {
            log.error("메시지 전송 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "메시지 전송에 실패했습니다."
            ));
        }
    }

    /**
     * 메시지 읽음 처리
     */
    @PostMapping("/sessions/{sessionId}/read")
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(
            @PathVariable String sessionId,
            @RequestBody Map<String, Long> request) {
        try {
            Long receiverId = request.get("receiverId");
            if (receiverId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", "99",
                    "message", "수신자 ID가 필요합니다."
                ));
            }
            
            chatService.markMessagesAsRead(sessionId, receiverId);
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "메시지가 읽음 처리되었습니다."
            ));
        } catch (Exception e) {
            log.error("메시지 읽음 처리 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", "99",
                "message", "메시지 읽음 처리에 실패했습니다."
            ));
        }
    }
}
