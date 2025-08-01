package com.kh.demo.web.member.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.member.controller.page.form.MemberDTO;
import com.kh.demo.web.member.controller.page.form.MypageForm;
import com.kh.demo.web.member.controller.page.form.PasswordChangeForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 마이페이지 관리 컨트롤러
 * - 프로필 수정
 * - 비밀번호 변경
 * - 회원 탈퇴
 */
@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberMypageController extends BaseController {

    private final MemberSVC memberSVC;
    private final CodeSVC codeSVC;
    private final MessageSource messageSource;

    @ModelAttribute("regionCodes")
    public List<Code> regionCodes() {
        List<Code> codes = codeSVC.getCodeList("REGION").stream()
            .filter(code -> !code.getCode().equals("REGION")) // 상위코드 제외
            .collect(Collectors.toList());
        log.info("지역 코드 조회 결과: {}", codes);
        return codes;
    }

    @ModelAttribute("genderCodes")
    public List<Code> genderCodes() {
      List<Code> codes = codeSVC.getCodeList("GENDER").stream()
          .filter(code -> !code.getCode().equals("GENDER")) // 상위코드 제외
          .collect(Collectors.toList());
      log.info("성별 코드 조회 결과: {}", codes);
      return codes;      
    }

    @ModelAttribute("hobbyCodes")
    public List<Code> hobbyCodes() {
        return codeSVC.getCodeList("HOBBY").stream()
            .filter(code -> !code.getCode().equals("HOBBY")) // 상위코드 제외
            .collect(Collectors.toList());
    }

    /**
     * Member 엔티티를 MemberDTO로 변환
     */
    private MemberDTO convertToMemberDTO(Member member) {
        MemberDTO memberDTO = new MemberDTO();
        BeanUtils.copyProperties(member, memberDTO);
        
        // 코드 값들을 디코드로 변환
        if (member.getRegion() != null) {
            String regionDecode = codeSVC.getCodeDecode("REGION", member.getRegion());
            if (regionDecode != null) {
                memberDTO.setRegionName(regionDecode);
            }
        }
        
        // 성별 decode로 변환
        if (member.getGender() != null) {
            Long genderCodeId = codeSVC.getCodeId("GENDER", member.getGender());
            if (genderCodeId != null) {
                String genderDecode = codeSVC.getCodeDecode("GENDER", genderCodeId);
                if (genderDecode != null) {
                    memberDTO.setGenderName(genderDecode);
                }
            }
        }
        
        if (member.getHobby() != null) {
            // hobby는 콤마로 구분된 code_id들이므로 decode로 변환
            Map<Long, String> hobbyDecodeMap = codeSVC.getCodeDecodeMap("HOBBY");
            String hobbyDecodes = member.getHobbyDecodes(hobbyDecodeMap);
            memberDTO.setHobby(hobbyDecodes);
        }
        
        return memberDTO;
    }

    /**
     * 마이페이지 메인
     */
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model, jakarta.servlet.http.HttpServletRequest request) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Optional<Member> memberOpt = memberSVC.findById(loginMember.getMemberId());
        if (memberOpt.isPresent()) {
            MemberDTO memberDTO = convertToMemberDTO(memberOpt.get());
            model.addAttribute("member", memberDTO);
        }

        return "member/mypage";
    }

    /**
     * 프로필 수정 폼
     */
    @GetMapping("/mypage/edit")
    public String editForm(HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Optional<Member> memberOpt = memberSVC.findById(loginMember.getMemberId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            MypageForm mypageForm = new MypageForm();
            BeanUtils.copyProperties(member, mypageForm);

            if (member.getHobby() != null && !member.getHobby().trim().isEmpty()) {
                List<String> hobbyList = Arrays.stream(member.getHobby().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
                mypageForm.setHobby(hobbyList);
            }

            log.info("mypageForm={}", mypageForm);
            model.addAttribute("mypageForm", mypageForm);
        }

        return "member/profile/editForm";
    }

    /**
     * 프로필 수정 처리
     */
    @PostMapping("/mypage/edit")
    public String edit(@Valid @ModelAttribute MypageForm mypageForm,
                      BindingResult bindingResult,
                      HttpSession session,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "member/profile/editForm";
        }

        try {
            Optional<Member> memberOpt = memberSVC.findById(loginMember.getMemberId());
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                
                // 정보 업데이트 (hobby 제외)
                BeanUtils.copyProperties(mypageForm, member);
                
                // hobby List<String>를 문자열로 변환
                if (mypageForm.getHobby() != null && !mypageForm.getHobby().isEmpty()) {
                    String hobbyString = String.join(",", mypageForm.getHobby());
                    member.setHobby(hobbyString);
                } else {
                    member.setHobby(null);
                }
                
                memberSVC.updateMember(loginMember.getMemberId(), member);

                // 세션 정보 업데이트 (실제 구현에서는 별도 처리 필요)
                // LoginMember updatedLoginMember = new LoginMember();
                // BeanUtils.copyProperties(member, updatedLoginMember);
                // session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);

                redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 수정되었습니다.");
                return "redirect:/member/mypage";
            }
        } catch (Exception e) {
            bindingResult.reject("edit.error", "프로필 수정 중 오류가 발생했습니다.");
            return "member/profile/editForm";
        }

        return "redirect:/member/mypage";
    }

    /**
     * 비밀번호 변경 폼
     */
    @GetMapping("/mypage/password")
    public String passwordForm(Model model) {
        model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        return "member/profile/passwordForm";
    }

    /**
     * 비밀번호 변경 처리
     */
    @PostMapping("/mypage/password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeForm passwordChangeForm,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "member/profile/passwordForm";
        }

        try {
            int result = memberSVC.changePasswd(loginMember.getMemberId(),
                                              passwordChangeForm.getCurrentPassword(),
                                              passwordChangeForm.getNewPassword());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
                return "redirect:/member/mypage";
            } else {
                bindingResult.rejectValue("currentPassword", "password.error", "현재 비밀번호가 올바르지 않습니다.");
                return "member/profile/passwordForm";
            }
        } catch (Exception e) {
            bindingResult.reject("password.error", "비밀번호 변경 중 오류가 발생했습니다.");
            return "member/profile/passwordForm";
        }
    }

    /**
     * 회원 탈퇴 폼
     */
    @GetMapping("/mypage/withdraw")
    public String withdrawForm() {
        return "member/profile/withdrawForm";
    }

    /**
     * 회원 탈퇴 처리
     */
    @PostMapping("/mypage/withdraw")
    public String withdraw(HttpSession session, RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            memberSVC.deleteMember(loginMember.getMemberId());
            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "회원 탈퇴 중 오류가 발생했습니다.");
            return "redirect:/member/mypage";
        }
    }
} 