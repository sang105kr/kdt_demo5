package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.entity.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 토큰 DAO 구현체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenDAOImpl implements TokenDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    /**
     * 토큰 RowMapper
     */
    private Token tokenRowMapper(ResultSet rs, int rowNum) throws SQLException {
        Token token = new Token();
        token.setTokenId(rs.getLong("token_id"));
        token.setEmail(rs.getString("email"));
        token.setTokenType(rs.getString("token_type"));
        token.setTokenValue(rs.getString("token_value"));
        token.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
        token.setStatus(rs.getString("status"));
        token.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
        token.setUdate(rs.getTimestamp("udate").toLocalDateTime());
        return token;
    }
    
    @Override
    public Long save(Token token) {
        String sql = """
            INSERT INTO tokens (token_id, email, token_type, token_value, expiry_date, status, cdate, udate)
            VALUES (seq_token_id.nextval, :email, :tokenType, :tokenValue, :expiryDate, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", token.getEmail())
                .addValue("tokenType", token.getTokenType())
                .addValue("tokenValue", token.getTokenValue())
                .addValue("expiryDate", token.getExpiryDate())
                .addValue("status", token.getStatus() != null ? token.getStatus() : Token.TokenStatus.ACTIVE);
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"token_id"});
        
        return keyHolder.getKey().longValue();
    }
    
    @Override
    public Optional<Token> findById(Long tokenId) {
        String sql = "SELECT * FROM tokens WHERE token_id = :tokenId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("tokenId", tokenId);
        
        List<Token> results = template.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Token> findActiveByEmailAndType(String email, String tokenType) {
        String sql = """
            SELECT * FROM tokens 
            WHERE email = :email AND token_type = :tokenType AND status = 'ACTIVE' AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("tokenType", tokenType);
        
        List<Token> results = template.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Token> findActiveByEmailAndValue(String email, String tokenValue) {
        String sql = """
            SELECT * FROM tokens 
            WHERE email = :email AND token_value = :tokenValue AND status = 'ACTIVE' AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("tokenValue", tokenValue);
        
        List<Token> results = template.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Token> findActiveByValue(String tokenValue) {
        String sql = """
            SELECT * FROM tokens 
            WHERE token_value = :tokenValue AND status = 'ACTIVE' AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("tokenValue", tokenValue);
        
        List<Token> results = template.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<Token> findByEmail(String email) {
        String sql = "SELECT * FROM tokens WHERE email = :email ORDER BY cdate DESC";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("email", email);
        return template.query(sql, param, this::tokenRowMapper);
    }
    
    @Override
    public List<Token> findByType(String tokenType) {
        String sql = "SELECT * FROM tokens WHERE token_type = :tokenType ORDER BY cdate DESC";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("tokenType", tokenType);
        return template.query(sql, param, this::tokenRowMapper);
    }
    
    @Override
    public int deactivateByEmailAndType(String email, String tokenType) {
        String sql = """
            UPDATE tokens 
            SET status = 'VERIFIED', udate = SYSTIMESTAMP 
            WHERE email = :email AND token_type = :tokenType AND status = 'ACTIVE'
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("tokenType", tokenType);
        
        return template.update(sql, param);
    }
    
    @Override
    public int deactivateById(Long tokenId) {
        String sql = "UPDATE tokens SET status = 'VERIFIED', udate = SYSTIMESTAMP WHERE token_id = :tokenId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("tokenId", tokenId);
        return template.update(sql, param);
    }
    
    @Override
    public int deactivateExpiredTokens() {
        String sql = """
            UPDATE tokens 
            SET status = 'EXPIRED', udate = SYSTIMESTAMP 
            WHERE status = 'ACTIVE' AND expiry_date <= SYSTIMESTAMP
            """;
        
        return template.update(sql, new MapSqlParameterSource());
    }
    
    @Override
    public int updateStatus(Long tokenId, String status) {
        String sql = "UPDATE tokens SET status = :status, udate = SYSTIMESTAMP WHERE token_id = :tokenId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("tokenId", tokenId)
                .addValue("status", status);
        return template.update(sql, param);
    }
    
    @Override
    public int deleteByEmail(String email) {
        String sql = "DELETE FROM tokens WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("email", email);
        return template.update(sql, param);
    }
    
    @Override
    public int deleteById(Long tokenId) {
        String sql = "DELETE FROM tokens WHERE token_id = :tokenId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("tokenId", tokenId);
        return template.update(sql, param);
    }
} 