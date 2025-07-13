package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.entity.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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

@Slf4j
@Repository
@RequiredArgsConstructor
public class CodeDAOImpl implements CodeDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private final RowMapper<Code> codeRowMapper = (ResultSet rs, int rowNum) -> {
        Code code = new Code();
        code.setCodeId(rs.getLong("code_id"));
        code.setGcode(rs.getString("gcode"));
        code.setCode(rs.getString("code"));
        code.setDecode(rs.getString("decode"));
        code.setPcode(rs.getLong("pcode"));
        code.setCodePath(rs.getString("code_path"));
        code.setCodeLevel(rs.getInt("code_level"));
        code.setSortOrder(rs.getInt("sort_order"));
        code.setUseYn(rs.getString("use_yn"));
        code.setCdate(rs.getObject("cdate", LocalDateTime.class));
        code.setUdate(rs.getObject("udate", LocalDateTime.class));
        return code;
    };

    @Override
    public Long save(Code code) {
        String sql = """
            INSERT INTO code (code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate)
            VALUES (seq_code_id.nextval, :gcode, :code, :decode, :pcode, :codePath, :codeLevel, :sortOrder, :useYn, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, new BeanPropertySqlParameterSource(code), keyHolder);
        
        return keyHolder.getKey().longValue();
    }

    @Override
    public int update(Code code) {
        String sql = """
            UPDATE code 
            SET gcode = :gcode, code = :code, decode = :decode, pcode = :pcode, 
                code_path = :codePath, code_level = :codeLevel, sort_order = :sortOrder, 
                use_yn = :useYn, udate = SYSTIMESTAMP
            WHERE code_id = :codeId
            """;
        
        return template.update(sql, new BeanPropertySqlParameterSource(code));
    }

    @Override
    public int delete(Long codeId) {
        String sql = "DELETE FROM code WHERE code_id = :codeId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("codeId", codeId);
        
        return template.update(sql, param);
    }

    @Override
    public Optional<Code> findById(Long codeId) {
        String sql = "SELECT * FROM code WHERE code_id = :codeId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("codeId", codeId);
        
        try {
            Code code = template.queryForObject(sql, param, codeRowMapper);
            return Optional.ofNullable(code);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Code> findByGcode(String gcode) {
        String sql = """
            SELECT * FROM code 
            WHERE gcode = :gcode 
            ORDER BY sort_order, code_id
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gcode", gcode);
        
        return template.query(sql, param, codeRowMapper);
    }

    @Override
    public List<Code> findActiveByGcode(String gcode) {
        String sql = """
            SELECT * FROM code 
            WHERE gcode = :gcode AND use_yn = 'Y'
            ORDER BY sort_order, code_id
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gcode", gcode);
        
        return template.query(sql, param, codeRowMapper);
    }

    @Override
    public List<Code> findByPcode(Long pcode) {
        String sql = """
            SELECT * FROM code 
            WHERE pcode = :pcode 
            ORDER BY sort_order, code_id
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("pcode", pcode);
        
        return template.query(sql, param, codeRowMapper);
    }

    @Override
    public List<Code> findByCodePath(String codePath) {
        String sql = """
            SELECT * FROM code 
            WHERE code_path LIKE :codePath || '%'
            ORDER BY code_path, sort_order
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("codePath", codePath);
        
        return template.query(sql, param, codeRowMapper);
    }

    @Override
    public List<Code> findAll() {
        String sql = "SELECT * FROM code ORDER BY gcode, sort_order, code_id";
        return template.query(sql, codeRowMapper);
    }

    @Override
    public boolean existsByGcodeAndCode(String gcode, String code) {
        String sql = "SELECT COUNT(*) FROM code WHERE gcode = :gcode AND code = :code";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gcode", gcode)
                .addValue("code", code);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public int countByGcode(String gcode) {
        String sql = "SELECT COUNT(*) FROM code WHERE gcode = :gcode";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gcode", gcode);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }
} 