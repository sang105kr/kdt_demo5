package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.review.entity.ReviewComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewCommentDAOImpl implements ReviewCommentDAO {
    
    private final NamedParameterJdbcTemplate template;
    private final CodeSVC codeSVC;
    
    // RowMapper 정의 - member nickname 포함
    private final RowMapper<ReviewComment> reviewCommentRowMapper = (rs, rowNum) -> {
        ReviewComment comment = new ReviewComment();
        comment.setCommentId(rs.getLong("comment_id"));
        comment.setReviewId(rs.getLong("review_id"));
        comment.setMemberId(rs.getLong("member_id"));
        comment.setParentId(rs.getObject("parent_id", Long.class));
        comment.setContent(rs.getString("content"));
        comment.setStatusId(rs.getLong("status_id"));
        comment.setCdate(rs.getObject("cdate", LocalDateTime.class));
        comment.setUdate(rs.getObject("udate", LocalDateTime.class));
        comment.setReportCount(rs.getInt("report_count"));
        comment.setHelpfulCount(rs.getInt("helpful_count"));
        comment.setUnhelpfulCount(rs.getInt("unhelpful_count"));
        
        // member nickname 설정 (JOIN으로 가져온 데이터)
        try {
            comment.setMemberNickname(rs.getString("nickname"));
        } catch (SQLException e) {
            // nickname 컬럼이 없는 경우 (JOIN이 없는 경우) null로 설정
            comment.setMemberNickname(null);
        }
        
        return comment;
    };
    
    @Override
    public Long save(ReviewComment comment) {
        String sql = """
            INSERT INTO review_comments (comment_id, review_id, member_id, content, status_id, cdate, udate)
            VALUES (seq_review_comment_id.nextval, :reviewId, :memberId, :content, :statusId, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        // statusId가 null이면 ACTIVE 상태로 설정 (캐시에서 가져오기)
        Long statusId = comment.getStatusId();
        if (statusId == null) {
            statusId = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
            if (statusId == null) {
                log.error("REVIEW_COMMENT_STATUS ACTIVE 코드를 찾을 수 없습니다.");
                throw new IllegalStateException("REVIEW_COMMENT_STATUS ACTIVE 코드가 존재하지 않습니다.");
            }
        }
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reviewId", comment.getReviewId())
            .addValue("memberId", comment.getMemberId())
            .addValue("content", comment.getContent())
            .addValue("statusId", statusId);
        
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
                status_id = :statusId, udate = SYSTIMESTAMP
            WHERE comment_id = :commentId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("commentId", commentId)
            .addValue("reviewId", comment.getReviewId())
            .addValue("memberId", comment.getMemberId())
            .addValue("content", comment.getContent())
            .addValue("statusId", comment.getStatusId());
        
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
        String sql = """
            SELECT comment_id, review_id, member_id, parent_id, content, 
                   status_id, cdate, udate, report_count, helpful_count
            FROM review_comments 
            WHERE comment_id = :commentId AND status_id = :activeStatus
            """;
        
        Long activeStatus = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("commentId", commentId)
            .addValue("activeStatus", activeStatus);
        
        try {
            ReviewComment comment = template.queryForObject(sql, params, reviewCommentRowMapper);
            return Optional.ofNullable(comment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<ReviewComment> findAll() {
        String sql = """
            SELECT comment_id, review_id, member_id, parent_id, content, 
                   status_id, cdate, udate, report_count, helpful_count
            FROM review_comments 
            WHERE status_id = :activeStatus 
            ORDER BY cdate ASC
            """;
        Long activeStatus = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource("activeStatus", activeStatus);
        return template.query(sql, params, reviewCommentRowMapper);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM review_comments WHERE status_id = :activeStatus";
        Long activeStatus = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource("activeStatus", activeStatus);
        return template.queryForObject(sql, params, Integer.class);
    }
    
    @Override
    public List<ReviewComment> findByReviewId(Long reviewId) {
        String sql = """
            SELECT rc.*, m.nickname 
            FROM review_comments rc
            LEFT JOIN member m ON rc.member_id = m.member_id
            WHERE rc.review_id = :reviewId AND rc.status_id = :activeStatus 
            ORDER BY rc.cdate ASC
            """;
        
        Long activeStatus = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reviewId", reviewId)
            .addValue("activeStatus", activeStatus);
        return template.query(sql, params, reviewCommentRowMapper);
    }
    
    @Override
    public int countByReviewId(Long reviewId) {
        String sql = "SELECT COUNT(*) FROM review_comments WHERE review_id = :reviewId AND status_id = :activeStatus";
        Long activeStatus = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reviewId", reviewId)
            .addValue("activeStatus", activeStatus);
        return template.queryForObject(sql, params, Integer.class);
    }
    
    @Override
    public List<ReviewComment> findByMemberId(Long memberId) {
        String sql = """
            SELECT comment_id, review_id, member_id, parent_id, content, 
                   status_id, cdate, udate, report_count, helpful_count
            FROM review_comments 
            WHERE member_id = :memberId AND status_id = :activeStatus 
            ORDER BY cdate DESC
            """;
        
        Long activeStatus = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("activeStatus", activeStatus);
        return template.query(sql, params, reviewCommentRowMapper);
    }
    
    @Override
    public int updateStatus(Long commentId, Long statusCodeId) {
        String sql = "UPDATE review_comments SET status_id = :status, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("commentId", commentId)
            .addValue("status", statusCodeId);
        return template.update(sql, params);
    }
    
    @Override
    public int incrementHelpfulCount(Long commentId) {
        String sql = "UPDATE review_comments SET helpful_count = NVL(helpful_count, 0) + 1, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.update(sql, params);
    }
    
    @Override
    public int incrementUnhelpfulCount(Long commentId) {
        String sql = "UPDATE review_comments SET unhelpful_count = NVL(unhelpful_count, 0) + 1, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.update(sql, params);
    }
    
    @Override
    public int incrementReportCount(Long commentId) {
        String sql = "UPDATE review_comments SET report_count = NVL(report_count, 0) + 1, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.update(sql, params);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReviewComment> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT comment_id, review_id, member_id, parent_id, content, 
                   status_id, cdate, udate, report_count, helpful_count
            FROM review_comments 
            WHERE status_id = :activeStatus
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        
        Long activeStatus = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("activeStatus", activeStatus)
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, reviewCommentRowMapper);
    }
} 