package com.kh.demo.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 모든 SSR 컨트롤러의 기본 클래스
 * 공통 기능들을 제공
 */
@Slf4j
public abstract class BaseController {
    
    /**
     * 공통 모델 속성 설정
     */
    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
    
    /**
     * 세션 정보 확인
     */
    protected boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("loginMember") != null;
    }
    
    /**
     * 로그인된 사용자 정보 가져오기
     */
    protected Object getLoginMember(HttpSession session) {
        return session != null ? session.getAttribute("loginMember") : null;
    }
} 