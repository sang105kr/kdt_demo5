package com.kh.demo.web.exception.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * SSR 전용 글로벌 예외 처리
 * 모든 @Controller에서 발생하는 예외를 처리
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리 (통합)
     */
    @ExceptionHandler(com.kh.demo.web.exception.BusinessException.class)
    public String handleBusinessException(
            com.kh.demo.web.exception.BusinessException ex,
            RedirectAttributes redirectAttributes) {
        
        log.warn("Business error: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        
        // 에러 코드에 따른 적절한 리다이렉트 처리
        String redirectUrl = determineRedirectUrl(ex.getErrorCode());
        
        redirectAttributes.addFlashAttribute("errorCode", ex.getErrorCode());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        
        // 상세 정보가 있으면 추가
        if (ex.getDetails() != null) {
            redirectAttributes.addFlashAttribute("errorDetails", ex.getDetails());
        }
        
        return redirectUrl;
    }

    /**
     * 비즈니스 예외 처리 (하위 호환성)
     */
    @ExceptionHandler(com.kh.demo.web.exception.BusinessValidationException.class)
    public String handleBusinessValidationException(
            com.kh.demo.web.exception.BusinessValidationException ex,
            RedirectAttributes redirectAttributes) {
        
        log.warn("Business validation error: {}", ex.getMessage());
        
        redirectAttributes.addFlashAttribute("errorCode", "BUSINESS_VALIDATION_ERROR");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    /**
     * 엔티티를 찾을 수 없을 때 처리
     */
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public String handleNoSuchElementException(
            java.util.NoSuchElementException ex,
            RedirectAttributes redirectAttributes) {
        
        log.error("Entity not found: {}", ex.getMessage());
        
        redirectAttributes.addFlashAttribute("errorMessage", "요청한 데이터를 찾을 수 없습니다.");
        return "redirect:/";
    }

    /**
     * 인증 예외 처리 (향후 Spring Security 도입 시 활성화)
     */
    /*
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public String handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex,
            RedirectAttributes redirectAttributes) {
        
        log.error("Access denied: {}", ex.getMessage());
        
        redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
        return "redirect:/login";
    }
    */

    /**
     * 에러 코드에 따른 적절한 리다이렉트 URL 결정
     */
    private String determineRedirectUrl(String errorCode) {
        if (errorCode == null) {
            return "redirect:/";
        }
        
        switch (errorCode) {
            case "PRODUCT_NOT_FOUND":
                return "redirect:/products";
            case "ORDER_NOT_FOUND":
                return "redirect:/orders";
            case "CART_EMPTY":
            case "CART_ITEM_NOT_FOUND":
                return "redirect:/cart";
            case "MEMBER_NOT_FOUND":
            case "UNAUTHORIZED":
                return "redirect:/login";
            case "BOARD_NOT_FOUND":
                return "redirect:/board";
            default:
                return "redirect:/";
        }
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Unhandled exception occurred", ex);
        
        redirectAttributes.addFlashAttribute("errorMessage", "서버 내부 오류가 발생했습니다.");
        return "redirect:/";
    }
} 