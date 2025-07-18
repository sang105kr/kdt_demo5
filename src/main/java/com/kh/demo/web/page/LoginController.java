package com.kh.demo.web.page;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.web.page.form.login.LoginForm;
import com.kh.demo.web.page.form.login.LoginMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.kh.demo.web.exception.LoginFailException;
import com.kh.demo.web.session.SessionConst;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController extends BaseController {

  private final com.kh.demo.domain.member.svc.MemberSVC memberSVC;
  private final MessageSource messageSource;

  //로그인 화면
  @GetMapping("/login")
  public String loginForm(Model model) {
    model.addAttribute("loginForm", new LoginForm());
    return "login/loginForm";
  }

  //로그인 처리
  @PostMapping("/login")
  public String login(@Valid @ModelAttribute LoginForm loginForm,
                      BindingResult bindingResult,
                      HttpServletRequest request,
                      Model model) {
    log.info("loginForm={}", loginForm);

    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "login/loginForm";
    }

    // 1) 회원 존재 여부 체크
    if (!memberSVC.isMember(loginForm.getEmail())) {
      String errorMessage = messageSource.getMessage("member.login.not.exist", null, null);
      bindingResult.reject("loginNotExist", errorMessage);
      return "login/loginForm";
    }

    // 2) 비밀번호 인증
    try {
      Member member = memberSVC.login(loginForm.getEmail(), loginForm.getPasswd());
      LoginMember loginMember = new LoginMember(
          member.getMemberId(),
          member.getEmail(),
          member.getNickname(),
          member.getGubun(),
          // 프로필 사진 존재 여부
          member.getPic() != null
      );

      // 세션 고정 공격 방지: 기존 세션 무효화 후 새 세션 생성
      HttpSession oldSession = request.getSession(false);
      if (oldSession != null) oldSession.invalidate();

      HttpSession session = request.getSession(true);
      session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

      // 3) 원래 요청 페이지로 리다이렉트
      String redirectURL = request.getParameter("redirectURL");
      if (redirectURL != null && !redirectURL.trim().isEmpty()) {
        log.info("로그인 성공 후 원래 요청 페이지로 리다이렉트: {}", redirectURL);
        return "redirect:" + redirectURL;
      } else {
        log.info("로그인 성공 후 홈페이지로 리다이렉트");
        return "redirect:/";
      }

    } catch (LoginFailException e) {
      String errorMessage = messageSource.getMessage("member.login.failed", null, null);
      bindingResult.reject("loginFail", errorMessage);
      return "login/loginForm";
    }
  }

  //로그아웃
  @GetMapping("/logout")
  public String logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return "redirect:/";
  }
} 