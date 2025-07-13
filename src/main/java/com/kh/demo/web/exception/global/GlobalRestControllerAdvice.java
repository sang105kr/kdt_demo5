package com.kh.demo.web.exception.global;

import com.kh.demo.web.restcontroller.dto.ApiResponse;
import com.kh.demo.web.restcontroller.dto.ApiResponseCode;
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
     * 비즈니스 유효성 검증 예외 처리
     */
    @ExceptionHandler(com.kh.demo.web.exception.BusinessValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessValidationException(
        com.kh.demo.web.exception.BusinessValidationException ex) {

        log.error("Business validation error: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
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