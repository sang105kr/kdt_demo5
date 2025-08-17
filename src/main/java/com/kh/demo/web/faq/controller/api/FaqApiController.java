package com.kh.demo.web.faq.controller.api;

import com.kh.demo.domain.faq.svc.FaqService;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.entity.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqApiController {

    private final FaqService faqService;
    private final CodeSVC codeSVC;

    /**
     * FAQ 카테고리 목록 조회
     */
    @GetMapping("/categories")
    @ResponseBody
    public ApiResponse<List<Code>> getCategories() {
        try {
            List<Code> categories = codeSVC.getCodeList("FAQ_CATEGORY").stream()
                .filter(code -> !code.getCode().equals("FAQ_CATEGORY")) // 상위코드 제외
                .collect(Collectors.toList());
            log.info("FAQ 카테고리 목록 조회 성공: {}개", categories.size());
            return ApiResponse.of(ApiResponseCode.SUCCESS, categories);
        } catch (Exception e) {
            log.error("FAQ 카테고리 목록 조회 실패", e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    /**
     * FAQ 도움됨 수 증가
     */
    @PostMapping("/{faqId}/helpful")
    @ResponseBody
    public ApiResponse<Object> incrementHelpfulCount(@PathVariable Long faqId) {
        try {
            int result = faqService.incrementHelpfulCount(faqId);
            
            if (result > 0) {
                // 업데이트된 FAQ 정보 조회
                var faqOpt = faqService.findById(faqId);
                if (faqOpt.isPresent()) {
                    var faq = faqOpt.get();
                    var responseData = new java.util.HashMap<String, Object>();
                    responseData.put("helpfulCount", faq.getHelpfulCount());
                    responseData.put("unhelpfulCount", faq.getUnhelpfulCount());
                    
                    log.info("FAQ 도움됨 수 증가 성공: faqId={}, helpfulCount={}", faqId, faq.getHelpfulCount());
                    return ApiResponse.of(ApiResponseCode.SUCCESS, responseData);
                } else {
                    return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
                }
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
            }
            
        } catch (Exception e) {
            log.error("FAQ 도움됨 수 증가 실패: faqId={}", faqId, e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    /**
     * FAQ 도움안됨 수 증가
     */
    @PostMapping("/{faqId}/unhelpful")
    @ResponseBody
    public ApiResponse<Object> incrementUnhelpfulCount(@PathVariable Long faqId) {
        try {
            int result = faqService.incrementUnhelpfulCount(faqId);
            
            if (result > 0) {
                // 업데이트된 FAQ 정보 조회
                var faqOpt = faqService.findById(faqId);
                if (faqOpt.isPresent()) {
                    var faq = faqOpt.get();
                    var responseData = new java.util.HashMap<String, Object>();
                    responseData.put("helpfulCount", faq.getHelpfulCount());
                    responseData.put("unhelpfulCount", faq.getUnhelpfulCount());
                    
                    log.info("FAQ 도움안됨 수 증가 성공: faqId={}, unhelpfulCount={}", faqId, faq.getUnhelpfulCount());
                    return ApiResponse.of(ApiResponseCode.SUCCESS, responseData);
                } else {
                    return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
                }
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
            }
            
        } catch (Exception e) {
            log.error("FAQ 도움안됨 수 증가 실패: faqId={}", faqId, e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
        }
    }
}
