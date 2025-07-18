package com.kh.demo.domain.order.dao;

import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.entity.OrderItem;
import com.kh.demo.domain.order.dto.OrderDTO;
import com.kh.demo.domain.order.dto.OrderItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderDAOImpl implements OrderDAO {

    private final NamedParameterJdbcTemplate template;

    /**
     * 주문 RowMapper
     */
    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        Order order = new Order();
        order.setOrderId(rs.getLong("order_id"));
        order.setMemberId(rs.getLong("member_id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setOrderStatus(rs.getString("order_status"));
        order.setTotalAmount(rs.getInt("total_amount"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setRecipientName(rs.getString("recipient_name"));
        order.setRecipientPhone(rs.getString("recipient_phone"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setShippingMemo(rs.getString("shipping_memo"));
        order.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
        order.setUdate(rs.getTimestamp("udate").toLocalDateTime());
        return order;
    };

    /**
     * 주문 상품 RowMapper
     */
    private final RowMapper<OrderItem> orderItemRowMapper = (rs, rowNum) -> {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(rs.getLong("order_item_id"));
        orderItem.setOrderId(rs.getLong("order_id"));
        orderItem.setProductId(rs.getLong("product_id"));
        orderItem.setProductName(rs.getString("product_name"));
        orderItem.setProductPrice(rs.getInt("product_price"));
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setSubtotal(rs.getInt("subtotal"));
        orderItem.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
        orderItem.setUdate(rs.getTimestamp("udate").toLocalDateTime());
        return orderItem;
    };

    /**
     * 주문 상품 DTO RowMapper
     */
    private final RowMapper<OrderItemDTO> orderItemDTORowMapper = (rs, rowNum) -> {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderItemId(rs.getLong("order_item_id"));
        orderItemDTO.setOrderId(rs.getLong("order_id"));
        orderItemDTO.setProductId(rs.getLong("product_id"));
        orderItemDTO.setProductName(rs.getString("product_name"));
        orderItemDTO.setProductPrice(rs.getInt("product_price"));
        orderItemDTO.setQuantity(rs.getInt("quantity"));
        orderItemDTO.setSubtotal(rs.getInt("subtotal"));
        orderItemDTO.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
        orderItemDTO.setUdate(rs.getTimestamp("udate").toLocalDateTime());
        return orderItemDTO;
    };

    @Override
    public Long save(Order order) {
        String sql = """
            INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, 
                               payment_method, payment_status, recipient_name, recipient_phone, 
                               shipping_address, shipping_memo, cdate, udate)
            VALUES (seq_order_id.nextval, :memberId, :orderNumber, :orderStatus, :totalAmount,
                    :paymentMethod, :paymentStatus, :recipientName, :recipientPhone,
                    :shippingAddress, :shippingMemo, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", order.getMemberId())
                .addValue("orderNumber", order.getOrderNumber())
                .addValue("orderStatus", order.getOrderStatus())
                .addValue("totalAmount", order.getTotalAmount())
                .addValue("paymentMethod", order.getPaymentMethod())
                .addValue("paymentStatus", order.getPaymentStatus())
                .addValue("recipientName", order.getRecipientName())
                .addValue("recipientPhone", order.getRecipientPhone())
                .addValue("shippingAddress", order.getShippingAddress())
                .addValue("shippingMemo", order.getShippingMemo());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"order_id"});
        
        return keyHolder.getKey().longValue();
    }

    @Override
    public Long saveOrderItem(OrderItem orderItem) {
        String sql = """
            INSERT INTO order_items (order_item_id, order_id, product_id, product_name, 
                                    product_price, quantity, subtotal, cdate, udate)
            VALUES (seq_order_item_id.nextval, :orderId, :productId, :productName,
                    :productPrice, :quantity, :subtotal, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderItem.getOrderId())
                .addValue("productId", orderItem.getProductId())
                .addValue("productName", orderItem.getProductName())
                .addValue("productPrice", orderItem.getProductPrice())
                .addValue("quantity", orderItem.getQuantity())
                .addValue("subtotal", orderItem.getSubtotal());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"order_item_id"});
        
        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<OrderDTO> findDTOByOrderNumber(String orderNumber) {
        // 주문 정보 조회
        Optional<Order> orderOpt = findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Order order = orderOpt.get();
        OrderDTO orderDTO = convertToDTO(order);
        
        // 주문 상품 정보 조회
        List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
        orderDTO.setOrderItems(orderItems);
        
        return Optional.of(orderDTO);
    }

    @Override
    public Optional<OrderDTO> findDTOByOrderId(Long orderId) {
        // 주문 정보 조회
        Optional<Order> orderOpt = findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Order order = orderOpt.get();
        OrderDTO orderDTO = convertToDTO(order);
        
        // 주문 상품 정보 조회
        List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
        orderDTO.setOrderItems(orderItems);
        
        return Optional.of(orderDTO);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status, total_amount,
                   payment_method, payment_status, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE order_number = :orderNumber
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderNumber", orderNumber);
        
        List<Order> result = template.query(sql, param, orderRowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Order> findByOrderId(Long orderId) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status, total_amount,
                   payment_method, payment_status, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        
        List<Order> result = template.query(sql, param, orderRowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        // findByOrderId와 동일한 구현
        return findByOrderId(orderId);
    }

    @Override
    public List<OrderDTO> findDTOByMemberId(Long memberId) {
        List<Order> orders = findByMemberId(memberId);
        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = convertToDTO(order);
                    List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
                    orderDTO.setOrderItems(orderItems);
                    return orderDTO;
                })
                .toList();
    }

    @Override
    public List<Order> findByMemberId(Long memberId) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status, total_amount,
                   payment_method, payment_status, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE member_id = :memberId
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.query(sql, param, orderRowMapper);
    }

    @Override
    public int updateOrderStatus(Long orderId, String orderStatus) {
        String sql = """
            UPDATE orders 
            SET order_status = :orderStatus, udate = CURRENT_TIMESTAMP
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("orderStatus", orderStatus);
        
        return template.update(sql, param);
    }

    @Override
    public int updatePaymentStatus(Long orderId, String paymentStatus) {
        String sql = """
            UPDATE orders 
            SET payment_status = :paymentStatus, udate = CURRENT_TIMESTAMP
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("paymentStatus", paymentStatus);
        
        return template.update(sql, param);
    }

    @Override
    public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
        String sql = """
            SELECT order_item_id, order_id, product_id, product_name, product_price,
                   quantity, subtotal, cdate, udate
            FROM order_items
            WHERE order_id = :orderId
            ORDER BY order_item_id
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        
        return template.query(sql, param, orderItemRowMapper);
    }

    @Override
    public List<OrderItemDTO> findOrderItemDTOsByOrderId(Long orderId) {
        String sql = """
            SELECT order_item_id, order_id, product_id, product_name, product_price,
                   quantity, subtotal, cdate, udate
            FROM order_items
            WHERE order_id = :orderId
            ORDER BY order_item_id
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        
        return template.query(sql, param, orderItemDTORowMapper);
    }

    @Override
    public String generateOrderNumber() {
        // 오늘 날짜로 주문번호 생성 (YYYYMMDD-XXXXX)
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 오늘 날짜의 주문 개수 조회
        String countSql = """
            SELECT COUNT(*) 
            FROM orders 
            WHERE order_number LIKE :today || '-%'
            """;
        
        MapSqlParameterSource countParam = new MapSqlParameterSource()
                .addValue("today", today);
        
        Integer count = template.queryForObject(countSql, countParam, Integer.class);
        int sequence = (count != null ? count : 0) + 1;
        
        return String.format("%s-%05d", today, sequence);
    }

    @Override
    public List<OrderDTO> findAllOrderDTOs() {
        List<Order> orders = findAllOrders();
        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = convertToDTO(order);
                    List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
                    orderDTO.setOrderItems(orderItems);
                    return orderDTO;
                })
                .toList();
    }

    @Override
    public List<Order> findAllOrders() {
        String sql = """
            SELECT order_id, member_id, order_number, order_status, total_amount,
                   payment_method, payment_status, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            ORDER BY cdate DESC
            """;
        
        return template.query(sql, orderRowMapper);
    }

    @Override
    public List<OrderDTO> findDTOByOrderStatus(String orderStatus) {
        List<Order> orders = findByOrderStatus(orderStatus);
        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = convertToDTO(order);
                    List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
                    orderDTO.setOrderItems(orderItems);
                    return orderDTO;
                })
                .toList();
    }

    @Override
    public List<Order> findByOrderStatus(String orderStatus) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status, total_amount,
                   payment_method, payment_status, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE order_status = :orderStatus
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderStatus", orderStatus);
        
        return template.query(sql, param, orderRowMapper);
    }
    
    /**
     * Order 엔티티를 OrderDTO로 변환
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setMemberId(order.getMemberId());
        orderDTO.setOrderNumber(order.getOrderNumber());
        orderDTO.setOrderStatus(order.getOrderStatus());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setPaymentMethod(order.getPaymentMethod());
        orderDTO.setPaymentStatus(order.getPaymentStatus());
        orderDTO.setRecipientName(order.getRecipientName());
        orderDTO.setRecipientPhone(order.getRecipientPhone());
        orderDTO.setShippingAddress(order.getShippingAddress());
        orderDTO.setShippingMemo(order.getShippingMemo());
        orderDTO.setCdate(order.getCdate());
        orderDTO.setUdate(order.getUdate());
        return orderDTO;
    }
} 