package com.kh.demo.web.page;

import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.payment.dto.PaymentRequest;
import com.kh.demo.domain.payment.dto.PaymentResponse;
import com.kh.demo.domain.payment.svc.PaymentService;
import com.kh.demo.web.page.form.payment.PaymentForm;
import com.kh.demo.web.page.form.login.LoginMember;
import com.kh.demo.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final MessageSource messageSource;
    
    /**
     * 결제 페이지
     */
    @GetMapping("/{orderId}")
    public String paymentForm(@PathVariable Long orderId, 
                            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember,
                            Model model) {
        
        // 로그인 체크
        if (loginMember == null) {
            return "redirect:/login";
        }
        
        // 주문 정보 조회
        Optional<com.kh.demo.domain.order.entity.Order> orderOpt = orderService.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", messageSource.getMessage("payment.order.not.found", null, null));
            return "error/error";
        }
        
        com.kh.demo.domain.order.entity.Order order = orderOpt.get();
        
        // 주문자 본인 확인
        if (!order.getMemberId().equals(loginMember.getMemberId())) {
            model.addAttribute("errorMessage", messageSource.getMessage("payment.order.access.denied", null, null));
            return "error/error";
        }
        
        // 이미 결제된 주문인지 확인
        Optional<com.kh.demo.domain.payment.entity.Payment> paymentOpt = paymentService.findByOrderId(orderId);
        if (paymentOpt.isPresent()) {
            com.kh.demo.domain.payment.entity.Payment payment = paymentOpt.get();
            if ("COMPLETED".equals(payment.getStatus())) {
                model.addAttribute("errorMessage", messageSource.getMessage("payment.already.completed", null, null));
                return "error/error";
            }
        }
        
        // 결제 폼 생성
        PaymentForm paymentForm = new PaymentForm();
        paymentForm.setOrderId(orderId);
        paymentForm.setAmount(new java.math.BigDecimal(order.getTotalAmount()));
        paymentForm.setPayerName(loginMember.getNickname());
        paymentForm.setPayerPhone(""); // LoginMember에 phone 필드가 없음
        paymentForm.setPayerEmail(loginMember.getEmail());
        
        model.addAttribute("paymentForm", paymentForm);
        model.addAttribute("order", order);
        model.addAttribute("loginMember", loginMember);
        
        return "payment/paymentForm";
    }
    
    /**
     * 결제 처리
     */
    @PostMapping("/process")
    public String processPayment(@Valid @ModelAttribute PaymentForm paymentForm,
                               BindingResult bindingResult,
                               @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        // 로그인 체크
        if (loginMember == null) {
            return "redirect:/login";
        }
        
        // 주문 정보 조회
        Optional<com.kh.demo.domain.order.entity.Order> orderOpt = orderService.findByOrderId(paymentForm.getOrderId());
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", messageSource.getMessage("payment.order.not.found", null, null));
            return "error/error";
        }
        
        com.kh.demo.domain.order.entity.Order order = orderOpt.get();
        
        // 주문자 본인 확인
        if (!order.getMemberId().equals(loginMember.getMemberId())) {
            model.addAttribute("errorMessage", messageSource.getMessage("payment.order.access.denied", null, null));
            return "error/error";
        }
        
        // 결제 금액 검증
        if (!order.getTotalAmount().equals(paymentForm.getAmount().intValue())) {
            bindingResult.rejectValue("amount", "payment.amount.mismatch", 
                messageSource.getMessage("payment.amount.mismatch", null, null));
        }
        
        // 결제 방법별 필수 필드 검증
        if (paymentForm.isCardPayment()) {
            if (paymentForm.getCardNumber() == null || paymentForm.getCardNumber().trim().isEmpty()) {
                bindingResult.rejectValue("cardNumber", "payment.card.number.required", 
                    messageSource.getMessage("payment.card.number.required", null, null));
            }
            if (paymentForm.getCardExpiryMonth() == null || paymentForm.getCardExpiryMonth().trim().isEmpty()) {
                bindingResult.rejectValue("cardExpiryMonth", "payment.card.expiry.required", 
                    messageSource.getMessage("payment.card.expiry.required", null, null));
            }
            if (paymentForm.getCardCvc() == null || paymentForm.getCardCvc().trim().isEmpty()) {
                bindingResult.rejectValue("cardCvc", "payment.card.cvc.required", 
                    messageSource.getMessage("payment.card.cvc.required", null, null));
            }
        } else if (paymentForm.isBankTransfer()) {
            if (paymentForm.getBankCode() == null || paymentForm.getBankCode().trim().isEmpty()) {
                bindingResult.rejectValue("bankCode", "payment.bank.code.required", 
                    messageSource.getMessage("payment.bank.code.required", null, null));
            }
            if (paymentForm.getAccountNumber() == null || paymentForm.getAccountNumber().trim().isEmpty()) {
                bindingResult.rejectValue("accountNumber", "payment.account.number.required", 
                    messageSource.getMessage("payment.account.number.required", null, null));
            }
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("loginMember", loginMember);
            return "payment/paymentForm";
        }
        
        // 결제 요청 생성
        PaymentRequest paymentRequest = createPaymentRequest(paymentForm);
        
        // 결제 처리
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        
        if (response.isSuccess()) {
            // 결제 성공
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("payment.success", null, null));
            redirectAttributes.addFlashAttribute("paymentNumber", response.getPaymentNumber());
            redirectAttributes.addFlashAttribute("approvalNumber", response.getApprovalNumber());
            return "redirect:/payment/success/" + paymentForm.getOrderId();
        } else {
            // 결제 실패
            model.addAttribute("errorMessage", response.getMessage());
            model.addAttribute("failureReason", response.getFailureReason());
            model.addAttribute("order", order);
            model.addAttribute("loginMember", loginMember);
            return "payment/paymentForm";
        }
    }
    
    /**
     * 결제 성공 페이지
     */
    @GetMapping("/success/{orderId}")
    public String paymentSuccess(@PathVariable Long orderId,
                               @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember,
                               Model model) {
        
        // 로그인 체크
        if (loginMember == null) {
            return "redirect:/login";
        }
        
        // 주문 정보 조회
        Optional<com.kh.demo.domain.order.entity.Order> orderOpt = orderService.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", messageSource.getMessage("payment.order.not.found", null, null));
            return "error/error";
        }
        
        // 결제 정보 조회
        Optional<com.kh.demo.domain.payment.entity.Payment> paymentOpt = paymentService.findByOrderId(orderId);
        if (paymentOpt.isEmpty()) {
            model.addAttribute("errorMessage", messageSource.getMessage("payment.not.found", null, null));
            return "error/error";
        }
        
        model.addAttribute("order", orderOpt.get());
        model.addAttribute("payment", paymentOpt.get());
        model.addAttribute("loginMember", loginMember);
        
        return "payment/paymentSuccess";
    }
    
    /**
     * 결제 내역 조회
     */
    @GetMapping("/history")
    public String paymentHistory(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember,
                               Model model) {
        
        // 로그인 체크
        if (loginMember == null) {
            return "redirect:/login";
        }
        
        // 사용자의 주문 목록 조회
        List<com.kh.demo.domain.order.entity.Order> userOrders = orderService.findOrdersByMemberId(loginMember.getMemberId());
        
        // 주문별 결제 정보를 포함한 결제 내역 목록 생성
        List<PaymentHistoryItem> paymentList = new ArrayList<>();
        
        for (com.kh.demo.domain.order.entity.Order order : userOrders) {
            Optional<com.kh.demo.domain.payment.entity.Payment> paymentOpt = paymentService.findByOrderId(order.getOrderId());
            if (paymentOpt.isPresent()) {
                com.kh.demo.domain.payment.entity.Payment payment = paymentOpt.get();
                PaymentHistoryItem item = new PaymentHistoryItem();
                item.setOrderNumber(order.getOrderNumber());
                item.setPaymentNumber(payment.getPaymentNumber());
                item.setPaymentMethod(payment.getPaymentMethod());
                item.setAmount(payment.getAmount());
                item.setStatus(payment.getStatus());
                item.setApprovedAt(payment.getApprovedAt());
                paymentList.add(item);
            }
        }
        
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("loginMember", loginMember);
        
        return "payment/paymentHistory";
    }
    
    /**
     * PaymentForm을 PaymentRequest로 변환
     */
    private PaymentRequest createPaymentRequest(PaymentForm form) {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(form.getOrderId());
        request.setPaymentMethod(form.getPaymentMethod());
        request.setAmount(form.getAmount());
        request.setCardNumber(form.getCardNumber());
        request.setCardExpiryMonth(form.getCardExpiryMonth());
        request.setCardExpiryYear(form.getCardExpiryYear());
        request.setCardCvc(form.getCardCvc());
        request.setCardCompany(form.getCardCompany());
        request.setBankCode(form.getBankCode());
        request.setAccountNumber(form.getAccountNumber());
        request.setPayerName(form.getPayerName());
        request.setPayerPhone(form.getPayerPhone());
        request.setPayerEmail(form.getPayerEmail());
        return request;
    }
    
    /**
     * 결제 내역 아이템 클래스
     */
    public static class PaymentHistoryItem {
        private String orderNumber;
        private String paymentNumber;
        private String paymentMethod;
        private java.math.BigDecimal amount;
        private String status;
        private java.time.LocalDateTime approvedAt;
        
        // Getters and Setters
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        
        public String getPaymentNumber() { return paymentNumber; }
        public void setPaymentNumber(String paymentNumber) { this.paymentNumber = paymentNumber; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public java.time.LocalDateTime getApprovedAt() { return approvedAt; }
        public void setApprovedAt(java.time.LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
        
        public String getPaymentMethodText() {
            switch (paymentMethod) {
                case "CARD": return "신용카드";
                case "BANK_TRANSFER": return "계좌이체";
                case "CASH": return "현금결제";
                default: return "알 수 없음";
            }
        }
        
        public String getStatusText() {
            switch (status) {
                case "PENDING": return "결제 대기";
                case "PROCESSING": return "결제 처리중";
                case "COMPLETED": return "결제 완료";
                case "FAILED": return "결제 실패";
                case "CANCELLED": return "결제 취소";
                case "REFUNDED": return "환불 완료";
                default: return "알 수 없음";
            }
        }
    }
} 