package com.kh.demo.domain.member.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 토큰 상세 정보 DTO
 * Token 엔티티와 Code 테이블을 조인한 결과
 */
@Data
public class TokenDetailDTO {
    
    // Token 기본 정보
    private Long tokenId;
    private String email;
    private String tokenValue;
    private LocalDateTime expiryDate;
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    // 코드 참조 필드들
    private Long tokenTypeId;       // 토큰 타입 코드 ID
    private Long statusId;          // 상태 코드 ID
    
    // 코드 decode 값들 (조인으로 조회)
    private String tokenTypeCode;   // 토큰 타입 코드 (EMAIL_VERIFICATION, PASSWORD_RESET, etc.)
    private String tokenTypeName;   // 토큰 타입명 (이메일인증, 비밀번호재설정, etc.)
    private String statusCode;      // 상태 코드 (ACTIVE, VERIFIED, EXPIRED)
    private String statusName;      // 상태명 (활성, 인증완료, 만료)
    
    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * 토큰 활성 여부 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(statusCode) && !isExpired();
    }
    
    /**
     * 토큰 인증 완료 여부
     */
    public boolean isVerified() {
        return "VERIFIED".equals(statusCode);
    }
    
    /**
     * 이메일 인증 토큰 여부
     */
    public boolean isEmailVerification() {
        return "EMAIL_VERIFICATION".equals(tokenTypeCode);
    }
    
    /**
     * 비밀번호 재설정 토큰 여부
     */
    public boolean isPasswordReset() {
        return "PASSWORD_RESET".equals(tokenTypeCode);
    }
}