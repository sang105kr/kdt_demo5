package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.entity.Token;

import java.util.List;
import java.util.Optional;

/**
 * 토큰 DAO 인터페이스
 * 이메일 인증, 비밀번호 재설정 등 다양한 토큰을 관리
 */
public interface TokenDAO {
    
    /**
     * 토큰 저장
     */
    Long save(Token token);
    
    /**
     * 토큰 ID로 조회
     */
    Optional<Token> findById(Long tokenId);
    
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
     * 이메일로 모든 토큰 조회
     */
    List<Token> findByEmail(String email);
    
    /**
     * 토큰 타입으로 모든 토큰 조회
     */
    List<Token> findByType(String tokenType);
    
    /**
     * 이메일과 토큰 타입으로 토큰 비활성화
     */
    int deactivateByEmailAndType(String email, String tokenType);
    
    /**
     * 토큰 ID로 토큰 비활성화
     */
    int deactivateById(Long tokenId);
    
    /**
     * 만료된 토큰들을 비활성화
     */
    int deactivateExpiredTokens();
    
    /**
     * 토큰 상태 업데이트
     */
    int updateStatus(Long tokenId, String status);
    
    /**
     * 이메일로 모든 토큰 삭제
     */
    int deleteByEmail(String email);
    
    /**
     * 토큰 ID로 삭제
     */
    int deleteById(Long tokenId);
} 