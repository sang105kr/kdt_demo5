package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.entity.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
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
 * 토큰 데이터 접근 객체 구현체
 * NamedJdbcTemplate을 사용하여 토큰의 CRUD 기능을 구현합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenDAOImpl implements TokenDAO {
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final CodeSVC codeSVC;  // CodeCache 대신 CodeSVC 사용
    
    private Token tokenRowMapper(ResultSet rs, int rowNum) throws SQLException {
        Token token = new Token();
        token.setTokenId(rs.getLong("token_id"));
        token.setEmail(rs.getString("email"));
        token.setTokenType(rs.getString("token_type"));
        token.setTokenValue(rs.getString("token_value"));
        token.setExpiryDate(rs.getObject("expiry_date", LocalDateTime.class));
        token.setStatus(rs.getLong("status"));
        token.setCdate(rs.getObject("cdate", LocalDateTime.class));
        token.setUdate(rs.getObject("udate", LocalDateTime.class));
        return token;
    }
    
    @Override
    public Long save(Token token) {
        String sql = """
            INSERT INTO token (token_id, email, token_value, token_type, status, expires_at, created_at, updated_at)
            VALUES (seq_token_id.nextval, :email, :tokenValue, :tokenType, :status, :expiresAt, :createdAt, :updatedAt)
            """;
        
        // 기본값 설정
        if (token.getStatus() == null) {
            Long defaultStatus = codeSVC.getCodeId("TOKEN_STATUS", "ACTIVE");
            token.setStatus(defaultStatus);
        }
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", token.getEmail())
                .addValue("tokenType", token.getTokenType())
                .addValue("tokenValue", token.getTokenValue())
                .addValue("expiryDate", token.getExpiryDate())
                .addValue("status", token.getStatus())
                .addValue("expiresAt", token.getExpiryDate())
                .addValue("createdAt", token.getCdate())
                .addValue("updatedAt", token.getUdate());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, param, keyHolder, new String[]{"token_id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("토큰 저장 실패: 키 생성 실패");
        }
        
        log.info("토큰 저장 완료: tokenId={}", key.longValue());
        return key.longValue();
    }
    
    @Override
    public Optional<Token> findById(Long tokenId) {
        String sql = "SELECT * FROM tokens WHERE token_id = :tokenId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("tokenId", tokenId);
        
        List<Token> results = namedParameterJdbcTemplate.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Token> findActiveByEmailAndType(String email, String tokenType) {
        String sql = """
            SELECT * FROM tokens 
            WHERE email = :email 
            AND token_type = :tokenType 
            AND status = (SELECT code_id FROM code WHERE gcode='TOKEN_STATUS' AND code='ACTIVE') 
            AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("tokenType", tokenType);
        
        List<Token> results = namedParameterJdbcTemplate.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Token> findActiveByEmailAndValue(String email, String tokenValue) {
        String sql = """
            SELECT * FROM tokens 
            WHERE email = :email 
            AND token_value = :tokenValue 
            AND status = (SELECT code_id FROM code WHERE gcode='TOKEN_STATUS' AND code='ACTIVE') 
            AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("tokenValue", tokenValue);
        
        List<Token> results = namedParameterJdbcTemplate.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<Token> findActiveByValue(String tokenValue) {
        String sql = """
            SELECT * FROM tokens 
            WHERE token_value = :tokenValue 
            AND status = (SELECT code_id FROM code WHERE gcode='TOKEN_STATUS' AND code='ACTIVE') 
            AND expiry_date > SYSTIMESTAMP
            ORDER BY cdate DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("tokenValue", tokenValue);
        
        List<Token> results = namedParameterJdbcTemplate.query(sql, param, this::tokenRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<Token> findByEmail(String email) {
        String sql = "SELECT * FROM tokens WHERE email = :email ORDER BY cdate DESC";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("email", email);
        return namedParameterJdbcTemplate.query(sql, param, this::tokenRowMapper);
    }
    
    @Override
    public List<Token> findByType(String tokenType) {
        String sql = "SELECT * FROM tokens WHERE token_type = :tokenType ORDER BY cdate DESC";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("tokenType", tokenType);
        return namedParameterJdbcTemplate.query(sql, param, this::tokenRowMapper);
    }
    
    @Override
    public int deactivateByEmailAndType(String email, String tokenType) {
        String sql = """
            UPDATE tokens 
            SET status = :verifiedStatus, udate = SYSTIMESTAMP 
            WHERE email = :email 
            AND token_type = :tokenType 
            AND status = :activeStatus
            """;
        
        Long verifiedStatus = codeSVC.getCodeId("TOKEN_STATUS", "VERIFIED");
        Long activeStatus = codeSVC.getCodeId("TOKEN_STATUS", "ACTIVE");
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("tokenType", tokenType)
                .addValue("verifiedStatus", verifiedStatus)
                .addValue("activeStatus", activeStatus);
        
        return namedParameterJdbcTemplate.update(sql, param);
    }
    
    @Override
    public int deactivateById(Long tokenId) {
        String sql = """
            UPDATE tokens 
            SET status = :verifiedStatus, udate = SYSTIMESTAMP 
            WHERE token_id = :tokenId
            """;
        
        Long verifiedStatus = codeSVC.getCodeId("TOKEN_STATUS", "VERIFIED");
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("tokenId", tokenId)
                .addValue("verifiedStatus", verifiedStatus);
        
        return namedParameterJdbcTemplate.update(sql, param);
    }
    
    @Override
    public int deactivateExpiredTokens() {
        String sql = """
            UPDATE tokens 
            SET status = :expiredStatus, udate = SYSTIMESTAMP 
            WHERE status = :activeStatus 
            AND expiry_date <= SYSTIMESTAMP
            """;
        
        Long expiredStatus = codeSVC.getCodeId("TOKEN_STATUS", "EXPIRED");
        Long activeStatus = codeSVC.getCodeId("TOKEN_STATUS", "ACTIVE");
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("expiredStatus", expiredStatus)
                .addValue("activeStatus", activeStatus);
        
        return namedParameterJdbcTemplate.update(sql, param);
    }
    
    @Override
    public int updateStatus(Long tokenId, Long statusCodeId) {
        String sql = "UPDATE tokens SET status = :status, udate = SYSTIMESTAMP WHERE token_id = :tokenId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("tokenId", tokenId)
                .addValue("status", statusCodeId);
        return namedParameterJdbcTemplate.update(sql, param);
    }
    
    @Override
    public int deleteByEmail(String email) {
        String sql = "DELETE FROM tokens WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("email", email);
        return namedParameterJdbcTemplate.update(sql, param);
    }
    
    @Override
    public int deleteById(Long tokenId) {
        String sql = "DELETE FROM tokens WHERE token_id = :tokenId";
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("tokenId", tokenId);
        return namedParameterJdbcTemplate.update(sql, param);
    }
} 