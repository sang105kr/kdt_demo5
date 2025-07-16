package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.review.entity.ReviewComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
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
public class ReviewCommentDAOImpl implements ReviewCommentDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    // RowMapper 정의
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
    public Long save(ReviewComment comment) {
        String sql = """
            INSERT INTO review_comments (review_id, member_id, content, status, cdate, udate)
            VALUES (seq_review_comment_id.nextval, :reviewId, :memberId, :content, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reviewId", comment.getReviewId())
            .addValue("memberId", comment.getMemberId())
            .addValue("content", comment.getContent())
            .addValue("status", comment.getStatus() != null ? comment.getStatus() : "ACTIVE");
        
        template.update(sql, params);
        
        // 생성된 comment_id 조회
        String selectSql = "SELECT seq_review_comment_id.currval FROM dual";
        Long commentId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);
        
        if (commentId == null) {
            throw new IllegalStateException("Failed to retrieve generated comment_id");
        }
        return commentId;
    }
    
    @Override
    public int updateById(Long commentId, ReviewComment comment) {
        String sql = """
            UPDATE review_comments 
            SET review_id = :reviewId, member_id = :memberId, content = :content, 
                status = :status, udate = SYSTIMESTAMP
            WHERE comment_id = :commentId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("commentId", commentId)
            .addValue("reviewId", comment.getReviewId())
            .addValue("memberId", comment.getMemberId())
            .addValue("content", comment.getContent())
            .addValue("status", comment.getStatus());
        
        return template.update(sql, params);
    }
    
    @Override
    public int deleteById(Long commentId) {
        String sql = "DELETE FROM review_comments WHERE comment_id = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.update(sql, params);
    }
    
    @Override
    public Optional<ReviewComment> findById(Long commentId) {
        String sql = "SELECT * FROM review_comments WHERE comment_id = :commentId AND status = 'ACTIVE'";
        
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        
        try {
            ReviewComment comment = template.queryForObject(sql, params, reviewCommentRowMapper);
            return Optional.ofNullable(comment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<ReviewComment> findAll() {
        String sql = "SELECT * FROM review_comments WHERE status = 'ACTIVE' ORDER BY cdate ASC";
        
        return template.query(sql, reviewCommentRowMapper);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM review_comments WHERE status = 'ACTIVE'";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    @Override
    public List<ReviewComment> findByReviewId(Long reviewId) {
        String sql = "SELECT * FROM review_comments WHERE review_id = :reviewId AND status = 'ACTIVE' ORDER BY cdate ASC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        return template.query(sql, params, reviewCommentRowMapper);
    }
    
    @Override
    public int countByReviewId(Long reviewId) {
        String sql = "SELECT COUNT(*) FROM review_comments WHERE review_id = :reviewId AND status = 'ACTIVE'";
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        return template.queryForObject(sql, params, Integer.class);
    }
    
    @Override
    public List<ReviewComment> findByMemberId(Long memberId) {
        String sql = "SELECT * FROM review_comments WHERE member_id = :memberId AND status = 'ACTIVE' ORDER BY cdate DESC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, params, reviewCommentRowMapper);
    }
    
    @Override
    public int updateStatus(Long commentId, String status) {
        String sql = "UPDATE review_comments SET status = :status, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("commentId", commentId)
            .addValue("status", status);
        
        return template.update(sql, params);
    }
} 