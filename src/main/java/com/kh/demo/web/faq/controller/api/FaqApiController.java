package com.kh.demo.web.faq.controller.api;

import com.kh.demo.domain.faq.svc.FaqService;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqApiController {

    private final FaqService faqService;

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
                    return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "FAQ 정보를 찾을 수 없습니다.");
                }
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "도움됨 수 증가에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("FAQ 도움됨 수 증가 실패: faqId={}", faqId, e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "도움됨 수 증가 중 오류가 발생했습니다.");
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
                    return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "FAQ 정보를 찾을 수 없습니다.");
                }
            } else {
                return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "도움안됨 수 증가에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("FAQ 도움안됨 수 증가 실패: faqId={}", faqId, e);
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "도움안됨 수 증가 중 오류가 발생했습니다.");
        }
    }
}
