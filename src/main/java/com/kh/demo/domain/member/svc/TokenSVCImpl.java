package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.common.svc.CodeSVC;
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
    private final CodeSVC codeSVC;  // CodeCache 대신 CodeSVC 사용

    @Override
    public Long createEmailVerificationToken(String email, String verificationCode) {
        // 기존 이메일 인증 토큰 비활성화
        tokenDAO.deactivateByEmailAndType(email, "EMAIL_VERIFICATION");
        
        // 새 토큰 생성
        Token token = new Token();
        token.setEmail(email);
        token.setTokenTypeId(codeSVC.getCodeId("TOKEN_TYPE", "EMAIL_VERIFICATION"));
        token.setTokenValue(verificationCode);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // 10분 만료
        Long activeStatusId = codeSVC.getCodeId("TOKEN_STATUS", "ACTIVE");
        token.setStatusId(activeStatusId);
        
        return tokenDAO.save(token);
    }
    
    @Override
    public Long createPasswordResetToken(String email, String resetToken) {
        log.info("비밀번호 재설정 토큰 생성 시작: email={}, token={}", email, resetToken);
        
        // 기존 비밀번호 재설정 토큰 비활성화
        int deactivatedCount = tokenDAO.deactivateByEmailAndType(email, "PASSWORD_RESET");
        log.info("기존 비밀번호 재설정 토큰 비활성화 완료: count={}", deactivatedCount);
        
        // 새 토큰 생성
        Token token = new Token();
        token.setEmail(email);
        token.setTokenTypeId(codeSVC.getCodeId("TOKEN_TYPE", "PASSWORD_RESET"));
        token.setTokenValue(resetToken);
        token.setExpiryDate(LocalDateTime.now().plusHours(1)); // 1시간 만료
        Long activeStatusId = codeSVC.getCodeId("TOKEN_STATUS", "ACTIVE");
        token.setStatusId(activeStatusId);
        
        Long tokenId = tokenDAO.save(token);
        log.info("비밀번호 재설정 토큰 생성 완료: tokenId={}", tokenId);
        
        return tokenId;
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
            
            // 토큰 타입 확인 (코드 ID로부터 코드 값 가져와서 비교)
            String actualTokenType = codeSVC.getCodeValue("TOKEN_TYPE", token.getTokenTypeId());
            if (!actualTokenType.equals(tokenType)) {
                log.warn("Token type mismatch. Expected: {}, Actual: {}", tokenType, actualTokenType);
                return false;
            }
            
            // 만료 확인
            if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                log.warn("Token expired. Token ID: {}", token.getTokenId());
                Long expiredStatusId = codeSVC.getCodeId("TOKEN_STATUS", "EXPIRED");
                tokenDAO.updateStatus(token.getTokenId(), expiredStatusId);
                return false;
            }
            
            // 토큰 비활성화
            Long verifiedStatusId = codeSVC.getCodeId("TOKEN_STATUS", "VERIFIED");
            tokenDAO.updateStatus(token.getTokenId(), verifiedStatusId);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean updateTokenStatus(Long tokenId, String status) {
        // status 문자열을 code_id로 변환
        Long statusCodeId = codeSVC.getCodeId("TOKEN_STATUS", status);
        int updated = tokenDAO.updateStatus(tokenId, statusCodeId);
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
