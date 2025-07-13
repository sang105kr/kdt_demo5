package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.LikeDislike;
import com.kh.demo.domain.common.dto.LikeDislikeDTO;
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
 * 좋아요/싫어요 데이터 접근 객체 구현체
 */
@Slf4j
@RequiredArgsConstructor
@Repository
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
     * 좋아요/싫어요 등록
     */
    @Override
    public Long save(LikeDislike likeDislike) {
        String sql = """
            INSERT INTO like_dislike (like_dislike_id, target_type, target_id, member_id, like_type, cdate, udate) 
            VALUES (seq_like_dislike_id.nextval, :targetType, :targetId, :memberId, :likeType, SYSTIMESTAMP, SYSTIMESTAMP) 
            """;

        SqlParameterSource param = new BeanPropertySqlParameterSource(likeDislike);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"like_dislike_id"});
        Number likeDislikeIdNumber = keyHolder.getKey();
        if (likeDislikeIdNumber == null) {
            throw new IllegalStateException("Failed to retrieve generated like_dislike_id");
        }
        return likeDislikeIdNumber.longValue();
    }

    /**
     * 좋아요/싫어요 수정
     */
    @Override
    public int updateById(Long likeDislikeId, LikeDislike likeDislike) {
        String sql = """
            UPDATE like_dislike 
            SET target_type = :targetType, target_id = :targetId, member_id = :memberId, 
                like_type = :likeType, udate = SYSTIMESTAMP
            WHERE like_dislike_id = :likeDislikeId 
            """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", likeDislike.getTargetType())
                .addValue("targetId", likeDislike.getTargetId())
                .addValue("memberId", likeDislike.getMemberId())
                .addValue("likeType", likeDislike.getLikeType())
                .addValue("likeDislikeId", likeDislikeId);

        return template.update(sql, param);
    }

    /**
     * 좋아요/싫어요 ID로 조회
     */
    @Override
    public Optional<LikeDislike> findById(Long likeDislikeId) {
        String sql = """
            SELECT like_dislike_id, target_type, target_id, member_id, like_type, cdate, udate
            FROM like_dislike 
            WHERE like_dislike_id = :likeDislikeId 
            """;

        SqlParameterSource param = new MapSqlParameterSource().addValue("likeDislikeId", likeDislikeId);

        try {
            LikeDislike likeDislike = template.queryForObject(sql, param, likeDislikeRowMapper);
            return Optional.of(likeDislike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 대상과 회원으로 조회
     */
    @Override
    public Optional<LikeDislike> findByTargetAndMember(String targetType, Long targetId, Long memberId) {
        String sql = """
            SELECT like_dislike_id, target_type, target_id, member_id, like_type, cdate, udate
            FROM like_dislike 
            WHERE target_type = :targetType AND target_id = :targetId AND member_id = :memberId 
            """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId)
                .addValue("memberId", memberId);

        try {
            LikeDislike likeDislike = template.queryForObject(sql, param, likeDislikeRowMapper);
            return Optional.of(likeDislike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 좋아요/싫어요 통계 조회
     */
    @Override
    public LikeDislikeDTO getLikeDislikeStats(String targetType, Long targetId, Long memberId) {
        // 먼저 해당 회원의 평가 정보를 조회
        Optional<LikeDislike> userEvaluation = findByTargetAndMember(targetType, targetId, memberId);
        
        if (userEvaluation.isPresent()) {
            LikeDislike likeDislike = userEvaluation.get();
            LikeDislikeDTO dto = new LikeDislikeDTO();
            dto.setLikeDislikeId(likeDislike.getLikeDislikeId());
            dto.setTargetType(likeDislike.getTargetType());
            dto.setTargetId(likeDislike.getTargetId());
            dto.setMemberId(likeDislike.getMemberId());
            dto.setLikeType(likeDislike.getLikeType());
            dto.setCdate(likeDislike.getCdate());
            dto.setUdate(likeDislike.getUdate());
            return dto;
        }
        
        // 평가 정보가 없으면 null 반환
        return null;
    }

    /**
     * 모든 좋아요/싫어요 조회
     */
    @Override
    public List<LikeDislike> findAll() {
        String sql = """
            SELECT like_dislike_id, target_type, target_id, member_id, like_type, cdate, udate
            FROM like_dislike 
            ORDER BY like_dislike_id DESC 
            """;

        return template.query(sql, likeDislikeRowMapper);
    }

    /**
     * 좋아요/싫어요 삭제
     */
    @Override
    public int deleteById(Long likeDislikeId) {
        String sql = """
            DELETE FROM like_dislike 
            WHERE like_dislike_id = :likeDislikeId 
            """;

        Map<String, Long> param = Map.of("likeDislikeId", likeDislikeId);
        return template.update(sql, param);
    }

    /**
     * 총 개수 조회
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(like_dislike_id) FROM like_dislike ";
        SqlParameterSource param = new MapSqlParameterSource();
        Long count = template.queryForObject(sql, param, Long.class);
        return count != null ? count.intValue() : 0;
    }
    
    /**
     * 대상과 회원으로 삭제
     */
    @Override
    public int deleteByTargetAndMember(String targetType, Long targetId, Long memberId) {
        String sql = """
            DELETE FROM like_dislike 
            WHERE target_type = :targetType AND target_id = :targetId AND member_id = :memberId 
            """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("targetType", targetType)
                .addValue("targetId", targetId)
                .addValue("memberId", memberId);

        return template.update(sql, param);
    }
    
    /**
     * 좋아요/싫어요 수정 (update 메서드 추가)
     */
    @Override
    public int update(LikeDislike likeDislike) {
        return updateById(likeDislike.getLikeDislikeId(), likeDislike);
    }
} 