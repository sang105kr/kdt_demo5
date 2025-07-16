package com.kh.demo.domain.payment.dao;

import com.kh.demo.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentDAOImpl implements PaymentDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    @Override
    public Long save(Payment payment) {
        String sql = """
            INSERT INTO payments (order_id, payment_number, payment_method, amount, status,
                                card_number, card_company, approval_number, approved_at,
                                failure_reason, refund_reason, refunded_at, cdate, udate)
            VALUES (:orderId, :paymentNumber, :paymentMethod, :amount, :status,
                   :cardNumber, :cardCompany, :approvalNumber, :approvedAt,
                   :failureReason, :refundReason, :refundedAt, SYSDATE, SYSDATE)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", payment.getOrderId())
                .addValue("paymentNumber", payment.getPaymentNumber())
                .addValue("paymentMethod", payment.getPaymentMethod())
                .addValue("amount", payment.getAmount())
                .addValue("status", payment.getStatus())
                .addValue("cardNumber", payment.getCardNumber())
                .addValue("cardCompany", payment.getCardCompany())
                .addValue("approvalNumber", payment.getApprovalNumber())
                .addValue("approvedAt", payment.getApprovedAt())
                .addValue("failureReason", payment.getFailureReason())
                .addValue("refundReason", payment.getRefundReason())
                .addValue("refundedAt", payment.getRefundedAt());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder, new String[]{"payment_id"});
        
        return keyHolder.getKey().longValue();
    }
    
    @Override
    public Optional<Payment> findById(Long paymentId) {
        String sql = """
            SELECT payment_id, order_id, payment_number, payment_method, amount, status,
                   card_number, card_company, approval_number, approved_at,
                   failure_reason, refund_reason, refunded_at, cdate, udate
            FROM payments
            WHERE payment_id = :paymentId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentId", paymentId);
        
        List<Payment> results = template.query(sql, params, getPaymentRowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        String sql = """
            SELECT payment_id, order_id, payment_number, payment_method, amount, status,
                   card_number, card_company, approval_number, approved_at,
                   failure_reason, refund_reason, refunded_at, cdate, udate
            FROM payments
            WHERE order_id = :orderId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        
        List<Payment> results = template.query(sql, params, getPaymentRowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Payment> findByPaymentNumber(String paymentNumber) {
        String sql = """
            SELECT payment_id, order_id, payment_number, payment_method, amount, status,
                   card_number, card_company, approval_number, approved_at,
                   failure_reason, refund_reason, refunded_at, cdate, udate
            FROM payments
            WHERE payment_number = :paymentNumber
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentNumber", paymentNumber);
        
        List<Payment> results = template.query(sql, params, getPaymentRowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Payment> findByApprovalNumber(String approvalNumber) {
        String sql = """
            SELECT payment_id, order_id, payment_number, payment_method, amount, status,
                   card_number, card_company, approval_number, approved_at,
                   failure_reason, refund_reason, refunded_at, cdate, udate
            FROM payments
            WHERE approval_number = :approvalNumber
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("approvalNumber", approvalNumber);
        
        List<Payment> results = template.query(sql, params, getPaymentRowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<Payment> findAll() {
        String sql = """
            SELECT payment_id, order_id, payment_number, payment_method, amount, status,
                   card_number, card_company, approval_number, approved_at,
                   failure_reason, refund_reason, refunded_at, cdate, udate
            FROM payments
            ORDER BY cdate DESC
            """;
        
        return template.query(sql, getPaymentRowMapper());
    }
    
    @Override
    public List<Payment> findByStatus(String status) {
        String sql = """
            SELECT payment_id, order_id, payment_number, payment_method, amount, status,
                   card_number, card_company, approval_number, approved_at,
                   failure_reason, refund_reason, refunded_at, cdate, udate
            FROM payments
            WHERE status = :status
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", status);
        
        return template.query(sql, params, getPaymentRowMapper());
    }
    
    @Override
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        String sql = """
            SELECT payment_id, order_id, payment_number, payment_method, amount, status,
                   card_number, card_company, approval_number, approved_at,
                   failure_reason, refund_reason, refunded_at, cdate, udate
            FROM payments
            WHERE payment_method = :paymentMethod
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentMethod", paymentMethod);
        
        return template.query(sql, params, getPaymentRowMapper());
    }
    
    @Override
    public int update(Payment payment) {
        String sql = """
            UPDATE payments 
            SET order_id = :orderId, payment_number = :paymentNumber, payment_method = :paymentMethod,
                amount = :amount, status = :status, card_number = :cardNumber, card_company = :cardCompany,
                approval_number = :approvalNumber, approved_at = :approvedAt, failure_reason = :failureReason,
                refund_reason = :refundReason, refunded_at = :refundedAt, udate = SYSDATE
            WHERE payment_id = :paymentId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentId", payment.getPaymentId())
                .addValue("orderId", payment.getOrderId())
                .addValue("paymentNumber", payment.getPaymentNumber())
                .addValue("paymentMethod", payment.getPaymentMethod())
                .addValue("amount", payment.getAmount())
                .addValue("status", payment.getStatus())
                .addValue("cardNumber", payment.getCardNumber())
                .addValue("cardCompany", payment.getCardCompany())
                .addValue("approvalNumber", payment.getApprovalNumber())
                .addValue("approvedAt", payment.getApprovedAt())
                .addValue("failureReason", payment.getFailureReason())
                .addValue("refundReason", payment.getRefundReason())
                .addValue("refundedAt", payment.getRefundedAt());
        
        return template.update(sql, params);
    }
    
    @Override
    public int updateStatus(Long paymentId, String status) {
        String sql = """
            UPDATE payments 
            SET status = :status, udate = SYSDATE
            WHERE payment_id = :paymentId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("status", status);
        
        return template.update(sql, params);
    }
    
    @Override
    public int cancelPayment(Long paymentId, String reason) {
        String sql = """
            UPDATE payments 
            SET status = 'CANCELLED', failure_reason = :reason, udate = SYSDATE
            WHERE payment_id = :paymentId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("reason", reason);
        
        return template.update(sql, params);
    }
    
    @Override
    public int refundPayment(Long paymentId, String reason) {
        String sql = """
            UPDATE payments 
            SET status = 'REFUNDED', refund_reason = :reason, refunded_at = SYSDATE, udate = SYSDATE
            WHERE payment_id = :paymentId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("reason", reason);
        
        return template.update(sql, params);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM payments";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    @Override
    public int getCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM payments WHERE status = :status";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", status);
        return template.queryForObject(sql, params, Integer.class);
    }
    
    private org.springframework.jdbc.core.RowMapper<Payment> getPaymentRowMapper() {
        return (rs, rowNum) -> {
            Payment payment = new Payment();
            payment.setPaymentId(rs.getLong("payment_id"));
            payment.setOrderId(rs.getLong("order_id"));
            payment.setPaymentNumber(rs.getString("payment_number"));
            payment.setPaymentMethod(rs.getString("payment_method"));
            payment.setAmount(rs.getBigDecimal("amount"));
            payment.setStatus(rs.getString("status"));
            payment.setCardNumber(rs.getString("card_number"));
            payment.setCardCompany(rs.getString("card_company"));
            payment.setApprovalNumber(rs.getString("approval_number"));
            
            if (rs.getTimestamp("approved_at") != null) {
                payment.setApprovedAt(rs.getTimestamp("approved_at").toLocalDateTime());
            }
            
            payment.setFailureReason(rs.getString("failure_reason"));
            payment.setRefundReason(rs.getString("refund_reason"));
            
            if (rs.getTimestamp("refunded_at") != null) {
                payment.setRefundedAt(rs.getTimestamp("refunded_at").toLocalDateTime());
            }
            
            payment.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
            payment.setUdate(rs.getTimestamp("udate").toLocalDateTime());
            
            return payment;
        };
    }
} 