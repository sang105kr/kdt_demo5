package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDAOImpl implements ReviewDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    // RowMapper 정의
    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setProductId(rs.getLong("product_id"));
        review.setMemberId(rs.getLong("member_id"));
        review.setOrderId(rs.getLong("order_id"));
        review.setRating(rs.getBigDecimal("rating"));
        review.setTitle(rs.getString("title"));
        review.setContent(rs.getString("content"));
        review.setHelpfulCount(rs.getInt("helpful_count"));
        review.setReportCount(rs.getInt("report_count"));
        review.setStatus(rs.getString("status"));
        review.setCdate(rs.getObject("cdate", LocalDateTime.class));
        review.setUdate(rs.getObject("udate", LocalDateTime.class));
        return review;
    };

    // ReviewComment RowMapper 정의
    private final RowMapper<ReviewComment> reviewCommentRowMapper = (rs, rowNum) -> {
        ReviewComment comment = new ReviewComment();
        comment.setCommentId(rs.getLong("comment_id"));
        comment.setReviewId(rs.getLong("review_id"));
        comment.setMemberId(rs.getLong("member_id"));
        comment.setContent(rs.getString("content"));
        comment.setStatus(rs.getString("status"));
        comment.setCdate(rs.getObject("cdate", LocalDateTime.class));
        comment.setUdate(rs.getObject("udate", LocalDateTime.class));
        return comment;
    };
    
    @Override
    public Long save(Review review) {
        String sql = """
            INSERT INTO reviews (product_id, member_id, order_id, rating, title, content, helpful_count, report_count, status, cdate, udate)
            VALUES (seq_review_id.nextval, :productId, :memberId, :orderId, :rating, :title, :content, :helpfulCount, :reportCount, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("productId", review.getProductId())
            .addValue("memberId", review.getMemberId())
            .addValue("orderId", review.getOrderId())
            .addValue("rating", review.getRating())
            .addValue("title", review.getTitle())
            .addValue("content", review.getContent())
            .addValue("helpfulCount", review.getHelpfulCount() != null ? review.getHelpfulCount() : 0)
            .addValue("reportCount", review.getReportCount() != null ? review.getReportCount() : 0)
            .addValue("status", review.getStatus() != null ? review.getStatus() : "ACTIVE");
        
        template.update(sql, params);
        
        // 생성된 review_id 조회
        String selectSql = "SELECT seq_review_id.currval FROM dual";
        Long reviewId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);
        
        if (reviewId == null) {
            throw new IllegalStateException("Failed to retrieve generated review_id");
        }
        return reviewId;
    }
    
    @Override
    public int updateById(Long reviewId, Review review) {
        String sql = """
            UPDATE reviews 
            SET product_id = :productId, member_id = :memberId, order_id = :orderId, 
                rating = :rating, title = :title, content = :content, 
                helpful_count = :helpfulCount, report_count = :reportCount, 
                status = :status, udate = SYSTIMESTAMP
            WHERE review_id = :reviewId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reviewId", reviewId)
            .addValue("productId", review.getProductId())
            .addValue("memberId", review.getMemberId())
            .addValue("orderId", review.getOrderId())
            .addValue("rating", review.getRating())
            .addValue("title", review.getTitle())
            .addValue("content", review.getContent())
            .addValue("helpfulCount", review.getHelpfulCount())
            .addValue("reportCount", review.getReportCount())
            .addValue("status", review.getStatus());
        
        return template.update(sql, params);
    }
    
    public int update(Review review) {
        return updateById(review.getReviewId(), review);
    }
    
    @Override
    public int deleteById(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = :reviewId";
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        return template.update(sql, params);
    }
    
    public int delete(Long reviewId) {
        return deleteById(reviewId);
    }
    
    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = :reviewId AND status = 'ACTIVE'";
        
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        
        try {
            Review review = template.queryForObject(sql, params, reviewRowMapper);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Review> findAll() {
        String sql = "SELECT * FROM reviews WHERE status = 'ACTIVE' ORDER BY cdate DESC";
        
        return template.query(sql, reviewRowMapper);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM reviews WHERE status = 'ACTIVE'";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    @Override
    public List<Review> findByProductId(Long productId, int offset, int limit) {
        String sql = """
            SELECT * FROM reviews 
            WHERE product_id = :productId AND status = 'ACTIVE'
            ORDER BY helpful_count DESC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("productId", productId)
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, reviewRowMapper);
    }
    
    @Override
    public int countByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = :productId AND status = 'ACTIVE'";
        MapSqlParameterSource params = new MapSqlParameterSource("productId", productId);
        return template.queryForObject(sql, params, Integer.class);
    }
    
    @Override
    public List<Review> findByMemberId(Long memberId) {
        String sql = "SELECT * FROM reviews WHERE member_id = :memberId AND status = 'ACTIVE' ORDER BY cdate DESC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, params, reviewRowMapper);
    }

    @Override
    public List<ReviewComment> findCommentsByReviewId(Long reviewId) {
        String sql = "SELECT * FROM review_comments WHERE review_id = :reviewId AND status = 'ACTIVE' ORDER BY cdate ASC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        return template.query(sql, params, reviewCommentRowMapper);
    }
    
    @Override
    public Optional<Review> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM reviews WHERE order_id = :orderId AND status = 'ACTIVE'";
        
        MapSqlParameterSource params = new MapSqlParameterSource("orderId", orderId);
        
        try {
            Review review = template.queryForObject(sql, params, reviewRowMapper);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Review> findByProductIdAndRating(Long productId, Double rating, int offset, int limit) {
        String sql = """
            SELECT * FROM reviews 
            WHERE product_id = :productId AND rating = :rating AND status = 'ACTIVE'
            ORDER BY helpful_count DESC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("productId", productId)
            .addValue("rating", rating)
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, reviewRowMapper);
    }
    
    @Override
    public int incrementHelpfulCount(Long reviewId) {
        String sql = "UPDATE reviews SET helpful_count = helpful_count + 1, udate = SYSTIMESTAMP WHERE review_id = :reviewId";
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        return template.update(sql, params);
    }
    
    @Override
    public int incrementReportCount(Long reviewId) {
        String sql = "UPDATE reviews SET report_count = report_count + 1, udate = SYSTIMESTAMP WHERE review_id = :reviewId";
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        return template.update(sql, params);
    }
    
    @Override
    public int updateStatus(Long reviewId, String status) {
        String sql = "UPDATE reviews SET status = :status, udate = SYSTIMESTAMP WHERE review_id = :reviewId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reviewId", reviewId)
            .addValue("status", status);
        return template.update(sql, params);
    }
    
    @Override
    public int updateProductRating(Long productId) {
        String sql = """
            UPDATE products 
            SET rating = (
                SELECT ROUND(AVG(rating), 1) 
                FROM reviews 
                WHERE product_id = :productId AND status = 'ACTIVE'
            ),
            udate = SYSTIMESTAMP
            WHERE product_id = :productId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("productId", productId);
        return template.update(sql, params);
    }
} 