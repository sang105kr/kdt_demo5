package com.kh.demo.web.common.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.util.MemberAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 모든 SSR 컨트롤러의 기본 클래스
 * 공통 기능들을 제공
 */
@Slf4j
public abstract class BaseController {
    @Autowired
    protected CodeSVC codeSVC;

    @Autowired
    protected MemberAuthUtil memberAuthUtil;

    @Autowired
    protected MessageSource messageSource;

    /**
     * 공통 모델 속성 설정
     */
    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * 상품 카테고리 목록 가져오기
    */
    @ModelAttribute("productCategories")
    public List<Code> productCategories() {
        return codeSVC.getCodeList("PRODUCT_CATEGORY").stream()
            .filter(code -> "Y".equals(code.getUseYn()) && code.getPcode() != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 게시판 카테고리 목록 가져오기
     */
    @ModelAttribute("boardCategories")
    public List<Code> boardCategories() {
        return codeSVC.getCodeList("BOARD").stream()
            .filter(code -> "Y".equals(code.getUseYn()) && code.getPcode() != null)
            .collect(Collectors.toList());
    }

    /**
     * 세션 정보 확인
     */
    protected boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(SessionConst.LOGIN_MEMBER) != null;
    }
    
    /**
     * 로그인된 사용자 정보 가져오기
     */
    protected Object getLoginMember(HttpSession session) {
        return session != null ? session.getAttribute(SessionConst.LOGIN_MEMBER) : null;
    }
    
    /**
     * 로그인된 사용자 이메일 가져오기
     */
    protected String getLoginMemberEmail() {
        HttpSession session = getCurrentSession();
        if (session != null) {
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
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
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
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

    /**
     * 현재 로그인한 회원이 관리자인지 확인
     */
    protected boolean isCurrentUserAdmin(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return false;
        }
        return memberAuthUtil.isAdmin(loginMember.getMemberId());
    }

    /**
     * 현재 로그인한 회원이 VIP인지 확인
     */
    protected boolean isCurrentUserVip(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return false;
        }
        return memberAuthUtil.isVip(loginMember.getMemberId());
    }

    /**
     * 관리자 권한이 필요함을 명시 (예외 발생)
     */
    protected void requireAdmin(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            throw new SecurityException("로그인이 필요합니다.");
        }
        memberAuthUtil.requireAdmin(loginMember.getMemberId());
    }

    /**
     * VIP 권한이 필요함을 명시 (예외 발생)
     */
    protected void requireVip(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            throw new SecurityException("로그인이 필요합니다.");
        }
        memberAuthUtil.requireVip(loginMember.getMemberId());
    }

    /**
     * View단 권한 체크를 위한 모델 속성 추가
     */
    protected void addAuthInfoToModel(Model model, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        
        if (loginMember != null) {
            model.addAttribute("isAdmin", memberAuthUtil.isAdmin(loginMember.getMemberId()));
            model.addAttribute("isVip", memberAuthUtil.isVip(loginMember.getMemberId()));
            model.addAttribute("currentMemberId", loginMember.getMemberId());
        } else {
            model.addAttribute("isAdmin", false);
            model.addAttribute("isVip", false);
            model.addAttribute("currentMemberId", null);
        }
    }

    /**
     * View단에서 사용할 권한 체크 유틸리티 메서드들
     */
    protected boolean hasCurrentUserPermission(HttpSession session, String permission) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember != null && memberAuthUtil.hasPermission(loginMember.getMemberId(), permission);
    }

    /**
     * 메시지 소스에서 메시지를 가져오는 유틸리티 메서드
     */
    protected String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null) {
            locale = Locale.KOREA;
        }
        return messageSource.getMessage(code, null, locale);
    }

    protected String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    protected String getMessage(String code, Object[] args, String defaultMessage) {
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    protected String getMessage(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 모든 컨트롤러에서 공통으로 사용할 권한 정보를 모델에 추가
     */
    @ModelAttribute("authInfo")
    public Map<String, Object> getAuthInfo(HttpSession session) {
        Map<String, Object> authInfo = new HashMap<>();
        
        if (session != null && session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            
            authInfo.put("isLoggedIn", true);
            authInfo.put("isAdmin", memberAuthUtil.isAdmin(loginMember.getMemberId()));
            authInfo.put("isVip", memberAuthUtil.isVip(loginMember.getMemberId()));
            authInfo.put("isNormal", !memberAuthUtil.isAdmin(loginMember.getMemberId()) && !memberAuthUtil.isVip(loginMember.getMemberId()));
            authInfo.put("memberId", loginMember.getMemberId());
            authInfo.put("email", loginMember.getEmail());
            authInfo.put("nickname", loginMember.getNickname());
            authInfo.put("gubun", loginMember.getGubun());
        } else {
            authInfo.put("isLoggedIn", false);
            authInfo.put("isAdmin", false);
            authInfo.put("isVip", false);
            authInfo.put("isNormal", false);
            authInfo.put("memberId", null);
            authInfo.put("email", null);
            authInfo.put("nickname", null);
            authInfo.put("gubun", null);
        }
        
        return authInfo;
    }
} 