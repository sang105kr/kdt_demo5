package com.kh.demo.web.payment.controller.page.form;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class PaymentForm {
    
    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;
    
    @NotBlank(message = "결제 방법을 선택해주세요.")
    @Pattern(regexp = "^(CARD|BANK_TRANSFER|CASH)$", message = "올바른 결제 방법을 선택해주세요.")
    private String paymentMethod;
    
    @NotNull(message = "결제 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "결제 금액은 0.01원 이상이어야 합니다.")
    private BigDecimal amount;
    
    // 카드 결제 정보
    @Pattern(regexp = "^[0-9]{13,19}$", message = "올바른 카드번호를 입력해주세요.")
    private String cardNumber;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "올바른 만료월을 입력해주세요.")
    private String cardExpiryMonth;
    
    @Pattern(regexp = "^[0-9]{2}$", message = "올바른 만료년도를 입력해주세요.")
    private String cardExpiryYear;
    
    @Pattern(regexp = "^[0-9]{3,4}$", message = "올바른 CVC를 입력해주세요.")
    private String cardCvc;
    
    private String cardCompany;
    
    // 계좌이체 정보
    private String bankCode;
    
    @Pattern(regexp = "^[0-9-]{10,20}$", message = "올바른 계좌번호를 입력해주세요.")
    private String accountNumber;
    
    // 결제자 정보
    @NotBlank(message = "결제자 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "결제자 이름은 2자 이상 50자 이하여야 합니다.")
    private String payerName;
    
    @NotBlank(message = "결제자 연락처는 필수입니다.")
    @Pattern(regexp = "^[0-9-]{10,15}$", message = "올바른 연락처를 입력해주세요.")
    private String payerPhone;
    
    @Email(message = "올바른 이메일 주소를 입력해주세요.")
    private String payerEmail;
    
    /**
     * 카드 결제인지 확인
     */
    public boolean isCardPayment() {
        return "CARD".equals(paymentMethod);
    }
    
    /**
     * 계좌이체인지 확인
     */
    public boolean isBankTransfer() {
        return "BANK_TRANSFER".equals(paymentMethod);
    }
    
    /**
     * 현금결제인지 확인
     */
    public boolean isCashPayment() {
        return "CASH".equals(paymentMethod);
    }
} 