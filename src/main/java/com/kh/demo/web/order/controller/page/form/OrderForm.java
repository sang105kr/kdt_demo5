package com.kh.demo.web.order.controller.page.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderForm {
    
    @NotNull(message = "결제 방법을 선택해주세요.")
    private Long paymentMethodId;
    
    @NotBlank(message = "수령인명을 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{2,20}$", message = "수령인명은 2-20자의 한글, 영문, 공백만 입력 가능합니다.")
    private String recipientName;
    
    @NotBlank(message = "수령인 연락처를 입력해주세요.")
    @Pattern(regexp = "^[0-9-]{10,15}$", message = "연락처는 10-15자의 숫자와 하이픈(-)만 입력 가능합니다.")
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