package com.kh.demo.domain.report.dao;

import com.kh.demo.domain.report.entity.Report;
import com.kh.demo.domain.report.dto.ReportDetailDTO;
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
public class ReportDAOImpl implements ReportDAO {

    private final NamedParameterJdbcTemplate template;

    private final RowMapper<Report> reportRowMapper = (rs, rowNum) -> {
        Report report = new Report();
        report.setReportId(rs.getLong("report_id"));
        report.setReporterId(rs.getLong("reporter_id"));
        report.setTargetType(rs.getString("target_type"));
        report.setTargetId(rs.getLong("target_id"));
        report.setCategoryId(rs.getLong("category_id"));
        report.setReason(rs.getString("reason"));
        report.setEvidence(rs.getString("evidence"));
        report.setStatus(rs.getString("status"));
        report.setAdminNotes(rs.getString("admin_notes"));
        report.setResolvedBy(rs.getObject("resolved_by", Long.class));
        report.setResolvedAt(rs.getObject("resolved_at", LocalDateTime.class));
        report.setCdate(rs.getObject("cdate", LocalDateTime.class));
        report.setUdate(rs.getObject("udate", LocalDateTime.class));
        
        return report;
    };

    private final RowMapper<ReportDetailDTO> reportDetailRowMapper = (rs, rowNum) -> {
        ReportDetailDTO dto = new ReportDetailDTO();
        dto.setReportId(rs.getLong("report_id"));
        dto.setReporterId(rs.getLong("reporter_id"));
        dto.setTargetType(rs.getString("target_type"));
        dto.setTargetId(rs.getLong("target_id"));
        dto.setCategoryId(rs.getLong("category_id"));
        dto.setReason(rs.getString("reason"));
        dto.setEvidence(rs.getString("evidence"));
        dto.setStatus(rs.getString("status"));
        dto.setAdminNotes(rs.getString("admin_notes"));
        dto.setResolvedBy(rs.getObject("resolved_by", Long.class));
        dto.setResolvedAt(rs.getObject("resolved_at", LocalDateTime.class));
        dto.setCdate(rs.getObject("cdate", LocalDateTime.class));
        dto.setUdate(rs.getObject("udate", LocalDateTime.class));
        
        // JOIN 결과
        dto.setReporterName(rs.getString("reporter_name"));
        dto.setCategoryName(rs.getString("category_name"));
        dto.setResolverName(rs.getString("resolver_name"));
        dto.setTargetContent(rs.getString("target_content"));
        
        return dto;
    };

    @Override
    public Long save(Report report) {
        String sql = """
            INSERT INTO reports (report_id, reporter_id, target_type, target_id, category_id, 
                                reason, evidence, status, admin_notes, resolved_by, resolved_at, cdate, udate)
            VALUES (seq_report_id.nextval, :reporterId, :targetType, :targetId, :categoryId,
                    :reason, :evidence, :status, :adminNotes, :resolvedBy, :resolvedAt, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reporterId", report.getReporterId())
            .addValue("targetType", report.getTargetType())
            .addValue("targetId", report.getTargetId())
            .addValue("categoryId", report.getCategoryId())
            .addValue("reason", report.getReason())
            .addValue("evidence", report.getEvidence())
            .addValue("status", report.getStatus() != null ? report.getStatus() : "PENDING")
            .addValue("adminNotes", report.getAdminNotes())
            .addValue("resolvedBy", report.getResolvedBy())
            .addValue("resolvedAt", report.getResolvedAt());
        
        template.update(sql, params);
        
        // 생성된 report_id 조회
        String selectSql = "SELECT seq_report_id.currval FROM dual";
        Long reportId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);
        
        if (reportId == null) {
            throw new IllegalStateException("Failed to retrieve generated report_id");
        }
        return reportId;
    }

    @Override
    public Optional<Report> findById(Long reportId) {
        String sql = "SELECT * FROM reports WHERE report_id = :reportId";
        
        MapSqlParameterSource params = new MapSqlParameterSource("reportId", reportId);
        
        try {
            Report report = template.queryForObject(sql, params, reportRowMapper);
            return Optional.ofNullable(report);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Report> findAll() {
        String sql = "SELECT * FROM reports ORDER BY cdate DESC";
        
        return template.query(sql, reportRowMapper);
    }

    @Override
    public List<Report> findByStatus(String status) {
        String sql = "SELECT * FROM reports WHERE status = :status ORDER BY cdate DESC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("status", status);
        return template.query(sql, params, reportRowMapper);
    }

    @Override
    public List<Report> findByTargetType(String targetType) {
        String sql = "SELECT * FROM reports WHERE target_type = :targetType ORDER BY cdate DESC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("targetType", targetType);
        return template.query(sql, params, reportRowMapper);
    }

    @Override
    public List<Report> findByTarget(String targetType, Long targetId) {
        String sql = "SELECT * FROM reports WHERE target_type = :targetType AND target_id = :targetId ORDER BY cdate DESC";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("targetType", targetType)
            .addValue("targetId", targetId);
        return template.query(sql, params, reportRowMapper);
    }

    @Override
    public List<Report> findByReporterId(Long reporterId) {
        String sql = "SELECT * FROM reports WHERE reporter_id = :reporterId ORDER BY cdate DESC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("reporterId", reporterId);
        return template.query(sql, params, reportRowMapper);
    }

    @Override
    public List<Report> findByResolverId(Long resolverId) {
        String sql = "SELECT * FROM reports WHERE resolved_by = :resolverId ORDER BY resolved_at DESC";
        
        MapSqlParameterSource params = new MapSqlParameterSource("resolverId", resolverId);
        return template.query(sql, params, reportRowMapper);
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = :status";
        MapSqlParameterSource params = new MapSqlParameterSource("status", status);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public int countByTargetType(String targetType) {
        String sql = "SELECT COUNT(*) FROM reports WHERE target_type = :targetType";
        MapSqlParameterSource params = new MapSqlParameterSource("targetType", targetType);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public int countByTarget(String targetType, Long targetId) {
        String sql = "SELECT COUNT(*) FROM reports WHERE target_type = :targetType AND target_id = :targetId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("targetType", targetType)
            .addValue("targetId", targetId);
        return template.queryForObject(sql, params, Integer.class);
    }
    
    @Override
    public int countByCategory(String categoryCode) {
        String sql = """
            SELECT COUNT(*) FROM reports r
            JOIN code c ON r.category_id = c.code_id
            WHERE c.gcode = 'REPORT_CATEGORY' AND c.code = :categoryCode
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("categoryCode", categoryCode);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public int updateStatus(Long reportId, String status, Long resolverId) {
        String sql = """
            UPDATE reports 
            SET status = :status, resolved_by = :resolverId, resolved_at = SYSTIMESTAMP, udate = SYSTIMESTAMP
            WHERE report_id = :reportId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reportId", reportId)
            .addValue("status", status)
            .addValue("resolverId", resolverId);
        
        return template.update(sql, params);
    }

    @Override
    public int updateAdminNotes(Long reportId, String adminNotes) {
        String sql = "UPDATE reports SET admin_notes = :adminNotes, udate = SYSTIMESTAMP WHERE report_id = :reportId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reportId", reportId)
            .addValue("adminNotes", adminNotes);
        
        return template.update(sql, params);
    }

    @Override
    public int updateReportStatistics(String targetType, Long targetId) {
        String sql = """
            MERGE INTO report_statistics rs
            USING (SELECT :targetType as target_type, :targetId as target_id FROM dual) d
            ON (rs.target_type = d.target_type AND rs.target_id = d.target_id)
            WHEN MATCHED THEN
                UPDATE SET 
                    total_reports = (SELECT COUNT(*) FROM reports WHERE target_type = :targetType AND target_id = :targetId),
                    pending_count = (SELECT COUNT(*) FROM reports WHERE target_type = :targetType AND target_id = :targetId AND status = 'PENDING'),
                    resolved_count = (SELECT COUNT(*) FROM reports WHERE target_type = :targetType AND target_id = :targetId AND status IN ('RESOLVED', 'REJECTED')),
                    last_reported = (SELECT MAX(cdate) FROM reports WHERE target_type = :targetType AND target_id = :targetId),
                    udate = SYSTIMESTAMP
            WHEN NOT MATCHED THEN
                INSERT (stat_id, target_type, target_id, total_reports, pending_count, resolved_count, last_reported, cdate, udate)
                VALUES (seq_report_stat_id.nextval, :targetType, :targetId, 
                        (SELECT COUNT(*) FROM reports WHERE target_type = :targetType AND target_id = :targetId),
                        (SELECT COUNT(*) FROM reports WHERE target_type = :targetType AND target_id = :targetId AND status = 'PENDING'),
                        (SELECT COUNT(*) FROM reports WHERE target_type = :targetType AND target_id = :targetId AND status IN ('RESOLVED', 'REJECTED')),
                        (SELECT MAX(cdate) FROM reports WHERE target_type = :targetType AND target_id = :targetId),
                        SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("targetType", targetType)
            .addValue("targetId", targetId);
        
        return template.update(sql, params);
    }

    @Override
    public List<Report> findTargetsForAutoAction(String targetType, int threshold) {
        String sql = """
            SELECT DISTINCT r.target_id, COUNT(*) as report_count
            FROM reports r
            WHERE r.target_type = :targetType AND r.status = 'PENDING'
            GROUP BY r.target_id
            HAVING COUNT(*) >= :threshold
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("targetType", targetType)
            .addValue("threshold", threshold);
        
        return template.query(sql, params, (rs, rowNum) -> {
            Report report = new Report();
            report.setTargetId(rs.getLong("target_id"));
            report.setTargetType(targetType);
            return report;
        });
    }

    @Override
    public int updateById(Long id, Report report) {
        String sql = """
            UPDATE reports 
            SET reporter_id = :reporterId, target_type = :targetType, target_id = :targetId,
                category_id = :categoryId, reason = :reason, evidence = :evidence,
                status = :status, admin_notes = :adminNotes, resolved_by = :resolvedBy,
                resolved_at = :resolvedAt, udate = SYSTIMESTAMP
            WHERE report_id = :reportId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("reportId", id)
            .addValue("reporterId", report.getReporterId())
            .addValue("targetType", report.getTargetType())
            .addValue("targetId", report.getTargetId())
            .addValue("categoryId", report.getCategoryId())
            .addValue("reason", report.getReason())
            .addValue("evidence", report.getEvidence())
            .addValue("status", report.getStatus())
            .addValue("adminNotes", report.getAdminNotes())
            .addValue("resolvedBy", report.getResolvedBy())
            .addValue("resolvedAt", report.getResolvedAt());
        
        return template.update(sql, params);
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM reports WHERE report_id = :reportId";
        MapSqlParameterSource params = new MapSqlParameterSource("reportId", id);
        return template.update(sql, params);
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM reports";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT * FROM reports 
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, reportRowMapper);
    }

    // === DTO 기반 조회 메서드들 ===

    @Override
    public Optional<ReportDetailDTO> findDetailById(Long reportId) {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            WHERE r.report_id = :reportId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("reportId", reportId);
        
        try {
            ReportDetailDTO dto = template.queryForObject(sql, params, reportDetailRowMapper);
            return Optional.ofNullable(dto);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReportDetailDTO> findAllDetails() {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            ORDER BY r.cdate DESC
            """;
        
        return template.query(sql, reportDetailRowMapper);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByStatus(String status) {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            WHERE r.status = :status
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("status", status);
        return template.query(sql, params, reportDetailRowMapper);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByTargetType(String targetType) {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            WHERE r.target_type = :targetType
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("targetType", targetType);
        return template.query(sql, params, reportDetailRowMapper);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByTarget(String targetType, Long targetId) {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            WHERE r.target_type = :targetType AND r.target_id = :targetId
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("targetType", targetType)
            .addValue("targetId", targetId);
        return template.query(sql, params, reportDetailRowMapper);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByReporterId(Long reporterId) {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            WHERE r.reporter_id = :reporterId
            ORDER BY r.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("reporterId", reporterId);
        return template.query(sql, params, reportDetailRowMapper);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByResolverId(Long resolverId) {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            WHERE r.resolved_by = :resolverId
            ORDER BY r.resolved_at DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("resolverId", resolverId);
        return template.query(sql, params, reportDetailRowMapper);
    }

    @Override
    public List<ReportDetailDTO> findDetailsWithOffset(int offset, int limit) {
        String sql = """
            SELECT r.*, 
                   m1.nickname as reporter_name,
                   c.decode as category_name,
                   m2.nickname as resolver_name,
                   CASE 
                       WHEN r.target_type = 'REVIEW' THEN (SELECT DBMS_LOB.SUBSTR(content, 1000, 1) FROM reviews WHERE review_id = r.target_id AND ROWNUM = 1)
                       WHEN r.target_type = 'COMMENT' THEN (SELECT content FROM review_comments WHERE comment_id = r.target_id AND ROWNUM = 1)
                       ELSE NULL
                   END as target_content
            FROM reports r
            LEFT JOIN member m1 ON r.reporter_id = m1.member_id
            LEFT JOIN code c ON r.category_id = c.code_id
            LEFT JOIN member m2 ON r.resolved_by = m2.member_id
            ORDER BY r.cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, reportDetailRowMapper);
    }
} 