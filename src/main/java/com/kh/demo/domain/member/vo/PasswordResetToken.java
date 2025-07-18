package com.kh.demo.domain.member.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 토큰 Value Object
 * 임시 인증 데이터를 담는 도메인 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    
    private Long tokenId;
    private String email;
    private String token;
    private LocalDateTime expiryDate;
    private String status; // ACTIVE, USED, EXPIRED
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * 토큰이 활성 상태인지 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isExpired();
    }
    
    /**
     * 토큰을 사용 완료 상태로 변경
     */
    public void markAsUsed() {
        this.status = "USED";
        this.udate = LocalDateTime.now();
    }
    
    /**
     * 토큰을 만료 상태로 변경
     */
    public void markAsExpired() {
        this.status = "EXPIRED";
        this.udate = LocalDateTime.now();
    }
} 