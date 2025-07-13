package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.Code;
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
 * 코드 데이터 접근 객체 구현체
 */
@Slf4j
@RequiredArgsConstructor
@Repository
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

    /**
     * 코드 등록
     */
    @Override
    public Long save(Code code) {
        String sql = """
            INSERT INTO code (code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate) 
            VALUES (seq_code_id.nextval, :gcode, :code, :decode, :pcode, :codePath, :codeLevel, :sortOrder, :useYn, SYSTIMESTAMP, SYSTIMESTAMP) 
            """;

        SqlParameterSource param = new BeanPropertySqlParameterSource(code);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"code_id"});
        Number codeIdNumber = keyHolder.getKey();
        if (codeIdNumber == null) {
            throw new IllegalStateException("Failed to retrieve generated code_id");
        }
        return codeIdNumber.longValue();
    }

    /**
     * 코드 ID로 조회
     */
    @Override
    public Optional<Code> findById(Long codeId) {
        String sql = """
            SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate
            FROM code 
            WHERE code_id = :codeId 
            """;

        SqlParameterSource param = new MapSqlParameterSource().addValue("codeId", codeId);

        try {
            Code code = template.queryForObject(sql, param, codeRowMapper);
            return Optional.of(code);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 그룹 코드별 조회
     */
    @Override
    public List<Code> findByGcode(String gcode) {
        String sql = """
            SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate
            FROM code 
            WHERE gcode = :gcode 
            ORDER BY sort_order, code_id 
            """;

        Map<String, String> param = Map.of("gcode", gcode);
        return template.query(sql, param, codeRowMapper);
    }

    /**
     * 활성화된 그룹 코드별 조회
     */
    @Override
    public List<Code> findActiveByGcode(String gcode) {
        String sql = """
            SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate
            FROM code 
            WHERE gcode = :gcode AND use_yn = 'Y' 
            ORDER BY sort_order, code_id 
            """;

        Map<String, String> param = Map.of("gcode", gcode);
        return template.query(sql, param, codeRowMapper);
    }

    /**
     * 부모 코드별 조회
     */
    @Override
    public List<Code> findByPcode(Long pcode) {
        String sql = """
            SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate
            FROM code 
            WHERE pcode = :pcode 
            ORDER BY sort_order, code_id 
            """;

        Map<String, Long> param = Map.of("pcode", pcode);
        return template.query(sql, param, codeRowMapper);
    }

    /**
     * 코드 경로별 조회
     */
    @Override
    public List<Code> findByCodePath(String codePath) {
        String sql = """
            SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate
            FROM code 
            WHERE code_path LIKE :codePath || '%' 
            ORDER BY sort_order, code_id 
            """;

        Map<String, String> param = Map.of("codePath", codePath);
        return template.query(sql, param, codeRowMapper);
    }

    /**
     * 모든 코드 조회
     */
    @Override
    public List<Code> findAll() {
        String sql = """
            SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate
            FROM code 
            ORDER BY gcode, sort_order, code_id 
            """;

        return template.query(sql, codeRowMapper);
    }

    /**
     * 코드 삭제
     */
    @Override
    public int deleteById(Long codeId) {
        String sql = """
            DELETE FROM code 
            WHERE code_id = :codeId 
            """;

        Map<String, Long> param = Map.of("codeId", codeId);
        return template.update(sql, param);
    }

    /**
     * 코드 수정 (ID 기반)
     */
    @Override
    public int updateById(Long codeId, Code code) {
        String sql = """
            UPDATE code 
            SET gcode = :gcode, code = :code, decode = :decode, pcode = :pcode, 
                code_path = :codePath, code_level = :codeLevel, sort_order = :sortOrder, use_yn = :useYn, udate = SYSTIMESTAMP
            WHERE code_id = :codeId 
            """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("gcode", code.getGcode())
                .addValue("code", code.getCode())
                .addValue("decode", code.getDecode())
                .addValue("pcode", code.getPcode())
                .addValue("codePath", code.getCodePath())
                .addValue("codeLevel", code.getCodeLevel())
                .addValue("sortOrder", code.getSortOrder())
                .addValue("useYn", code.getUseYn())
                .addValue("codeId", codeId);

        return template.update(sql, param);
    }

    /**
     * 총 개수 조회
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(code_id) FROM code ";
        SqlParameterSource param = new MapSqlParameterSource();
        Long count = template.queryForObject(sql, param, Long.class);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 코드 존재 여부 확인
     */
    @Override
    public boolean existsByGcodeAndCode(String gcode, String code) {
        String sql = "SELECT COUNT(*) FROM code WHERE gcode = :gcode AND code = :code";
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("gcode", gcode)
                .addValue("code", code);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 그룹코드별 코드 개수 조회
     */
    @Override
    public int countByGcode(String gcode) {
        String sql = "SELECT COUNT(*) FROM code WHERE gcode = :gcode";
        SqlParameterSource param = new MapSqlParameterSource().addValue("gcode", gcode);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }
} 