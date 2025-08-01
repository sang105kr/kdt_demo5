# Frontend Development Plan

## 1. 유효성 검사 코딩 패턴

### 1.1 필드 레벨 유효성 검사 (Request DTO)
```java
@Getter
@Setter
@ToString
public class ExampleRequest {
    
    @NotNull(message = "필수 필드입니다.")
    private String requiredField;
    
    @Size(min = 2, max = 50, message = "2-50자 사이로 입력해주세요.")
    private String name;
    
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
             message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    // 유틸리티 메서드
    public boolean hasRequiredField() {
        return requiredField != null && !requiredField.trim().isEmpty();
    }
    
    public long getFieldSize() {
        return requiredField != null ? requiredField.length() : 0;
    }
}
```

### 1.2 비즈니스 레벨 유효성 검사 (Controller)
```java
@PostMapping
public ResponseEntity<ApiResponse<ExampleResponse>> processRequest(
        @Validated @ModelAttribute ExampleRequest request,
        BindingResult bindingResult,
        HttpSession session) {
    
    // 1. 필드 레벨 유효성 검사 (BindingResult)
    if (bindingResult.hasErrors()) {
        StringBuilder errorMessage = new StringBuilder();
        bindingResult.getFieldErrors().forEach(error -> {
            errorMessage.append(error.getDefaultMessage()).append("; ");
        });
        
        ExampleResponse errorResponse = ExampleResponse.builder()
                .message(errorMessage.toString())
                .build();
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorResponse));
    }
    
    try {
        // 2. 비즈니스 레벨 유효성 검사 (컨트롤러에서 직접 처리)
        String businessValidationError = validateBusinessRules(request);
        if (businessValidationError != null) {
            ExampleResponse errorResponse = ExampleResponse.builder()
                    .message(businessValidationError)
                    .build();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, errorResponse));
        }
        
        // 비즈니스 로직 처리...
        
    } catch (Exception e) {
        // 예외 처리...
    }
}

/**
 * 비즈니스 규칙 검증
 */
private String validateBusinessRules(ExampleRequest request) {
    // 파일 크기 검사
    if (request.getFieldSize() > MAX_SIZE) {
        return "파일 크기는 제한을 초과했습니다.";
    }
    
    // 복합 조건 검사
    if (request.hasRequiredField() && request.getName().length() < 3) {
        return "이름은 3자 이상이어야 합니다.";
    }
    
    return null; // 검증 통과
}
```

### 1.3 검증 순서 및 패턴
1. **필드 레벨**: `@Validated` + `BindingResult` (단일 필드 검증)
2. **비즈니스 레벨**: 컨트롤러 메서드에서 직접 처리 (복합 조건 검증)

### 1.4 장점
- **단순성**: 복잡한 그룹 검증이나 커스텀 검증 클래스 불필요
- **유지보수성**: 필드 검증은 DTO, 비즈니스 검증은 컨트롤러에서 관리
- **확장성**: 새로운 검증 규칙 추가가 용이
- **실용성**: 실제 프로젝트에서 사용하기 적합

## 2. 기존 내용... 