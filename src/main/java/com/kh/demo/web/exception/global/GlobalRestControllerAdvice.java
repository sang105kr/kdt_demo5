package com.kh.demo.web.exception.global;

import com.kh.demo.web.api.dto.ApiResponse;
import com.kh.demo.web.api.dto.ApiResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST API 전용 글로벌 예외 처리
 * 모든 @RestController에서 발생하는 예외를 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalRestControllerAdvice {

    /**
     * 유효성 검증 실패 시 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, Object> details = new HashMap<>();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        log.error("Validation error: {}", details);
        
        ApiResponse<Void> response = ApiResponse.withDetails(
                ApiResponseCode.VALIDATION_ERROR,
                details,
                null
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 비즈니스 예외 처리 (통합)
     */
    @ExceptionHandler(com.kh.demo.web.exception.BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
        com.kh.demo.web.exception.BusinessException ex) {

        log.warn("Business error: code={}, message={}", ex.getErrorCode(), ex.getMessage());

        // 에러 코드에 따른 적절한 HTTP 상태 코드 결정
        HttpStatus status = determineHttpStatus(ex.getErrorCode());

        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", ex.getErrorCode());
        details.put("message", ex.getMessage());
        if (ex.getDetails() != null) {
            details.putAll(ex.getDetails());
        }

        ApiResponse<Void> response = ApiResponse.withDetails(
            ApiResponseCode.BUSINESS_ERROR,
            details,
            null
        );

        return ResponseEntity.status(status).body(response);
    }

    /**
     * 비즈니스 유효성 검증 예외 처리 (하위 호환성)
     */
    @ExceptionHandler(com.kh.demo.web.exception.BusinessValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessValidationException(
        com.kh.demo.web.exception.BusinessValidationException ex) {

        log.warn("Business validation error: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", "BUSINESS_VALIDATION_ERROR");
        details.put("message", ex.getMessage());
        if (ex.getDetails() != null) {
            details.putAll(ex.getDetails());
        }

        ApiResponse<Void> response = ApiResponse.withDetails(
            ApiResponseCode.BUSINESS_ERROR,
            details,
            null
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 엔티티를 찾을 수 없을 때 처리
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(
            NoSuchElementException ex) {
        
        log.error("Entity not found: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("message", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.withDetails(
                ApiResponseCode.ENTITY_NOT_FOUND,
                details,
                null
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 에러 코드에 따른 적절한 HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        switch (errorCode) {
            case "PRODUCT_NOT_FOUND":
            case "ORDER_NOT_FOUND":
            case "CART_ITEM_NOT_FOUND":
            case "MEMBER_NOT_FOUND":
            case "BOARD_NOT_FOUND":
            case "FILE_NOT_FOUND":
                return HttpStatus.NOT_FOUND;
            case "INSUFFICIENT_STOCK":
            case "INVALID_ORDER_STATUS":
            case "INVALID_INPUT":
            case "INVALID_FILE_TYPE":
            case "FILE_SIZE_EXCEEDED":
                return HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED":
                return HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN":
            case "UNAUTHORIZED_MODIFICATION":
            case "UNAUTHORIZED_DELETION":
                return HttpStatus.FORBIDDEN;
            case "CART_EMPTY":
            case "DUPLICATE_EMAIL":
            case "INVALID_PASSWORD":
            case "EMAIL_NOT_VERIFIED":
            case "TOKEN_EXPIRED":
                return HttpStatus.CONFLICT;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        
        ApiResponse<Void> response = ApiResponse.of(
                ApiResponseCode.INTERNAL_SERVER_ERROR,
                null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 