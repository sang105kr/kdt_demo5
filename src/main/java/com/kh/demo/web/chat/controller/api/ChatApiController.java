package com.kh.demo.web.chat.controller.api;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.chat.dto.ChatMessageDto;
import com.kh.demo.domain.chat.dto.ChatSessionDto;
import com.kh.demo.domain.chat.dto.ChatSessionRequest;
import com.kh.demo.domain.chat.dto.ChatMessageTypeDto;
import com.kh.demo.domain.chat.svc.ChatSVC;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.util.MemberAuthUtil;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 채팅 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatSVC chatSVC;
    private final MemberAuthUtil memberAuthUtil;
    private final CodeSVC codeSVC;

    /**
     * 채팅 세션 생성
     */
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSession(@RequestBody ChatSessionRequest request, HttpSession session) {
        try {
            // 세션에서 로그인 사용자 정보 확인
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            // 클라이언트에서 전송한 memberId 무시하고 세션의 사용자 ID 사용
            request.setMemberId(loginMember.getMemberId());
            
            if (request.getCategoryId() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            Long sessionId = chatSVC.startChatSession(request);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("sessionId", sessionId);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("채팅 세션 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 팝업 채팅용 세션 생성 (카테고리 코드 기반)
     */
    @PostMapping("/session/popup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPopupSession(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            log.info("팝업 채팅 세션 생성 요청: {}", request);
            
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            log.info("세션에서 조회한 loginMember: {}", loginMember);
            
            if (loginMember == null) {
                log.warn("로그인되지 않은 사용자");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            String category = request.get("category");
            log.info("요청된 카테고리: {}", category);
            
            if (category == null || category.trim().isEmpty()) {
                log.warn("카테고리가 비어있음");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            // 카테고리 코드를 ID로 변환
            Long categoryId = getCategoryIdByCode(category);
            log.info("변환된 카테고리 ID: {}", categoryId);
            
            if (categoryId == null) {
                log.warn("유효하지 않은 카테고리 코드: {}", category);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            ChatSessionRequest sessionRequest = new ChatSessionRequest();
            sessionRequest.setCategoryId(categoryId);
            sessionRequest.setMemberId(loginMember.getMemberId());
            
            log.info("채팅 세션 요청: categoryId={}, memberId={}", categoryId, loginMember.getMemberId());
            
            Long sessionId = chatSVC.startChatSession(sessionRequest);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("sessionId", sessionId);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("팝업 채팅 세션 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 카테고리 코드로 카테고리 ID 조회
     */
    private Long getCategoryIdByCode(String categoryCode) {
        // code 테이블에서 FAQ_CATEGORY 그룹의 code_id 조회
        return codeSVC.getCodeId("FAQ_CATEGORY", categoryCode.toUpperCase());
    }
    
    /**
     * 카테고리 코드로 카테고리명 조회
     */
    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCategoryName(@PathVariable String categoryCode) {
        try {
            String categoryName = getCategoryNameByCode(categoryCode);
            
            if (categoryName == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null));
            }
            
            Map<String, String> responseData = new HashMap<>();
            responseData.put("categoryName", categoryName);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("카테고리명 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }
    
    /**
     * 카테고리 코드로 카테고리명 조회 (내부 메서드)
     */
    private String getCategoryNameByCode(String categoryCode) {
        // code 테이블에서 FAQ_CATEGORY 그룹의 decode 조회
        Long codeId = codeSVC.getCodeId("FAQ_CATEGORY", categoryCode.toUpperCase());
        if (codeId != null) {
            return codeSVC.getCodeDecode("FAQ_CATEGORY", codeId);
        }
        return null;
    }

    /**
     * 채팅 세션 조회
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionDto>> getSession(@PathVariable Long sessionId) {
        try {
            var session = chatSVC.getSessionDetail(sessionId);
            
            if (session.isPresent()) {
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, session.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null));
            }
        } catch (Exception e) {
            log.error("채팅 세션 조회 실패", e);
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
     * presence 업데이트 (이탈/재접속) - 회원용
     * body: { "side": "MEMBER", "state": "INACTIVE"|"ACTIVE", "reason": "PAGE_HIDE", "graceSeconds": 300 }
     */
    @PostMapping("/sessions/{sessionId}/presence")
    public ResponseEntity<ApiResponse<Void>> updatePresence(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> body) {
        try {
            String side = "MEMBER"; // 회원은 항상 MEMBER
            String state = (String) body.getOrDefault("state", "INACTIVE");
            String reason = (String) body.getOrDefault("reason", "UNKNOWN");
            Long graceSeconds = null;
            Object gs = body.get("graceSeconds");
            if (gs instanceof Number) {
                graceSeconds = ((Number) gs).longValue();
            }

            chatSVC.updatePresence(sessionId, side, state, reason, graceSeconds);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("presence 업데이트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 재개 가능한 세션 조회 (회원용)
     */
    @GetMapping("/sessions/resumable")
    public ResponseEntity<ApiResponse<List<ChatSessionDto>>> getResumableSessions(@RequestParam Long memberId) {
        try {
            List<ChatSessionDto> sessions = chatSVC.getResumableSessions(memberId);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, sessions));
        } catch (Exception e) {
            log.error("재개 가능한 세션 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 메시지 전송
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<Void>> sendMessage(
            @PathVariable String sessionId,
            @RequestBody ChatMessageDto messageDto) {
        try {
            messageDto.setSessionId(Long.valueOf(sessionId));
            chatSVC.sendMessage(messageDto);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("메시지 전송 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 메시지 읽음 처리
     */
    @PostMapping("/sessions/{sessionId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable String sessionId,
            HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            chatSVC.markMessagesAsRead(Long.valueOf(sessionId), loginMember.getMemberId());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("메시지 읽음 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 상담 종료
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<ApiResponse<Void>> endChatSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            log.info("상담 종료 요청: sessionId={}, request={}", sessionId, request);
            
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
            }
            
            // exitReasonId (숫자) 또는 exitReason (문자열) 처리
            Long exitReasonId = null;
            String exitReason = null;
            String endedBy = (String) request.get("endedBy");
            Long memberId = loginMember.getMemberId();
            
            // exitReasonId 우선 확인 (새로운 방식)
            if (request.get("exitReasonId") instanceof Number) {
                exitReasonId = ((Number) request.get("exitReasonId")).longValue();
            } else if (request.get("exitReason") instanceof String) {
                exitReason = (String) request.get("exitReason");
                // 문자열을 ID로 변환 시도
                exitReasonId = getExitReasonId(exitReason);
            }
            
            if ((exitReasonId == null && exitReason == null) || endedBy == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            if (exitReasonId != null) {
                // exit_reason_id가 있는 경우 endSessionWithReason 호출
                chatSVC.endSessionWithReason(Long.valueOf(sessionId), exitReasonId, endedBy);
                log.info("상담 종료 완료 (ID 기반): sessionId={}, exitReasonId={}, endedBy={}", sessionId, exitReasonId, endedBy);
            } else {
                // 기존 방식으로 처리 (하위 호환성)
                chatSVC.endChatSession(Long.valueOf(sessionId), exitReason, endedBy, memberId);
                log.info("상담 종료 완료 (문자열 기반): sessionId={}, exitReason={}, endedBy={}", sessionId, exitReason, endedBy);
            }
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("상담 종료 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }
    
    /**
     * exitReason 문자열을 exit_reason_id로 변환
     */
    private Long getExitReasonId(String exitReason) {
        try {
            // exitReason이 CHAT_EXIT_REASON 코드인지 확인
            return codeSVC.getCodeId("CHAT_EXIT_REASON", exitReason);
        } catch (Exception e) {
            log.warn("exitReason을 ID로 변환할 수 없습니다: {}", exitReason);
            return null;
        }
    }

    /**
     * 채팅 세션 종료 (종료 사유 포함)
     */
    @PostMapping("/sessions/{sessionId}/end-with-reason")
    public ResponseEntity<ApiResponse<Void>> endSessionWithReason(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            log.info("채팅 세션 종료 (사유 포함) 요청: sessionId={}, request={}", sessionId, request);
            
            // 로그인 체크
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
            
            log.info("채팅 세션 종료 (사유 포함) 완료: sessionId={}, exitReasonId={}, endedBy={}", sessionId, exitReasonId, endedBy);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("채팅 세션 종료 (사유 포함) 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 채팅 세션 이탈 처리
     */
    @PostMapping("/sessions/{sessionId}/disconnect")
    public ResponseEntity<ApiResponse<Void>> handleDisconnect(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        try {
            log.info("채팅 세션 이탈 처리 요청: sessionId={}, request={}", sessionId, request);
            
            Long disconnectReasonId = null;
            if (request.get("disconnectReasonId") instanceof Number) {
                disconnectReasonId = ((Number) request.get("disconnectReasonId")).longValue();
            }
            
            // 유예 시간 설정 (기본 5분)
            LocalDateTime graceUntil = LocalDateTime.now().plusMinutes(5);
            if (request.get("graceMinutes") instanceof Number) {
                int graceMinutes = ((Number) request.get("graceMinutes")).intValue();
                graceUntil = LocalDateTime.now().plusMinutes(graceMinutes);
            }
            
            if (disconnectReasonId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
            }
            
            chatSVC.handleDisconnect(Long.valueOf(sessionId), disconnectReasonId, graceUntil);
            
            log.info("채팅 세션 이탈 처리 완료: sessionId={}, disconnectReasonId={}, graceUntil={}", sessionId, disconnectReasonId, graceUntil);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("채팅 세션 이탈 처리 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 채팅 세션 재접속 처리
     */
    @PostMapping("/sessions/{sessionId}/reconnect")
    public ResponseEntity<ApiResponse<Void>> handleReconnect(@PathVariable String sessionId) {
        try {
            log.info("채팅 세션 재접속 처리 요청: sessionId={}", sessionId);
            
            chatSVC.handleReconnect(Long.valueOf(sessionId));
            
            log.info("채팅 세션 재접속 처리 완료: sessionId={}", sessionId);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("채팅 세션 재접속 처리 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 마지막 접속 시간 업데이트 - 회원용
     */
    @PostMapping("/sessions/{sessionId}/last-seen")
    public ResponseEntity<ApiResponse<Void>> updateLastSeen(@PathVariable String sessionId) {
        try {
            chatSVC.updateLastSeen(Long.valueOf(sessionId), "MEMBER");
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, null));
        } catch (Exception e) {
            log.error("마지막 접속 시간 업데이트 실패: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 종료 사유 목록 조회 - 회원용
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
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
        } catch (Exception e) {
            log.error("종료 사유 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 채팅 메시지 타입 코드 조회
     */
    @GetMapping("/message-types")
    public ResponseEntity<Map<String, Object>> getMessageTypes() {
        try {
            log.info("채팅 메시지 타입 코드 조회 시작");
            
            // codeSVC 캐시에서 CHAT_MESSAGE_TYPE 그룹의 모든 코드 조회
            var messageTypeCodes = codeSVC.getCodeList("CHAT_MESSAGE_TYPE");
            
            if (messageTypeCodes == null || messageTypeCodes.isEmpty()) {
                log.warn("CHAT_MESSAGE_TYPE 코드가 없습니다. 캐시 새로고침 시도...");
                codeSVC.refreshCache();
                messageTypeCodes = codeSVC.getCodeList("CHAT_MESSAGE_TYPE");
                
                if (messageTypeCodes == null || messageTypeCodes.isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                        "code", "01",
                        "message", "메시지 타입 코드를 찾을 수 없습니다.",
                        "data", List.of()
                    ));
                }
            }
            
            // DTO로 변환
            List<ChatMessageTypeDto> messageTypes = messageTypeCodes.stream()
                .map(code -> {
                    ChatMessageTypeDto dto = new ChatMessageTypeDto();
                    dto.setCodeId(code.getCodeId());
                    dto.setCode(code.getCode());
                    dto.setDecode(code.getDecode());
                    return dto;
                })
                .collect(Collectors.toList());
            
            log.info("채팅 메시지 타입 코드 조회 완료: {}개", messageTypes.size());
            
            return ResponseEntity.ok(Map.of(
                "code", "00",
                "message", "성공",
                "data", messageTypes
            ));
            
        } catch (Exception e) {
            log.error("채팅 메시지 타입 코드 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                "code", "99",
                "message", "메시지 타입 코드 조회 중 오류가 발생했습니다.",
                "data", List.of()
            ));
        }
    }


}
