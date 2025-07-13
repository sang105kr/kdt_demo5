package com.kh.demo.web;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.entity.Member;
import com.kh.demo.domain.entity.Code;
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

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberSVC memberSVC;
  private final CodeSVC codeSVC;

  //취미 코드 가져오기
  @ModelAttribute("hobbyCodes")
  public List<Code> getHobbies() {
    return codeSVC.findActiveByGcode("H01");
  }

  //지역 코드 가져오기
  @ModelAttribute("regionCodes")
  public List<Code> getRegion() {
    return codeSVC.findActiveByGcode("A02");
  }

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
      BindingResult bindingResult
//      Model model
  ){

    log.info("joinForm={}", joinForm);

    // 유효성 체크
    //1) 필드오류
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
//      model.addAttribute("joinForm", joinForm);
      return "member/joinForm";
    }
    //2) 글로벌 오류
    if(!joinForm.getPasswd().equals(joinForm.getPasswdChk())){
      bindingResult.reject("passwdErr","비밀번호와 비밀번호확인 값이 일지 하지 않습니다!");
    }
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
//      model.addAttribute("joinForm", joinForm);
      return "member/joinForm";
    }

    // 회원가입 정상로직 처리
    Member member = new Member();
    BeanUtils.copyProperties(joinForm,member);

    // List의 취미요소를 콤마를 구분자로하여 문자열로 변환 (null 방지)
    member.setHobby(joinForm.getHobby() != null ? String.join(",",joinForm.getHobby()) : null);
    Member joinedMember = memberSVC.join(member);

    return "redirect:/login";    // 302 GET http://localhost:9080/login
  }
}
