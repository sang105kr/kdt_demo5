package com.kh.demo.web;


import com.kh.demo.domain.member.dao.MemberDAO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

  private final MemberDAO memberDAO;

  //로그인화면
  @GetMapping("/login")      // GET http://localhost:9080/login
  public String loginForm(){

    return "login/loginForm";
  }

  //로그인처리
  @PostMapping("/login")     // POST http://localhost:9080/login
  public String login() {


    return "redirect:/";    // 초기화면
  }
}
