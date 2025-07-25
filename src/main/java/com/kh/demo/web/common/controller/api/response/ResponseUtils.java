package com.kh.demo.web.common.controller.api.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 응답 생성 유틸리티
 */
public class ResponseUtils {
    
    /**
     * 성공 응답 생성
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        ApiResponse<T> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 생성 성공 응답
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        ApiResponse<T> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 페이징 응답 생성
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> paged(List<T> data, int pageNo, int numOfRows, int totalCount) {
        ApiResponse<List<T>> response = ApiResponse.of(
            ApiResponseCode.SUCCESS,
            data,
            new ApiResponse.Paging(pageNo, numOfRows, totalCount)
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * 에러 응답 생성
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ApiResponseCode code, String message) {
        ApiResponse<T> response = ApiResponse.of(code, null);
        return ResponseEntity.ok(response);
    }
} 