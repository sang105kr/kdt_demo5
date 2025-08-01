package com.kh.demo.web.member.controller.api.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 회원 중복 확인 응답 DTO
 */
@Getter
@Builder
@ToString
public class MemberCheckResponse {
    
    /**
     * 중복 여부
     */
    private boolean exists;
    
    /**
     * 확인한 값 (이메일 또는 닉네임)
     */
    private String checkedValue;
    
    /**
     * 메시지
     */
    private String message;
} 