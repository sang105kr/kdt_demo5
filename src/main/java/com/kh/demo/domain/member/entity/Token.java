package com.kh.demo.domain.member.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토큰 엔티티
 * 이메일 인증, 비밀번호 재설정 등 다양한 토큰을 관리
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    
    private Long tokenId;           // 토큰 ID
    private String email;           // 이메일 주소
    private String tokenType;       // 토큰 타입 (EMAIL_VERIFICATION, PASSWORD_RESET, EMAIL_CHANGE)
    private String tokenValue;      // 토큰 값 (인증 코드 또는 토큰)
    private LocalDateTime expiryDate; // 만료 시간
    private Long status;          // 상태 (ACTIVE, VERIFIED, EXPIRED)
    private LocalDateTime cdate;    // 생성 시간
    private LocalDateTime udate;    // 수정 시간
    
    /**
     * 토큰 타입 상수
     */
    public static final class TokenType {
        public static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
        public static final String PASSWORD_RESET = "PASSWORD_RESET";
        public static final String EMAIL_CHANGE = "EMAIL_CHANGE";
    }
    
    /**
     * 토큰 상태 상수
     */
    public static final class TokenStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String VERIFIED = "VERIFIED";
        public static final String EXPIRED = "EXPIRED";
    }
} 