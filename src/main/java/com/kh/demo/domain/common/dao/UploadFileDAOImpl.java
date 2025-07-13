package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.UploadFile;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UploadFileDAOImpl implements UploadFileDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private final RowMapper<UploadFile> uploadFileRowMapper = (ResultSet rs, int rowNum) -> {
        UploadFile uploadFile = new UploadFile();
        uploadFile.setUploadfileId(rs.getLong("uploadfile_id"));
        uploadFile.setCode(rs.getLong("code"));
        uploadFile.setRid(rs.getString("rid"));
        uploadFile.setStoreFilename(rs.getString("store_filename"));
        uploadFile.setUploadFilename(rs.getString("upload_filename"));
        uploadFile.setFsize(rs.getString("fsize"));
        uploadFile.setFtype(rs.getString("ftype"));
        uploadFile.setCdate(rs.getObject("cdate", LocalDateTime.class));
        uploadFile.setUdate(rs.getObject("udate", LocalDateTime.class));
        return uploadFile;
    };

    @Override
    public Long save(UploadFile uploadFile) {
        String sql = """
            INSERT INTO uploadfile (uploadfile_id, code, rid, store_filename, upload_filename, fsize, ftype, cdate, udate)
            VALUES (seq_uploadfile_id.nextval, :code, :rid, :storeFilename, :uploadFilename, :fsize, :ftype, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, new BeanPropertySqlParameterSource(uploadFile), keyHolder);
        
        return keyHolder.getKey().longValue();
    }

    @Override
    public int update(UploadFile uploadFile) {
        String sql = """
            UPDATE uploadfile 
            SET code = :code, rid = :rid, store_filename = :storeFilename, 
                upload_filename = :uploadFilename, fsize = :fsize, ftype = :ftype, udate = SYSTIMESTAMP
            WHERE uploadfile_id = :uploadfileId
            """;
        
        return template.update(sql, new BeanPropertySqlParameterSource(uploadFile));
    }

    @Override
    public int delete(Long uploadfileId) {
        String sql = "DELETE FROM uploadfile WHERE uploadfile_id = :uploadfileId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("uploadfileId", uploadfileId);
        
        return template.update(sql, param);
    }

    @Override
    public Optional<UploadFile> findById(Long uploadfileId) {
        String sql = "SELECT * FROM uploadfile WHERE uploadfile_id = :uploadfileId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("uploadfileId", uploadfileId);
        
        try {
            UploadFile uploadFile = template.queryForObject(sql, param, uploadFileRowMapper);
            return Optional.ofNullable(uploadFile);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<UploadFile> findByCode(Long code) {
        String sql = """
            SELECT * FROM uploadfile 
            WHERE code = :code 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("code", code);
        
        return template.query(sql, param, uploadFileRowMapper);
    }

    @Override
    public List<UploadFile> findByRid(String rid) {
        String sql = """
            SELECT * FROM uploadfile 
            WHERE rid = :rid 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("rid", rid);
        
        return template.query(sql, param, uploadFileRowMapper);
    }

    @Override
    public List<UploadFile> findByCodeAndRid(Long code, String rid) {
        String sql = """
            SELECT * FROM uploadfile 
            WHERE code = :code AND rid = :rid 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("code", code)
                .addValue("rid", rid);
        
        return template.query(sql, param, uploadFileRowMapper);
    }

    @Override
    public List<UploadFile> findAll() {
        String sql = "SELECT * FROM uploadfile ORDER BY cdate DESC";
        return template.query(sql, uploadFileRowMapper);
    }

    @Override
    public boolean existsByStoreFilename(String storeFilename) {
        String sql = "SELECT COUNT(*) FROM uploadfile WHERE store_filename = :storeFilename";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("storeFilename", storeFilename);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public int countByCode(Long code) {
        String sql = "SELECT COUNT(*) FROM uploadfile WHERE code = :code";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("code", code);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public int countByRid(String rid) {
        String sql = "SELECT COUNT(*) FROM uploadfile WHERE rid = :rid";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("rid", rid);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }
} 