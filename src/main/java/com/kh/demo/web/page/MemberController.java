package com.kh.demo.web.page;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.page.form.member.JoinForm;
import com.kh.demo.domain.common.entity.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberController extends BaseController {

  private final MemberSVC memberSVC;
  private final CodeSVC codeSVC;
  private final MessageSource messageSource;

  @ModelAttribute("regionCodes")
  public List<Code> regionCodes() {
    return codeSVC.findByGcode("REGION").stream()
        .filter(c -> c.getPcode() == 0L)
        .findFirst()
        .map(parent -> codeSVC.findByPcode(parent.getCodeId()))
        .orElse(List.of());
  }

  @ModelAttribute("genderCodes")
  public List<Code> genderCodes() {
    return codeSVC.findByGcode("GENDER");
  }

  @ModelAttribute("hobbyCodes")
  public List<Code> hobbyCodes() {
    return codeSVC.findByGcode("HOBBY").stream()
        .filter(c -> c.getPcode() == 0L)
        .findFirst()
        .map(parent -> codeSVC.findByPcode(parent.getCodeId()))
        .orElse(List.of());
  }

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
      String errorMessage = messageSource.getMessage("member.validation.error", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/joinForm";
    }

    //2) 회원가입 처리
    try {
      Member member = new Member();
      BeanUtils.copyProperties(joinForm, member);

      // hobby: List<String> → String(콤마구분)
      if (joinForm.getHobby() != null) {
        member.setHobby(String.join(",", joinForm.getHobby()));
      }
      // gender는 그대로 복사됨
      Member savedMember = memberSVC.join(member);
      log.info("회원가입 성공: memberId={}", savedMember.getMemberId());
      String successMessage = messageSource.getMessage("member.join.success", null, null);
      redirectAttributes.addFlashAttribute("message", successMessage);
      return "redirect:/login";

    } catch (Exception e) {
      log.error("회원가입 실패", e);
      String errorMessage = messageSource.getMessage("member.join.failed", null, null);
      model.addAttribute("errorMessage", errorMessage);
      return "member/joinForm";
    }
  }
} 