package com.kh.demo.web.api.validator;

import com.kh.demo.web.api.product.request.CreateReq;
import com.kh.demo.web.api.product.request.UpdateReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

/**
 * 상품 도메인 비즈니스 검증 로직
 * 복잡한 비즈니스 규칙을 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductValidator implements Validator {

    private final MessageSource messageSource;

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateReq.class.equals(clazz) || UpdateReq.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof CreateReq) {
            validateCreateRequest((CreateReq) target, errors);
        } else if (target instanceof UpdateReq) {
            validateUpdateRequest((UpdateReq) target, errors);
        }
    }

    /**
     * 상품 등록 요청 검증
     */
    private void validateCreateRequest(CreateReq request, Errors errors) {
        Locale locale = LocaleContextHolder.getLocale();
        
        // 카테고리별 가격 제한 검증
        if ("전자제품".equals(request.getCategory()) && request.getPrice() != null && request.getPrice() > 500000) {
            String message = messageSource.getMessage("product.price.exceeded", new Object[]{500000}, locale);
            errors.rejectValue("price", "price.exceeded", message);
        }
        
        // 프리미엄 상품 검증 (평점 4.5 이상인 경우 최소 가격 검증)
        if (request.getRating() != null && request.getRating() > 4.5) {
            if (request.getPrice() != null && request.getPrice() < 100000) {
                String message = messageSource.getMessage("product.premium.price.too.low", new Object[]{100000}, locale);
                errors.rejectValue("price", "price.too.low", message);
            }
        }
        
        // 카테고리 유효성 검증 (외부 시스템 연동 시뮬레이션)
        if (request.getCategory() != null && !isValidCategory(request.getCategory())) {
            String message = messageSource.getMessage("product.category.invalid", null, locale);
            errors.rejectValue("category", "category.invalid", message);
        }
    }

    /**
     * 상품 수정 요청 검증
     */
    private void validateUpdateRequest(UpdateReq request, Errors errors) {
        Locale locale = LocaleContextHolder.getLocale();
        
        // 수정 시 특별한 검증 로직
        if ("전자제품".equals(request.getCategory()) && request.getPrice() != null && request.getPrice() > 500000) {
            String message = messageSource.getMessage("product.price.exceeded", new Object[]{500000}, locale);
            errors.rejectValue("price", "price.exceeded", message);
        }
    }

    /**
     * 카테고리 유효성 검증 (외부 시스템 연동 시뮬레이션)
     */
    private boolean isValidCategory(String category) {
        // 실제로는 외부 API 호출이나 데이터베이스 조회
        String[] validCategories = {"전자제품", "의류", "도서", "식품", "스포츠"};
        for (String validCategory : validCategories) {
            if (validCategory.equals(category)) {
                return true;
            }
        }
        return false;
    }
} 