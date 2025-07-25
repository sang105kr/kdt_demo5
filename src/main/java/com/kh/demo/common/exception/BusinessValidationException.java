package com.kh.demo.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BusinessValidationException extends RuntimeException {
  private final Map<String, String> details;

  public BusinessValidationException(String message) {
    super(message);
    this.details = new HashMap<>();
  }

  public BusinessValidationException(String message, Map<String, String> details) {
    super(message);
    this.details = details;
  }

  public BusinessValidationException(String message, org.springframework.validation.BindingResult bindingResult) {
    super(message);
    this.details = new HashMap<>();
    
    // BindingResult의 에러들을 details에 추가
    if (bindingResult.hasGlobalErrors()) {
      bindingResult.getGlobalErrors().forEach(error -> 
        details.put("global", error.getDefaultMessage())
      );
    }
    
    if (bindingResult.hasFieldErrors()) {
      bindingResult.getFieldErrors().forEach(error -> 
        details.put(error.getField(), error.getDefaultMessage())
      );
    }
  }

  // 글로벌 에러를 추가하는 편의 메서드
  public void addGlobalError(String message) {
    details.put("global", message);
  }

  // 필드 에러를 추가하는 편의 메서드
  public void addFieldError(String field, String message) {
    details.put(field, message);
  }
}
