package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.LikeDislike;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeDislikeDAOImpl implements LikeDislikeDAO {
    private final NamedParameterJdbcTemplate template;

    private final RowMapper<LikeDislike> rowMapper = (ResultSet rs, int rowNum) -> {
        LikeDislike ld = new LikeDislike();
        ld.setLikeDislikeId(rs.getLong("like_dislike_id"));
        ld.setTargetType(rs.getString("target_type"));
        ld.setTargetId(rs.getLong("target_id"));
        ld.setMemberId(rs.getLong("member_id"));
        ld.setLikeType(rs.getString("like_type"));
        ld.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
        ld.setUdate(rs.getTimestamp("udate").toLocalDateTime());
        return ld;
    };

    @Override
    public Long save(LikeDislike likeDislike) {
        String sql = """
            INSERT INTO like_dislike (like_dislike_id, target_type, target_id, member_id, like_type, cdate, udate)
            VALUES (seq_like_dislike_id.nextval, :targetType, :targetId, :memberId, :likeType, SYSTIMESTAMP, SYSTIMESTAMP)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, new BeanPropertySqlParameterSource(likeDislike), keyHolder, new String[]{"like_dislike_id"});
        Number id = keyHolder.getKey();
        if (id == null) throw new IllegalStateException("Failed to retrieve generated like_dislike_id");
        return id.longValue();
    }

    @Override
    public int delete(Long likeDislikeId) {
        String sql = "DELETE FROM like_dislike WHERE like_dislike_id = :likeDislikeId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("likeDislikeId", likeDislikeId);
        return template.update(sql, param);
    }

    @Override
    public Optional<LikeDislike> findById(Long likeDislikeId) {
        String sql = "SELECT * FROM like_dislike WHERE like_dislike_id = :likeDislikeId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("likeDislikeId", likeDislikeId);
        List<LikeDislike> result = template.query(sql, param, rowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public int countByTarget(String targetType, Long targetId, String likeType) {
        String sql = "SELECT COUNT(*) FROM like_dislike WHERE target_type = :targetType AND target_id = :targetId AND like_type = :likeType";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId)
                .addValue("likeType", likeType);
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Optional<LikeDislike> findByTargetAndMember(String targetType, Long targetId, Long memberId) {
        String sql = "SELECT * FROM like_dislike WHERE target_type = :targetType AND target_id = :targetId AND member_id = :memberId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId)
                .addValue("memberId", memberId);
        List<LikeDislike> result = template.query(sql, param, rowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<LikeDislike> findAllByTarget(String targetType, Long targetId) {
        String sql = "SELECT * FROM like_dislike WHERE target_type = :targetType AND target_id = :targetId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId);
        return template.query(sql, param, rowMapper);
    }
} 