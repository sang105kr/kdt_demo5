package com.kh.demo.web.controller;

import com.kh.demo.web.controller.login.LoginForm;
import com.kh.demo.web.controller.login.LoginMember;
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

@Slf4j
@RequestMapping("/login")
@Controller
@RequiredArgsConstructor
public class LoginController extends BaseController {

  //로그인 화면
  @GetMapping
  public String loginForm(Model model) {
    model.addAttribute("loginForm", new LoginForm());
    return "login/loginForm";
  }

  //로그인 처리
  @PostMapping
  public String login(@Valid @ModelAttribute LoginForm loginForm,
                      BindingResult bindingResult,
                      HttpServletRequest request,
                      Model model) {
    log.info("loginForm={}", loginForm);

    //1) 유효성 검증
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "login/loginForm";
    }

    //2) 로그인 처리
    //TODO: 로그인 로직 구현
    LoginMember loginMember = new LoginMember(1L, loginForm.getEmail(), "테스트닉네임", "M01A");

    //3) 세션에 로그인 정보 저장
    HttpSession session = request.getSession(true);
    session.setAttribute("loginMember", loginMember);

    //4) 홈으로 리다이렉트
    return "redirect:/";
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