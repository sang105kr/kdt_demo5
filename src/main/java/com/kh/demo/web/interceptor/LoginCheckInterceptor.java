package com.kh.demo.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.kh.demo.web.session.SessionConst;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    log.info("LoginCheckInterceptor 실행 - handler={}", handler.getClass());
    
    // 요청 URI 추출
    String requestURI = request.getRequestURI();
    log.debug("요청 URI: {}", requestURI);
    
    // 세션 조회
    HttpSession session = request.getSession(false);
    
    // 로그인 체크: 세션이 없거나 loginMember 정보가 없는 경우
    if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
      log.info("로그인 필요 - 원래 요청: {}", requestURI);
      
      // 원래 요청 URL을 파라미터로 전달하여 로그인 페이지로 리다이렉트
      String redirectUrl = "/login?redirectURL=" + requestURI;
      response.sendRedirect(redirectUrl);
      
      return false; // 컨트롤러 실행 중단
    }
    
    log.debug("로그인 확인됨 - 요청 진행: {}", requestURI);
    return true; // 컨트롤러 실행 허용
  }
}
