package com.kh.demo.web.api.member;

import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.api.dto.ApiResponse;
import com.kh.demo.web.api.dto.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberSVC memberSVC;

    @GetMapping("/email-exists")
    public ResponseEntity<ApiResponse<Map<String, Object>>> emailExists(@RequestParam String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest().body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null));
        }
        boolean exists = memberSVC.isMember(email);
        return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, Map.of("exists", exists)));
    }
} 