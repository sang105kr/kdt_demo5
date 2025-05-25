package com.kh.demo.web;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.form.member.JoinForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberSVC memberSVC;
  private final CodeSVC codeSVC;

  //가입화면
  @GetMapping("/join")        // GET /members/join
  public String joinForm(Model model){

    model.addAttribute("joinForm",new JoinForm());
    return "member/joinForm";
  }

  //가입처리
  @PostMapping("/join")      // POST /members/join
  public String join(
      @Valid @ModelAttribute JoinForm joinForm,
      BindingResult bindingResult){

    log.info("joinForm={}", joinForm);

    // 유효성 체크
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "/member/joinForm";
    }

    // 회원가입 정상로직 처리
    Member member = new Member();
    BeanUtils.copyProperties(joinForm,member);

    // List의 취미요소를 콤마를 구분자로하여 문자열로 변환
    member.setHobby(String.join(",",joinForm.getHobby()));
    Member joinedMember = memberSVC.join(member);

    return "redirect:/login";    // 302 GET http://localhost:9080/login
  }
}
