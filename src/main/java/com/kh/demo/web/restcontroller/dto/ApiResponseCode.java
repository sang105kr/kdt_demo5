package com.kh.demo.web.restcontroller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 응답 코드 열거형
 */
@Getter
@AllArgsConstructor
public enum ApiResponseCode {
    SUCCESS("00", "정상 처리되었습니다."),
    VALIDATION_ERROR("01", "입력값이 올바르지 않습니다."),
    BUSINESS_ERROR("02", "비즈니스 규칙 위반입니다."),
    ENTITY_NOT_FOUND("03", "요청한 데이터를 찾을 수 없습니다."),
    UNAUTHORIZED("04", "인증이 필요합니다."),
    FORBIDDEN("05", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("99", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;
} 