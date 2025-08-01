package com.kh.demo.web.member.controller.api;

import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.web.member.controller.api.request.EmailCheckRequest;
import com.kh.demo.web.member.controller.api.request.NicknameCheckRequest;
import com.kh.demo.web.member.controller.api.response.MemberCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberSVC memberSVC;

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/email-exists")
    public ResponseEntity<ApiResponse<MemberCheckResponse>> emailExists(
            @Validated @ModelAttribute EmailCheckRequest request,
            BindingResult bindingResult) {
        
        log.debug("이메일 중복 확인 요청: email={}", request.getEmail());
        
        // 1. 필드 레벨 유효성 검사 (BindingResult)
        if (bindingResult.hasErrors()) {
            log.warn("이메일 중복 확인 유효성 검사 실패: errors={}", bindingResult.getAllErrors());
            
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            });
            
            MemberCheckResponse errorResponse = MemberCheckResponse.builder()
                    .exists(false)
                    .checkedValue(request.getEmail())
                    .message(errorMessage.toString())
                    .build();
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorResponse));
        }
        
        try {
            // 2. 비즈니스 레벨 유효성 검사 (컨트롤러에서 직접 처리)
            String businessValidationError = validateEmailBusinessRules(request);
            if (businessValidationError != null) {
                log.warn("이메일 중복 확인 비즈니스 검증 실패: {}", businessValidationError);
                MemberCheckResponse errorResponse = MemberCheckResponse.builder()
                        .exists(false)
                        .checkedValue(request.getEmail())
                        .message(businessValidationError)
                        .build();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, errorResponse));
            }
            
            // 중복 확인 로직
            boolean exists = memberSVC.isMember(request.getEmail());
            
            MemberCheckResponse response = MemberCheckResponse.builder()
                    .exists(exists)
                    .checkedValue(request.getEmail())
                    .message(exists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.")
                    .build();
            
            log.info("이메일 중복 확인 완료: email={}, exists={}", request.getEmail(), exists);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, response));
            
        } catch (Exception e) {
            log.error("이메일 중복 확인 중 오류 발생", e);
            MemberCheckResponse errorResponse = MemberCheckResponse.builder()
                    .exists(false)
                    .checkedValue(request.getEmail())
                    .message("이메일 확인 중 오류가 발생했습니다.")
                    .build();
            return ResponseEntity.status(500)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorResponse));
        }
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/nickname-exists")
    public ResponseEntity<ApiResponse<MemberCheckResponse>> nicknameExists(
            @Validated @ModelAttribute NicknameCheckRequest request,
            BindingResult bindingResult) {
        
        log.debug("닉네임 중복 확인 요청: nickname={}, currentEmail={}", 
                 request.getNickname(), request.getCurrentEmail());
        
        // 1. 필드 레벨 유효성 검사 (BindingResult)
        if (bindingResult.hasErrors()) {
            log.warn("닉네임 중복 확인 유효성 검사 실패: errors={}", bindingResult.getAllErrors());
            
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            });
            
            MemberCheckResponse errorResponse = MemberCheckResponse.builder()
                    .exists(false)
                    .checkedValue(request.getNickname())
                    .message(errorMessage.toString())
                    .build();
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorResponse));
        }
        
        try {
            // 2. 비즈니스 레벨 유효성 검사 (컨트롤러에서 직접 처리)
            String businessValidationError = validateNicknameBusinessRules(request);
            if (businessValidationError != null) {
                log.warn("닉네임 중복 확인 비즈니스 검증 실패: {}", businessValidationError);
                MemberCheckResponse errorResponse = MemberCheckResponse.builder()
                        .exists(false)
                        .checkedValue(request.getNickname())
                        .message(businessValidationError)
                        .build();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, errorResponse));
            }
            
            // 현재 사용자의 닉네임인지 확인
            boolean isCurrentUser = false;
            if (request.hasCurrentEmail()) {
                isCurrentUser = memberSVC.isMember(request.getCurrentEmail()) && 
                              memberSVC.findByEmail(request.getCurrentEmail())
                                      .map(member -> request.getNickname().equals(member.getNickname()))
                                      .orElse(false);
            }
            
            // 현재 사용자의 닉네임이면 중복이 아님
            if (isCurrentUser) {
                MemberCheckResponse response = MemberCheckResponse.builder()
                        .exists(false)
                        .checkedValue(request.getNickname())
                        .message("현재 사용 중인 닉네임입니다.")
                        .build();
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, response));
            }
            
            // 중복 확인 로직
            boolean exists = memberSVC.isNicknameExists(request.getNickname());
            
            MemberCheckResponse response = MemberCheckResponse.builder()
                    .exists(exists)
                    .checkedValue(request.getNickname())
                    .message(exists ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.")
                    .build();
            
            log.info("닉네임 중복 확인 완료: nickname={}, exists={}", request.getNickname(), exists);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, response));
            
        } catch (Exception e) {
            log.error("닉네임 중복 확인 중 오류 발생", e);
            MemberCheckResponse errorResponse = MemberCheckResponse.builder()
                    .exists(false)
                    .checkedValue(request.getNickname())
                    .message("닉네임 확인 중 오류가 발생했습니다.")
                    .build();
            return ResponseEntity.status(500)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorResponse));
        }
    }
    
    /**
     * 이메일 비즈니스 규칙 검증
     */
    private String validateEmailBusinessRules(EmailCheckRequest request) {
        // 추가적인 비즈니스 규칙이 있다면 여기에 구현
        // 예: 특정 도메인 제한, 금지된 이메일 패턴 등
        return null; // 검증 통과
    }
    
    /**
     * 닉네임 비즈니스 규칙 검증
     */
    private String validateNicknameBusinessRules(NicknameCheckRequest request) {
        // 금지된 닉네임 패턴 검사
        if (request.getNickname().contains("admin") || request.getNickname().contains("관리자")) {
            return "사용할 수 없는 닉네임입니다.";
        }
        
        // 특수문자 제한 검사
        if (!request.getNickname().matches("^[a-zA-Z0-9가-힣_]+$")) {
            return "닉네임은 영문, 숫자, 한글, 언더스코어만 사용 가능합니다.";
        }
        
        return null; // 검증 통과
    }
} 