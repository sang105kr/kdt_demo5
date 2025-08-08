package com.kh.demo.domain.payment.dao;

import com.kh.demo.domain.payment.entity.Payment;
import com.kh.demo.domain.common.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface PaymentDAO extends BaseDAO<Payment, Long> {
    
    /**
     * 주문 ID로 결제 정보 조회
     */
    Optional<Payment> findByOrderId(Long orderId);
    
    /**
     * 결제번호로 조회
     */
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    
    /**
     * 승인번호로 조회
     */
    Optional<Payment> findByApprovalNumber(String approvalNumber);
    
    /**
     * 결제 상태별 목록 조회
     */
    List<Payment> findByStatus(Long statusId);
    
    /**
     * 결제 방법별 목록 조회
     */
    List<Payment> findByPaymentMethod(Long methodId);
    
    /**
     * 결제 상태 업데이트
     */
    int updateStatus(Long paymentId, Long statusId);
    
    /**
     * 결제 취소
     */
    int cancelPayment(Long paymentId, String reason);
    
    /**
     * 결제 환불
     */
    int refundPayment(Long paymentId, String reason);
    
    /**
     * 결제 상태별 건수 조회
     */
    int getCountByStatus(Long statusId);
} 