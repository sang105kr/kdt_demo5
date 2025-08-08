package com.kh.demo.admin.member;

import com.kh.demo.admin.member.page.form.AdminMemberForm;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.dto.MemberDetailDTO;
import com.kh.demo.domain.member.dto.MemberHobbyDTO;
import com.kh.demo.domain.member.svc.MemberSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController extends BaseController{
    private final MemberSVC memberSVC;
    private final CodeSVC codeSVC;
    private final MessageSource messageSource;
    private static final int PAGE_SIZE = 10;

    @ModelAttribute("gubunMap")
    public Map<Long, String> gubunMap() {
        return codeSVC.getCodeDecodeMap("MEMBER_GUBUN");
    }
    @ModelAttribute("statusMap")
    public Map<Long, String> statusMap() {
        return codeSVC.getCodeList("MEMBER_STATUS").stream()
                .collect(Collectors.toMap(Code::getCodeId, Code::getDecode));
    }
    @ModelAttribute("statusCodes")
    public List<Code> statusCodes() {
        return codeSVC.getCodeList("MEMBER_STATUS");
    }
    
    @ModelAttribute("hobbyCodes")
    public List<Code> hobbyCodes() {
        return codeSVC.getCodeList("HOBBY").stream()
            .filter(code -> !code.getCode().equals("HOBBY")) // 상위코드 제외
            .collect(Collectors.toList());
    }
    
    @ModelAttribute("hobbyDecodeMap")
    public Map<Long, String> hobbyDecodeMap() {
        return codeSVC.getCodeDecodeMap("HOBBY");
    }

    @ModelAttribute("genderCodes")
    public List<Code> genderCodes() {
        return codeSVC.getCodeList("GENDER");
    }

    @ModelAttribute("regionCodes")
    public List<Code> regionCodes() {
        return codeSVC.getCodeList("REGION");
    }



    /**
     * 회원 목록 (검색/페이징)
     */
    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                      @RequestParam(value = "status", required = false) String status,
                      @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                      Model model) {
        
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
        model.addAttribute("title", "회원 관리");
        return "admin/member/list";
    }

    /**
     * 회원 상태 변경
     */
    @PostMapping("/{memberId}/status")
    public String updateStatus(@PathVariable Long memberId,
                              @RequestParam Long status,
                              RedirectAttributes redirectAttributes,
                              Locale locale) {
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
    public String detail(@PathVariable Long memberId, Model model) {
        
        try {
            Optional<MemberDetailDTO> memberDetailOpt = memberSVC.findMemberDetailById(memberId);
            if (memberDetailOpt.isEmpty()) {
                model.addAttribute("errorMessage", "회원을 찾을 수 없습니다.");
                return "redirect:/admin/members";
            }
            
            MemberDetailDTO memberDetail = memberDetailOpt.get();
            model.addAttribute("member", memberDetail);
            
            // VIP, 관리자 코드 ID 추가
            Long vipCodeId = codeSVC.getCodeId("MEMBER_GUBUN", "VIP");
            Long adminCodeId = codeSVC.getCodeId("MEMBER_GUBUN", "ADMIN1");
            
            model.addAttribute("vipCodeId", vipCodeId);
            model.addAttribute("adminCodeId", adminCodeId);
            
            return "admin/member/detail";
            
        } catch (Exception e) {
            log.error("회원 상세 조회 실패: memberId={}", memberId, e);
            model.addAttribute("errorMessage", "회원 정보 조회 중 오류가 발생했습니다.");
            return "redirect:/admin/members";
        }
    }

    /**
     * VIP 등급 변경
     */
    @PostMapping("/{memberId}/vip-status")
    public String updateVipStatus(@PathVariable Long memberId,
                                 @RequestParam boolean isVip,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        try {
            // VIP 코드 ID 조회
            Long vipCodeId = codeSVC.getCodeId("MEMBER_GUBUN", "VIP");
            Long normalCodeId = codeSVC.getCodeId("MEMBER_GUBUN", "NORMAL");
            
            // 회원 조회
            Optional<Member> memberOpt = memberSVC.findById(memberId);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "회원을 찾을 수 없습니다.");
                return "redirect:/admin/members";
            }
            
            Member member = memberOpt.get();
            Long newGubun = isVip ? vipCodeId : normalCodeId;
            
            // 등급 변경
            member.setGubun(newGubun);
            memberSVC.updateMember(memberId, member);
            
            String message = isVip ? "VIP 등급으로 변경되었습니다." : "일반 회원으로 변경되었습니다.";
            redirectAttributes.addFlashAttribute("successMessage", message);
            
        } catch (Exception e) {
            log.error("VIP 등급 변경 실패: memberId={}, isVip={}", memberId, isVip, e);
            redirectAttributes.addFlashAttribute("errorMessage", "등급 변경 중 오류가 발생했습니다.");
        }
        
        return "redirect:/admin/members/" + memberId;
    }

    /**
     * 관리자 권한 부여/해제
     */
    @PostMapping("/{memberId}/admin-status")
    public String updateAdminStatus(@PathVariable Long memberId,
                                   @RequestParam boolean isAdmin,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {
        try {
            // 관리자 코드 ID 조회
            Long admin1CodeId = codeSVC.getCodeId("MEMBER_GUBUN", "ADMIN1");
            Long normalCodeId = codeSVC.getCodeId("MEMBER_GUBUN", "NORMAL");
            
            // 회원 조회
            Optional<Member> memberOpt = memberSVC.findById(memberId);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "회원을 찾을 수 없습니다.");
                return "redirect:/admin/members";
            }
            
            Member member = memberOpt.get();
            Long newGubun = isAdmin ? admin1CodeId : normalCodeId;
            
            // 권한 변경
            member.setGubun(newGubun);
            memberSVC.updateMember(memberId, member);
            
            String message = isAdmin ? "관리자 권한이 부여되었습니다." : "관리자 권한이 해제되었습니다.";
            redirectAttributes.addFlashAttribute("successMessage", message);
            
        } catch (Exception e) {
            log.error("관리자 권한 변경 실패: memberId={}, isAdmin={}", memberId, isAdmin, e);
            redirectAttributes.addFlashAttribute("errorMessage", "권한 변경 중 오류가 발생했습니다.");
        }
        
        return "redirect:/admin/members/" + memberId;
    }

    /**
     * 회원 정보 수정 폼
     */
    @GetMapping("/{memberId}/edit")
    public String editForm(@PathVariable Long memberId, Model model) {
        
        try {
            Optional<MemberDetailDTO> memberDetailOpt = memberSVC.findMemberDetailById(memberId);
            if (memberDetailOpt.isEmpty()) {
                model.addAttribute("errorMessage", "회원을 찾을 수 없습니다.");
                return "redirect:/admin/members";
            }
            
            MemberDetailDTO memberDetail = memberDetailOpt.get();
            
            // MemberDetailDTO를 AdminMemberForm으로 변환
            AdminMemberForm form = new AdminMemberForm();
            form.setMemberId(memberDetail.getMemberId());
            form.setEmail(memberDetail.getEmail());
            form.setNickname(memberDetail.getNickname());
            form.setTel(memberDetail.getTel());
            form.setGender(memberDetail.getGender());
            form.setBirthDate(memberDetail.getBirthDate());
            form.setGubun(memberDetail.getGubun());
            form.setStatus(memberDetail.getStatus());
            form.setRegion(memberDetail.getRegion());
            form.setAddress(memberDetail.getAddress());
            form.setAddressDetail(memberDetail.getAddressDetail());
            form.setZipcode(memberDetail.getZipcode());
            
            // 취미 리스트 설정
            if (memberDetail.getHobbies() != null) {
                List<Long> hobbyIds = memberDetail.getHobbies().stream()
                    .map(MemberHobbyDTO::getHobbyCodeId)  // getHobbyCode 대신 getHobbyCodeId 사용
                    .collect(Collectors.toList());
                form.setHobbies(hobbyIds);
            }
            
            model.addAttribute("memberForm", form);
            return "admin/member/edit";
            
        } catch (Exception e) {
            log.error("회원 수정 폼 조회 실패: memberId={}", memberId, e);
            model.addAttribute("errorMessage", "회원 정보 조회 중 오류가 발생했습니다.");
            return "redirect:/admin/members";
        }
    }

    /**
     * 회원 정보 수정 처리
     */
    @PostMapping("/{memberId}/edit")
    public String edit(@PathVariable Long memberId,
                      @Valid @ModelAttribute("memberForm") AdminMemberForm form,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        
        if (bindingResult.hasErrors()) {
            log.warn("회원 수정 폼 검증 실패: {}", bindingResult.getAllErrors());
            return "admin/member/edit";
        }
        
        try {
            // 기존 회원 정보 조회
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
            member.setRegion(form.getRegion());
            member.setAddress(form.getAddress());
            member.setAddressDetail(form.getAddressDetail());
            member.setZipcode(form.getZipcode());
            
            // 회원 기본 정보 업데이트
            memberSVC.updateById(memberId, member);
            
            // 취미 정보 업데이트 (1:N 관계)
            if (form.getHobbies() != null) {
                // 취미 코드 ID를 직접 사용 (변환 불필요)
                memberSVC.updateMemberHobbies(memberId, form.getHobbies());
            } else {
                // 취미가 선택되지 않은 경우 모든 취미 삭제
                memberSVC.updateMemberHobbies(memberId, new ArrayList<>());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 수정되었습니다.");
            return "redirect:/admin/members/" + memberId;
            
        } catch (Exception e) {
            log.error("회원 정보 수정 실패: memberId={}", memberId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "회원 정보 수정 중 오류가 발생했습니다.");
            return "redirect:/admin/members/" + memberId + "/edit";
        }
    }

    /**
     * 회원 탈퇴(삭제)
     */
    @PostMapping("/{memberId}/delete")
    public String delete(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        
        try {
            Optional<Member> memberOpt = memberSVC.findById(memberId);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "회원을 찾을 수 없습니다.");
                return "redirect:/admin/members";
            }
            
            // 회원 삭제 시 관련 취미 정보도 함께 삭제됨 (CASCADE 설정)
            memberSVC.deleteById(memberId);
            redirectAttributes.addFlashAttribute("successMessage", "회원이 탈퇴(삭제)되었습니다.");
            
        } catch (Exception e) {
            log.error("회원 삭제 실패: memberId={}", memberId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "회원 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/admin/members";
    }

    /**
     * 신규 회원 목록 (가입일 기준 최근 30일)
     */
    @GetMapping("/new")
    public String newMembers(@RequestParam(defaultValue = "1") int pageNo, Model model) {
        log.info("신규 회원 목록 조회 - pageNo: {}", pageNo);
        
        // 최근 30일 내 가입한 회원 조회
        List<Member> newMembers = memberSVC.findNewMembersWithPaging(pageNo, PAGE_SIZE);
        int totalCount = memberSVC.countNewMembers();
        
        Pagination pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
        
        model.addAttribute("members", newMembers);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pagination", pagination);
        return "admin/member/new";
    }

    /**
     * VIP 회원 목록
     */
    @GetMapping("/vip")
    public String vipMembers(@RequestParam(defaultValue = "1") int pageNo, Model model) {
        log.info("VIP 회원 목록 조회 - pageNo: {}", pageNo);
        
        // VIP 회원 조회 (gubun이 VIP인 회원)
        List<Member> vipMembers = memberSVC.findVipMembersWithPaging(pageNo, PAGE_SIZE);
        int totalCount = memberSVC.countVipMembers();
        
        Pagination pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
        
        model.addAttribute("members", vipMembers);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pagination", pagination);
        return "admin/member/vip";
    }

    /**
     * 휴면 회원 목록
     */
    @GetMapping("/inactive")
    public String inactiveMembers(@RequestParam(defaultValue = "1") int pageNo, Model model) {
        log.info("휴면 회원 목록 조회 - pageNo: {}", pageNo);
        
        // 휴면 회원 조회 (status가 INACTIVE인 회원)
        List<Member> inactiveMembers = memberSVC.findInactiveMembersWithPaging(pageNo, PAGE_SIZE);
        int totalCount = memberSVC.countInactiveMembers();
        
        Pagination pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
        
        model.addAttribute("members", inactiveMembers);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pagination", pagination);
        return "admin/member/inactive";
    }
} 