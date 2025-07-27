package com.kh.demo.web.notification.controller.api;

import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.notification.dto.NotificationDto;
import com.kh.demo.domain.notification.svc.NotificationSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationSVC notificationSVC;

    /**
     * 알림 개수 조회
     * 
     * 로그인한 회원의 읽지 않은 알림 개수를 조회합니다.
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getNotificationCount(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, 0));
        }

        try {
            // 실제 DB에서 읽지 않은 알림 개수 조회
            int count = notificationSVC.countUnreadByMemberId(loginMember.getMemberId());
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, count));

        } catch (Exception e) {
            log.error("알림 개수 조회 실패 - memberId: {}, error: {}", 
                    loginMember.getMemberId(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0));
        }
    }

    /**
     * 알림 목록 조회
     * 
     * 로그인한 회원의 알림 목록을 조회합니다.
     * 최신순으로 정렬되며, 알림 타입명을 포함합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember,
            @RequestParam(defaultValue = "10") int limit) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, List.of()));
        }

        try {
            // 실제 DB에서 알림 목록 조회
            List<NotificationDto> notifications = notificationSVC.findNotificationDtosByMemberId(
                loginMember.getMemberId(), limit);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, notifications));

        } catch (Exception e) {
            log.error("알림 목록 조회 실패 - memberId: {}, error: {}", 
                    loginMember.getMemberId(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.of()));
        }
    }

    /**
     * 알림 읽음 처리
     * 
     * 지정된 알림을 읽음 상태로 변경합니다.
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Boolean>> markAsRead(
            @PathVariable Long notificationId,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, false));
        }

        try {
            // 실제 DB에서 알림 읽음 처리
            int updatedCount = notificationSVC.markAsRead(notificationId);
            
            if (updatedCount > 0) {
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, true));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, false));
            }

        } catch (Exception e) {
            log.error("알림 읽음 처리 실패 - memberId: {}, notificationId: {}, error: {}", 
                    loginMember.getMemberId(), notificationId, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false));
        }
    }

    /**
     * 모든 알림 읽음 처리
     * 
     * 로그인한 회원의 모든 읽지 않은 알림을 읽음 상태로 변경합니다.
     */
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Boolean>> markAllAsRead(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, false));
        }

        try {
            // 실제 DB에서 모든 알림 읽음 처리
            int updatedCount = notificationSVC.markAllAsRead(loginMember.getMemberId());
            
            if (updatedCount >= 0) {
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, true));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, false));
            }

        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패 - memberId: {}, error: {}", 
                    loginMember.getMemberId(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false));
        }
    }
} 