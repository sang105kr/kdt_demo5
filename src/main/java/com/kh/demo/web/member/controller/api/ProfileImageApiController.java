package com.kh.demo.web.member.controller.api;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.web.member.controller.api.request.ProfileImageUploadRequest;
import com.kh.demo.web.member.controller.api.response.ProfileImageResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 프로필 이미지 API 컨트롤러 - CSR 방식 (AJAX 요청 처리)
 */
@Slf4j
@RestController
@RequestMapping("/api/member/mypage/profile-image")
@RequiredArgsConstructor
public class ProfileImageApiController {
    
    private final MemberSVC memberSVC;
    
    // 허용된 이미지 파일 타입
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 프로필 이미지 업로드 처리
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProfileImageResponse>> uploadProfileImage(
            @Validated @ModelAttribute ProfileImageUploadRequest request,
            BindingResult bindingResult,
            HttpSession session) {
        
        log.debug("프로필 이미지 업로드 요청");
        
        LoginMember loginMember = getLoginMemberOrRedirect(session);
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
        }
        
        // 1. 필드 레벨 유효성 검사 (BindingResult)
        if (bindingResult.hasErrors()) {
            log.warn("프로필 이미지 업로드 폼 유효성 검사 실패: errors={}", bindingResult.getAllErrors());
            
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            });
            
            ProfileImageResponse errorResponse = ProfileImageResponse.builder()
                    .message(errorMessage.toString())
                    .hasProfileImage(false)
                    .build();
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorResponse));
        }
        
        try {
            // 2. 비즈니스 레벨 유효성 검사 (컨트롤러에서 직접 처리)
            String businessValidationError = validateBusinessRules(request);
            if (businessValidationError != null) {
                log.warn("프로필 이미지 업로드 비즈니스 검증 실패: {}", businessValidationError);
                ProfileImageResponse errorResponse = ProfileImageResponse.builder()
                        .message(businessValidationError)
                        .hasProfileImage(false)
                        .build();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, errorResponse));
            }
            
            // 파일을 byte 배열로 변환
            byte[] imageData = request.getProfileImage().getBytes();
            
            // 데이터베이스에 저장
            int result = memberSVC.updateProfileImage(loginMember.getMemberId(), imageData);
            
            if (result > 0) {
                // 성공 시 세션 정보 업데이트
                updateLoginMemberProfileImage(session, loginMember.getMemberId(), true);
                
                log.info("프로필 이미지 업로드 성공: memberId={}", loginMember.getMemberId());
                
                // Response DTO 생성
                ProfileImageResponse response = ProfileImageResponse.builder()
                        .message("프로필 이미지가 업로드되었습니다.")
                        .profileImageUrl("/member/mypage/profile-image/view/" + loginMember.getMemberId())
                        .hasProfileImage(true)
                        .fileInfo(ProfileImageResponse.FileInfo.builder()
                                .fileName(request.getFileName())
                                .fileSize(request.getFileSize())
                                .contentType(request.getContentType())
                                .build())
                        .build();
                
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, response));
            } else {
                log.warn("프로필 이미지 업로드 실패: memberId={}", loginMember.getMemberId());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null));
            }
            
        } catch (BusinessValidationException e) {
            log.warn("프로필 이미지 업로드 유효성 검사 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null));
        } catch (IOException e) {
            log.error("프로필 이미지 파일 읽기 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }
    

    
    /**
     * 프로필 이미지 삭제 처리
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<ProfileImageResponse>> deleteProfileImage(HttpSession session) {
        
        log.debug("프로필 이미지 삭제 요청");
        
        LoginMember loginMember = getLoginMemberOrRedirect(session);
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null));
        }
        
        try {
            int result = memberSVC.deleteProfileImage(loginMember.getMemberId());
            
            if (result > 0) {
                // 성공 시 세션 정보 업데이트
                updateLoginMemberProfileImage(session, loginMember.getMemberId(), false);
                
                log.info("프로필 이미지 삭제 성공: memberId={}", loginMember.getMemberId());
                
                // Response DTO 생성
                ProfileImageResponse response = ProfileImageResponse.builder()
                        .message("프로필 이미지가 삭제되었습니다.")
                        .profileImageUrl(null)
                        .hasProfileImage(false)
                        .build();
                
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, response));
            } else {
                log.warn("프로필 이미지 삭제 실패: memberId={}", loginMember.getMemberId());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null));
            }
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }
    
    /**
     * 로그인한 회원 정보 가져오기 (null일 경우 null 반환)
     */
    private LoginMember getLoginMemberOrRedirect(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            log.warn("로그인하지 않은 사용자의 요청");
            return null;
        }
        return loginMember;
    }
    
    /**
     * 로그인한 회원의 프로필 이미지 상태 업데이트
     */
    private void updateLoginMemberProfileImage(HttpSession session, Long memberId, boolean hasProfileImage) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember != null && loginMember.getMemberId().equals(memberId)) {
            // 새로운 LoginMember 객체 생성하여 세션 업데이트
            LoginMember updatedLoginMember = new LoginMember(
                loginMember.getMemberId(),
                loginMember.getEmail(),
                loginMember.getNickname(),
                loginMember.getGubun(),
                hasProfileImage
            );
            session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);
            log.debug("세션 프로필 이미지 상태 업데이트: memberId={}, hasImage={}", memberId, hasProfileImage);
        }
    }
    
    /**
     * 비즈니스 규칙 검증
     */
    private String validateBusinessRules(ProfileImageUploadRequest request) {
        // 파일 크기 검사 (5MB)
        if (request.getFileSize() > 5 * 1024 * 1024) {
            return "파일 크기는 5MB 이하여야 합니다.";
        }
        
        // 파일 타입 검사
        String contentType = request.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            return "지원하지 않는 파일 형식입니다. (JPEG, PNG, GIF, WebP만 가능)";
        }
        
        return null; // 검증 통과
    }
    
    /**
     * 허용된 이미지 타입인지 확인
     */
    private boolean isAllowedImageType(String contentType) {
        return ALLOWED_TYPES.contains(contentType.toLowerCase());
    }
} 