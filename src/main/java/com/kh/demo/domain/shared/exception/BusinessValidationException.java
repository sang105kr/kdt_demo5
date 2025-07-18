package com.kh.demo.domain.shared.exception;

/**
 * 비즈니스 검증 예외
 * 도메인 레벨에서 발생하는 비즈니스 규칙 위반 시 사용
 */
public class BusinessValidationException extends RuntimeException {
    
    public BusinessValidationException(String message) {
        super(message);
    }
    
    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessValidationException(Throwable cause) {
        super(cause);
    }
} 