package com.kh.demo.domain.review.dao;

import com.kh.demo.domain.review.entity.ReviewReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewReportDAOImpl implements ReviewReportDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    // RowMapper 정의
    private final RowMapper<ReviewReport> reportRowMapper = (rs, rowNum) -> {
        ReviewReport report = new ReviewReport();
        report.setReportId(rs.getLong("report_id"));
        report.setReviewId(rs.getLong("review_id"));
        report.setCommentId(rs.getLong("comment_id"));
        report.setReporterId(rs.getLong("reporter_id"));
        report.setReportType(rs.getString("report_type"));
        report.setReportReason(rs.getString("report_reason"));
        report.setStatus(rs.getString("status"));
        report.setAdminMemo(rs.getString("admin_memo"));
        report.setCdate(rs.getObject("cdate", LocalDateTime.class));
        report.setUdate(rs.getObject("udate", LocalDateTime.class));
        
        // 조인 필드들
        report.setReporterNickname(rs.getString("reporter_nickname"));
        report.setReviewTitle(rs.getString("review_title"));
        report.setCommentContent(rs.getString("comment_content"));
        
        return report;
    };
    
    @Override
    public Long save(ReviewReport report) {
        String sql = """
            INSERT INTO review_reports (review_id, comment_id, reporter_id, report_type, report_reason, status, admin_memo, cdate, udate)
            VALUES (seq_review_report_id.nextval, :reviewId, :commentId, :reporterId, :reportType, :reportReason, :status, :adminMemo, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reviewId", report.getReviewId())
            .addValue("commentId", report.getCommentId())
            .addValue("reporterId", report.getReporterId())
            .addValue("reportType", report.getReportType())
            .addValue("reportReason", report.getReportReason())
            .addValue("status", report.getStatus() != null ? report.getStatus() : "PENDING")
            .addValue("adminMemo", report.getAdminMemo());
        
        template.update(sql, params);
        
        // 생성된 report_id 조회
        String selectSql = "SELECT seq_review_report_id.currval FROM dual";
        Long reportId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);
        
        if (reportId == null) {
            throw new IllegalStateException("Failed to retrieve generated report_id");
        }
        return reportId;
    }
    
    @Override
    public int updateById(Long reportId, ReviewReport report) {
        String sql = """
            UPDATE review_reports 
            SET review_id = :reviewId, comment_id = :commentId, reporter_id = :reporterId,
                report_type = :reportType, report_reason = :reportReason, status = :status,
                admin_memo = :adminMemo, udate = SYSTIMESTAMP
            WHERE report_id = :reportId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reportId", reportId)
            .addValue("reviewId", report.getReviewId())
            .addValue("commentId", report.getCommentId())
            .addValue("reporterId", report.getReporterId())
            .addValue("reportType", report.getReportType())
            .addValue("reportReason", report.getReportReason())
            .addValue("status", report.getStatus())
            .addValue("adminMemo", report.getAdminMemo());
        
        return template.update(sql, params);
    }
    
    public int update(ReviewReport report) {
        return updateById(report.getReportId(), report);
    }
    
    @Override
    public int deleteById(Long reportId) {
        String sql = "DELETE FROM review_reports WHERE report_id = :reportId";
        MapSqlParameterSource params = new MapSqlParameterSource("reportId", reportId);
        return template.update(sql, params);
    }
    
    public int delete(Long reportId) {
        return deleteById(reportId);
    }
    
    @Override
    public Optional<ReviewReport> findById(Long reportId) {
        String sql = """
            SELECT r.*, m.nickname as reporter_nickname, 
                   rev.title as review_title, c.content as comment_content
            FROM review_reports r
            LEFT JOIN member m ON r.reporter_id = m.member_id
            LEFT JOIN reviews rev ON r.review_id = rev.review_id
            LEFT JOIN review_comments c ON r.comment_id = c.comment_id
            WHERE r.report_id = :reportId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("reportId", reportId);
        
        try {
            ReviewReport report = template.queryForObject(sql, params, reportRowMapper);
            return Optional.ofNullable(report);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<ReviewReport> findAll() {
        String sql = """
            SELECT r.*, m.nickname as reporter_nickname, 
                   rev.title as review_title, c.content as comment_content
            FROM review_reports r
            LEFT JOIN member m ON r.reporter_id = m.member_id
            LEFT JOIN reviews rev ON r.review_id = rev.review_id
            LEFT JOIN review_comments c ON r.comment_id = c.comment_id
            ORDER BY r.cdate DESC
            """;
        
        return template.query(sql, reportRowMapper);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM review_reports";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    @Override
    public List<ReviewReport> findByReviewId(Long reviewId) {
        String sql = """
            SELECT r.*, m.nickname as reporter_nickname, 
                   rev.title as review_title, c.content as comment_content
            FROM review_reports r
            LEFT JOIN member m ON r.reporter_id = m.member_id
            LEFT JOIN reviews rev ON r.review_id = rev.review_id
            LEFT JOIN review_comments c ON r.comment_id = c.comment_id
            WHERE r.review_id = :reviewId
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        return template.query(sql, params, reportRowMapper);
    }
    
    @Override
    public List<ReviewReport> findByCommentId(Long commentId) {
        String sql = """
            SELECT r.*, m.nickname as reporter_nickname, 
                   rev.title as review_title, c.content as comment_content
            FROM review_reports r
            LEFT JOIN member m ON r.reporter_id = m.member_id
            LEFT JOIN reviews rev ON r.review_id = rev.review_id
            LEFT JOIN review_comments c ON r.comment_id = c.comment_id
            WHERE r.comment_id = :commentId
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.query(sql, params, reportRowMapper);
    }
    
    @Override
    public List<ReviewReport> findByReporterId(Long reporterId) {
        String sql = """
            SELECT r.*, m.nickname as reporter_nickname, 
                   rev.title as review_title, c.content as comment_content
            FROM review_reports r
            LEFT JOIN member m ON r.reporter_id = m.member_id
            LEFT JOIN reviews rev ON r.review_id = rev.review_id
            LEFT JOIN review_comments c ON r.comment_id = c.comment_id
            WHERE r.reporter_id = :reporterId
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("reporterId", reporterId);
        return template.query(sql, params, reportRowMapper);
    }
    
    @Override
    public List<ReviewReport> findByStatus(String status) {
        String sql = """
            SELECT r.*, m.nickname as reporter_nickname, 
                   rev.title as review_title, c.content as comment_content
            FROM review_reports r
            LEFT JOIN member m ON r.reporter_id = m.member_id
            LEFT JOIN reviews rev ON r.review_id = rev.review_id
            LEFT JOIN review_comments c ON r.comment_id = c.comment_id
            WHERE r.status = :status
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("status", status);
        return template.query(sql, params, reportRowMapper);
    }
    
    @Override
    public int updateStatus(Long reportId, String status, String adminMemo) {
        String sql = "UPDATE review_reports SET status = :status, admin_memo = :adminMemo, udate = SYSTIMESTAMP WHERE report_id = :reportId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reportId", reportId)
            .addValue("status", status)
            .addValue("adminMemo", adminMemo);
        return template.update(sql, params);
    }
} 