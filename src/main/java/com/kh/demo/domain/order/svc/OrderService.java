package com.kh.demo.domain.order.svc;

import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.order.dto.OrderDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    /**
     * 장바구니에서 주문 생성
     */
    Order createOrderFromCart(Long memberId, String paymentMethod,
                             String recipientName, String recipientPhone,
                             String shippingAddress, String shippingMemo);
    
    /**
     * 단일 상품 바로 주문 생성
     */
    Order createDirectOrder(Long memberId, Long productId, Integer quantity,
                           String paymentMethod, String recipientName, String recipientPhone,
                           String shippingAddress, String shippingMemo);
    
    /**
     * 주문번호로 주문 조회 (DTO 포함)
     */
    Optional<OrderDTO> findDTOByOrderNumber(String orderNumber);
    
    /**
     * 주문번호로 주문 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * 주문 ID로 주문 조회 (DTO 포함)
     */
    Optional<OrderDTO> findDTOByOrderId(Long orderId);
    
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
    List<Order> findOrdersByMemberId(Long memberId);
    
    /**
     * 주문 상태 업데이트
     */
    void updateOrderStatus(Long orderId, String orderStatus);
    
    /**
     * 결제 상태 업데이트
     */
    void updatePaymentStatus(Long orderId, String paymentStatus);
    
    /**
     * 주문 취소
     */
    void cancelOrder(Long orderId);
    
    /**
     * 주문 상품 목록 조회
     */
    List<OrderItem> getOrderItems(Long orderId);
    
    /**
     * 전체 주문 목록 조회 (관리자용, DTO 포함)
     */
    List<OrderDTO> findAllOrderDTOs();
    
    /**
     * 전체 주문 목록 조회 (관리자용)
     */
    List<Order> getAllOrders();
    
    /**
     * 주문 상태별 주문 목록 조회 (관리자용, DTO 포함)
     */
    List<OrderDTO> findDTOByOrderStatus(String orderStatus);
    
    /**
     * 주문 상태별 주문 목록 조회 (관리자용)
     */
    List<Order> getOrdersByStatus(String orderStatus);
} 