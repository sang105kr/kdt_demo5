package com.kh.demo.admin.chat.api;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.dto.ChatSessionDto;
import com.kh.demo.domain.chat.svc.ChatSVC;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 채팅 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
public class AdminApiChatController {

    private final ChatSVC chatSVC;

    /**
     * 채팅 통계 조회 
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChatStatistics() {
        try {
            Map<String, Object> stats = chatSVC.getChatStatistics();
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, stats));
        } catch (Exception e) {
            log.error("채팅 통계 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 대기 중인 채팅 세션 목록 조회 
     */
    @GetMapping("/sessions/waiting")
    public ResponseEntity<ApiResponse<List<ChatSessionDto>>> getWaitingSessions() {
        try {
            List<ChatSessionDto> sessions = chatSVC.getWaitingSessions();
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, sessions));
        } catch (Exception e) {
            log.error("대기 중인 채팅 세션 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 진행중인 채팅 세션 목록 조회 
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<List<ChatSessionDto>>> getActiveSessions() {
        try {
            List<ChatSessionDto> sessions = chatSVC.getActiveSessions();
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, sessions));
        } catch (Exception e) {
            log.error("진행중인 채팅 세션 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 오늘 완료된 채팅 세션 목록 조회 
     */
    @GetMapping("/sessions/completed")
    public ResponseEntity<ApiResponse<List<ChatSessionDto>>> getCompletedSessions() {
        try {
            List<ChatSessionDto> sessions = chatSVC.getCompletedSessions();
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, sessions));
        } catch (Exception e) {
            log.error("완료된 채팅 세션 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 채팅 세션 히스토리 조회 
     */
    @GetMapping("/sessions/history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String dateFilter,
            @RequestParam(defaultValue = "all") String exitReasonFilter,
            @RequestParam(defaultValue = "") String search) {
        try {
            Map<String, Object> result = chatSVC.getSessionHistory(page, size, dateFilter, exitReasonFilter, search);
            log.info("채팅 세션 히스토리 조회 결과 result={}", result);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, result));
        } catch (Exception e) {
            log.error("채팅 세션 히스토리 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 진행중 상태 ID 조회
     */
    @GetMapping("/status/active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveStatusId() {
        try {
            Long activeStatusId = chatSVC.getActiveStatusId();
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("statusId", activeStatusId);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("진행중 상태 ID 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 완료 상태 ID 조회
     */
    @GetMapping("/status/completed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCompletedStatusId() {
        try {
            Long completedStatusId = chatSVC.getCompletedStatusId();
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("statusId", completedStatusId);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("완료 상태 ID 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 채팅 세션 상태 업데이트 
     */
    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<ApiResponse<Void>> updateSessionStatus(
            @PathVariable Long sessionId, 
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            Long statusId = null;
            String status = null;
            
            // statusId 또는 status 파라미터 처리
            if (request.containsKey("statusId")) {
                statusId = ((Number) request.get("statusId")).longValue();
            } else if (request.containsKey("status")) {
                status = (String) request.get("status");
                // 상태명으로 statusId 조회
                statusId = chatSVC.getStatusIdByStatus(status);
            }
            
            if (statusId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            // 현재 로그인한 관리자 ID 가져오기
            Long adminId = null;
            try {
                LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
                if (loginMember != null) {
                    adminId = loginMember.getMemberId();
                    log.info("관리자 ID 조회 성공: {}", adminId);
                } else {
                    log.warn("로그인된 관리자 정보가 없습니다.");
                    // 임시로 기본 관리자 ID 사용
                    adminId = 3L; // admin1
                }
            } catch (Exception e) {
                log.warn("관리자 ID 조회 실패: {}", e.getMessage());
                // 임시로 기본 관리자 ID 사용
                adminId = 3L; // admin1
            }
            
            chatSVC.updateSessionStatus(sessionId,  statusId, adminId);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("채팅 세션 상태 업데이트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 관리자 메시지 전송
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<Void>> sendAdminMessage(
            @PathVariable String sessionId,
            @RequestBody ChatMessageDto messageDto,
            HttpSession session) {
        try {
            // 관리자 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            messageDto.setSessionId(Long.valueOf(sessionId));
            messageDto.setSenderId(loginMember.getMemberId());
            messageDto.setSenderType("ADMIN");
            
            chatSVC.sendMessage(messageDto);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("관리자 메시지 전송 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 관리자 메시지 읽음 처리
     */
    @PostMapping("/sessions/{sessionId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsReadByAdmin(
            @PathVariable String sessionId,
            HttpSession session) {
        try {
            // 관리자 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            chatSVC.markMessagesAsRead(Long.valueOf(sessionId), loginMember.getMemberId());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("관리자 메시지 읽음 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 관리자 상담 종료
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<ApiResponse<Void>> endChatSessionByAdmin(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            log.info("관리자 상담 종료 요청: sessionId={}, request={}", sessionId, request);
            
            // 관리자 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            String exitReason = (String) request.get("exitReason");
            String endedBy = (String) request.get("endedBy");
            Long adminId = loginMember.getMemberId();
            
            if (exitReason == null || endedBy == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            chatSVC.endChatSession(sessionId, exitReason, endedBy, adminId);
            
            log.info("관리자 상담 종료 완료: sessionId={}, reason={}, endedBy={}", sessionId, exitReason, endedBy);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("관리자 상담 종료 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 관리자 채팅 세션 종료 (종료 사유 포함)
     */
    @PostMapping("/sessions/{sessionId}/end-with-reason")
    public ResponseEntity<ApiResponse<Void>> endSessionWithReasonByAdmin(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            log.info("관리자 채팅 세션 종료 (사유 포함) 요청: sessionId={}, request={}", sessionId, request);
            
            // 관리자 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            Long exitReasonId = null;
            if (request.get("exitReasonId") instanceof Number) {
                exitReasonId = ((Number) request.get("exitReasonId")).longValue();
            }
            String endedBy = (String) request.get("endedBy");
            
            if (exitReasonId == null || endedBy == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            chatSVC.endSessionWithReason(sessionId, exitReasonId, endedBy);
            
            log.info("관리자 채팅 세션 종료 (사유 포함) 완료: sessionId={}, exitReasonId={}, endedBy={}", sessionId, exitReasonId, endedBy);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("관리자 채팅 세션 종료 (사유 포함) 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 관리자 presence 업데이트 (이탈/재접속)
     */
    @PostMapping("/sessions/{sessionId}/presence")
    public ResponseEntity<ApiResponse<Void>> updateAdminPresence(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {
        try {
            String side = "ADMIN"; // 관리자는 항상 ADMIN
            String state = (String) body.getOrDefault("state", "INACTIVE");
            String reason = (String) body.getOrDefault("reason", "UNKNOWN");
            Long graceSeconds = null;
            Object gs = body.get("graceSeconds");
            if (gs instanceof Number) {
                graceSeconds = ((Number) gs).longValue();
            }

            chatSVC.updatePresence(Long.valueOf(sessionId), side, state, reason, graceSeconds);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("관리자 presence 업데이트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 관리자 마지막 접속 시간 업데이트
     */
    @PostMapping("/sessions/{sessionId}/last-seen")
    public ResponseEntity<ApiResponse<Void>> updateAdminLastSeen(@PathVariable String sessionId) {
        try {
            chatSVC.updateLastSeen(Long.valueOf(sessionId), "ADMIN");
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("관리자 마지막 접속 시간 업데이트 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 이탈 사유 코드 목록 조회
     */
    @GetMapping("/disconnect-reasons")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDisconnectReasons() {
        try {
            List<com.kh.demo.domain.common.entity.Code> reasons = chatSVC.getDisconnectReasons();
            
            List<Map<String, Object>> responseData = reasons.stream()
                .map(code -> {
                    Map<String, Object> codeMap = new HashMap<>();
                    codeMap.put("codeId", code.getCodeId());
                    codeMap.put("code", code.getCode());
                    codeMap.put("decode", code.getDecode());
                    return codeMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("이탈 사유 코드 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 종료 사유 코드 목록 조회
     */
    @GetMapping("/exit-reasons")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExitReasons() {
        try {
            List<com.kh.demo.domain.common.entity.Code> reasons = chatSVC.getExitReasons();
            
            List<Map<String, Object>> responseData = reasons.stream()
                .map(code -> {
                    Map<String, Object> codeMap = new HashMap<>();
                    codeMap.put("codeId", code.getCodeId());
                    codeMap.put("code", code.getCode());
                    codeMap.put("decode", code.getDecode());
                    return codeMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("종료 사유 코드 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 개별 채팅 세션 조회
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionDto>> getSession(@PathVariable Long sessionId) {
        try {
            log.info("관리자 세션 조회 요청: sessionId={}", sessionId);
            
            var session = chatSVC.getSessionDetail(sessionId);
            
            if (session.isPresent()) {
                log.info("세션 조회 성공: sessionId={}, session={}", sessionId, session.get());
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, session.get()));
            } else {
                log.warn("세션을 찾을 수 없음: sessionId={}", sessionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null));
            }
        } catch (Exception e) {
            log.error("채팅 세션 조회 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 채팅 세션 메시지 목록 조회
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getSessionMessages(@PathVariable Long sessionId) {
        try {
            List<ChatMessageDto> messages = chatSVC.getSessionMessages(Long.valueOf(sessionId));
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, messages));
        } catch (Exception e) {
            log.error("메시지 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }
    
    /**
     * 세션별 메시지 개수 조회
     */
    @GetMapping("/sessions/{sessionId}/message-count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionMessageCount(@PathVariable Long sessionId) {
        try {
            Long messageCount = chatSVC.getMessageCountBySessionId(sessionId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("sessionId", sessionId);
            responseData.put("messageCount", messageCount);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("메시지 개수 조회 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }
    
    /**
     * 여러 세션의 메시지 개수 조회
     */
    @PostMapping("/sessions/message-counts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionMessageCounts(@RequestBody List<Long> sessionIds) {
        try {
            Map<Long, Long> messageCounts = chatSVC.getMessageCountBySessionIds(sessionIds);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("messageCounts", messageCounts);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("메시지 개수 조회 실패: sessionIds={}", sessionIds, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }
}
