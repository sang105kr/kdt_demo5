package com.kh.demo.domain.member.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {
    
    private Long tokenId;
    private String email;
    private String verificationCode;
    private LocalDateTime expiryDate;
    private String status; // ACTIVE, VERIFIED, EXPIRED
    private LocalDateTime cdate;
    private LocalDateTime udate;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isExpired();
    }
} 