package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.entity.LikeDislike;
import com.kh.demo.domain.dto.LikeDislikeDTO;
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
import java.util.Optional;

/**
 * 호감/비호감 데이터 접근 객체 구현체
 * NamedJdbcTemplate을 사용하여 호감/비호감의 CRUD 및 통계 기능을 구현합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeDislikeDAOImpl implements LikeDislikeDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private final RowMapper<LikeDislike> likeDislikeRowMapper = (ResultSet rs, int rowNum) -> {
        LikeDislike likeDislike = new LikeDislike();
        likeDislike.setLikeDislikeId(rs.getLong("like_dislike_id"));
        likeDislike.setTargetType(rs.getString("target_type"));
        likeDislike.setTargetId(rs.getLong("target_id"));
        likeDislike.setMemberId(rs.getLong("member_id"));
        likeDislike.setLikeType(rs.getString("like_type"));
        likeDislike.setCdate(rs.getObject("cdate", LocalDateTime.class));
        likeDislike.setUdate(rs.getObject("udate", LocalDateTime.class));
        return likeDislike;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public Long save(LikeDislike likeDislike) {
        String sql = """
            INSERT INTO like_dislike (like_dislike_id, target_type, target_id, member_id, like_type, cdate, udate)
            VALUES (seq_like_dislike_id.nextval, :targetType, :targetId, :memberId, :likeType, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, new BeanPropertySqlParameterSource(likeDislike), keyHolder);
        
        return keyHolder.getKey().longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(LikeDislike likeDislike) {
        String sql = """
            UPDATE like_dislike 
            SET like_type = :likeType, udate = SYSTIMESTAMP
            WHERE like_dislike_id = :likeDislikeId
            """;
        
        return template.update(sql, new BeanPropertySqlParameterSource(likeDislike));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Long likeDislikeId) {
        String sql = "DELETE FROM like_dislike WHERE like_dislike_id = :likeDislikeId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("likeDislikeId", likeDislikeId);
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LikeDislike> findById(Long likeDislikeId) {
        String sql = "SELECT * FROM like_dislike WHERE like_dislike_id = :likeDislikeId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("likeDislikeId", likeDislikeId);
        
        try {
            LikeDislike likeDislike = template.queryForObject(sql, param, likeDislikeRowMapper);
            return Optional.ofNullable(likeDislike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LikeDislike> findByTargetAndMember(String targetType, Long targetId, Long memberId) {
        String sql = """
            SELECT * FROM like_dislike 
            WHERE target_type = :targetType AND target_id = :targetId AND member_id = :memberId
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId)
                .addValue("memberId", memberId);
        
        try {
            LikeDislike likeDislike = template.queryForObject(sql, param, likeDislikeRowMapper);
            return Optional.ofNullable(likeDislike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LikeDislikeDTO getLikeDislikeStats(String targetType, Long targetId, Long memberId) {
        String sql = """
            SELECT 
                :targetType as target_type,
                :targetId as target_id,
                COALESCE(SUM(CASE WHEN like_type = 'LIKE' THEN 1 ELSE 0 END), 0) as like_count,
                COALESCE(SUM(CASE WHEN like_type = 'DISLIKE' THEN 1 ELSE 0 END), 0) as dislike_count,
                (SELECT like_type FROM like_dislike 
                 WHERE target_type = :targetType AND target_id = :targetId AND member_id = :memberId) as user_like_type
            FROM like_dislike 
            WHERE target_type = :targetType AND target_id = :targetId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId)
                .addValue("memberId", memberId);
        
        return template.queryForObject(sql, param, (rs, rowNum) -> {
            LikeDislikeDTO dto = new LikeDislikeDTO();
            dto.setTargetType(rs.getString("target_type"));
            dto.setTargetId(rs.getLong("target_id"));
            dto.setLikeCount(rs.getLong("like_count"));
            dto.setDislikeCount(rs.getLong("dislike_count"));
            dto.setUserLikeType(rs.getString("user_like_type"));
            return dto;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteByTargetAndMember(String targetType, Long targetId, Long memberId) {
        String sql = """
            DELETE FROM like_dislike 
            WHERE target_type = :targetType AND target_id = :targetId AND member_id = :memberId
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId)
                .addValue("memberId", memberId);
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteByTarget(String targetType, Long targetId) {
        String sql = """
            DELETE FROM like_dislike 
            WHERE target_type = :targetType AND target_id = :targetId
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId);
        
        return template.update(sql, param);
    }
} 