package com.kh.demo.web;

import com.kh.demo.common.exception.BusinessException;
import com.kh.demo.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 예외 로깅 테스트용 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/test/exception")
public class TestExceptionController {

    /**
     * BusinessException 테스트
     */
    @GetMapping("/business")
    public String testBusinessException() {
        log.info("BusinessException 테스트 시작");
        
        Map<String, Object> details = new HashMap<>();
        details.put("productId", 123);
        details.put("userId", "testUser");
        
        throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND.name(), "테스트용 상품을 찾을 수 없습니다.", details);
    }

    /**
     * NoSuchElementException 테스트
     */
    @GetMapping("/no-such-element")
    public String testNoSuchElementException() {
        log.info("NoSuchElementException 테스트 시작");
        throw new NoSuchElementException("테스트용 엔티티를 찾을 수 없습니다.");
    }

    /**
     * 일반 Exception 테스트
     */
    @GetMapping("/general")
    public String testGeneralException() {
        log.info("General Exception 테스트 시작");
        throw new RuntimeException("테스트용 일반 예외입니다.");
    }

    /**
     * NullPointerException 테스트
     */
    @GetMapping("/null-pointer")
    public String testNullPointerException() {
        log.info("NullPointerException 테스트 시작");
        String nullString = null;
        return nullString.toString(); // 의도적으로 NPE 발생
    }
} 