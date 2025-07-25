package com.kh.demo.web.common.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

/**
 * 모든 REST API 컨트롤러의 기본 클래스
 * 공통 기능들을 제공
 */
@Slf4j
public abstract class BaseApiController {
    
    /**
     * 성공 응답 생성
     */
    protected <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }
    
    /**
     * 생성 성공 응답
     */
    protected <T> ResponseEntity<T> created(T data) {
        return ResponseEntity.status(201).body(data);
    }
    
    /**
     * 삭제 성공 응답
     */
    protected ResponseEntity<Void> deleted() {
        return ResponseEntity.noContent().build();
    }
} 