package com.kh.demo.domain.payment.svc;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.payment.dao.PaymentDAO;
import com.kh.demo.domain.payment.dto.PaymentRequest;
import com.kh.demo.domain.payment.dto.PaymentResponse;
import com.kh.demo.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentDAO paymentDAO;
    private final MockPaymentService mockPaymentService;
    private final CodeSVC codeSVC;  // CodeSVC 주입
    
    /**
     * 결제 상태 텍스트 반환
     */
    public String getPaymentStatusText(Long statusId) {
        if (statusId == null) return "알 수 없음";
        return codeSVC.getCodeValue("PAYMENT_STATUS", statusId);
    }
    
    /**
     * 결제 방법 텍스트 반환
     */
    public String getPaymentMethodText(Long methodId) {
        if (methodId == null) return "알 수 없음";
        return codeSVC.getCodeValue("PAYMENT_METHOD", methodId);
    }
    
    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("결제 처리 시작: 주문ID={}, 결제방법={}", request.getOrderId(), request.getPaymentMethod());
        
        // 모의 결제 처리
        PaymentResponse response = mockPaymentService.processPayment(request);
        
        // 결제 정보 저장
        Payment payment = createPaymentFromRequest(request, response);
        Long paymentId = savePayment(payment);
        
        log.info("결제 처리 완료: 결제ID={}, 성공={}", paymentId, response.isSuccess());
        
        return response;
    }
    
    @Override
    public Long savePayment(Payment payment) {
        return paymentDAO.save(payment);
    }
    
    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentDAO.findById(paymentId);
    }
    
    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentDAO.findByOrderId(orderId);
    }
    
    @Override
    public Optional<Payment> findByPaymentNumber(String paymentNumber) {
        return paymentDAO.findByPaymentNumber(paymentNumber);
    }
    
    @Override
    public List<Payment> findAll() {
        return paymentDAO.findAll();
    }
    
    @Override
    public List<Payment> findByStatus(String status) {
        Long statusId = codeSVC.getCodeId("PAYMENT_STATUS", status);
        return paymentDAO.findByStatus(statusId);
    }
    
    @Override
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        Long methodId = codeSVC.getCodeId("PAYMENT_METHOD", paymentMethod);
        return paymentDAO.findByPaymentMethod(methodId);
    }
    
    @Override
    @Transactional
    public int updatePayment(Payment payment) {
        return paymentDAO.updateById(payment.getPaymentId(), payment);
    }
    
    @Override
    @Transactional
    public int updateStatus(Long paymentId, String status) {
        Long statusId = codeSVC.getCodeId("PAYMENT_STATUS", status);
        return paymentDAO.updateStatus(paymentId, statusId);
    }
    
    @Override
    @Transactional
    public int cancelPayment(Long paymentId, String reason) {
        return paymentDAO.cancelPayment(paymentId, reason);
    }
    
    @Override
    @Transactional
    public int refundPayment(Long paymentId, String reason) {
        return paymentDAO.refundPayment(paymentId, reason);
    }
    
    @Override
    public int getTotalCount() {
        return paymentDAO.getTotalCount();
    }
    
    @Override
    public int getCountByStatus(String status) {
        Long statusId = codeSVC.getCodeId("PAYMENT_STATUS", status);
        return paymentDAO.getCountByStatus(statusId);
    }
    
    /**
     * 결제 요청으로부터 Payment 엔티티 생성
     */
    private Payment createPaymentFromRequest(PaymentRequest request, PaymentResponse response) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setPaymentMethod(codeSVC.getCodeId("PAYMENT_METHOD", request.getPaymentMethod()));
        payment.setAmount(request.getAmount());
        payment.setStatus(codeSVC.getCodeId("PAYMENT_STATUS", response.getStatus()));
        
        if (response.isSuccess()) {
            payment.setPaymentNumber(response.getPaymentNumber());
            payment.setApprovalNumber(response.getApprovalNumber());
            payment.setApprovedAt(response.getApprovedAt());
            
            // 카드 결제인 경우 카드 정보 설정
            if ("CARD".equals(request.getPaymentMethod())) {
                payment.setCardNumber(request.getCardNumber());
                payment.setCardCompany(mockPaymentService.detectCardCompany(request.getCardNumber()));
            }
        } else {
            payment.setFailureReason(response.getFailureReason());
        }
        
        return payment;
    }
} 