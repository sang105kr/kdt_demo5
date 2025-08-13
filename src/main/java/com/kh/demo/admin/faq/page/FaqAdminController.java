package com.kh.demo.admin.faq.page;

import com.kh.demo.domain.faq.svc.FaqService;
import com.kh.demo.domain.faq.entity.Faq;
import com.kh.demo.domain.faq.dto.FaqDTO;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.domain.common.util.MemberAuthUtil;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.common.session.LoginMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/faq")
@RequiredArgsConstructor
public class FaqAdminController {

    private final FaqService faqService;
    private final CodeSVC codeSVC;
    private final MemberAuthUtil memberAuthUtil;

    /**
     * FAQ 관리 목록 페이지
     */
    @GetMapping
    public String faqList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        log.info("관리자 FAQ 목록 조회: page={}, categoryId={}, keyword={}, size={}", page, categoryId, keyword, size);

        List<FaqDTO> faqs;
        int totalCount;

        // 카테고리별 조회
        if (categoryId != null) {
            faqs = faqService.findByCategoryId(categoryId, page, size).stream()
                    .map(faq -> {
                        FaqDTO dto = new FaqDTO();
                        dto.setFaqId(faq.getFaqId());
                        dto.setCategoryId(faq.getCategoryId());
                        dto.setQuestion(faq.getQuestion());
                        dto.setAnswer(faq.getAnswer());
                        dto.setKeywords(faq.getKeywords());
                        dto.setViewCount(faq.getViewCount());
                        dto.setHelpfulCount(faq.getHelpfulCount());
                        dto.setUnhelpfulCount(faq.getUnhelpfulCount());
                        dto.setSortOrder(faq.getSortOrder());
                        dto.setIsActive(faq.getIsActive());
                        dto.setAdminId(faq.getAdminId());
                        dto.setCdate(faq.getCdate());
                        dto.setUdate(faq.getUdate());
                        return dto;
                    })
                    .toList();
            totalCount = faqService.countByCategoryId(categoryId);
        }
        // 키워드 검색
        else if (keyword != null && !keyword.trim().isEmpty()) {
            faqs = faqService.findByKeyword(keyword, page, size).stream()
                    .map(faq -> {
                        FaqDTO dto = new FaqDTO();
                        dto.setFaqId(faq.getFaqId());
                        dto.setCategoryId(faq.getCategoryId());
                        dto.setQuestion(faq.getQuestion());
                        dto.setAnswer(faq.getAnswer());
                        dto.setKeywords(faq.getKeywords());
                        dto.setViewCount(faq.getViewCount());
                        dto.setHelpfulCount(faq.getHelpfulCount());
                        dto.setUnhelpfulCount(faq.getUnhelpfulCount());
                        dto.setSortOrder(faq.getSortOrder());
                        dto.setIsActive(faq.getIsActive());
                        dto.setAdminId(faq.getAdminId());
                        dto.setCdate(faq.getCdate());
                        dto.setUdate(faq.getUdate());
                        return dto;
                    })
                    .toList();
            totalCount = faqService.countByKeyword(keyword);
        }
        // 전체 조회
        else {
            faqs = faqService.findActiveWithJoin(page, size);
            totalCount = faqService.countActive();
        }

        // 카테고리 정보 설정
        for (FaqDTO faq : faqs) {
            faq.setCategoryName(codeSVC.getCodeDecode("FAQ_CATEGORY", faq.getCategoryId()));
        }

        // 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalCount / size);
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        // 모델에 데이터 추가
        model.addAttribute("faqs", faqs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        
        // FAQ 카테고리 목록
        model.addAttribute("categories", codeSVC.getCodeList("FAQ_CATEGORY"));

        return "admin/faq/list";
    }

    /**
     * FAQ 관리 상세 페이지
     */
    @GetMapping("/{faqId}")
    public String faqDetail(@PathVariable Long faqId, Model model) {
        log.info("관리자 FAQ 상세 조회: faqId={}", faqId);

        Optional<FaqDTO> faqOpt = faqService.findByIdWithJoin(faqId);
        
        if (faqOpt.isEmpty()) {
            model.addAttribute("error", "존재하지 않는 FAQ입니다.");
            return "admin/faq/detail";
        }

        FaqDTO faq = faqOpt.get();
        
        // 카테고리명 설정
        faq.setCategoryName(codeSVC.getCodeDecode("FAQ_CATEGORY", faq.getCategoryId()));

        model.addAttribute("faq", faq);
        return "admin/faq/detail";
    }

    /**
     * FAQ 작성 폼
     */
    @GetMapping("/write")
    public String faqWriteForm(Model model) {
        model.addAttribute("categories", codeSVC.getCodeList("FAQ_CATEGORY"));
        return "admin/faq/write";
    }

    /**
     * FAQ 작성 처리
     */
    @PostMapping("/write")
    @ResponseBody
    public ApiResponse<String> faqWrite(@RequestBody Faq faq, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        
        // 관리자 권한 체크
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, "권한이 없습니다.");
        }

        try {
            Faq createdFaq = faqService.createFaq(faq, loginMember.getMemberId());
            
            log.info("관리자 FAQ 작성 성공: faqId={}, adminId={}", createdFaq.getFaqId(), loginMember.getMemberId());
            return ApiResponse.of(ApiResponseCode.SUCCESS, "FAQ가 성공적으로 작성되었습니다.");
            
        } catch (Exception e) {
            log.error("관리자 FAQ 작성 실패: adminId={}", loginMember.getMemberId(), e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "FAQ 작성 중 오류가 발생했습니다.");
        }
    }

    /**
     * FAQ 수정 폼
     */
    @GetMapping("/{faqId}/edit")
    public String faqEditForm(@PathVariable Long faqId, Model model) {
        Optional<Faq> faqOpt = faqService.findById(faqId);
        
        if (faqOpt.isEmpty()) {
            model.addAttribute("error", "존재하지 않는 FAQ입니다.");
            return "admin/faq/edit";
        }

        model.addAttribute("faq", faqOpt.get());
        model.addAttribute("categories", codeSVC.getCodeList("FAQ_CATEGORY"));
        return "admin/faq/edit";
    }

    /**
     * FAQ 수정 처리
     */
    @PutMapping("/{faqId}")
    @ResponseBody
    public ApiResponse<String> faqEdit(@PathVariable Long faqId, @RequestBody Faq faq, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        
        // 관리자 권한 체크
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, "권한이 없습니다.");
        }

        try {
            int result = faqService.updateFaq(faqId, faq, loginMember.getMemberId());
            
            if (result > 0) {
                log.info("관리자 FAQ 수정 성공: faqId={}, adminId={}", faqId, loginMember.getMemberId());
                return ApiResponse.of(ApiResponseCode.SUCCESS, "FAQ가 성공적으로 수정되었습니다.");
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "FAQ 수정에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("관리자 FAQ 수정 실패: faqId={}, adminId={}", faqId, loginMember.getMemberId(), e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "FAQ 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * FAQ 삭제
     */
    @DeleteMapping("/{faqId}")
    @ResponseBody
    public ApiResponse<String> faqDelete(@PathVariable Long faqId, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        
        // 관리자 권한 체크
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, "권한이 없습니다.");
        }

        try {
            int result = faqService.deleteFaq(faqId, loginMember.getMemberId());
            
            if (result > 0) {
                log.info("관리자 FAQ 삭제 성공: faqId={}, adminId={}", faqId, loginMember.getMemberId());
                return ApiResponse.of(ApiResponseCode.SUCCESS, "FAQ가 성공적으로 삭제되었습니다.");
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "FAQ 삭제에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("관리자 FAQ 삭제 실패: faqId={}, adminId={}", faqId, loginMember.getMemberId(), e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "FAQ 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * FAQ 활성화/비활성화
     */
    @PutMapping("/{faqId}/status")
    @ResponseBody
    public ApiResponse<String> updateFaqStatus(
            @PathVariable Long faqId, 
            @RequestParam String isActive, 
            HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        
        // 관리자 권한 체크
        if (loginMember == null || !memberAuthUtil.isAdmin(loginMember.getMemberId())) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, "권한이 없습니다.");
        }

        try {
            int result = faqService.updateActiveStatus(faqId, isActive, loginMember.getMemberId());
            
            if (result > 0) {
                String statusText = "Y".equals(isActive) ? "활성화" : "비활성화";
                log.info("관리자 FAQ 상태 변경 성공: faqId={}, status={}, adminId={}", faqId, isActive, loginMember.getMemberId());
                return ApiResponse.of(ApiResponseCode.SUCCESS, "FAQ가 " + statusText + "되었습니다.");
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "FAQ 상태 변경에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("관리자 FAQ 상태 변경 실패: faqId={}, status={}, adminId={}", faqId, isActive, loginMember.getMemberId(), e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "FAQ 상태 변경 중 오류가 발생했습니다.");
        }
    }
}
