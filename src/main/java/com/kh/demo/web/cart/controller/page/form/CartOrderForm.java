package com.kh.demo.web.cart.controller.page.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CartOrderForm {
    
    private Long orderStatusId;
    private Long paymentStatusId;

    @NotNull(message = "결제 방법을 선택해주세요.")
    private Long paymentMethodId;
    
    @NotBlank(message = "수령인명을 입력해주세요.")
    private String recipientName;
    
    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식을 입력해주세요.")
    private String recipientPhone;
    
    private String shippingMemo;
    
    // 주소 관련 필드
    @NotBlank(message = "우편번호를 입력해주세요.")
    private String zipcode;         // 우편번호
    @NotBlank(message = "기본주소를 입력해주세요.")
    private String address;         // 기본주소
    @NotBlank(message = "상세주소를 입력해주세요.")
    private String addressDetail;   // 상세주소
} 