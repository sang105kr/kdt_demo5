package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.dao.TokenDAO;
import com.kh.demo.domain.member.entity.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 통합 토큰 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TokenSVCImpl implements TokenSVC {
    
    private final TokenDAO tokenDAO;
    
    @Override
    public Long createEmailVerificationToken(String email, String verificationCode) {
        // 기존 이메일 인증 토큰 비활성화
        tokenDAO.deactivateByEmailAndType(email, Token.TokenType.EMAIL_VERIFICATION);
        
        // 새 토큰 생성
        Token token = new Token();
        token.setEmail(email);
        token.setTokenType(Token.TokenType.EMAIL_VERIFICATION);
        token.setTokenValue(verificationCode);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // 10분 만료
        token.setStatus(Token.TokenStatus.ACTIVE);
        
        return tokenDAO.save(token);
    }
    
    @Override
    public Long createPasswordResetToken(String email, String resetToken) {
        // 기존 비밀번호 재설정 토큰 비활성화
        tokenDAO.deactivateByEmailAndType(email, Token.TokenType.PASSWORD_RESET);
        
        // 새 토큰 생성
        Token token = new Token();
        token.setEmail(email);
        token.setTokenType(Token.TokenType.PASSWORD_RESET);
        token.setTokenValue(resetToken);
        token.setExpiryDate(LocalDateTime.now().plusHours(1)); // 1시간 만료
        token.setStatus(Token.TokenStatus.ACTIVE);
        
        return tokenDAO.save(token);
    }
    
    @Override
    public Optional<Token> findActiveByEmailAndType(String email, String tokenType) {
        return tokenDAO.findActiveByEmailAndType(email, tokenType);
    }
    
    @Override
    public Optional<Token> findActiveByEmailAndValue(String email, String tokenValue) {
        return tokenDAO.findActiveByEmailAndValue(email, tokenValue);
    }
    
    @Override
    public Optional<Token> findActiveByValue(String tokenValue) {
        return tokenDAO.findActiveByValue(tokenValue);
    }
    
    @Override
    public boolean verifyAndDeactivateToken(String email, String tokenValue, String tokenType) {
        Optional<Token> tokenOpt = tokenDAO.findActiveByEmailAndValue(email, tokenValue);
        
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            
            // 토큰 타입 확인
            if (!token.getTokenType().equals(tokenType)) {
                log.warn("Token type mismatch. Expected: {}, Actual: {}", tokenType, token.getTokenType());
                return false;
            }
            
            // 만료 확인
            if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                log.warn("Token expired. Token ID: {}", token.getTokenId());
                tokenDAO.updateStatus(token.getTokenId(), Token.TokenStatus.EXPIRED);
                return false;
            }
            
            // 토큰 비활성화
            tokenDAO.updateStatus(token.getTokenId(), Token.TokenStatus.VERIFIED);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean updateTokenStatus(Long tokenId, String status) {
        int updated = tokenDAO.updateStatus(tokenId, status);
        return updated > 0;
    }
    
    @Override
    public int deactivateExpiredTokens() {
        return tokenDAO.deactivateExpiredTokens();
    }
    
    @Override
    public int deleteByEmail(String email) {
        return tokenDAO.deleteByEmail(email);
    }
    
    @Override
    public int deleteById(Long tokenId) {
        return tokenDAO.deleteById(tokenId);
    }
} 