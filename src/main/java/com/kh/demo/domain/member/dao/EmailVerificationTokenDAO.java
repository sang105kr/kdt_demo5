package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.vo.EmailVerificationToken;
import com.kh.demo.domain.shared.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenDAO extends BaseDAO<EmailVerificationToken, Long> {
    
    // 이메일로 활성 토큰 조회
    Optional<EmailVerificationToken> findByEmailAndActive(String email);
    
    // 인증 코드로 조회
    Optional<EmailVerificationToken> findByVerificationCode(String verificationCode);
    
    // 만료된 토큰들 조회
    List<EmailVerificationToken> findExpiredTokens();
    
    // 토큰 상태 업데이트
    int updateStatus(Long tokenId, String status);
    
    // 이메일의 모든 토큰 비활성화
    int deactivateByEmail(String email);
} 