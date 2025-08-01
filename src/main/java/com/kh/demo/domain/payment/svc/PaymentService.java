package com.kh.demo.domain.payment.svc;

import com.kh.demo.domain.payment.dto.PaymentRequest;
import com.kh.demo.domain.payment.dto.PaymentResponse;
import com.kh.demo.domain.payment.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    
    /**
     * 결제 처리
     */
    PaymentResponse processPayment(PaymentRequest request);
    
    /**
     * 결제 정보 저장
     */
    Long savePayment(Payment payment);
    
    /**
     * 결제 ID로 조회
     */
    Optional<Payment> findById(Long paymentId);
    
    /**
     * 주문 ID로 결제 정보 조회
     */
    Optional<Payment> findByOrderId(Long orderId);
    
    /**
     * 결제번호로 조회
     */
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    
    /**
     * 전체 결제 목록 조회
     */
    List<Payment> findAll();
    
    /**
     * 결제 상태별 목록 조회
     */
    List<Payment> findByStatus(String status);
    
    /**
     * 결제 방법별 목록 조회
     */
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    /**
     * 결제 정보 업데이트
     */
    int updatePayment(Payment payment);
    
    /**
     * 결제 상태 업데이트
     */
    int updateStatus(Long paymentId, String status);
    
    /**
     * 결제 취소
     */
    int cancelPayment(Long paymentId, String reason);
    
    /**
     * 결제 환불
     */
    int refundPayment(Long paymentId, String reason);
    
    /**
     * 전체 결제 건수 조회
     */
    int getTotalCount();
    
    /**
     * 결제 상태별 건수 조회
     */
    int getCountByStatus(String status);
} 