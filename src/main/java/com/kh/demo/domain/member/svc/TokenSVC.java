package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.entity.Token;

import java.util.List;
import java.util.Optional;

/**
 * 통합 토큰 서비스 인터페이스
 * 이메일 인증, 비밀번호 재설정 등 다양한 토큰을 관리
 */
public interface TokenSVC {
    
    /**
     * 이메일 인증 토큰 생성
     */
    Long createEmailVerificationToken(String email, String verificationCode);
    
    /**
     * 비밀번호 재설정 토큰 생성
     */
    Long createPasswordResetToken(String email, String resetToken);
    
    /**
     * 이메일과 토큰 타입으로 활성 토큰 조회
     */
    Optional<Token> findActiveByEmailAndType(String email, String tokenType);
    
    /**
     * 이메일과 토큰 값으로 활성 토큰 조회
     */
    Optional<Token> findActiveByEmailAndValue(String email, String tokenValue);
    
    /**
     * 토큰 값으로 활성 토큰 조회
     */
    Optional<Token> findActiveByValue(String tokenValue);
    
    /**
     * 토큰 검증 및 비활성화
     */
    boolean verifyAndDeactivateToken(String email, String tokenValue, String tokenType);
    
    /**
     * 토큰 상태 업데이트
     */
    boolean updateTokenStatus(Long tokenId, String status);
    
    /**
     * 만료된 토큰들을 비활성화
     */
    int deactivateExpiredTokens();
    
    /**
     * 이메일로 모든 토큰 삭제
     */
    int deleteByEmail(String email);
    
    /**
     * 토큰 ID로 삭제
     */
    int deleteById(Long tokenId);
} 