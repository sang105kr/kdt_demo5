package com.kh.demo.domain.order.dao;

import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.order.dto.OrderDTO;
import com.kh.demo.domain.order.dto.OrderItemDTO;
import com.kh.demo.domain.common.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface OrderDAO extends BaseDAO<Order, Long> {
    
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
     * 회원의 주문 목록 조회 (DTO 포함)
     */
    List<OrderDTO> findDTOByMemberId(Long memberId);
    
    /**
     * 회원의 주문 상태별 목록 조회 (DTO 포함)
     */
    List<OrderDTO> findDTOByMemberIdAndStatus(Long memberId, Long orderStatusId);
    
    /**
     * 회원의 주문 목록 조회
     */
    List<Order> findByMemberId(Long memberId);
    
    /**
     * 주문 상태 업데이트
     */
    int updateOrderStatus(Long orderId, Long orderStatusId);
    
    /**
     * 결제 상태 업데이트
     */
    int updatePaymentStatus(Long orderId, Long paymentStatusId);
    
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
     * 주문 상태별 주문 목록 조회 (관리자용, DTO 포함)
     */
    List<OrderDTO> findDTOByOrderStatus(Long orderStatusId);
    
    /**
     * 주문 상태별 주문 목록 조회 (관리자용)
     */
    List<Order> findByOrderStatus(Long orderStatusId);
    
    /**
     * 주문 상태별 주문 개수 조회
     */
    int countByOrderStatus(Long orderStatusId);
    
    /**
     * 전체 주문 목록 조회 (페이징, 관리자용, DTO 포함)
     */
    List<OrderDTO> findAllOrderDTOsWithPaging(int pageNo, int pageSize);
    
    /**
     * 주문 상태별 주문 목록 조회 (페이징, 관리자용, DTO 포함)
     */
    List<OrderDTO> findDTOByOrderStatusWithPaging(Long orderStatusId, int pageNo, int pageSize);
    
    /**
     * 사용자의 배송완료된 주문 중 특정 상품이 포함된 주문 조회 (리뷰 작성용)
     */
    List<Order> findDeliveredOrdersByMemberAndProduct(Long memberId, Long productId, Long deliveredStatusId);
} 