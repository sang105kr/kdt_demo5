package com.kh.demo.web.member.controller.page;

import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.web.member.controller.page.form.*;
import com.kh.demo.web.member.mapper.MemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * 회원 인증 관련 컨트롤러
 * - 회원가입
 * - 이메일 인증
 * - 비밀번호 찾기/재설정
 * - 아이디 찾기
 */
@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberAuthController extends BaseController {

    private final MemberSVC memberSVC;
    private final CodeSVC codeSVC;
    private final MessageSource messageSource;
    private final MemberMapper memberMapper;

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

    /**
     * Member 엔티티를 MemberDTO로 변환
     */
    private MemberDTO convertToMemberDTO(Member member) {
        MemberDTO memberDTO = new MemberDTO();
        BeanUtils.copyProperties(member, memberDTO);
        
        // 코드 값들을 디코드로 변환
        if (member.getRegion() != null) {
            Optional<Code> regionCode = codeSVC.findById(member.getRegion());
            regionCode.ifPresent(code -> memberDTO.setRegionName(code.getDecode()));
        }
        
        if (member.getGender() != null) {
            // gender는 String 타입이므로 직접 사용
            memberDTO.setGenderName(member.getGender());
        }
        
        if (member.getHobby() != null) {
            // hobby는 String 타입이므로 직접 사용
            memberDTO.setHobby(member.getHobby());
        }
        
        return memberDTO;
    }

    /**
     * 회원가입 폼
     */
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("joinForm", new JoinForm());
        return "member/join/joinForm";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinForm joinForm,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes redirectAttributes,
                      HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            return "member/join/joinForm";
        }

        try {
            // 이메일 중복 확인
            if (memberSVC.isMember(joinForm.getEmail())) {
                bindingResult.rejectValue("email", "duplicate.email", "이미 사용 중인 이메일입니다.");
                return "member/join/joinForm";
            }
            // 이메일 인증을 위한 임시 세션 저장
            HttpSession session = request.getSession();
            session.setAttribute("pendingJoinForm", joinForm);
            
            // 이메일 인증 코드 발송
            memberSVC.sendVerificationCode(joinForm.getEmail());
            
            redirectAttributes.addFlashAttribute("message", "이메일로 인증 코드가 발송되었습니다. 인증을 완료해주세요.");
            return "redirect:/member/email/verify";

        } catch (BusinessValidationException e) {
            bindingResult.reject("join.error", e.getMessage());
            return "member/join/joinForm";
        }
    }

    /**
     * 이메일 인증 폼
     */
    @GetMapping("/email/verify")
    public String emailVerificationForm(Model model, HttpServletRequest request) {
        // 세션에서 이메일 정보 가져오기
        HttpSession session = request.getSession();
        JoinForm pendingJoinForm = (JoinForm) session.getAttribute("pendingJoinForm");
        
        EmailVerificationForm form = new EmailVerificationForm();
        if (pendingJoinForm != null) {
            form.setEmail(pendingJoinForm.getEmail());
        } else {
            // 세션에 이메일 정보가 없으면 회원가입 페이지로 리다이렉트
            return "redirect:/member/join";
        }
        
        model.addAttribute("emailVerificationForm", form);
        return "member/join/emailVerification";
    }

    /**
     * 인증 코드 발송
     */
    @PostMapping("/email/send-code")
    public String sendVerificationCode(@RequestParam String email, 
                                     RedirectAttributes redirectAttributes) {
        try {
            memberSVC.sendVerificationCode(email);
            redirectAttributes.addFlashAttribute("message", "인증 코드가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "인증 코드 발송에 실패했습니다.");
        }
        return "redirect:/member/email/verify";
    }
    
    /**
     * 인증 코드 재발송 (세션에서 이메일 가져오기)
     */
    @PostMapping("/email/resend-code")
    public String resendVerificationCode(RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            JoinForm pendingJoinForm = (JoinForm) session.getAttribute("pendingJoinForm");
            
            if (pendingJoinForm != null && pendingJoinForm.getEmail() != null) {
                memberSVC.sendVerificationCode(pendingJoinForm.getEmail());
                redirectAttributes.addFlashAttribute("message", "인증 코드가 재발송되었습니다.");
            } else {
                // 세션에 이메일 정보가 없으면 회원가입 페이지로 리다이렉트
                redirectAttributes.addFlashAttribute("error", "회원가입 정보를 찾을 수 없습니다. 다시 회원가입해주세요.");
                return "redirect:/member/join";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "인증 코드 재발송에 실패했습니다.");
        }
        return "redirect:/member/email/verify";
    }

    /**
     * 이메일 인증 처리
     */
    @PostMapping("/email/verify")
    public String verifyEmailCode(@Valid @ModelAttribute EmailVerificationForm emailVerificationForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            return "member/emailVerification";
        }

        try {
            boolean verified = memberSVC.verifyEmailCode(emailVerificationForm.getEmail(), 
                                                       emailVerificationForm.getVerificationCode());
            
            if (verified) {
                // 인증 성공 시 세션에서 회원가입 정보 가져와서 실제 회원가입 처리
                HttpSession session = request.getSession();
                JoinForm pendingJoinForm = (JoinForm) session.getAttribute("pendingJoinForm");
                
                if (pendingJoinForm != null && pendingJoinForm.getEmail().equals(emailVerificationForm.getEmail())) {
                    // 매퍼를 사용한 회원 생성
                    Member member = memberMapper.toMember(pendingJoinForm);
                    Member savedMember = memberSVC.join(member);
                    
                    // 세션에서 임시 데이터 제거
                    session.removeAttribute("pendingJoinForm");
                    
                    redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
                    return "redirect:/login";
                } else {
                    // 세션에 회원가입 정보가 없거나 이메일이 일치하지 않는 경우
                    redirectAttributes.addFlashAttribute("error", "회원가입 정보를 찾을 수 없습니다. 다시 회원가입해주세요.");
                    return "redirect:/member/join";
                }
            } else {
                bindingResult.reject("verification.error", "인증 코드가 올바르지 않습니다.");
                return "member/emailVerification";
            }
        } catch (Exception e) {
            bindingResult.reject("verification.error", "인증 처리 중 오류가 발생했습니다.");
            return "member/emailVerification";
        }
    }

    /**
     * 비밀번호 찾기 폼
     */
    @GetMapping("/password/find")
    public String passwordFindForm(Model model) {
        return "member/passwordFind";
    }

    /**
     * 비밀번호 재설정 토큰 발송
     */
    @PostMapping("/password/send-token")
    public String sendPasswordResetToken(@RequestParam String email, 
                                       RedirectAttributes redirectAttributes) {
        try {
            memberSVC.sendPasswordResetToken(email);
            redirectAttributes.addFlashAttribute("message", "비밀번호 재설정 링크가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "비밀번호 재설정 토큰 발송에 실패했습니다.");
        }
        return "redirect:/member/password/find";
    }

    /**
     * 비밀번호 재설정 폼
     */
    @GetMapping("/password/reset")
    public String passwordResetForm(@RequestParam String token, Model model) {
        PasswordResetForm form = new PasswordResetForm();
        form.setToken(token);
        model.addAttribute("passwordResetForm", form);
        return "member/passwordReset";
    }

    /**
     * 비밀번호 재설정 처리
     */
    @PostMapping("/password/reset")
    public String passwordReset(@Valid @ModelAttribute PasswordResetForm passwordResetForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "member/passwordReset";
        }

        try {
            boolean reset = memberSVC.resetPassword(passwordResetForm.getToken(), 
                                                  passwordResetForm.getNewPassword());
            
            if (reset) {
                redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 재설정되었습니다. 로그인해주세요.");
                return "redirect:/login";
            } else {
                bindingResult.reject("reset.error", "비밀번호 재설정에 실패했습니다.");
                return "member/passwordReset";
            }
        } catch (Exception e) {
            bindingResult.reject("reset.error", "비밀번호 재설정 중 오류가 발생했습니다.");
            return "member/passwordReset";
        }
    }

    /**
     * 아이디 찾기 폼
     */
    @GetMapping("/id/find")
    public String findIdForm(Model model) {
        model.addAttribute("findIdForm", new FindIdForm());
        return "member/findId";
    }

    /**
     * 아이디 찾기 처리
     */
    @PostMapping("/id/find")
    public String findId(@Valid @ModelAttribute FindIdForm findIdForm,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "member/findId";
        }

        try {
            Optional<String> emailOpt = memberSVC.findEmailByPhoneAndBirth(findIdForm.getTel(), findIdForm.getBirthDate().toString());
            
            if (emailOpt.isPresent()) {
                // 이메일 주소 마스킹 처리
                String email = emailOpt.get();
                String maskedEmail = maskEmail(email);
                model.addAttribute("foundEmail", maskedEmail);
                model.addAttribute("originalEmail", email);
                model.addAttribute("message", "입력하신 정보로 등록된 이메일을 찾았습니다.");
            } else {
                bindingResult.reject("find.error", "입력하신 정보와 일치하는 계정을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            bindingResult.reject("find.error", "아이디 찾기 중 오류가 발생했습니다.");
        }
        
        return "member/findId";
    }
    
    /**
     * 이메일 주소 마스킹 처리
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email; // @ 앞에 문자가 1개 이하면 마스킹하지 않음
        }
        
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        
        // 로컬 파트의 첫 글자와 마지막 글자만 남기고 나머지는 *로 마스킹
        if (localPart.length() <= 2) {
            return email; // 2글자 이하면 마스킹하지 않음
        }
        
        String maskedLocalPart = localPart.charAt(0) + 
                               "*".repeat(localPart.length() - 2) + 
                               localPart.charAt(localPart.length() - 1);
        
        return maskedLocalPart + domainPart;
    }
} 