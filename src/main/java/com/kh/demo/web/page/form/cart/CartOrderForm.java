package com.kh.demo.web.page.form.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CartOrderForm {
    
    @NotBlank(message = "결제 방법을 선택해주세요.")
    private String paymentMethod;
    
    @NotBlank(message = "수령인명을 입력해주세요.")
    private String recipientName;
    
    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식을 입력해주세요.")
    private String recipientPhone;
    
    @NotBlank(message = "배송주소를 입력해주세요.")
    private String shippingAddress;
    
    private String shippingMemo;
} 