package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.vo.PasswordResetToken;
import com.kh.demo.domain.shared.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenDAO extends BaseDAO<PasswordResetToken, Long> {
    
    // 이메일로 활성 토큰 조회
    Optional<PasswordResetToken> findByEmailAndActive(String email);
    
    // 토큰으로 조회
    Optional<PasswordResetToken> findByToken(String token);
    
    // 만료된 토큰들 조회
    List<PasswordResetToken> findExpiredTokens();
    
    // 토큰 상태 업데이트
    int updateStatus(Long tokenId, String status);
    
    // 이메일의 모든 토큰 비활성화
    int deactivateByEmail(String email);
} 