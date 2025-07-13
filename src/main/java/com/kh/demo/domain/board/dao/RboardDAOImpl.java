package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.entity.Replies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 댓글 데이터 접근 객체 구현체
 * NamedJdbcTemplate을 사용하여 댓글의 CRUD 및 검색 기능을 구현합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RboardDAOImpl implements RboardDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private final RowMapper<Replies> replyRowMapper = (ResultSet rs, int rowNum) -> {
        Replies reply = new Replies();
        reply.setReplyId(rs.getLong("reply_id"));
        reply.setBoardId(rs.getLong("board_id"));
        reply.setEmail(rs.getString("email"));
        reply.setNickname(rs.getString("nickname"));
        reply.setRcontent(rs.getString("rcontent"));
        reply.setParentId(rs.getLong("parent_id"));
        reply.setRgroup(rs.getLong("rgroup"));
        reply.setRstep(rs.getInt("rstep"));
        reply.setRindent(rs.getInt("rindent"));
        reply.setStatus(rs.getString("status"));
        reply.setCdate(rs.getObject("cdate", LocalDateTime.class));
        reply.setUdate(rs.getObject("udate", LocalDateTime.class));
        return reply;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public Long save(Replies reply) {
        String sql = """
            INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status, cdate, udate)
            VALUES (seq_reply_id.nextval, :boardId, :email, :nickname, :rcontent, :parentId, :rgroup, :rstep, :rindent, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, new BeanPropertySqlParameterSource(reply), keyHolder);
        
        return keyHolder.getKey().longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Replies reply) {
        String sql = """
            UPDATE replies 
            SET board_id = :boardId, email = :email, nickname = :nickname, rcontent = :rcontent, 
                parent_id = :parentId, rgroup = :rgroup, rstep = :rstep, rindent = :rindent, 
                status = :status, udate = SYSTIMESTAMP
            WHERE reply_id = :replyId
            """;
        
        return template.update(sql, new BeanPropertySqlParameterSource(reply));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Long replyId) {
        String sql = "DELETE FROM replies WHERE reply_id = :replyId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("replyId", replyId);
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Replies> findById(Long replyId) {
        String sql = "SELECT * FROM replies WHERE reply_id = :replyId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("replyId", replyId);
        
        try {
            Replies reply = template.queryForObject(sql, param, replyRowMapper);
            return Optional.ofNullable(reply);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByBoardId(Long boardId) {
        String sql = """
            SELECT * FROM replies 
            WHERE board_id = :boardId 
            ORDER BY rgroup ASC, rstep ASC, cdate ASC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByBoardIdWithPaging(Long boardId, int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM replies WHERE board_id = :boardId ORDER BY rgroup ASC, rstep ASC, cdate ASC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId)
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByEmail(String email) {
        String sql = """
            SELECT * FROM replies 
            WHERE email = :email 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByEmailWithPaging(String email, int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM replies WHERE email = :email ORDER BY cdate DESC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByParentId(Long parentId) {
        String sql = """
            SELECT * FROM replies 
            WHERE parent_id = :parentId 
            ORDER BY cdate ASC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("parentId", parentId);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByRgroup(Long rgroup) {
        String sql = """
            SELECT * FROM replies 
            WHERE rgroup = :rgroup 
            ORDER BY rstep ASC, cdate ASC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("rgroup", rgroup);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByRcontentContaining(String keyword) {
        String sql = """
            SELECT * FROM replies 
            WHERE rcontent LIKE '%' || :keyword || '%' 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Replies> findByRcontentContainingWithPaging(String keyword, int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM replies WHERE rcontent LIKE '%' || :keyword || '%' ORDER BY cdate DESC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword)
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByReplyId(Long replyId) {
        String sql = "SELECT COUNT(*) FROM replies WHERE reply_id = :replyId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("replyId", replyId);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByBoardId(Long boardId) {
        String sql = "SELECT COUNT(*) FROM replies WHERE board_id = :boardId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM replies WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByParentId(Long parentId) {
        String sql = "SELECT COUNT(*) FROM replies WHERE parent_id = :parentId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("parentId", parentId);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByRcontentContaining(String keyword) {
        String sql = "SELECT COUNT(*) FROM replies WHERE rcontent LIKE '%' || :keyword || '%'";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }
} 