package com.kh.demo.domain.common.util;

import com.kh.demo.domain.common.exception.BusinessValidationException;

/**
 * 검증 공통 유틸리티
 */
public class ValidationUtils {
    
    /**
     * null 체크
     * @param value 검증할 값
     * @param fieldName 필드명
     * @throws BusinessValidationException null인 경우
     */
    public static void notNull(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessValidationException(fieldName + "은(는) 필수입니다.");
        }
    }
    
    /**
     * 빈 문자열 체크
     * @param value 검증할 문자열
     * @param fieldName 필드명
     * @throws BusinessValidationException 빈 문자열인 경우
     */
    public static void notEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessValidationException(fieldName + "은(는) 필수입니다.");
        }
    }
    
    /**
     * 최소 길이 체크
     * @param value 검증할 문자열
     * @param minLength 최소 길이
     * @param fieldName 필드명
     * @throws BusinessValidationException 최소 길이보다 짧은 경우
     */
    public static void minLength(String value, int minLength, String fieldName) {
        if (value != null && value.length() < minLength) {
            throw new BusinessValidationException(fieldName + "은(는) 최소 " + minLength + "자 이상이어야 합니다.");
        }
    }
    
    /**
     * 최대 길이 체크
     * @param value 검증할 문자열
     * @param maxLength 최대 길이
     * @param fieldName 필드명
     * @throws BusinessValidationException 최대 길이보다 긴 경우
     */
    public static void maxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessValidationException(fieldName + "은(는) 최대 " + maxLength + "자까지 가능합니다.");
        }
    }
    
    /**
     * 최소값 체크
     * @param value 검증할 숫자
     * @param minValue 최소값
     * @param fieldName 필드명
     * @throws BusinessValidationException 최소값보다 작은 경우
     */
    public static void minValue(Number value, Number minValue, String fieldName) {
        if (value != null && value.doubleValue() < minValue.doubleValue()) {
            throw new BusinessValidationException(fieldName + "은(는) 최소 " + minValue + " 이상이어야 합니다.");
        }
    }
    
    /**
     * 최대값 체크
     * @param value 검증할 숫자
     * @param maxValue 최대값
     * @param fieldName 필드명
     * @throws BusinessValidationException 최대값보다 큰 경우
     */
    public static void maxValue(Number value, Number maxValue, String fieldName) {
        if (value != null && value.doubleValue() > maxValue.doubleValue()) {
            throw new BusinessValidationException(fieldName + "은(는) 최대 " + maxValue + "까지 가능합니다.");
        }
    }
    
    /**
     * 양수 체크
     * @param value 검증할 숫자
     * @param fieldName 필드명
     * @throws BusinessValidationException 양수가 아닌 경우
     */
    public static void positive(Number value, String fieldName) {
        if (value != null && value.doubleValue() <= 0) {
            throw new BusinessValidationException(fieldName + "은(는) 양수여야 합니다.");
        }
    }
    
    /**
     * 0 이상 체크
     * @param value 검증할 숫자
     * @param fieldName 필드명
     * @throws BusinessValidationException 0보다 작은 경우
     */
    public static void nonNegative(Number value, String fieldName) {
        if (value != null && value.doubleValue() < 0) {
            throw new BusinessValidationException(fieldName + "은(는) 0 이상이어야 합니다.");
        }
    }
    
    /**
     * 이메일 형식 체크
     * @param email 검증할 이메일
     * @param fieldName 필드명
     * @throws BusinessValidationException 이메일 형식이 아닌 경우
     */
    public static void validEmail(String email, String fieldName) {
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessValidationException(fieldName + "의 형식이 올바르지 않습니다.");
        }
    }
    
    /**
     * 조건 체크
     * @param condition 조건
     * @param message 오류 메시지
     * @throws BusinessValidationException 조건이 false인 경우
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BusinessValidationException(message);
        }
    }
} 