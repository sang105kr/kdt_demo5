package com.kh.demo.web.common.interceptor;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.util.MemberAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 권한 정보를 모든 요청에 추가하는 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final MemberAuthUtil memberAuthUtil;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
                if (loginMember != null) {
                    // 권한 정보를 모델에 추가
                    modelAndView.addObject("isAdmin", memberAuthUtil.isAdmin(loginMember.getMemberId()));
                    modelAndView.addObject("isVip", memberAuthUtil.isVip(loginMember.getMemberId()));
                    modelAndView.addObject("isNormal", !memberAuthUtil.isAdmin(loginMember.getMemberId()) && !memberAuthUtil.isVip(loginMember.getMemberId()));
                } else {
                    // 로그인하지 않은 경우
                    modelAndView.addObject("isAdmin", false);
                    modelAndView.addObject("isVip", false);
                    modelAndView.addObject("isNormal", false);
                }
            } else {
                // 세션이 없는 경우
                modelAndView.addObject("isAdmin", false);
                modelAndView.addObject("isVip", false);
                modelAndView.addObject("isNormal", false);
            }
        }
    }
}
