package com.kh.demo.domain.order.svc;

import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.cart.svc.CartService;
import com.kh.demo.domain.order.dao.OrderDAO;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.common.exception.ErrorCode;
import com.kh.demo.domain.notification.svc.NotificationSVC;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kh.demo.domain.order.dto.OrderDTO;
import com.kh.demo.domain.order.dto.OrderItemDTO;
import com.kh.demo.domain.review.svc.ReviewService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final CartService cartService;
    private final ProductService productService;
    private final CodeSVC codeSVC;
    private final ReviewService reviewService; // Inject ReviewService
    private final NotificationSVC notificationSVC; // Inject NotificationSVC

    @Override
    public Order createOrderFromCart(Long memberId, Long paymentMethodId, Long orderStatusId, Long paymentStatusId,
                                   String recipientName, String recipientPhone,
                                   String zipcode, String address, String addressDetail, String shippingMemo) {
        log.info("장바구니에서 주문 생성 - memberId: {}, paymentMethodId: {}", memberId, paymentMethodId);
        
        // 장바구니 상품 조회
        List<CartItem> cartItems = cartService.getCartItems(memberId);
        if (cartItems.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("memberId", memberId);
            throw ErrorCode.CART_EMPTY.toException(details);
        }
        
        // 재고 확인 및 상품 정보 조회
        validateStockAndGetProducts(cartItems);
        
        // 주문 생성
        Order order = createOrder(memberId, paymentMethodId, orderStatusId, paymentStatusId, recipientName, recipientPhone, zipcode, address, addressDetail, shippingMemo);
        
        // 주문 상품 생성
        int totalAmount = 0;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = createOrderItem(order.getOrderId(), cartItem);
            totalAmount += orderItem.getSubtotal();
            
            // 재고 차감
            productService.decreaseStock(cartItem.getProductId(), cartItem.getQuantity());
        }
        
        // 총 금액 업데이트
        order.setTotalAmount(totalAmount);
        updateOrderTotalAmount(order.getOrderId(), totalAmount);
        
        // 장바구니 비우기
        cartService.clearCart(memberId);
        
        // 주문 완료 알림 생성
        try {
            notificationSVC.createOrderNotification(
                memberId,
                "주문이 완료되었습니다",
                String.format("주문번호 %s의 주문이 완료되었습니다. 총 금액: %,d원", 
                    order.getOrderNumber(), totalAmount),
                order.getOrderId()
            );
            log.info("주문 완료 알림 생성 - orderId: {}, memberId: {}", order.getOrderId(), memberId);
        } catch (Exception e) {
            log.error("주문 완료 알림 생성 실패 - orderId: {}, memberId: {}, error: {}", 
                    order.getOrderId(), memberId, e.getMessage());
        }
        
        log.info("주문 생성 완료 - orderId: {}, orderNumber: {}, totalAmount: {}", 
                order.getOrderId(), order.getOrderNumber(), totalAmount);
        
        return order;
    }

    @Override
    public Order createDirectOrder(Long memberId, Long productId, Integer quantity,
                                 Long paymentMethodId, Long orderStatusId, Long paymentStatusId, String recipientName, String recipientPhone,
                                 String zipcode, String address, String addressDetail, String shippingMemo) {
        log.info("단일 상품 바로 주문 - memberId: {}, productId: {}, quantity: {}", 
                memberId, productId, quantity);
        
        // 상품 정보 조회
        Optional<Products> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("productId", productId);
            throw ErrorCode.PRODUCT_NOT_FOUND.toException(details);
        }
        
        Products product = productOpt.get();
        
        // 재고 확인
        if (product.getStockQuantity() < quantity) {
            Map<String, Object> details = new HashMap<>();
            details.put("productId", productId);
            details.put("productName", product.getPname());
            details.put("requestedQuantity", quantity);
            details.put("availableStock", product.getStockQuantity());
            throw ErrorCode.INSUFFICIENT_STOCK.toException(details);
        }
        
        // 주문 생성
        Order order = createOrder(memberId, paymentMethodId, orderStatusId, paymentStatusId, recipientName, recipientPhone, zipcode, address, addressDetail, shippingMemo);
        
        // 주문 상품 생성
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setProductId(productId);
        orderItem.setProductName(product.getPname());
        orderItem.setProductPrice(product.getPrice());
        orderItem.setQuantity(quantity);
        orderItem.calculateSubtotal();
        
        orderDAO.saveOrderItem(orderItem);
        
        // 총 금액 업데이트
        order.setTotalAmount(orderItem.getSubtotal());
        updateOrderTotalAmount(order.getOrderId(), orderItem.getSubtotal());
        
        // 재고 차감
        productService.decreaseStock(productId, quantity);
        
        // 주문 완료 알림 생성
        try {
            notificationSVC.createOrderNotification(
                memberId,
                "주문이 완료되었습니다",
                String.format("주문번호 %s의 주문이 완료되었습니다. 총 금액: %,d원", 
                    order.getOrderNumber(), orderItem.getSubtotal()),
                order.getOrderId()
            );
            log.info("단일 상품 주문 완료 알림 생성 - orderId: {}, memberId: {}", order.getOrderId(), memberId);
        } catch (Exception e) {
            log.error("단일 상품 주문 완료 알림 생성 실패 - orderId: {}, memberId: {}, error: {}", 
                    order.getOrderId(), memberId, e.getMessage());
        }
        
        log.info("단일 상품 주문 생성 완료 - orderId: {}, orderNumber: {}, totalAmount: {}", 
                order.getOrderId(), order.getOrderNumber(), orderItem.getSubtotal());
        
        return order;
    }

    @Override
    public Optional<OrderDTO> findDTOByOrderNumber(String orderNumber) {
        log.info("주문번호로 주문 조회 (DTO 포함) - orderNumber: {}", orderNumber);
        return orderDAO.findDTOByOrderNumber(orderNumber);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        log.info("주문번호로 주문 조회 - orderNumber: {}", orderNumber);
        return orderDAO.findByOrderNumber(orderNumber);
    }

    @Override
    public Optional<OrderDTO> findDTOByOrderId(Long orderId) {
        log.info("주문 ID로 주문 조회 (DTO 포함) - orderId: {}", orderId);
        return orderDAO.findDTOByOrderId(orderId);
    }

    @Override
    public Optional<Order> findByOrderId(Long orderId) {
        log.info("주문 ID로 주문 조회 - orderId: {}", orderId);
        return orderDAO.findById(orderId);
    }

    @Override
    public List<OrderDTO> findDTOByMemberId(Long memberId) {
        log.info("회원 주문 목록 조회 (DTO 포함) - memberId: {}", memberId);
        List<OrderDTO> orderDTOs = orderDAO.findDTOByMemberId(memberId);
        for (OrderDTO orderDTO : orderDTOs) {
            if (orderDTO.getOrderItems() != null) {
                for (OrderItemDTO item : orderDTO.getOrderItems()) {
                    reviewService.findActiveByOrderIdAndProductId(orderDTO.getOrderId(), item.getProductId())
                        .ifPresentOrElse(
                            review -> {
                                item.setReviewed(true);
                                item.setReviewId(review.getReviewId());
                            },
                            () -> {
                                item.setReviewed(false);
                                item.setReviewId(null);
                            }
                        );
                }
            }
        }
        return orderDTOs;
    }
    
    @Override
    public List<OrderDTO> findDTOByMemberIdAndStatus(Long memberId, Long orderStatusId) {
        List<OrderDTO> orderDTOs = orderDAO.findDTOByMemberIdAndStatus(memberId, orderStatusId);
        for (OrderDTO orderDTO : orderDTOs) {
            if (orderDTO.getOrderItems() != null) {
                for (OrderItemDTO item : orderDTO.getOrderItems()) {
                    reviewService.findActiveByOrderIdAndProductId(orderDTO.getOrderId(), item.getProductId())
                        .ifPresentOrElse(
                            review -> {
                                item.setReviewed(true);
                                item.setReviewId(review.getReviewId());
                            },
                            () -> {
                                item.setReviewed(false);
                                item.setReviewId(null);
                            }
                        );
                }
            }
        }
        return orderDTOs;
    }
    
    @Override
    public List<Order> findOrdersByMemberId(Long memberId) {
        log.info("회원 주문 목록 조회 - memberId: {}", memberId);
        return orderDAO.findByMemberId(memberId);
    }

    @Override
    public List<OrderDTO> findAllOrderDTOs() {
        log.info("전체 주문 목록 조회 (관리자용, DTO 포함)");
        return orderDAO.findAllOrderDTOs();
    }

    @Override
    public List<OrderDTO> findDTOByOrderStatus(Long orderStatusId) {
        log.info("주문 상태별 주문 목록 조회 (DTO 포함) - orderStatusId: {}", orderStatusId);
        return orderDAO.findDTOByOrderStatus(orderStatusId);
    }

    @Override
    public void updateOrderStatus(Long orderId, Long orderStatusId) {
        log.info("주문 상태 업데이트 - orderId: {}, orderStatusId: {}", orderId, orderStatusId);
        
        // 주문 정보 조회
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        if (orderOpt.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            throw ErrorCode.ORDER_NOT_FOUND.toException(details);
        }
        
        Order order = orderOpt.get();
        
        int updatedRows = orderDAO.updateOrderStatus(orderId, orderStatusId);
        if (updatedRows == 0) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            throw ErrorCode.ORDER_NOT_FOUND.toException(details);
        }
        
        // 주문 상태 변경 알림 생성
        try {
            String statusName = codeSVC.getCodeDecode("ORDER_STATUS", orderStatusId);
            notificationSVC.createOrderNotification(
                order.getMemberId(),
                "주문 상태가 변경되었습니다",
                String.format("주문번호 %s의 상태가 '%s'로 변경되었습니다.", 
                    order.getOrderNumber(), statusName),
                orderId
            );
            log.info("주문 상태 변경 알림 생성 - orderId: {}, memberId: {}, status: {}", 
                    orderId, order.getMemberId(), statusName);
        } catch (Exception e) {
            log.error("주문 상태 변경 알림 생성 실패 - orderId: {}, error: {}", orderId, e.getMessage());
        }
    }

    @Override
    public void updatePaymentStatus(Long orderId, Long paymentStatusId) {
        log.info("결제 상태 업데이트 - orderId: {}, paymentStatusId: {}", orderId, paymentStatusId);
        
        int updatedRows = orderDAO.updatePaymentStatus(orderId, paymentStatusId);
        if (updatedRows == 0) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            throw ErrorCode.ORDER_NOT_FOUND.toException(details);
        }
    }

    @Override
    public void cancelOrder(Long orderId) {
        log.info("주문 취소 - orderId: {}", orderId);
        
        // 주문 조회
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        if (orderOpt.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            throw ErrorCode.ORDER_NOT_FOUND.toException(details);
        }
        
        Order order = orderOpt.get();
        
        // 주문 상태가 취소 가능한 상태인지 확인
        if (order.getOrderStatusId().equals(codeSVC.getCodeId("ORDER_STATUS", "PENDING")) || order.getOrderStatusId().equals(codeSVC.getCodeId("ORDER_STATUS", "DELIVERED"))) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            details.put("currentStatus", order.getOrderStatusId());
            details.put("orderNumber", order.getOrderNumber());
            throw ErrorCode.INVALID_ORDER_STATUS.toException(details);
        }
        
        // 주문 상품 목록 조회
        List<OrderItem> orderItems = orderDAO.findOrderItemsByOrderId(orderId);
        
        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            productService.increaseStock(orderItem.getProductId(), orderItem.getQuantity());
        }
        
        // 주문 상태를 취소로 변경
        orderDAO.updateOrderStatus(orderId, codeSVC.getCodeId("ORDER_STATUS", "CANCELLED"));
        
        log.info("주문 취소 완료 - orderId: {}", orderId);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        log.info("주문 상품 목록 조회 - orderId: {}", orderId);
        
        return orderDAO.findOrderItemsByOrderId(orderId);
    }
    
    @Override
    public List<OrderItemDTO> getOrderItemDTOs(Long orderId) {
        List<OrderItemDTO> items = orderDAO.findOrderItemDTOsByOrderId(orderId);
        for (OrderItemDTO item : items) {
            reviewService.findByOrderIdAndProductId(orderId, item.getProductId())
                .ifPresentOrElse(
                    review -> {
                        item.setReviewed(true);
                        item.setReviewId(review.getReviewId());
                    },
                    () -> {
                        item.setReviewed(false);
                        item.setReviewId(null);
                    }
                );
        }
        return items;
    }
    
    @Override
    public List<Order> getAllOrders() {
        log.info("전체 주문 목록 조회 (관리자용)");
        
        List<Order> orders = orderDAO.findAll();
        
        return orders;
    }

    @Override
    public List<Order> getOrdersByStatus(Long orderStatusId) {
        log.info("주문 상태별 주문 목록 조회 - orderStatusId: {}", orderStatusId);
        
        List<Order> orders = orderDAO.findByOrderStatus(orderStatusId);
        
        return orders;
    }
    
    @Override
    public int countAllOrders() {
        log.info("전체 주문 개수 조회 (관리자용)");
        return orderDAO.getTotalCount();
    }
    
    @Override
    public int countOrdersByStatus(Long orderStatusId) {
        log.info("주문 상태별 주문 개수 조회 - orderStatusId: {}", orderStatusId);
        return orderDAO.countByOrderStatus(orderStatusId);
    }
    
    @Override
    public List<OrderDTO> findAllOrderDTOsWithPaging(int pageNo, int pageSize) {
        log.info("전체 주문 목록 조회 (페이징, DTO 포함) - pageNo: {}, pageSize: {}", pageNo, pageSize);
        return orderDAO.findAllOrderDTOsWithPaging(pageNo, pageSize);
    }
    
    @Override
    public List<OrderDTO> findDTOByOrderStatusWithPaging(Long orderStatusId, int pageNo, int pageSize) {
        log.info("주문 상태별 주문 목록 조회 (페이징, DTO 포함) - orderStatusId: {}, pageNo: {}, pageSize: {}", 
                orderStatusId, pageNo, pageSize);
        return orderDAO.findDTOByOrderStatusWithPaging(orderStatusId, pageNo, pageSize);
    }

    @Override
    public List<Order> findDeliveredOrdersByMemberAndProduct(Long memberId, Long productId) {
        log.info("사용자의 배송완료된 주문 조회 - memberId: {}, productId: {}", memberId, productId);
        
        // 배송완료 상태 코드 조회
        Long deliveredStatusId = codeSVC.getCodeId("ORDER_STATUS", "DELIVERED");
        if (deliveredStatusId == null) {
            log.error("DELIVERED 상태 코드를 찾을 수 없습니다.");
            return List.of();
        }
        
        // 사용자의 배송완료된 주문 중 해당 상품이 포함된 주문 조회
        return orderDAO.findDeliveredOrdersByMemberAndProduct(memberId, productId, deliveredStatusId);
    }
    
    @Override
    public boolean isProductPurchasedByMember(Long productId, Long memberId) {
        log.info("상품 구매 여부 확인 - productId: {}, memberId: {}", productId, memberId);
        
        // 배송완료 상태 코드 조회
        Long deliveredStatusId = codeSVC.getCodeId("ORDER_STATUS", "DELIVERED");
        if (deliveredStatusId == null) {
            log.error("DELIVERED 상태 코드를 찾을 수 없습니다.");
            return false;
        }
        
        // 사용자의 배송완료된 주문 중 해당 상품이 포함된 주문이 있는지 확인
        List<Order> deliveredOrders = orderDAO.findDeliveredOrdersByMemberAndProduct(memberId, productId, deliveredStatusId);
        
        boolean isPurchased = !deliveredOrders.isEmpty();
        log.info("상품 구매 여부 확인 결과 - productId: {}, memberId: {}, isPurchased: {}", 
                productId, memberId, isPurchased);
        
        return isPurchased;
    }
    
    /**
     * 주문 생성 (공통 로직)
     */
    private Order createOrder(Long memberId, Long paymentMethodId, Long orderStatusId, Long paymentStatusId, String recipientName, String recipientPhone, String zipcode, String address, String addressDetail, String shippingMemo) {
        Order order = new Order();
        order.setMemberId(memberId);
        order.setOrderNumber(orderDAO.generateOrderNumber());
        order.setOrderStatusId(orderStatusId);
        order.setPaymentMethodId(paymentMethodId);
        order.setPaymentStatusId(paymentStatusId);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setZipcode(zipcode);
        order.setAddress(address);
        order.setAddressDetail(addressDetail);
        order.setShippingMemo(shippingMemo);
        order.setTotalAmount(0); // 임시값, 나중에 업데이트
        
        Long orderId = orderDAO.save(order);
        order.setOrderId(orderId);
        
        return order;
    }
    
    /**
     * 주문 상품 생성 (공통 로직)
     */
    private OrderItem createOrderItem(Long orderId, CartItem cartItem) {
        log.info("주문 상품 생성 시작 - orderId: {}, productId: {}, quantity: {}", 
                orderId, cartItem.getProductId(), cartItem.getQuantity());
        
        // 상품 정보 조회
        Optional<Products> productOpt = productService.findById(cartItem.getProductId());
        if (productOpt.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("productId", cartItem.getProductId());
            throw ErrorCode.PRODUCT_NOT_FOUND.toException(details);
        }
        Products product = productOpt.get();
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductName(product.getPname());
        orderItem.setProductPrice(product.getPrice());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.calculateSubtotal();
        
        log.info("주문 상품 정보 설정 완료 - productName: {}, productPrice: {}, subtotal: {}", 
                orderItem.getProductName(), orderItem.getProductPrice(), orderItem.getSubtotal());
        
        Long orderItemId = orderDAO.saveOrderItem(orderItem);
        orderItem.setOrderItemId(orderItemId);
        
        log.info("주문 상품 저장 완료 - orderItemId: {}", orderItemId);
        
        return orderItem;
    }
    
    /**
     * 재고 확인 및 상품 정보 조회
     */
    private void validateStockAndGetProducts(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Optional<Products> productOpt = productService.findById(cartItem.getProductId());
            if (productOpt.isEmpty()) {
                Map<String, Object> details = new HashMap<>();
                details.put("productId", cartItem.getProductId());
                throw ErrorCode.PRODUCT_NOT_FOUND.toException(details);
            }
            
            Products product = productOpt.get();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                Map<String, Object> details = new HashMap<>();
                details.put("productId", cartItem.getProductId());
                details.put("productName", product.getPname());
                details.put("requestedQuantity", cartItem.getQuantity());
                details.put("availableStock", product.getStockQuantity());
                throw ErrorCode.INSUFFICIENT_STOCK.toException(details);
            }
        }
    }
    
    /**
     * 주문 총 금액 업데이트
     */
    private void updateOrderTotalAmount(Long orderId, Integer totalAmount) {
        log.info("주문 총금액 업데이트 - orderId: {}, totalAmount: {}", orderId, totalAmount);
        
        int updatedRows = orderDAO.updateTotalAmount(orderId, totalAmount);
        if (updatedRows == 0) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            throw ErrorCode.ORDER_NOT_FOUND.toException(details);
        }
        
        log.info("주문 총금액 업데이트 완료 - orderId: {}, totalAmount: {}", orderId, totalAmount);
    }
} 
