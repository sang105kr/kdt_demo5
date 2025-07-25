package com.kh.demo.admin.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.svc.MemberSVC;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {
    private final MemberSVC memberSVC;
    private final CodeSVC codeSVC;
    private final MessageSource messageSource;
    private static final int PAGE_SIZE = 10;

    @ModelAttribute("gubunMap")
    public Map<Long, String> gubunMap() {
        return codeSVC.findByGcode("MEMBER_GUBUN").stream()
                .collect(Collectors.toMap(Code::getCodeId, Code::getDecode));
    }
    @ModelAttribute("statusMap")
    public Map<String, String> statusMap() {
        return codeSVC.findByGcode("MEMBER_STATUS").stream()
                .collect(Collectors.toMap(Code::getCode, Code::getDecode));
    }
    @ModelAttribute("statusCodes")
    public List<Code> statusCodes() {
        return codeSVC.findSubCodesByGcode("MEMBER_STATUS");
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember != null && (loginMember.getGubun() == 4 || loginMember.getGubun() == 5);
    }

    /**
     * 회원 목록 (검색/페이징)
     */
    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                      @RequestParam(value = "status", required = false) String status,
                      @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                      Model model, HttpServletRequest request) {
        if (!isAdmin(request)) return "redirect:/login";
        
        // 디버깅: 파라미터 값 확인
        log.info("=== 회원 목록 조회 시작 ===");
        log.info("keyword: [{}]", keyword);
        log.info("status: [{}]", status);
        log.info("pageNo: {}", pageNo);
        
        int totalCount;
        List<Member> members;
        boolean allStatus = (status == null || status.equals("ALL"));
        boolean hasKeyword = (keyword != null && !keyword.isBlank());
        
        log.info("allStatus: {}, hasKeyword: {}", allStatus, hasKeyword);
        
        if (allStatus && !hasKeyword) {
            log.info("분기: 전체 상태 + 키워드 없음 -> findAllWithPaging 호출");
            totalCount = memberSVC.getTotalCount();
            members = memberSVC.findAllWithPaging(pageNo, PAGE_SIZE);
        } else if (!allStatus && !hasKeyword) {
            log.info("분기: 특정 상태 + 키워드 없음 -> findByStatusWithPaging 호출");
            totalCount = memberSVC.countByStatus(status);
            members = memberSVC.findByStatusWithPaging(status, pageNo, PAGE_SIZE);
        } else if (allStatus && hasKeyword) {
            log.info("분기: 전체 상태 + 키워드 있음 -> findByKeywordWithPaging 호출");
            totalCount = memberSVC.countByKeyword(keyword);
            members = memberSVC.findByKeywordWithPaging(keyword, pageNo, PAGE_SIZE);
        } else {
            log.info("분기: 특정 상태 + 키워드 있음 -> findByStatusAndKeywordWithPaging 호출");
            totalCount = memberSVC.countByStatusAndKeyword(status, keyword);
            members = memberSVC.findByStatusAndKeywordWithPaging(status, keyword, pageNo, PAGE_SIZE);
        }
        
        log.info("totalCount: {}, members.size(): {}", totalCount, members.size());
        log.info("=== 회원 목록 조회 완료 ===");
        
        Pagination pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
        model.addAttribute("members", members);
        model.addAttribute("pagination", pagination);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/member/list";
    }

    /**
     * 회원 상태 변경
     */
    @PostMapping("/{memberId}/status")
    public String updateStatus(@PathVariable Long memberId,
                              @RequestParam String status,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes,
                              Locale locale) {
        if (!isAdmin(request)) return "redirect:/login";
        try {
            Optional<Member> memberOpt = memberSVC.findById(memberId);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("member.not.found", null, locale));
                return "redirect:/admin/members";
            }
            Member member = memberOpt.get();
            member.setStatus(status);
            memberSVC.updateById(memberId, member);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("member.status.update.success", null, locale));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/members";
    }

    /**
     * 회원 상세보기
     */
    @GetMapping("/{memberId}")
    public String detail(@PathVariable Long memberId, Model model, HttpServletRequest request) {
        if (!isAdmin(request)) return "redirect:/login";
        Optional<Member> memberOpt = memberSVC.findById(memberId);
        if (memberOpt.isEmpty()) {
            model.addAttribute("errorMessage", "회원을 찾을 수 없습니다.");
            return "redirect:/admin/members";
        }
        model.addAttribute("member", memberOpt.get());
        return "admin/member/detail";
    }

    /**
     * 회원 정보 수정 폼
     */
    @GetMapping("/{memberId}/edit")
    public String editForm(@PathVariable Long memberId, Model model, HttpServletRequest request) {
        if (!isAdmin(request)) return "redirect:/login";
        Optional<Member> memberOpt = memberSVC.findById(memberId);
        if (memberOpt.isEmpty()) {
            model.addAttribute("errorMessage", "회원을 찾을 수 없습니다.");
            return "redirect:/admin/members";
        }
        model.addAttribute("member", memberOpt.get());
        return "admin/member/edit";
    }

    /**
     * 회원 정보 수정 처리
     */
    @PostMapping("/{memberId}/edit")
    public String edit(@PathVariable Long memberId,
                      @ModelAttribute Member form,
                      HttpServletRequest request,
                      RedirectAttributes redirectAttributes) {
        if (!isAdmin(request)) return "redirect:/login";
        Optional<Member> memberOpt = memberSVC.findById(memberId);
        if (memberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원을 찾을 수 없습니다.");
            return "redirect:/admin/members";
        }
        Member member = memberOpt.get();
        // 수정 가능한 필드만 반영
        member.setNickname(form.getNickname());
        member.setTel(form.getTel());
        member.setGender(form.getGender());
        member.setBirthDate(form.getBirthDate());
        member.setHobby(form.getHobby());
        memberSVC.updateById(memberId, member);
        redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 수정되었습니다.");
        return "redirect:/admin/members/" + memberId;
    }

    /**
     * 회원 탈퇴(삭제)
     */
    @PostMapping("/{memberId}/delete")
    public String delete(@PathVariable Long memberId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (!isAdmin(request)) return "redirect:/login";
        Optional<Member> memberOpt = memberSVC.findById(memberId);
        if (memberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원을 찾을 수 없습니다.");
            return "redirect:/admin/members";
        }
        memberSVC.deleteById(memberId);
        redirectAttributes.addFlashAttribute("successMessage", "회원이 탈퇴(삭제)되었습니다.");
        return "redirect:/admin/members";
    }
} 