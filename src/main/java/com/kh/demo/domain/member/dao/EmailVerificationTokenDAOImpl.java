package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.vo.EmailVerificationToken;
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
public class EmailVerificationTokenDAOImpl implements EmailVerificationTokenDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    // RowMapper 정의
    private final RowMapper<EmailVerificationToken> tokenRowMapper = (rs, rowNum) -> {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setTokenId(rs.getLong("token_id"));
        token.setEmail(rs.getString("email"));
        token.setVerificationCode(rs.getString("verification_code"));
        token.setExpiryDate(rs.getObject("expiry_date", LocalDateTime.class));
        token.setStatus(rs.getString("status"));
        token.setCdate(rs.getObject("cdate", LocalDateTime.class));
        token.setUdate(rs.getObject("udate", LocalDateTime.class));
        return token;
    };
    
    @Override
    public Long save(EmailVerificationToken token) {
        String sql = """
            INSERT INTO email_verification_tokens (email, verification_code, expiry_date, status, cdate, udate)
            VALUES (seq_email_verification_token_id.nextval, :email, :verificationCode, :expiryDate, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("email", token.getEmail())
            .addValue("verificationCode", token.getVerificationCode())
            .addValue("expiryDate", token.getExpiryDate())
            .addValue("status", token.getStatus() != null ? token.getStatus() : "ACTIVE");
        
        template.update(sql, params);
        
        // 생성된 token_id 조회
        String selectSql = "SELECT seq_email_verification_token_id.currval FROM dual";
        Long tokenId = template.queryForObject(selectSql, new MapSqlParameterSource(), Long.class);
        
        if (tokenId == null) {
            throw new IllegalStateException("Failed to retrieve generated token_id");
        }
        return tokenId;
    }
    
    @Override
    public int updateById(Long tokenId, EmailVerificationToken token) {
        String sql = """
            UPDATE email_verification_tokens 
            SET email = :email, verification_code = :verificationCode, expiry_date = :expiryDate, 
                status = :status, udate = SYSTIMESTAMP
            WHERE token_id = :tokenId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tokenId", tokenId)
            .addValue("email", token.getEmail())
            .addValue("verificationCode", token.getVerificationCode())
            .addValue("expiryDate", token.getExpiryDate())
            .addValue("status", token.getStatus());
        
        return template.update(sql, params);
    }
    
    @Override
    public int deleteById(Long tokenId) {
        String sql = "DELETE FROM email_verification_tokens WHERE token_id = :tokenId";
        MapSqlParameterSource params = new MapSqlParameterSource("tokenId", tokenId);
        return template.update(sql, params);
    }
    
    @Override
    public Optional<EmailVerificationToken> findById(Long tokenId) {
        String sql = "SELECT * FROM email_verification_tokens WHERE token_id = :tokenId";
        
        MapSqlParameterSource params = new MapSqlParameterSource("tokenId", tokenId);
        
        try {
            EmailVerificationToken token = template.queryForObject(sql, params, tokenRowMapper);
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<EmailVerificationToken> findAll() {
        String sql = "SELECT * FROM email_verification_tokens ORDER BY cdate DESC";
        return template.query(sql, tokenRowMapper);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM email_verification_tokens";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    @Override
    public Optional<EmailVerificationToken> findByEmailAndActive(String email) {
        String sql = """
            SELECT * FROM email_verification_tokens 
            WHERE email = :email AND status = 'ACTIVE' AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        
        try {
            EmailVerificationToken token = template.queryForObject(sql, params, tokenRowMapper);
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<EmailVerificationToken> findByVerificationCode(String verificationCode) {
        String sql = "SELECT * FROM email_verification_tokens WHERE verification_code = :verificationCode";
        
        MapSqlParameterSource params = new MapSqlParameterSource("verificationCode", verificationCode);
        
        try {
            EmailVerificationToken token = template.queryForObject(sql, params, tokenRowMapper);
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<EmailVerificationToken> findExpiredTokens() {
        String sql = "SELECT * FROM email_verification_tokens WHERE expiry_date <= SYSTIMESTAMP AND status = 'ACTIVE'";
        return template.query(sql, tokenRowMapper);
    }
    
    @Override
    public int updateStatus(Long tokenId, String status) {
        String sql = "UPDATE email_verification_tokens SET status = :status, udate = SYSTIMESTAMP WHERE token_id = :tokenId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tokenId", tokenId)
            .addValue("status", status);
        
        return template.update(sql, params);
    }
    
    @Override
    public int deactivateByEmail(String email) {
        String sql = "UPDATE email_verification_tokens SET status = 'VERIFIED', udate = SYSTIMESTAMP WHERE email = :email AND status = 'ACTIVE'";
        
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        return template.update(sql, params);
    }
} 