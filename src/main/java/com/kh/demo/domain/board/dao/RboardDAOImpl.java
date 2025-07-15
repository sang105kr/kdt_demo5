package com.kh.demo.domain.board.dao;

import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.domain.shared.base.BaseDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 댓글 데이터 접근 객체 구현체
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
        reply.setLikeCount(rs.getInt("like_count"));
        reply.setDislikeCount(rs.getInt("dislike_count"));
        reply.setStatus(rs.getString("status"));
        reply.setCdate(rs.getObject("cdate", LocalDateTime.class));
        reply.setUdate(rs.getObject("udate", LocalDateTime.class));
        return reply;
    };

    /**
     * 댓글 등록
     */
    @Override
    public Long save(Replies reply) {
        String sql = """
            INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status, cdate, udate)
            VALUES (seq_reply_id.nextval, :boardId, :email, :nickname, :rcontent, :parentId, :rgroup, :rstep, :rindent, :status, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        SqlParameterSource param = new BeanPropertySqlParameterSource(reply);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"reply_id"});
        Number replyIdNumber = keyHolder.getKey();
        if (replyIdNumber == null) {
            throw new IllegalStateException("Failed to retrieve generated reply_id");
        }
        
        return replyIdNumber.longValue();
    }

    /**
     * 댓글 ID로 조회
     */
    @Override
    public Optional<Replies> findById(Long replyId) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE reply_id = :replyId 
            """;
        
        SqlParameterSource param = new MapSqlParameterSource().addValue("replyId", replyId);
        
        try {
            Replies reply = template.queryForObject(sql, param, replyRowMapper);
            return Optional.of(reply);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 모든 댓글 조회
     */
    @Override
    public List<Replies> findAll() {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            ORDER BY reply_id DESC 
            """;
        
        return template.query(sql, replyRowMapper);
    }

    /**
     * 댓글 수정
     */
    @Override
    public int updateById(Long replyId, Replies reply) {
        String sql = """
            UPDATE replies 
            SET board_id = :boardId, email = :email, nickname = :nickname, rcontent = :rcontent, 
                parent_id = :parentId, rgroup = :rgroup, rstep = :rstep, rindent = :rindent, 
                status = :status, udate = SYSTIMESTAMP
            WHERE reply_id = :replyId
            """;
        
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", reply.getBoardId())
                .addValue("email", reply.getEmail())
                .addValue("nickname", reply.getNickname())
                .addValue("rcontent", reply.getRcontent())
                .addValue("parentId", reply.getParentId())
                .addValue("rgroup", reply.getRgroup())
                .addValue("rstep", reply.getRstep())
                .addValue("rindent", reply.getRindent())
                .addValue("status", reply.getStatus())
                .addValue("replyId", replyId);
        
        return template.update(sql, param);
    }

    /**
     * 댓글 삭제
     */
    @Override
    public int deleteById(Long replyId) {
        String sql = """
            DELETE FROM replies 
            WHERE reply_id = :replyId 
            """;
        
        Map<String, Long> param = Map.of("replyId", replyId);
        return template.update(sql, param);
    }

    /**
     * 총 개수 조회
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(reply_id) FROM replies ";
        SqlParameterSource param = new MapSqlParameterSource();
        Long count = template.queryForObject(sql, param, Long.class);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 게시글별 댓글 조회
     */
    @Override
    public List<Replies> findByBoardId(Long boardId) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE board_id = :boardId 
            ORDER BY rgroup DESC, rstep ASC, cdate DESC
            """;
        
        Map<String, Long> param = Map.of("boardId", boardId);
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * 게시글별 댓글 페이징 조회
     */
    @Override
    public List<Replies> findByBoardIdWithPaging(Long boardId, int pageNo, int pageSize) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE board_id = :boardId 
            ORDER BY rgroup DESC, rstep ASC, cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        
        int offset = (pageNo - 1) * pageSize;
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("boardId", boardId)
                .addValue("pageSize", pageSize)
                .addValue("offset", offset);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * 이메일별 댓글 조회
     */
    @Override
    public List<Replies> findByEmail(String email) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE email = :email 
            ORDER BY cdate DESC
            """;
        
        Map<String, String> param = Map.of("email", email);
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * 이메일별 댓글 페이징 조회
     */
    @Override
    public List<Replies> findByEmailWithPaging(String email, int pageNo, int pageSize) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE email = :email 
            ORDER BY cdate DESC
            OFFSET (:pageNo - 1) * :pageSize ROWS 
            FETCH NEXT :pageSize ROWS ONLY 
            """;
        
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("pageNo", pageNo)
                .addValue("pageSize", pageSize);
        
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * 부모 댓글별 답글 조회
     */
    @Override
    public List<Replies> findByParentId(Long parentId) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE parent_id = :parentId 
            ORDER BY cdate ASC
            """;
        
        Map<String, Long> param = Map.of("parentId", parentId);
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * 댓글 그룹 조회
     */
    @Override
    public List<Replies> findByRgroup(Long rgroup) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE rgroup = :rgroup 
            ORDER BY rstep ASC, cdate DESC
            """;
        
        Map<String, Long> param = Map.of("rgroup", rgroup);
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * 댓글 내용 검색
     */
    @Override
    public List<Replies> findByRcontentContaining(String keyword) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE rcontent LIKE '%' || :keyword || '%' 
            ORDER BY cdate DESC
            """;
        
        Map<String, String> param = Map.of("keyword", keyword);
        return template.query(sql, param, replyRowMapper);
    }

    /**
     * 댓글 내용 검색 페이징 조회
     */
    @Override
    public List<Replies> findByRcontentContainingWithPaging(String keyword, int pageNo, int pageSize) {
        String sql = """
            SELECT reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, like_count, dislike_count, status, cdate, udate
            FROM replies 
            WHERE rcontent LIKE '%' || :keyword || '%' 
            ORDER BY cdate DESC
            OFFSET (:pageNo - 1) * :pageSize ROWS 
            FETCH NEXT :pageSize ROWS ONLY 
            """;
        
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword)
                .addValue("pageNo", pageNo)
                .addValue("pageSize", pageSize);
        
        return template.query(sql, param, replyRowMapper);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementLikeCount(Long replyId) {
        String sql = "UPDATE replies SET like_count = like_count + 1 WHERE reply_id = :replyId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("replyId", replyId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int decrementLikeCount(Long replyId) {
        String sql = "UPDATE replies SET like_count = like_count - 1 WHERE reply_id = :replyId AND like_count > 0";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("replyId", replyId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementDislikeCount(Long replyId) {
        String sql = "UPDATE replies SET dislike_count = dislike_count + 1 WHERE reply_id = :replyId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("replyId", replyId);
        
        return template.update(sql, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int decrementDislikeCount(Long replyId) {
        String sql = "UPDATE replies SET dislike_count = dislike_count - 1 WHERE reply_id = :replyId AND dislike_count > 0";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("replyId", replyId);
        
        return template.update(sql, param);
    }
} 