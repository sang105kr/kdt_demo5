package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
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
    private final CodeSVC codeSVC;


    // RowMapper 정의
    private final RowMapper<Boards> boardRowMapper = (ResultSet rs, int rowNum) -> {
        Boards board = new Boards();
        board.setBoardId(rs.getLong("board_id"));
        board.setBcategory(rs.getLong("bcategory"));
        board.setTitle(rs.getString("title"));
        board.setEmail(rs.getString("email"));
        board.setNickname(rs.getString("nickname"));
        board.setHit(rs.getInt("hit"));
        board.setBcontent(rs.getString("bcontent"));
        board.setPboardId(rs.getObject("pboard_id", Long.class));
        board.setBgroup(rs.getLong("bgroup"));
        board.setStep(rs.getInt("step"));
        board.setBindent(rs.getInt("bindent"));
        board.setLikeCount(rs.getInt("like_count"));
        board.setDislikeCount(rs.getInt("dislike_count"));
        board.setStatusId(rs.getLong("status_id"));
        board.setCdate(rs.getObject("cdate", LocalDateTime.class));
        board.setUdate(rs.getObject("udate", LocalDateTime.class));
        return board;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public Long save(Boards board) {
        log.info("게시글 DAO 저장 시작 - board: {}", board);
        
        String sql = """
            INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status_id, cdate, udate)
            VALUES (seq_board_id.nextval, :bcategory, :title, :email, :nickname, :hit, :bcontent, :pboardId, :bgroup, :step, :bindent, :statusId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        // CLOB을 직접 바인딩 (Oracle에서 지원)
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
                .addValue("statusId", board.getStatusId());
        
        log.info("SQL 파라미터: {}", param.getValues());
        
        // Oracle에서는 시퀀스를 명시적으로 사용
        template.update(sql, param);
        
        // 생성된 board_id 조회 (currval 사용)
        String selectSql = "SELECT seq_board_id.currval FROM dual";
        Long boardId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);
        
        if (boardId == null) {
            throw new IllegalStateException("Failed to retrieve generated board_id");
        }
        
        log.info("게시글 DAO 저장 완료 - boardId: {}", boardId);
        return boardId;
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
                step = :step, bindent = :bindent, status_id = :statusId, udate = SYSTIMESTAMP
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
                .addValue("statusId", board.getStatusId())
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */

    
    @Override
    public int updateContent(Long boardId, Long bcategory, String title, String email, String nickname, String bcontent) {
        String sql = """
            UPDATE boards 
            SET bcategory = :bcategory, title = :title, email = :email, nickname = :nickname, 
                bcontent = :bcontent, udate = SYSTIMESTAMP
            WHERE board_id = :boardId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", bcategory)
                .addValue("title", title)
                .addValue("email", email)
                .addValue("nickname", nickname)
                .addValue("bcontent", bcontent)
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
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
    public List<Boards> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT * FROM boards 
            ORDER BY bgroup DESC, step ASC, cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", limit);
        
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBcategory(Long bcategory) {
        String sql = "SELECT * FROM boards WHERE bcategory = :bcategory ORDER BY bgroup DESC, step ASC, cdate DESC";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("bcategory", bcategory);
        return template.query(sql, param, boardRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBcategoryWithPaging(Long bcategory, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM boards 
            WHERE bcategory = :bcategory 
            ORDER BY bgroup DESC, step ASC, cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", bcategory)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
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
    public List<Boards> findByEmailWithPaging(String email, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM boards 
            WHERE email = :email 
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
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
    public List<Boards> findByTitleContainingWithPaging(String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM boards 
            WHERE title LIKE '%' || :keyword || '%' 
            ORDER BY bgroup DESC, step ASC, cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
        return template.query(sql, param, boardRowMapper);
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
    public int adjustExistingSteps(Long bgroup, int newStep) {
        String sql = """
            UPDATE boards 
            SET step = step + 1 
            WHERE bgroup = :bgroup AND step >= :newStep
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bgroup", bgroup)
                .addValue("newStep", newStep);
        
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int countByBcategoryAndTitleContaining(Long bcategory, String keyword) {
        String sql = "SELECT COUNT(*) FROM boards WHERE bcategory = :bcategory AND title LIKE '%' || :keyword || '%'";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", bcategory)
                .addValue("keyword", keyword);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Boards> findByBcategoryAndTitleContainingWithPaging(Long bcategory, String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM boards 
            WHERE bcategory = :bcategory AND title LIKE '%' || :keyword || '%' 
            ORDER BY bgroup DESC, step ASC, cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("bcategory", bcategory)
                .addValue("keyword", keyword)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
        return template.query(sql, param, boardRowMapper);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementLikeCount(Long boardId) {
        String sql = "UPDATE boards SET like_count = like_count + 1 WHERE board_id = :boardId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int decrementLikeCount(Long boardId) {
        String sql = "UPDATE boards SET like_count = like_count - 1 WHERE board_id = :boardId AND like_count > 0";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementDislikeCount(Long boardId) {
        String sql = "UPDATE boards SET dislike_count = dislike_count + 1 WHERE board_id = :boardId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int decrementDislikeCount(Long boardId) {
        String sql = "UPDATE boards SET dislike_count = dislike_count - 1 WHERE board_id = :boardId AND dislike_count > 0";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId);
        
        return template.update(sql, param);
    }
} 