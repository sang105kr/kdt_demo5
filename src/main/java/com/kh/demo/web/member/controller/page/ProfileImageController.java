package com.kh.demo.web.member.controller.page;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.member.controller.page.form.ProfileImageForm;
import com.kh.demo.web.member.controller.page.dto.ProfileImagePageDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 프로필 이미지 관리 컨트롤러 - member 테이블의 pic BLOB 컬럼 사용
 */
@Slf4j
@Controller
@RequestMapping("/member/mypage/profile-image")
@RequiredArgsConstructor
public class ProfileImageController extends BaseController {
    
    private final MemberSVC memberSVC;
    
    // 허용된 이미지 파일 타입
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 프로필 이미지 관리 페이지
     */
    @GetMapping
    public String profileImagePage(HttpSession session, Model model) {
        log.debug("프로필 이미지 관리 페이지 요청");
        
        LoginMember loginMember = getLoginMemberOrRedirect(session);
        if (loginMember == null) {
            return "redirect:/login";
        }
        
        // 현재 회원 정보 조회
        Optional<Member> memberOpt = memberSVC.findByMemberId(loginMember.getMemberId());
        if (memberOpt.isEmpty()) {
            log.warn("회원 정보를 찾을 수 없음: memberId={}", loginMember.getMemberId());
            return "redirect:/login";
        }
        
        Member member = memberOpt.get();
        boolean hasProfileImage = member.getPic() != null && member.getPic().length > 0;
        
        model.addAttribute("member", loginMember);
        model.addAttribute("profileImageForm", new ProfileImageForm());
        model.addAttribute("hasProfileImage", hasProfileImage);
        
        log.debug("프로필 이미지 관리 페이지 준비 완료: memberId={}, hasImage={}", 
                 loginMember.getMemberId(), hasProfileImage);
        
        return "member/profile/profileImageForm";
    }
    
    /**
     * 프로필 이미지 업로드 처리 - SSR 방식 (페이지 리다이렉트)
     */
    @PostMapping
    public String uploadProfileImage(
            @Validated @ModelAttribute ProfileImageForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        log.debug("프로필 이미지 업로드 요청");
        
        LoginMember loginMember = getLoginMemberOrRedirect(session);
        if (loginMember == null) {
            return "redirect:/login";
        }
        
        // 폼 유효성 검사
        if (bindingResult.hasErrors()) {
            log.warn("프로필 이미지 업로드 폼 유효성 검사 실패: errors={}", bindingResult.getAllErrors());
            model.addAttribute("member", loginMember);
            model.addAttribute("hasProfileImage", hasProfileImage(loginMember.getMemberId()));
            return "member/profile/profileImageForm";
        }
        
        try {
            // 파일 유효성 검사 및 업로드 처리
            MultipartFile file = form.getProfileImage();
            validateFile(file);
            
            // 파일을 byte 배열로 변환
            byte[] imageData = file.getBytes();
            
            // 데이터베이스에 저장
            int result = memberSVC.updateProfileImage(loginMember.getMemberId(), imageData);
            
            if (result > 0) {
                // 성공 시 세션 정보 업데이트
                updateLoginMemberProfileImage(session, loginMember.getMemberId(), true);
                
                log.info("프로필 이미지 업로드 성공: memberId={}", loginMember.getMemberId());
                redirectAttributes.addFlashAttribute("message", "프로필 이미지가 업로드되었습니다.");
                return "redirect:/member/mypage/profile-image";
            } else {
                log.warn("프로필 이미지 업로드 실패: memberId={}", loginMember.getMemberId());
                model.addAttribute("member", loginMember);
                model.addAttribute("errorMessage", "프로필 이미지 업로드에 실패했습니다.");
                model.addAttribute("hasProfileImage", hasProfileImage(loginMember.getMemberId()));
                return "member/profile/profileImageForm";
            }
            
        } catch (BusinessValidationException e) {
            log.warn("프로필 이미지 업로드 유효성 검사 실패: {}", e.getMessage());
            model.addAttribute("member", loginMember);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("hasProfileImage", hasProfileImage(loginMember.getMemberId()));
            return "member/profile/profileImageForm";
        } catch (IOException e) {
            log.error("프로필 이미지 파일 읽기 실패", e);
            model.addAttribute("member", loginMember);
            model.addAttribute("errorMessage", "파일 처리 중 오류가 발생했습니다.");
            model.addAttribute("hasProfileImage", hasProfileImage(loginMember.getMemberId()));
            return "member/profile/profileImageForm";
        }
    }
    
    /**
     * 프로필 이미지 삭제 처리 - SSR 방식 (페이지 리다이렉트)
     */
    @PostMapping("/delete")
    public String deleteProfileImage(
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        log.debug("프로필 이미지 삭제 요청");
        
        LoginMember loginMember = getLoginMemberOrRedirect(session);
        if (loginMember == null) {
            return "redirect:/login";
        }
        
        try {
            int result = memberSVC.deleteProfileImage(loginMember.getMemberId());
            
            if (result > 0) {
                // 성공 시 세션 정보 업데이트
                updateLoginMemberProfileImage(session, loginMember.getMemberId(), false);
                
                log.info("프로필 이미지 삭제 성공: memberId={}", loginMember.getMemberId());
                redirectAttributes.addFlashAttribute("message", "프로필 이미지가 삭제되었습니다.");
            } else {
                log.warn("프로필 이미지 삭제 실패: memberId={}", loginMember.getMemberId());
                redirectAttributes.addFlashAttribute("errorMessage", "프로필 이미지 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "프로필 이미지 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/member/mypage/profile-image";
    }
    
    /**
     * 프로필 이미지 파일 조회 (이미지 출력)
     */
    @GetMapping("/view/{memberId}")
    public ResponseEntity<Resource> viewProfileImage(
            @PathVariable Long memberId,
            @RequestParam(required = false) String timestamp) {
        
        log.debug("프로필 이미지 조회 요청: memberId={}, timestamp={}", memberId, timestamp);
        
        try {
            Optional<Member> memberOpt = memberSVC.findByMemberId(memberId);
            
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                byte[] imageData = member.getPic();
                
                if (imageData != null && imageData.length > 0) {
                    ByteArrayResource resource = new ByteArrayResource(imageData);
                    
                    log.debug("프로필 이미지 조회 성공: memberId={}, size={} bytes", memberId, imageData.length);
                    
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG) // 기본적으로 JPEG로 설정
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.EXPIRES, "0")
                            .body(resource);
                } else {
                    log.debug("프로필 이미지 없음: memberId={}", memberId);
                    return ResponseEntity.notFound().build();
                }
            } else {
                log.debug("회원 없음: memberId={}", memberId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("프로필 이미지 조회 실패: memberId={}", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 현재 로그인한 사용자의 프로필 이미지 조회
     */
    @GetMapping("/view")
    public ResponseEntity<Resource> viewMyProfileImage(
            HttpSession session,
            @RequestParam(required = false) String timestamp) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return viewProfileImage(loginMember.getMemberId(), timestamp);
    }
    
    /**
     * 로그인한 회원 정보 가져오기 (null일 경우 null 반환)
     */
    private LoginMember getLoginMemberOrRedirect(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            log.warn("로그인되지 않은 사용자가 프로필 이미지 페이지에 접근");
        }
        return loginMember;
    }
    
    /**
     * 세션의 LoginMember 프로필 이미지 정보 업데이트
     */
    private void updateLoginMemberProfileImage(HttpSession session, Long memberId, boolean hasProfileImage) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember != null && loginMember.getMemberId().equals(memberId)) {
            // 새로운 LoginMember 객체 생성하여 세션 업데이트
            LoginMember updatedLoginMember = new LoginMember(
                loginMember.getMemberId(),
                loginMember.getEmail(),
                loginMember.getNickname(),
                loginMember.getGubun(),
                hasProfileImage
            );
            session.setAttribute(SessionConst.LOGIN_MEMBER, updatedLoginMember);
            log.debug("세션의 프로필 이미지 정보 업데이트: memberId={}, hasProfileImage={}", 
                     memberId, hasProfileImage);
        }
    }
    
    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("업로드할 파일을 선택해주세요.");
        }
        
        // 파일 크기 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessValidationException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        
        // 파일 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessValidationException("지원하지 않는 파일 형식입니다. (지원 형식: JPG, PNG, GIF, WEBP)");
        }
    }
    
    /**
     * 프로필 이미지 존재 여부 확인
     */
    private boolean hasProfileImage(Long memberId) {
        Optional<Member> memberOpt = memberSVC.findByMemberId(memberId);
        if (memberOpt.isPresent()) {
            byte[] pic = memberOpt.get().getPic();
            return pic != null && pic.length > 0;
        }
        return false;
    }
}