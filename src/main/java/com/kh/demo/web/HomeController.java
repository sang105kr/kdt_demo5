package com.kh.demo.web;

import com.kh.demo.web.form.login.LoginMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

  @GetMapping("/")
  public String home(HttpServletRequest request) {
    String view = null;

    HttpSession session = request.getSession(false);
    //로그인 전
    if (session == null || session.getAttribute("loginMember") == null) {
      view = "beforeLogin";

    } else {
      //로그인 후
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");

      //관리자
//      if("M01A".equals(loginMember.getGubun().substring(0,4))){
//        view = "admin";
//      } else{
//        view = "afterLogin";
//      }

      log.info("loginMember={}", loginMember);
      view = "M01A".equals(loginMember.getGubun().substring(0, 4)) ? "admin" : "afterLogin";

    }
    return view;
  }
}
