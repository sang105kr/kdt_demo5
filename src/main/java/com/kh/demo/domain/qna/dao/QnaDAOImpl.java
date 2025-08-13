package com.kh.demo.domain.qna.dao;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.qna.entity.Qna;
import com.kh.demo.domain.qna.entity.QnaComment;
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
public class QnaDAOImpl implements QnaDAO {

    private final NamedParameterJdbcTemplate template;
    private final CodeSVC codeSVC;

    // Qna RowMapper 정의
    private final RowMapper<Qna> qnaRowMapper = (rs, rowNum) -> {
        Qna qna = new Qna();
        qna.setQnaId(rs.getLong("qna_id"));
        qna.setProductId(rs.getObject("product_id", Long.class));
        qna.setMemberId(rs.getLong("member_id"));
        qna.setCategoryId(rs.getLong("category_id"));
        qna.setTitle(rs.getString("title"));
        qna.setContent(rs.getString("content"));
        qna.setHelpfulCount(rs.getInt("helpful_count"));
        qna.setUnhelpfulCount(rs.getInt("unhelpful_count"));
        qna.setViewCount(rs.getInt("view_count"));
        qna.setCommentCount(rs.getInt("comment_count"));
        qna.setStatusId(rs.getLong("status_id"));
        qna.setAdminId(rs.getObject("admin_id", Long.class));
        qna.setAnswer(rs.getString("answer"));
        qna.setAnsweredAt(rs.getObject("answered_at", LocalDateTime.class));
        qna.setCdate(rs.getObject("cdate", LocalDateTime.class));
        qna.setUdate(rs.getObject("udate", LocalDateTime.class));
        
        // JOIN으로 가져온 데이터
        try {
            qna.setNickname(rs.getString("member_nickname"));
        } catch (Exception e) {
            qna.setNickname(null);
        }
        
        return qna;
    };

    // QnaComment RowMapper 정의
    private final RowMapper<QnaComment> qnaCommentRowMapper = (rs, rowNum) -> {
        QnaComment comment = new QnaComment();
        comment.setCommentId(rs.getLong("comment_id"));
        comment.setQnaId(rs.getLong("qna_id"));
        comment.setMemberId(rs.getObject("member_id", Long.class));
        comment.setAdminId(rs.getObject("admin_id", Long.class));
        comment.setContent(rs.getString("content"));
        comment.setCommentTypeId(rs.getLong("comment_type_id"));
        comment.setHelpfulCount(rs.getInt("helpful_count"));
        comment.setUnhelpfulCount(rs.getInt("unhelpful_count"));
        comment.setStatusId(rs.getLong("status_id"));
        comment.setCdate(rs.getObject("cdate", LocalDateTime.class));
        comment.setUdate(rs.getObject("udate", LocalDateTime.class));
        
        // JOIN으로 가져온 데이터
        try {
            comment.setMemberNickname(rs.getString("member_nickname"));
        } catch (Exception e) {
            comment.setMemberNickname(null);
        }
        
        try {
            comment.setAdminNickname(rs.getString("admin_nickname"));
        } catch (Exception e) {
            comment.setAdminNickname(null);
        }
        
        return comment;
    };

    @Override
    public Long save(Qna qna) {
        String sql = """
            INSERT INTO qna (qna_id, product_id, member_id, category_id, title, content, 
                           helpful_count, unhelpful_count, view_count, comment_count, status_id, 
                           admin_id, answer, answered_at, cdate, udate)
            VALUES (seq_qna_id.nextval, :productId, :memberId, :categoryId, :title, :content,
                   :helpfulCount, :unhelpfulCount, :viewCount, :commentCount, :statusId,
                   :adminId, :answer, :answeredAt, SYSTIMESTAMP, SYSTIMESTAMP)
            """;

        // statusId가 null이면 PENDING 상태로 설정
        Long statusId = qna.getStatusId();
        if (statusId == null) {
            statusId = codeSVC.getCodeId("QNA_STATUS", "PENDING");
            if (statusId == null) {
                log.error("QNA_STATUS PENDING 코드를 찾을 수 없습니다.");
                throw new IllegalStateException("QNA_STATUS PENDING 코드가 존재하지 않습니다.");
            }
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("productId", qna.getProductId())
            .addValue("memberId", qna.getMemberId())
            .addValue("categoryId", qna.getCategoryId())
            .addValue("title", qna.getTitle())
            .addValue("content", qna.getContent())
            .addValue("helpfulCount", qna.getHelpfulCount() != null ? qna.getHelpfulCount() : 0)
            .addValue("unhelpfulCount", qna.getUnhelpfulCount() != null ? qna.getUnhelpfulCount() : 0)
            .addValue("viewCount", qna.getViewCount() != null ? qna.getViewCount() : 0)
            .addValue("commentCount", qna.getCommentCount() != null ? qna.getCommentCount() : 0)
            .addValue("statusId", statusId)
            .addValue("adminId", qna.getAdminId())
            .addValue("answer", qna.getAnswer())
            .addValue("answeredAt", qna.getAnsweredAt());

        template.update(sql, params);

        // 생성된 qna_id 조회
        String selectSql = "SELECT seq_qna_id.currval FROM dual";
        Long qnaId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);

        if (qnaId == null) {
            throw new IllegalStateException("Failed to retrieve generated qna_id");
        }
        return qnaId;
    }

    @Override
    public int updateById(Long qnaId, Qna qna) {
        String sql = """
            UPDATE qna 
            SET product_id = :productId, member_id = :memberId, category_id = :categoryId,
                title = :title, content = :content, helpful_count = :helpfulCount,
                unhelpful_count = :unhelpfulCount, view_count = :viewCount,
                comment_count = :commentCount, status_id = :statusId, admin_id = :adminId,
                answer = :answer, answered_at = :answeredAt, udate = SYSTIMESTAMP
            WHERE qna_id = :qnaId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("qnaId", qnaId)
            .addValue("productId", qna.getProductId())
            .addValue("memberId", qna.getMemberId())
            .addValue("categoryId", qna.getCategoryId())
            .addValue("title", qna.getTitle())
            .addValue("content", qna.getContent())
            .addValue("helpfulCount", qna.getHelpfulCount())
            .addValue("unhelpfulCount", qna.getUnhelpfulCount())
            .addValue("viewCount", qna.getViewCount())
            .addValue("commentCount", qna.getCommentCount())
            .addValue("statusId", qna.getStatusId())
            .addValue("adminId", qna.getAdminId())
            .addValue("answer", qna.getAnswer())
            .addValue("answeredAt", qna.getAnsweredAt());

        return template.update(sql, params);
    }

    @Override
    public int deleteById(Long qnaId) {
        String sql = "DELETE FROM qna WHERE qna_id = :qnaId";
        MapSqlParameterSource params = new MapSqlParameterSource("qnaId", qnaId);
        return template.update(sql, params);
    }

    @Override
    public Optional<Qna> findById(Long qnaId) {
        String sql = """
            SELECT q.*, m.nickname as member_nickname 
            FROM qna q
            LEFT JOIN member m ON q.member_id = m.member_id
            WHERE q.qna_id = :qnaId
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("qnaId", qnaId);
        
        try {
            Qna qna = template.queryForObject(sql, params, qnaRowMapper);
            return Optional.ofNullable(qna);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Qna> findAll() {
        String sql = "SELECT * FROM qna ORDER BY cdate DESC";
        return template.query(sql, qnaRowMapper);
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM qna";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public List<Qna> findByProductId(Long productId, int offset, int limit) {
        String sql = """
            SELECT * FROM qna 
            WHERE product_id = :productId AND status_id = :pendingStatus
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;

        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("productId", productId)
            .addValue("pendingStatus", pendingStatus)
            .addValue("offset", offset)
            .addValue("limit", limit);

        return template.query(sql, params, qnaRowMapper);
    }

    @Override
    public int countByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM qna WHERE product_id = :productId AND status_id = :pendingStatus";
        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("productId", productId)
            .addValue("pendingStatus", pendingStatus);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Qna> findByMemberId(Long memberId) {
        String sql = "SELECT * FROM qna WHERE member_id = :memberId AND status_id = :pendingStatus ORDER BY cdate DESC";
        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("pendingStatus", pendingStatus);
        return template.query(sql, params, qnaRowMapper);
    }
    
    @Override
    public List<Qna> findByMemberIdAllStatus(Long memberId) {
        String sql = "SELECT * FROM qna WHERE member_id = :memberId ORDER BY cdate DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, params, qnaRowMapper);
    }

    @Override
    public List<Qna> findByCategoryId(Long categoryId, int offset, int limit) {
        String sql = """
            SELECT q.*, m.nickname as member_nickname 
            FROM qna q
            LEFT JOIN member m ON q.member_id = m.member_id
            WHERE q.category_id = :categoryId AND q.status_id IN (:pendingStatus, :answeredStatus)
            ORDER BY q.cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;

        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        Long answeredStatus = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("categoryId", categoryId)
            .addValue("pendingStatus", pendingStatus)
            .addValue("answeredStatus", answeredStatus)
            .addValue("offset", offset)
            .addValue("limit", limit);

        return template.query(sql, params, qnaRowMapper);
    }

    @Override
    public int countByCategoryId(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM qna WHERE category_id = :categoryId AND status_id IN (:pendingStatus, :answeredStatus)";
        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        Long answeredStatus = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("categoryId", categoryId)
            .addValue("pendingStatus", pendingStatus)
            .addValue("answeredStatus", answeredStatus);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Qna> findByStatusId(Long statusId, int offset, int limit) {
        String sql = """
            SELECT q.*, m.nickname as member_nickname 
            FROM qna q
            LEFT JOIN member m ON q.member_id = m.member_id
            WHERE q.status_id = :statusId
            ORDER BY q.cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("statusId", statusId)
            .addValue("offset", offset)
            .addValue("limit", limit);

        return template.query(sql, params, qnaRowMapper);
    }

    @Override
    public int countByStatusId(Long statusId) {
        String sql = "SELECT COUNT(*) FROM qna WHERE status_id = :statusId";
        MapSqlParameterSource params = new MapSqlParameterSource("statusId", statusId);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Qna> findByKeyword(String keyword, int offset, int limit) {
        String sql = """
            SELECT q.*, m.nickname as member_nickname 
            FROM qna q
            LEFT JOIN member m ON q.member_id = m.member_id
            WHERE (q.title LIKE :keyword OR q.content LIKE :keyword) AND q.status_id IN (:pendingStatus, :answeredStatus)
            ORDER BY q.cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;

        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        Long answeredStatus = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("keyword", "%" + keyword + "%")
            .addValue("pendingStatus", pendingStatus)
            .addValue("answeredStatus", answeredStatus)
            .addValue("offset", offset)
            .addValue("limit", limit);

        return template.query(sql, params, qnaRowMapper);
    }

    @Override
    public int countByKeyword(String keyword) {
        String sql = "SELECT COUNT(*) FROM qna WHERE (title LIKE :keyword OR content LIKE :keyword) AND status_id IN (:pendingStatus, :answeredStatus)";
        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        Long answeredStatus = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("keyword", "%" + keyword + "%")
            .addValue("pendingStatus", pendingStatus)
            .addValue("answeredStatus", answeredStatus);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public int incrementViewCount(Long qnaId) {
        String sql = "UPDATE qna SET view_count = view_count + 1, udate = SYSTIMESTAMP WHERE qna_id = :qnaId";
        MapSqlParameterSource params = new MapSqlParameterSource("qnaId", qnaId);
        return template.update(sql, params);
    }

    @Override
    public int incrementHelpfulCount(Long qnaId) {
        String sql = "UPDATE qna SET helpful_count = helpful_count + 1, udate = SYSTIMESTAMP WHERE qna_id = :qnaId";
        MapSqlParameterSource params = new MapSqlParameterSource("qnaId", qnaId);
        return template.update(sql, params);
    }

    @Override
    public int incrementUnhelpfulCount(Long qnaId) {
        String sql = "UPDATE qna SET unhelpful_count = unhelpful_count + 1, udate = SYSTIMESTAMP WHERE qna_id = :qnaId";
        MapSqlParameterSource params = new MapSqlParameterSource("qnaId", qnaId);
        return template.update(sql, params);
    }

    @Override
    public int incrementCommentCount(Long qnaId) {
        String sql = "UPDATE qna SET comment_count = comment_count + 1, udate = SYSTIMESTAMP WHERE qna_id = :qnaId";
        MapSqlParameterSource params = new MapSqlParameterSource("qnaId", qnaId);
        return template.update(sql, params);
    }

    @Override
    public int decrementCommentCount(Long qnaId) {
        String sql = "UPDATE qna SET comment_count = GREATEST(comment_count - 1, 0), udate = SYSTIMESTAMP WHERE qna_id = :qnaId";
        MapSqlParameterSource params = new MapSqlParameterSource("qnaId", qnaId);
        return template.update(sql, params);
    }

    @Override
    public int updateStatus(Long qnaId, Long statusId) {
        String sql = "UPDATE qna SET status_id = :statusId, udate = SYSTIMESTAMP WHERE qna_id = :qnaId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("qnaId", qnaId)
            .addValue("statusId", statusId);
        return template.update(sql, params);
    }

    @Override
    public int updateAnswer(Long qnaId, String answer, Long adminId) {
        String sql = """
            UPDATE qna 
            SET answer = :answer, admin_id = :adminId, answered_at = SYSTIMESTAMP, 
                status_id = :answeredStatus, udate = SYSTIMESTAMP 
            WHERE qna_id = :qnaId
            """;

        Long answeredStatus = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("qnaId", qnaId)
            .addValue("answer", answer)
            .addValue("adminId", adminId)
            .addValue("answeredStatus", answeredStatus);

        return template.update(sql, params);
    }

    @Override
    public List<QnaComment> findCommentsByQnaId(Long qnaId) {
        String sql = """
            SELECT qc.*, m.nickname as member_nickname, admin_m.nickname as admin_nickname
            FROM qna_comment qc
            LEFT JOIN member m ON qc.member_id = m.member_id
            LEFT JOIN member admin_m ON qc.admin_id = admin_m.member_id
            WHERE qc.qna_id = :qnaId AND qc.status_id = :activeStatus
            ORDER BY qc.cdate ASC
            """;

        Long activeStatus = codeSVC.getCodeId("QNA_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("qnaId", qnaId)
            .addValue("activeStatus", activeStatus);

        return template.query(sql, params, qnaCommentRowMapper);
    }

    @Override
    public int countCommentsByQnaId(Long qnaId) {
        String sql = "SELECT COUNT(*) FROM qna_comment WHERE qna_id = :qnaId AND status_id = :activeStatus";
        Long activeStatus = codeSVC.getCodeId("QNA_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("qnaId", qnaId)
            .addValue("activeStatus", activeStatus);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Qna> findAllWithPaging(int offset, int limit) {
        String sql = """
            SELECT q.*, m.nickname as member_nickname 
            FROM qna q
            LEFT JOIN member m ON q.member_id = m.member_id
            WHERE q.status_id IN (:pendingStatus, :answeredStatus)
            ORDER BY q.cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;

        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        Long answeredStatus = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("pendingStatus", pendingStatus)
            .addValue("answeredStatus", answeredStatus)
            .addValue("offset", offset)
            .addValue("limit", limit);

        return template.query(sql, params, qnaRowMapper);
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM qna WHERE status_id IN (:pendingStatus, :answeredStatus)";
        Long pendingStatus = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        Long answeredStatus = codeSVC.getCodeId("QNA_STATUS", "ANSWERED");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("pendingStatus", pendingStatus)
            .addValue("answeredStatus", answeredStatus);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Qna> findAllWithOffset(int offset, int limit) {
        return findAllWithPaging(offset, limit);
    }
}
