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
        order.setOrderStatusId(rs.getLong("order_status_id"));
        order.setPaymentMethodId(rs.getLong("payment_method_id"));
        order.setPaymentStatusId(rs.getLong("payment_status_id"));
        order.setTotalAmount(rs.getInt("total_amount"));
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
            INSERT INTO orders (order_id, member_id, order_number, order_status_id, total_amount, 
                               payment_method_id, payment_status_id, recipient_name, recipient_phone, 
                               shipping_address, shipping_memo, cdate, udate)
            VALUES (seq_order_id.nextval, :memberId, :orderNumber, :orderStatusId, :totalAmount,
                    :paymentMethodId, :paymentStatusId, :recipientName, :recipientPhone,
                    :shippingAddress, :shippingMemo, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", order.getMemberId())
                .addValue("orderNumber", order.getOrderNumber())
                .addValue("orderStatusId", order.getOrderStatusId())
                .addValue("totalAmount", order.getTotalAmount())
                .addValue("paymentMethodId", order.getPaymentMethodId())
                .addValue("paymentStatusId", order.getPaymentStatusId())
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
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE order_number = :orderNumber
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderNumber", orderNumber);
        
        List<Order> result = template.query(sql, param, orderRowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public Optional<Order> findByOrderId(Long orderId) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
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
    public List<Order> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT * FROM orders
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, orderRowMapper);
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM orders";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public List<OrderDTO> findDTOByMemberId(Long memberId) {
        log.info("findDTOByMemberId 시작 - memberId: {}", memberId);
        
        List<Order> orders = findByMemberId(memberId);
        log.info("findByMemberId 결과 - 조회된 Order 개수: {}", orders.size());
        
        if (!orders.isEmpty()) {
            log.info("첫 번째 Order 타입: {}", orders.get(0).getClass().getName());
            log.info("첫 번째 Order 내용: {}", orders.get(0));
        }
        
        List<OrderDTO> result = orders.stream()
                .map(order -> {
                    log.info("Order를 OrderDTO로 변환 중 - orderId: {}", order.getOrderId());
                    OrderDTO orderDTO = convertToDTO(order);
                    log.info("convertToDTO 완료 - orderDTO 타입: {}", orderDTO.getClass().getName());
                    
                    List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
                    log.info("주문 상품 조회 완료 - orderId: {}, 상품 개수: {}", order.getOrderId(), orderItems.size());
                    
                    orderDTO.setOrderItems(orderItems);
                    log.info("OrderDTO 설정 완료 - orderId: {}, orderNumber: {}", orderDTO.getOrderId(), orderDTO.getOrderNumber());
                    
                    return orderDTO;
                })
                .toList();
        
        log.info("findDTOByMemberId 완료 - 최종 결과 개수: {}", result.size());
        if (!result.isEmpty()) {
            log.info("최종 결과 첫 번째 요소 타입: {}", result.get(0).getClass().getName());
        }
        
        return result;
    }
    
    @Override
    public List<OrderDTO> findDTOByMemberIdAndStatus(Long memberId, Long orderStatusId) {
        log.info("findDTOByMemberIdAndStatus 시작 - memberId: {}, orderStatusId: {}", memberId, orderStatusId);
        
        List<Order> orders = findByMemberIdAndStatus(memberId, orderStatusId);
        log.info("findByMemberIdAndStatus 결과 - 조회된 Order 개수: {}", orders.size());
        
        List<OrderDTO> result = orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = convertToDTO(order);
                    List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
                    orderDTO.setOrderItems(orderItems);
                    return orderDTO;
                })
                .toList();
        
        log.info("findDTOByMemberIdAndStatus 완료 - 최종 결과 개수: {}", result.size());
        return result;
    }
    
    @Override
    public List<Order> findByMemberId(Long memberId) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE member_id = :memberId
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.query(sql, param, orderRowMapper);
    }
    
    private List<Order> findByMemberIdAndStatus(Long memberId, Long orderStatusId) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE member_id = :memberId AND order_status_id = :orderStatusId
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("orderStatusId", orderStatusId);
        
        return template.query(sql, param, orderRowMapper);
    }

    @Override
    public int updateOrderStatus(Long orderId, Long orderStatusId) {
        String sql = """
            UPDATE orders 
            SET order_status_id = :orderStatusId, udate = CURRENT_TIMESTAMP
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("orderStatusId", orderStatusId);
        
        return template.update(sql, param);
    }

    @Override
    public int updatePaymentStatus(Long orderId, Long paymentStatusId) {
        String sql = """
            UPDATE orders 
            SET payment_status_id = :paymentStatusId, udate = CURRENT_TIMESTAMP
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("paymentStatusId", paymentStatusId);
        
        return template.update(sql, param);
    }
    
    @Override
    public int updateTotalAmount(Long orderId, Integer totalAmount) {
        String sql = """
            UPDATE orders 
            SET total_amount = :totalAmount, udate = CURRENT_TIMESTAMP
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("totalAmount", totalAmount);
        
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
        log.info("주문 상품 DTO 조회 시작 - orderId: {}", orderId);
        
        String sql = """
            SELECT order_item_id, order_id, product_id, product_name, product_price,
                   quantity, subtotal, cdate, udate
            FROM order_items
            WHERE order_id = :orderId
            ORDER BY order_item_id
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        
        List<OrderItemDTO> result = template.query(sql, param, orderItemDTORowMapper);
        log.info("주문 상품 DTO 조회 완료 - orderId: {}, 조회된 상품 개수: {}", orderId, result.size());
        
        return result;
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
        List<Order> orders = findAll();
        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = convertToDTO(order);
                    List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
                    orderDTO.setOrderItems(orderItems);
                    return orderDTO;
                })
                .toList();
    }

    // findAllOrders는 findAll()과 동일하므로 제거

    @Override
    public List<OrderDTO> findDTOByOrderStatus(Long orderStatusId) {
        List<Order> orders = findByOrderStatus(orderStatusId);
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
    public List<Order> findByOrderStatus(Long orderStatusId) {
        String sql = """
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE order_status_id = :orderStatusId
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderStatusId", orderStatusId);
        
        return template.query(sql, param, orderRowMapper);
    }

    @Override
    public List<Order> findAll() {
        String sql = """
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            ORDER BY cdate DESC
            """;
        
        return template.query(sql, orderRowMapper);
    }

    @Override
    public int updateById(Long orderId, Order order) {
        String sql = """
            UPDATE orders 
            SET member_id = :memberId, order_number = :orderNumber, order_status_id = :orderStatusId,
                total_amount = :totalAmount, payment_method_id = :paymentMethodId, 
                payment_status_id = :paymentStatusId, recipient_name = :recipientName,
                recipient_phone = :recipientPhone, shipping_address = :shippingAddress,
                shipping_memo = :shippingMemo, udate = CURRENT_TIMESTAMP
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("memberId", order.getMemberId())
                .addValue("orderNumber", order.getOrderNumber())
                .addValue("orderStatusId", order.getOrderStatusId())
                .addValue("totalAmount", order.getTotalAmount())
                .addValue("paymentMethodId", order.getPaymentMethodId())
                .addValue("paymentStatusId", order.getPaymentStatusId())
                .addValue("recipientName", order.getRecipientName())
                .addValue("recipientPhone", order.getRecipientPhone())
                .addValue("shippingAddress", order.getShippingAddress())
                .addValue("shippingMemo", order.getShippingMemo());
        
        return template.update(sql, param);
    }

    @Override
    public int deleteById(Long orderId) {
        String sql = "DELETE FROM orders WHERE order_id = :orderId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        
        return template.update(sql, param);
    }
    
    @Override
    public int countByOrderStatus(Long orderStatusId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_status_id = :orderStatusId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderStatusId", orderStatusId);
        
        return template.queryForObject(sql, param, Integer.class);
    }
    
    @Override
    public List<OrderDTO> findAllOrderDTOsWithPaging(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
        List<Order> orders = template.query(sql, param, orderRowMapper);
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
    public List<OrderDTO> findDTOByOrderStatusWithPaging(Long orderStatusId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT order_id, member_id, order_number, order_status_id, total_amount,
                   payment_method_id, payment_status_id, recipient_name, recipient_phone,
                   shipping_address, shipping_memo, cdate, udate
            FROM orders
            WHERE order_status_id = :orderStatusId
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderStatusId", orderStatusId)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
        List<Order> orders = template.query(sql, param, orderRowMapper);
        return orders.stream()
                .map(order -> {
                    OrderDTO orderDTO = convertToDTO(order);
                    List<OrderItemDTO> orderItems = findOrderItemDTOsByOrderId(order.getOrderId());
                    orderDTO.setOrderItems(orderItems);
                    return orderDTO;
                })
                .toList();
    }
    
    /**
     * Order 엔티티를 OrderDTO로 변환
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setMemberId(order.getMemberId());
        orderDTO.setOrderNumber(order.getOrderNumber());
        orderDTO.setOrderStatusId(order.getOrderStatusId());
        orderDTO.setPaymentMethodId(order.getPaymentMethodId());
        orderDTO.setPaymentStatusId(order.getPaymentStatusId());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setCdate(order.getCdate());
        orderDTO.setUdate(order.getUdate());
        return orderDTO;
    }
} 