package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.board.entity.Boards;
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

import java.sql.Clob;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 게시판 데이터 접근 객체 구현체
 * NamedJdbcTemplate을 사용하여 게시글의 CRUD 및 검색 기능을 구현합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BoardDAOImpl implements BoardDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private final RowMapper<Boards> boardRowMapper = (ResultSet rs, int rowNum) -> {
        Boards board = new Boards();
        board.setBoardId(rs.getLong("board_id"));
        board.setBcategory(rs.getLong("bcategory"));
        board.setTitle(rs.getString("title"));
        board.setEmail(rs.getString("email"));
        board.setNickname(rs.getString("nickname"));
        board.setHit(rs.getInt("hit"));
        board.setBcontent(rs.getClob("bcontent"));
        board.setPboardId(rs.getLong("pboard_id"));
        board.setBgroup(rs.getLong("bgroup"));
        board.setStep(rs.getInt("step"));
        board.setBindent(rs.getInt("bindent"));
        board.setStatus(rs.getString("status"));
        board.setCdate(rs.getObject("cdate", LocalDateTime.class));
        board.setUdate(rs.getObject("udate", LocalDateTime.class));
        return board;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public Long save(Boards board) {
        String sql = """
            INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status, cdate, udate)
            VALUES (seq_board_id.nextval, :bcategory, :title, :email, :nickname, :hit, :bcontent, :pboardId, :bgroup, :step, :bindent, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, new BeanPropertySqlParameterSource(board), keyHolder, new String[]{"board_id"});
        
        Number boardIdNumber = keyHolder.getKey();
        if (boardIdNumber == null) {
            throw new IllegalStateException("Failed to retrieve generated board_id");
        }
        return boardIdNumber.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateById(Long boardId, Boards board) {
        String sql = """
            UPDATE boards 
            SET bcategory = :bcategory, title = :title, email = :email, nickname = :nickname, 
                hit = :hit, bcontent = :bcontent, pboard_id = :pboardId, bgroup = :bgroup, 
                step = :step, bindent = :bindent, status = :status, udate = SYSTIMESTAMP
            WHERE board_id = :boardId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", board.getBcategory())
                .addValue("title", board.getTitle())
                .addValue("email", board.getEmail())
                .addValue("nickname", board.getNickname())
                .addValue("hit", board.getHit())
                .addValue("bcontent", board.getBcontent())
                .addValue("pboardId", board.getPboardId())
                .addValue("bgroup", board.getBgroup())
                .addValue("step", board.getStep())
                .addValue("bindent", board.getBindent())
                .addValue("status", board.getStatus())
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Boards board) {
        return updateById(board.getBoardId(), board);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteById(Long boardId) {
        String sql = "DELETE FROM boards WHERE board_id = :boardId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Long boardId) {
        return deleteById(boardId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boards> findById(Long boardId) {
        String sql = "SELECT * FROM boards WHERE board_id = :boardId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        try {
            Boards board = template.queryForObject(sql, param, boardRowMapper);
            return Optional.ofNullable(board);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findAll() {
        String sql = "SELECT * FROM boards ORDER BY bgroup DESC, step ASC, cdate DESC";
        return template.query(sql, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findAllWithPaging(int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM boards ORDER BY bgroup DESC, step ASC, cdate DESC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBcategory(Long bcategory) {
        String sql = """
            SELECT * FROM boards 
            WHERE bcategory = :bcategory 
            ORDER BY bgroup DESC, step ASC, cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", bcategory);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBcategoryWithPaging(Long bcategory, int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM boards WHERE bcategory = :bcategory ORDER BY bgroup DESC, step ASC, cdate DESC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", bcategory)
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByEmail(String email) {
        String sql = """
            SELECT * FROM boards 
            WHERE email = :email 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByEmailWithPaging(String email, int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM boards WHERE email = :email ORDER BY cdate DESC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByTitleContaining(String keyword) {
        String sql = """
            SELECT * FROM boards 
            WHERE title LIKE '%' || :keyword || '%' 
            ORDER BY bgroup DESC, step ASC, cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByTitleContainingWithPaging(String keyword, int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM boards WHERE title LIKE '%' || :keyword || '%' ORDER BY bgroup DESC, step ASC, cdate DESC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword)
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementHit(Long boardId) {
        String sql = "UPDATE boards SET hit = hit + 1 WHERE board_id = :boardId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBgroup(Long bgroup) {
        String sql = """
            SELECT * FROM boards 
            WHERE bgroup = :bgroup 
            ORDER BY step ASC, cdate ASC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bgroup", bgroup);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByBoardId(Long boardId) {
        String sql = "SELECT COUNT(*) FROM boards WHERE board_id = :boardId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM boards";
        Integer count = template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int countAll() {
        return getTotalCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByBcategory(Long bcategory) {
        String sql = "SELECT COUNT(*) FROM boards WHERE bcategory = :bcategory";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", bcategory);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM boards WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByTitleContaining(String keyword) {
        String sql = "SELECT COUNT(*) FROM boards WHERE title LIKE '%' || :keyword || '%'";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }
} 