package com.kh.demo.domain.order.svc;

import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.order.dto.OrderDTO;
import com.kh.demo.domain.order.dto.OrderItemDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    /**
     * 장바구니에서 주문 생성
     */
    Order createOrderFromCart(Long memberId, Long paymentMethodId, Long orderStatusId, Long paymentStatusId,
                             String recipientName, String recipientPhone,
                             String shippingAddress, String shippingMemo);
    
    /**
     * 단일 상품 바로 주문 생성
     */
    Order createDirectOrder(Long memberId, Long productId, Integer quantity,
                           Long paymentMethodId, Long orderStatusId, Long paymentStatusId, String recipientName, String recipientPhone,
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
     * 회원의 주문 상태별 목록 조회 (DTO 포함)
     */
    List<OrderDTO> findDTOByMemberIdAndStatus(Long memberId, Long orderStatusId);
    
    /**
     * 회원의 주문 목록 조회
     */
    List<Order> findOrdersByMemberId(Long memberId);
    
    /**
     * 주문 상태 업데이트
     */
    void updateOrderStatus(Long orderId, Long orderStatusId);
    
    /**
     * 결제 상태 업데이트
     */
    void updatePaymentStatus(Long orderId, Long paymentStatusId);
    
    /**
     * 주문 취소
     */
    void cancelOrder(Long orderId);
    
    /**
     * 주문 상품 목록 조회
     */
    List<OrderItem> getOrderItems(Long orderId);
    
    /**
     * 주문 상품 목록 조회 (DTO)
     */
    List<OrderItemDTO> getOrderItemDTOs(Long orderId);
    
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
    List<OrderDTO> findDTOByOrderStatus(Long orderStatusId);
    
    /**
     * 주문 상태별 주문 목록 조회 (관리자용)
     */
    List<Order> getOrdersByStatus(Long orderStatusId);
    
    /**
     * 전체 주문 개수 조회 (관리자용)
     */
    int countAllOrders();
    
    /**
     * 주문 상태별 주문 개수 조회 (관리자용)
     */
    int countOrdersByStatus(Long orderStatusId);
    
    /**
     * 전체 주문 목록 조회 (페이징, 관리자용, DTO 포함)
     */
    List<OrderDTO> findAllOrderDTOsWithPaging(int pageNo, int pageSize);
    
    /**
     * 주문 상태별 주문 목록 조회 (페이징, 관리자용, DTO 포함)
     */
    List<OrderDTO> findDTOByOrderStatusWithPaging(Long orderStatusId, int pageNo, int pageSize);
} 