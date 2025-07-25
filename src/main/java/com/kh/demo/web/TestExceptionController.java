package com.kh.demo.web;

import com.kh.demo.common.exception.BusinessException;
import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.common.exception.ErrorCode;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestExceptionController {
    
    private final CodeSVC codeSVC;

    @GetMapping("/exception")
    public String testException() {
        throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND.name(), "테스트 예외");
    }

    @GetMapping("/validation")
    public String testValidation() {
        throw new BusinessValidationException("테스트 검증 예외");
    }

    @GetMapping("/product-categories")
    public String testProductCategories() {
        List<Code> categories = codeSVC.findActiveSubCodesByGcode("PRODUCT_CATEGORY");
        StringBuilder result = new StringBuilder();
        result.append("=== Product Categories Test ===\n");
        
        for (Code category : categories) {
            result.append(String.format("CodeId: %d, Code: %s, Decode: %s, PCode: %s\n", 
                category.getCodeId(), category.getCode(), category.getDecode(), 
                category.getPcode() != null ? category.getPcode().toString() : "NULL"));
        }
        
        result.append(String.format("=== Total count: %d ===", categories.size()));
        
        log.info(result.toString());
        return result.toString().replace("\n", "<br>");
    }
} 