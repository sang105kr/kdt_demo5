package com.kh.demo.domain.order.svc;

import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.cart.svc.CartService;
import com.kh.demo.domain.order.dao.OrderDAO;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.web.exception.BusinessException;
import com.kh.demo.web.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final CartService cartService;
    private final ProductService productService;

    @Override
    public Order createOrderFromCart(Long memberId, String paymentMethod,
                                   String recipientName, String recipientPhone,
                                   String shippingAddress, String shippingMemo) {
        log.info("장바구니에서 주문 생성 - memberId: {}, paymentMethod: {}", memberId, paymentMethod);
        
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
        Order order = createOrder(memberId, paymentMethod, recipientName, recipientPhone, 
                                shippingAddress, shippingMemo);
        
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
        
        log.info("주문 생성 완료 - orderId: {}, orderNumber: {}, totalAmount: {}", 
                order.getOrderId(), order.getOrderNumber(), totalAmount);
        
        return order;
    }

    @Override
    public Order createDirectOrder(Long memberId, Long productId, Integer quantity,
                                 String paymentMethod, String recipientName, String recipientPhone,
                                 String shippingAddress, String shippingMemo) {
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
        Order order = createOrder(memberId, paymentMethod, recipientName, recipientPhone, 
                                shippingAddress, shippingMemo);
        
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
        
        log.info("단일 상품 주문 생성 완료 - orderId: {}, orderNumber: {}, totalAmount: {}", 
                order.getOrderId(), order.getOrderNumber(), orderItem.getSubtotal());
        
        return order;
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        log.info("주문번호로 주문 조회 - orderNumber: {}", orderNumber);
        
        Optional<Order> orderOpt = orderDAO.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            List<OrderItem> orderItems = orderDAO.findOrderItemsByOrderId(order.getOrderId());
            order.setOrderItems(orderItems);
        }
        
        return orderOpt;
    }

    @Override
    public Optional<Order> findByOrderId(Long orderId) {
        log.info("주문 ID로 주문 조회 - orderId: {}", orderId);
        
        Optional<Order> orderOpt = orderDAO.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            List<OrderItem> orderItems = orderDAO.findOrderItemsByOrderId(order.getOrderId());
            order.setOrderItems(orderItems);
        }
        
        return orderOpt;
    }

    @Override
    public List<Order> findOrdersByMemberId(Long memberId) {
        log.info("회원 주문 목록 조회 - memberId: {}", memberId);
        
        List<Order> orders = orderDAO.findByMemberId(memberId);
        
        // 각 주문의 상품 목록 조회
        for (Order order : orders) {
            List<OrderItem> orderItems = orderDAO.findOrderItemsByOrderId(order.getOrderId());
            order.setOrderItems(orderItems);
        }
        
        return orders;
    }

    @Override
    public void updateOrderStatus(Long orderId, String orderStatus) {
        log.info("주문 상태 업데이트 - orderId: {}, orderStatus: {}", orderId, orderStatus);
        
        int updatedRows = orderDAO.updateOrderStatus(orderId, orderStatus);
        if (updatedRows == 0) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            throw ErrorCode.ORDER_NOT_FOUND.toException(details);
        }
    }

    @Override
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        log.info("결제 상태 업데이트 - orderId: {}, paymentStatus: {}", orderId, paymentStatus);
        
        int updatedRows = orderDAO.updatePaymentStatus(orderId, paymentStatus);
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
        Optional<Order> orderOpt = orderDAO.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            throw ErrorCode.ORDER_NOT_FOUND.toException(details);
        }
        
        Order order = orderOpt.get();
        
        // 주문 상태가 취소 가능한 상태인지 확인
        if ("CANCELLED".equals(order.getOrderStatus()) || "DELIVERED".equals(order.getOrderStatus())) {
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", orderId);
            details.put("currentStatus", order.getOrderStatus());
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
        orderDAO.updateOrderStatus(orderId, "CANCELLED");
        
        log.info("주문 취소 완료 - orderId: {}", orderId);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        log.info("주문 상품 목록 조회 - orderId: {}", orderId);
        
        return orderDAO.findOrderItemsByOrderId(orderId);
    }

    @Override
    public List<Order> getAllOrders() {
        log.info("전체 주문 목록 조회 (관리자용)");
        
        List<Order> orders = orderDAO.findAllOrders();
        
        // 각 주문의 상품 목록 조회
        for (Order order : orders) {
            List<OrderItem> orderItems = orderDAO.findOrderItemsByOrderId(order.getOrderId());
            order.setOrderItems(orderItems);
        }
        
        return orders;
    }

    @Override
    public List<Order> getOrdersByStatus(String orderStatus) {
        log.info("주문 상태별 주문 목록 조회 - orderStatus: {}", orderStatus);
        
        List<Order> orders = orderDAO.findByOrderStatus(orderStatus);
        
        // 각 주문의 상품 목록 조회
        for (Order order : orders) {
            List<OrderItem> orderItems = orderDAO.findOrderItemsByOrderId(order.getOrderId());
            order.setOrderItems(orderItems);
        }
        
        return orders;
    }
    
    /**
     * 주문 생성 (공통 로직)
     */
    private Order createOrder(Long memberId, String paymentMethod, String recipientName, 
                            String recipientPhone, String shippingAddress, String shippingMemo) {
        Order order = new Order();
        order.setMemberId(memberId);
        order.setOrderNumber(orderDAO.generateOrderNumber());
        order.setOrderStatus("PENDING");
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setShippingAddress(shippingAddress);
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
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductName(cartItem.getProduct().getPname());
        orderItem.setProductPrice(cartItem.getProduct().getPrice());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.calculateSubtotal();
        
        orderDAO.saveOrderItem(orderItem);
        
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
        // 주문 총 금액 업데이트를 위한 별도 메서드 필요
        // 현재는 Order 엔티티에 직접 설정하므로 별도 업데이트 불필요
    }
} 