package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.entity.PasswordResetToken;
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
public class PasswordResetTokenDAOImpl implements PasswordResetTokenDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    // RowMapper 정의
    private final RowMapper<PasswordResetToken> tokenRowMapper = (rs, rowNum) -> {
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenId(rs.getLong("token_id"));
        token.setEmail(rs.getString("email"));
        token.setToken(rs.getString("token"));
        token.setExpiryDate(rs.getObject("expiry_date", LocalDateTime.class));
        token.setStatus(rs.getString("status"));
        token.setCdate(rs.getObject("cdate", LocalDateTime.class));
        token.setUdate(rs.getObject("udate", LocalDateTime.class));
        return token;
    };
    
    @Override
    public Long save(PasswordResetToken token) {
        String sql = """
            INSERT INTO password_reset_tokens (email, token, expiry_date, status, cdate, udate)
            VALUES (seq_password_reset_token_id.nextval, :email, :token, :expiryDate, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("email", token.getEmail())
            .addValue("token", token.getToken())
            .addValue("expiryDate", token.getExpiryDate())
            .addValue("status", token.getStatus() != null ? token.getStatus() : "ACTIVE");
        
        template.update(sql, params);
        
        // 생성된 token_id 조회
        String selectSql = "SELECT seq_password_reset_token_id.currval FROM dual";
        Long tokenId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);
        
        if (tokenId == null) {
            throw new IllegalStateException("Failed to retrieve generated token_id");
        }
        return tokenId;
    }
    
    @Override
    public int updateById(Long tokenId, PasswordResetToken token) {
        String sql = """
            UPDATE password_reset_tokens 
            SET email = :email, token = :token, expiry_date = :expiryDate, 
                status = :status, udate = SYSTIMESTAMP
            WHERE token_id = :tokenId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tokenId", tokenId)
            .addValue("email", token.getEmail())
            .addValue("token", token.getToken())
            .addValue("expiryDate", token.getExpiryDate())
            .addValue("status", token.getStatus());
        
        return template.update(sql, params);
    }
    
    @Override
    public int deleteById(Long tokenId) {
        String sql = "DELETE FROM password_reset_tokens WHERE token_id = :tokenId";
        MapSqlParameterSource params = new MapSqlParameterSource("tokenId", tokenId);
        return template.update(sql, params);
    }
    
    @Override
    public Optional<PasswordResetToken> findById(Long tokenId) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token_id = :tokenId";
        
        MapSqlParameterSource params = new MapSqlParameterSource("tokenId", tokenId);
        
        try {
            PasswordResetToken token = template.queryForObject(sql, params, tokenRowMapper);
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<PasswordResetToken> findAll() {
        String sql = "SELECT * FROM password_reset_tokens ORDER BY cdate DESC";
        return template.query(sql, tokenRowMapper);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM password_reset_tokens";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    @Override
    public Optional<PasswordResetToken> findByEmailAndActive(String email) {
        String sql = """
            SELECT * FROM password_reset_tokens 
            WHERE email = :email AND status = 'ACTIVE' AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        
        try {
            PasswordResetToken token = template.queryForObject(sql, params, tokenRowMapper);
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token = :token";
        
        MapSqlParameterSource params = new MapSqlParameterSource("token", token);
        
        try {
            PasswordResetToken resetToken = template.queryForObject(sql, params, tokenRowMapper);
            return Optional.ofNullable(resetToken);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<PasswordResetToken> findExpiredTokens() {
        String sql = "SELECT * FROM password_reset_tokens WHERE expiry_date <= SYSTIMESTAMP AND status = 'ACTIVE'";
        return template.query(sql, tokenRowMapper);
    }
    
    @Override
    public int updateStatus(Long tokenId, String status) {
        String sql = "UPDATE password_reset_tokens SET status = :status, udate = SYSTIMESTAMP WHERE token_id = :tokenId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tokenId", tokenId)
            .addValue("status", status);
        
        return template.update(sql, params);
    }
    
    @Override
    public int deactivateByEmail(String email) {
        String sql = "UPDATE password_reset_tokens SET status = 'USED', udate = SYSTIMESTAMP WHERE email = :email AND status = 'ACTIVE'";
        
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        return template.update(sql, params);
    }
} 