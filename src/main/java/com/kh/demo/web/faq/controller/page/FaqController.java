package com.kh.demo.web.faq.controller.page;

import com.kh.demo.domain.faq.svc.FaqService;
import com.kh.demo.domain.faq.dto.FaqDTO;
import com.kh.demo.domain.faq.entity.Faq;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;
    private final CodeSVC codeSVC;

    /**
     * FAQ 목록 페이지
     */
    @GetMapping
    public String faqList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        log.info("FAQ 목록 조회: page={}, categoryId={}, keyword={}, size={}", page, categoryId, keyword, size);

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

        // 통계 정보 계산
        int activeCount = faqService.countActive();
        int popularCount = faqService.findAllByViewCount(1, 5).size(); // 인기 FAQ는 조회수 기준 상위 5개
        int helpfulCount = faqService.findAll().stream()
                .mapToInt(Faq::getHelpfulCount)
                .sum();

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
        
        // 통계 정보
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("popularCount", popularCount);
        model.addAttribute("helpfulCount", helpfulCount);
        
        // FAQ 카테고리 목록
        model.addAttribute("categories", codeSVC.getCodeList("FAQ_CATEGORY"));

        return "faq/list";
    }

    /**
     * FAQ 상세 페이지
     */
    @GetMapping("/{faqId}")
    public String faqDetail(@PathVariable Long faqId, Model model) {
        log.info("FAQ 상세 조회: faqId={}", faqId);

        Optional<FaqDTO> faqOpt = faqService.findByIdWithJoin(faqId);
        
        if (faqOpt.isEmpty()) {
            model.addAttribute("error", "존재하지 않는 FAQ입니다.");
            return "faq/detail";
        }

        FaqDTO faq = faqOpt.get();
        
        // 카테고리명 설정
        faq.setCategoryName(codeSVC.getCodeDecode("FAQ_CATEGORY", faq.getCategoryId()));
        
        // 조회수 증가
        faqService.incrementViewCount(faqId);

        // 관련 FAQ 조회 (같은 카테고리의 다른 FAQ)
        List<FaqDTO> relatedFaqs = faqService.findByCategoryId(faq.getCategoryId(), 1, 5).stream()
                .filter(relatedFaq -> !relatedFaq.getFaqId().equals(faqId))
                .limit(3)
                .map(relatedFaq -> {
                    FaqDTO dto = new FaqDTO();
                    dto.setFaqId(relatedFaq.getFaqId());
                    dto.setQuestion(relatedFaq.getQuestion());
                    return dto;
                })
                .toList();

        model.addAttribute("faq", faq);
        model.addAttribute("relatedFaqs", relatedFaqs);
        return "faq/detail";
    }
}
