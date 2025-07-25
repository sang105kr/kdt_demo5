package com.kh.demo.web.member.controller.page;

import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.member.controller.page.form.ProfileImageForm;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 회원 프로필 이미지 관리 컨트롤러
 * - 프로필 이미지 업로드
 * - 프로필 이미지 삭제
 * - 프로필 이미지 조회
 */
@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberProfileImageController extends BaseController {
    
    private final MemberSVC memberSVC;

    /**
     * 프로필 이미지 업로드 폼
     */
    @GetMapping("/mypage/profile-image")
    public String profileImageForm(HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        model.addAttribute("member", loginMember);
        model.addAttribute("profileImageForm", new ProfileImageForm());
        return "member/profile/profileImageForm";
    }

    /**
     * 프로필 이미지 업로드 처리
     */
    @PostMapping("/mypage/profile-image")
    public String uploadProfileImage(@Valid @ModelAttribute ProfileImageForm profileImageForm,
                                   BindingResult bindingResult,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("member", loginMember);
            return "member/profile/profileImageForm";
        }

        try {
            // 파일 업로드 처리 로직
            MultipartFile file = profileImageForm.getProfileImage();
            
            // 파일 유효성 검사
            if (file.isEmpty()) {
                bindingResult.reject("upload.error", "업로드할 파일을 선택해주세요.");
                model.addAttribute("member", loginMember);
                return "member/profile/profileImageForm";
            }
            
            // 파일 크기 검사 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                bindingResult.reject("upload.error", "파일 크기는 5MB를 초과할 수 없습니다.");
                model.addAttribute("member", loginMember);
                return "member/profile/profileImageForm";
            }
            
            // 파일 타입 검사
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                bindingResult.reject("upload.error", "이미지 파일만 업로드 가능합니다.");
                model.addAttribute("member", loginMember);
                return "member/profile/profileImageForm";
            }
            
            // 파일 데이터를 바이트 배열로 변환
            byte[] imageData = file.getBytes();
            
            // 프로필 이미지 업데이트
            int result = memberSVC.updateProfileImage(loginMember.getMemberId(), imageData);
            
            if (result > 0) {
                // 업데이트된 회원 정보 조회
                Optional<Member> updatedMemberOpt = memberSVC.findByEmail(loginMember.getEmail());
                if (updatedMemberOpt.isPresent()) {
                    Member updatedMember = updatedMemberOpt.get();
                    
                    // 세션의 LoginMember 정보 업데이트
                    LoginMember updatedLoginMember = new LoginMember(
                        updatedMember.getMemberId(),
                        updatedMember.getEmail(),
                        updatedMember.getNickname(),
                        updatedMember.getGubun(),
                        updatedMember.hasProfileImage()
                    );
                    session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);
                    
                    model.addAttribute("member", updatedMember);
                    model.addAttribute("message", "프로필 이미지가 성공적으로 업로드되었습니다.");
                } else {
                    model.addAttribute("member", loginMember);
                    model.addAttribute("message", "프로필 이미지가 성공적으로 업로드되었습니다.");
                }
            } else {
                bindingResult.reject("upload.error", "프로필 이미지 업로드에 실패했습니다.");
                model.addAttribute("member", loginMember);
                return "member/profile/profileImageForm";
            }
            
            return "member/profile/profileImageForm";
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 중 오류 발생", e);
            bindingResult.reject("upload.error", "프로필 이미지 업로드 중 오류가 발생했습니다.");
            model.addAttribute("member", loginMember);
            return "member/profile/profileImageForm";
        }
    }

    /**
     * 프로필 이미지 삭제
     */
    @PostMapping("/mypage/profile-image/delete")
    public String deleteProfileImage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            // 프로필 이미지 삭제 처리
            int result = memberSVC.deleteProfileImage(loginMember.getMemberId());
            
            if (result > 0) {
                // 업데이트된 회원 정보 조회
                Optional<Member> updatedMemberOpt = memberSVC.findByEmail(loginMember.getEmail());
                if (updatedMemberOpt.isPresent()) {
                    Member updatedMember = updatedMemberOpt.get();
                    
                    // 세션의 LoginMember 정보 업데이트
                    LoginMember updatedLoginMember = new LoginMember(
                        updatedMember.getMemberId(),
                        updatedMember.getEmail(),
                        updatedMember.getNickname(),
                        updatedMember.getGubun(),
                        updatedMember.hasProfileImage()
                    );
                    session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);
                    
                    model.addAttribute("member", updatedMember);
                    model.addAttribute("message", "프로필 이미지가 삭제되었습니다.");
                } else {
                    model.addAttribute("member", loginMember);
                    model.addAttribute("message", "프로필 이미지가 삭제되었습니다.");
                }
            } else {
                model.addAttribute("member", loginMember);
                model.addAttribute("errorMessage", "프로필 이미지 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 중 오류 발생", e);
            model.addAttribute("member", loginMember);
            model.addAttribute("errorMessage", "프로필 이미지 삭제 중 오류가 발생했습니다.");
        }

        model.addAttribute("profileImageForm", new ProfileImageForm());
        return "member/profile/profileImageForm";
    }

    /**
     * 프로필 이미지 조회
     */
    @GetMapping("/profile-image/view")
    public void viewProfileImage(@RequestParam(required = false) String email,
                               jakarta.servlet.http.HttpServletResponse response,
                               HttpSession session) {
        try {
            // email이 null인 경우 세션에서 로그인된 사용자의 이메일 사용
            if (email == null || email.isEmpty()) {
                LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
                if (loginMember != null) {
                    email = loginMember.getEmail();
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            
            // 이메일로 회원 조회
            Optional<Member> memberOpt = memberSVC.findByEmail(email);
            if (memberOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Member member = memberOpt.get();
            
            // 프로필 이미지가 없는 경우
            if (member.getPic() == null || member.getPic().length == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 이미지 타입 설정 (기본값: JPEG)
            response.setContentType("image/jpeg");
            response.setContentLength(member.getPic().length);
            
            // 이미지 데이터 출력
            response.getOutputStream().write(member.getPic());
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            log.error("프로필 이미지 조회 중 오류 발생", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                log.error("응답 상태 설정 중 오류", ex);
            }
        }
    }
} 