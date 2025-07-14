package com.kh.demo.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.web.controller.form.login.LoginMember;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * 모든 SSR 컨트롤러의 기본 클래스
 * 공통 기능들을 제공
 */
@Slf4j
public abstract class BaseController {
    @Autowired
    protected CodeSVC codeSVC;

    /**
     * 공통 모델 속성 설정
     */
    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("boardCategories")
    public List<Code> boardCategories() {
      return codeSVC.findByGcode("BOARD").stream()
      .filter(c -> c.getPcode() == 0L)
      .findFirst()
      .map(parent -> codeSVC.findByPcode(parent.getCodeId()))
      .orElse(List.of());      
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
    
    /**
     * 로그인된 사용자 이메일 가져오기
     */
    protected String getLoginMemberEmail() {
        HttpSession session = getCurrentSession();
        if (session != null) {
            LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
            return loginMember != null ? loginMember.getEmail() : null;
        }
        return null;
    }
    
    /**
     * 로그인된 사용자 닉네임 가져오기
     */
    protected String getLoginMemberNickname() {
        HttpSession session = getCurrentSession();
        if (session != null) {
            LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
            return loginMember != null ? loginMember.getNickname() : null;
        }
        return null;
    }
    
    /**
     * 현재 세션 가져오기
     */
    private HttpSession getCurrentSession() {
        try {
            return ((HttpServletRequest) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getSession();
        } catch (Exception e) {
            return null;
        }
    }
} 