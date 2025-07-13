package com.kh.demo.web.controller;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.web.controller.form.login.LoginForm;
import com.kh.demo.web.controller.form.login.LoginMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.exception.LoginFailException;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController extends BaseController {

  private final com.kh.demo.domain.member.svc.MemberSVC memberSVC;

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
      bindingResult.reject("loginNotExist", "회원정보가 없습니다. 이메일을 확인해 주세요.");
      return "login/loginForm";
    }

    // 2) 비밀번호 인증
    try {
      Member member = memberSVC.loginOrThrow(loginForm.getEmail(), loginForm.getPasswd());
      LoginMember loginMember = new LoginMember(member.getMemberId(), member.getEmail(), member.getNickname(), String.valueOf(member.getGubun()));

      // 세션 고정 공격 방지: 기존 세션 무효화 후 새 세션 생성
      HttpSession oldSession = request.getSession(false);
      if (oldSession != null) oldSession.invalidate();
      HttpSession session = request.getSession(true);
      session.setAttribute("loginMember", loginMember);
      return "redirect:/";
    } catch (LoginFailException e) {
      bindingResult.reject("loginFail", e.getMessage());
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