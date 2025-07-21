package com.kh.demo.domain.order.dao;

import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.order.dto.OrderDTO;
import com.kh.demo.domain.order.dto.OrderItemDTO;

import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    
    /**
     * 주문 생성
     */
    Long save(Order order);
    
    /**
     * 주문 상품 추가
     */
    Long saveOrderItem(OrderItem orderItem);
    
    /**
     * 주문번호로 주문 조회 (DTO 포함)
     */
    Optional<OrderDTO> findDTOByOrderNumber(String orderNumber);
    
    /**
     * 주문 ID로 주문 조회 (DTO 포함)
     */
    Optional<OrderDTO> findDTOByOrderId(Long orderId);
    
    /**
     * 주문번호로 주문 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * 주문 ID로 주문 조회 (BaseDAO 패턴)
     */
    Optional<Order> findById(Long orderId);
    
    /**
     * 주문 ID로 주문 조회
     */
    Optional<Order> findByOrderId(Long orderId);
    
    /**
     * 회원의 주문 목록 조회 (DTO 포함)
     */
    List<OrderDTO> findDTOByMemberId(Long memberId);
    
    /**
     * 회원의 주문 목록 조회
     */
    List<Order> findByMemberId(Long memberId);
    
    /**
     * 주문 상태 업데이트
     */
    int updateOrderStatus(Long orderId, String orderStatus);
    
    /**
     * 결제 상태 업데이트
     */
    int updatePaymentStatus(Long orderId, String paymentStatus);
    
    /**
     * 주문 총금액 업데이트
     */
    int updateTotalAmount(Long orderId, Integer totalAmount);
    
    /**
     * 주문 상품 목록 조회
     */
    List<OrderItem> findOrderItemsByOrderId(Long orderId);
    
    /**
     * 주문 상품 목록 조회 (DTO)
     */
    List<OrderItemDTO> findOrderItemDTOsByOrderId(Long orderId);
    
    /**
     * 주문번호 생성 (YYYYMMDD-XXXXX 형식)
     */
    String generateOrderNumber();
    
    /**
     * 전체 주문 목록 조회 (관리자용, DTO 포함)
     */
    List<OrderDTO> findAllOrderDTOs();
    
    /**
     * 전체 주문 목록 조회 (관리자용)
     */
    List<Order> findAllOrders();
    
    /**
     * 주문 상태별 주문 목록 조회 (관리자용, DTO 포함)
     */
    List<OrderDTO> findDTOByOrderStatus(String orderStatus);
    
    /**
     * 주문 상태별 주문 목록 조회 (관리자용)
     */
    List<Order> findByOrderStatus(String orderStatus);
} 