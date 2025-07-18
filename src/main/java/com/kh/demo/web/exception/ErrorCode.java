package com.kh.demo.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 에러 코드 정의
 * 모든 비즈니스 에러 코드를 중앙에서 관리
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 공통 에러
    INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다."),
    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", "요청한 데이터를 찾을 수 없습니다."),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    
    // 회원 관련
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다."),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    
    // 상품 관련
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "재고가 부족합니다."),
    INVALID_STOCK_INFO("INVALID_STOCK_INFO", "재고 정보를 확인할 수 없습니다."),
    
    // 장바구니 관련
    CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", "장바구니 아이템을 찾을 수 없습니다."),
    CART_EMPTY("CART_EMPTY", "장바구니가 비어있습니다."),
    INVALID_QUANTITY("INVALID_QUANTITY", "유효하지 않은 수량입니다."),
    
    // 주문 관련
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", "유효하지 않은 주문 상태입니다."),
    ORDER_CANCEL_FAILED("ORDER_CANCEL_FAILED", "주문 취소에 실패했습니다."),
    
    // 게시판 관련
    BOARD_NOT_FOUND("BOARD_NOT_FOUND", "게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_MODIFICATION("UNAUTHORIZED_MODIFICATION", "수정 권한이 없습니다."),
    UNAUTHORIZED_DELETION("UNAUTHORIZED_DELETION", "삭제 권한이 없습니다."),
    
    // 파일 관련
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND("FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "파일 크기가 제한을 초과했습니다."),
    
    // 검색 관련
    SEARCH_FAILED("SEARCH_FAILED", "검색에 실패했습니다."),
    INVALID_SEARCH_CRITERIA("INVALID_SEARCH_CRITERIA", "유효하지 않은 검색 조건입니다.");
    
    private final String code;
    private final String defaultMessage;
    
    public BusinessException toException() {
        return new BusinessException(this.code, this.defaultMessage);
    }
    
    public BusinessException toException(String message) {
        return new BusinessException(this.code, message);
    }
    
    public BusinessException toException(Map<String, Object> details) {
        return new BusinessException(this.code, this.defaultMessage, details);
    }
} 