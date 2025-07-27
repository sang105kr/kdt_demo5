package com.kh.demo.web.cart.controller.page.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CartOrderForm {
    
    private Long orderStatusId;
    private Long paymentStatusId;

    // 첫 주문시 주문상태->주문대기, 결제상태->결재대기 초기값 설정
    // @PostConstruct
    // public void init() {
    //     this.orderStatusId = codeDAO.findByGcodeAndCode("ORDER_STATUS", "PENDING")
    //         .orElseThrow(() -> new IllegalStateException("PENDING 코드가 존재하지 않습니다."))
    //         .getCodeId();
    //     this.paymentStatusId = codeDAO.findByGcodeAndCode("PAYMENT_STATUS", "PENDING")
    //         .orElseThrow(() -> new IllegalStateException("PENDING 코드가 존재하지 않습니다."))
    //         .getCodeId();
    // }

    @NotNull(message = "결제 방법을 선택해주세요.")
    private Long paymentMethodId;
    
    @NotBlank(message = "수령인명을 입력해주세요.")
    private String recipientName;
    
    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식을 입력해주세요.")
    private String recipientPhone;
    
    @NotBlank(message = "배송주소를 입력해주세요.")
    private String shippingAddress;
    
    private String shippingMemo;
} 