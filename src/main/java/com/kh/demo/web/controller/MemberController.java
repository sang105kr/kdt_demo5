package com.kh.demo.web.controller;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.controller.member.JoinForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberController extends BaseController {

  private final MemberSVC memberSVC;

  //회원가입 화면
  @GetMapping("/join")
  public String joinForm(Model model) {
    model.addAttribute("joinForm", new JoinForm());
    return "member/joinForm";
  }

  //회원가입 처리
  @PostMapping("/join")
  public String join(@Valid @ModelAttribute JoinForm joinForm,
                     BindingResult bindingResult,
                     Model model,
                     RedirectAttributes redirectAttributes) {
    log.info("joinForm={}", joinForm);

    //1) 유효성 검증
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "member/joinForm";
    }

    //2) 회원가입 처리
    try {
      Member member = new Member();
      BeanUtils.copyProperties(joinForm, member);
      
      Member savedMember = memberSVC.join(member);
      log.info("회원가입 성공: memberId={}", savedMember.getMemberId());
      
      redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
      return "redirect:/login";
      
    } catch (Exception e) {
      log.error("회원가입 실패", e);
      model.addAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다.");
      return "member/joinForm";
    }
  }
} 