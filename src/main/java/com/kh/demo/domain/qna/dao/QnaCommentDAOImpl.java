package com.kh.demo.domain.qna.dao;

import com.kh.demo.domain.common.svc.CodeSVC;
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
public class QnaCommentDAOImpl implements QnaCommentDAO {

    private final NamedParameterJdbcTemplate template;
    private final CodeSVC codeSVC;

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
    public Long save(QnaComment comment) {
        String sql = """
            INSERT INTO qna_comment (comment_id, qna_id, member_id, admin_id, content, 
                                   comment_type_id, helpful_count, unhelpful_count, status_id, cdate, udate)
            VALUES (seq_qna_comment_id.nextval, :qnaId, :memberId, :adminId, :content,
                   :commentTypeId, :helpfulCount, :unhelpfulCount, :statusId, SYSTIMESTAMP, SYSTIMESTAMP)
            """;

        // statusId가 null이면 ACTIVE 상태로 설정
        Long statusId = comment.getStatusId();
        if (statusId == null) {
            statusId = codeSVC.getCodeId("QNA_COMMENT_STATUS", "ACTIVE");
            if (statusId == null) {
                log.error("QNA_COMMENT_STATUS ACTIVE 코드를 찾을 수 없습니다.");
                throw new IllegalStateException("QNA_COMMENT_STATUS ACTIVE 코드가 존재하지 않습니다.");
            }
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("qnaId", comment.getQnaId())
            .addValue("memberId", comment.getMemberId())
            .addValue("adminId", comment.getAdminId())
            .addValue("content", comment.getContent())
            .addValue("commentTypeId", comment.getCommentTypeId())
            .addValue("helpfulCount", comment.getHelpfulCount() != null ? comment.getHelpfulCount() : 0)
            .addValue("unhelpfulCount", comment.getUnhelpfulCount() != null ? comment.getUnhelpfulCount() : 0)
            .addValue("statusId", statusId);

        template.update(sql, params);

        // 생성된 comment_id 조회
        String selectSql = "SELECT seq_qna_comment_id.currval FROM dual";
        Long commentId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);

        if (commentId == null) {
            throw new IllegalStateException("Failed to retrieve generated comment_id");
        }
        return commentId;
    }

    @Override
    public int updateById(Long commentId, QnaComment comment) {
        String sql = """
            UPDATE qna_comment 
            SET qna_id = :qnaId, member_id = :memberId, admin_id = :adminId, content = :content,
                comment_type_id = :commentTypeId, helpful_count = :helpfulCount,
                unhelpful_count = :unhelpfulCount, status_id = :statusId, udate = SYSTIMESTAMP
            WHERE comment_id = :commentId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("commentId", commentId)
            .addValue("qnaId", comment.getQnaId())
            .addValue("memberId", comment.getMemberId())
            .addValue("adminId", comment.getAdminId())
            .addValue("content", comment.getContent())
            .addValue("commentTypeId", comment.getCommentTypeId())
            .addValue("helpfulCount", comment.getHelpfulCount())
            .addValue("unhelpfulCount", comment.getUnhelpfulCount())
            .addValue("statusId", comment.getStatusId());

        return template.update(sql, params);
    }

    @Override
    public int deleteById(Long commentId) {
        String sql = "DELETE FROM qna_comment WHERE comment_id = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.update(sql, params);
    }

    @Override
    public Optional<QnaComment> findById(Long commentId) {
        String sql = "SELECT * FROM qna_comment WHERE comment_id = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        
        try {
            QnaComment comment = template.queryForObject(sql, params, qnaCommentRowMapper);
            return Optional.ofNullable(comment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<QnaComment> findAll() {
        String sql = "SELECT * FROM qna_comment ORDER BY cdate DESC";
        return template.query(sql, qnaCommentRowMapper);
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM qna_comment";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public List<QnaComment> findByQnaId(Long qnaId) {
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
    public int countByQnaId(Long qnaId) {
        String sql = "SELECT COUNT(*) FROM qna_comment WHERE qna_id = :qnaId AND status_id = :activeStatus";
        Long activeStatus = codeSVC.getCodeId("QNA_COMMENT_STATUS", "ACTIVE");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("qnaId", qnaId)
            .addValue("activeStatus", activeStatus);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<QnaComment> findByMemberId(Long memberId) {
        String sql = "SELECT * FROM qna_comment WHERE member_id = :memberId ORDER BY cdate DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, params, qnaCommentRowMapper);
    }

    @Override
    public List<QnaComment> findByAdminId(Long adminId) {
        String sql = "SELECT * FROM qna_comment WHERE admin_id = :adminId ORDER BY cdate DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("adminId", adminId);
        return template.query(sql, params, qnaCommentRowMapper);
    }

    @Override
    public List<QnaComment> findByCommentTypeId(Long commentTypeId) {
        String sql = "SELECT * FROM qna_comment WHERE comment_type_id = :commentTypeId ORDER BY cdate DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("commentTypeId", commentTypeId);
        return template.query(sql, params, qnaCommentRowMapper);
    }

    @Override
    public int updateStatus(Long commentId, Long statusId) {
        String sql = "UPDATE qna_comment SET status_id = :statusId, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("commentId", commentId)
            .addValue("statusId", statusId);
        return template.update(sql, params);
    }

    @Override
    public int incrementHelpfulCount(Long commentId) {
        String sql = "UPDATE qna_comment SET helpful_count = helpful_count + 1, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.update(sql, params);
    }

    @Override
    public int incrementUnhelpfulCount(Long commentId) {
        String sql = "UPDATE qna_comment SET unhelpful_count = unhelpful_count + 1, udate = SYSTIMESTAMP WHERE comment_id = :commentId";
        MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);
        return template.update(sql, params);
    }

    @Override
    public List<QnaComment> findAllWithPaging(int offset, int limit) {
        String sql = """
            SELECT * FROM qna_comment 
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);

        return template.query(sql, params, qnaCommentRowMapper);
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM qna_comment";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public List<QnaComment> findAllWithOffset(int offset, int limit) {
        return findAllWithPaging(offset, limit);
    }
}
