package com.kh.demo.domain.member.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토큰 엔티티
 * 이메일 인증, 비밀번호 재설정 등 다양한 토큰을 관리
 * 
 * 토큰 타입과 상태는 code 테이블의 TOKEN_TYPE, TOKEN_STATUS 그룹을 참조
 * - TOKEN_TYPE: EMAIL_VERIFICATION, PASSWORD_RESET, EMAIL_CHANGE 등
 * - TOKEN_STATUS: ACTIVE, VERIFIED, EXPIRED 등
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Token extends BaseEntity {
    
    private Long tokenId;           // 토큰 ID
    private String email;           // 이메일 주소
    private Long tokenTypeId;       // 토큰 타입 (code_id 참조, gcode='TOKEN_TYPE')
    private String tokenValue;      // 토큰 값 (인증 코드 또는 토큰)
    private LocalDateTime expiryDate; // 만료 시간
    private Long statusId;          // 상태 (code_id 참조, gcode='TOKEN_STATUS')
} 