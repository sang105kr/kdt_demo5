package com.kh.demo.web.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 비즈니스 예외의 기본 클래스
 * 모든 비즈니스 관련 예외는 이 클래스를 상속받아야 함
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, Object> details;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.details = new HashMap<>();
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    public BusinessException(String message, Map<String, Object> details) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.details = details;
    }
    
    public BusinessException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.details = new HashMap<>();
    }
} 