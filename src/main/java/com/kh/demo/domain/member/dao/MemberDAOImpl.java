package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.common.exception.ErrorCode;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 회원 데이터 접근 객체 구현체
 * NamedJdbcTemplate을 사용하여 회원의 CRUD 및 검색 기능을 구현합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberDAOImpl implements MemberDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private final RowMapper<Member> memberRowMapper = (ResultSet rs, int rowNum) -> {
        Member member = new Member();
        member.setMemberId(rs.getLong("member_id"));
        member.setEmail(rs.getString("email"));
        member.setPasswd(rs.getString("passwd"));
        member.setTel(rs.getString("tel"));
        member.setNickname(rs.getString("nickname"));
        member.setGender(rs.getString("gender"));
        member.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
        member.setHobby(rs.getString("hobby"));
        member.setRegion(rs.getLong("region"));
        member.setGubun(rs.getLong("gubun"));
        member.setStatus(rs.getString("status"));
        member.setStatusReason(rs.getString("status_reason"));
        member.setStatusChangedAt(rs.getObject("status_changed_at", LocalDateTime.class));
        member.setPic(rs.getBytes("pic"));
        member.setCdate(rs.getObject("cdate", LocalDateTime.class));
        member.setUdate(rs.getObject("udate", LocalDateTime.class));
        return member;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public Long save(Member member) {
        String sql = """
            INSERT INTO member (member_id, email, passwd, tel, nickname, gender, birth_date, hobby, region, gubun, status, status_reason, status_changed_at, pic)
            VALUES (seq_member_id.nextval, :email, :passwd, :tel, :nickname, :gender, :birthDate, :hobby, :region, :gubun, :status, :statusReason, :statusChangedAt, :pic)
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", member.getEmail())
                .addValue("passwd", member.getPasswd())
                .addValue("tel", member.getTel())
                .addValue("nickname", member.getNickname())
                .addValue("gender", member.getGender())
                .addValue("birthDate", member.getBirthDate())
                .addValue("hobby", member.getHobby())
                .addValue("region", member.getRegion())
                .addValue("gubun", member.getGubun() != null ? member.getGubun() : 2L) // 기본값: 일반회원
                .addValue("status", member.getStatus() != null ? member.getStatus() : "ACTIVE") // 기본값: 활성
                .addValue("statusReason", member.getStatusReason())
                .addValue("statusChangedAt", member.getStatusChangedAt() != null ? member.getStatusChangedAt() : LocalDateTime.now());
        
        // BLOB 처리: SerialBlob을 바이트 배열로 변환
        if (member.getPic() != null) {
            try {
                param.addValue("pic", member.getPic());
            } catch (Exception e) {
                log.error("BLOB을 바이트 배열로 변환 실패", e);
                param.addValue("pic", null);
            }
        } else {
            param.addValue("pic", null);
        }
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"member_id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("operation", "member_save");
            throw ErrorCode.INTERNAL_SERVER_ERROR.toException(details);
        }
        return key.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateById(Long id, Member member) {
        String sql = """
            UPDATE member 
            SET email = :email, passwd = :passwd, tel = :tel, nickname = :nickname, 
                gender = :gender, birth_date = :birthDate, hobby = :hobby, region = :region, gubun = :gubun, 
                status = :status, status_reason = :statusReason, status_changed_at = :statusChangedAt,
                pic = :pic, udate = SYSTIMESTAMP
            WHERE member_id = :memberId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", id)
                .addValue("email", member.getEmail())
                .addValue("passwd", member.getPasswd())
                .addValue("tel", member.getTel())
                .addValue("nickname", member.getNickname())
                .addValue("gender", member.getGender())
                .addValue("birthDate", member.getBirthDate())
                .addValue("hobby", member.getHobby())
                .addValue("region", member.getRegion())
                .addValue("gubun", member.getGubun())
                .addValue("status", member.getStatus())
                .addValue("statusReason", member.getStatusReason())
                .addValue("statusChangedAt", member.getStatusChangedAt() != null ? member.getStatusChangedAt() : LocalDateTime.now());
        
        // BLOB 처리: SerialBlob을 바이트 배열로 변환
        if (member.getPic() != null) {
            try {
                param.addValue("pic", member.getPic());
            } catch (Exception e) {
                log.error("BLOB을 바이트 배열로 변환 실패", e);
                param.addValue("pic", null);
            }
        } else {
            param.addValue("pic", null);
        }
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteById(Long memberId) {
        String sql = "DELETE FROM member WHERE member_id = :memberId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Member> findById(Long memberId) {
        String sql = "SELECT * FROM member WHERE member_id = :memberId";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        try {
            Member member = template.queryForObject(sql, param, memberRowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member ORDER BY cdate DESC";
        return template.query(sql, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM member";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT * FROM member 
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", limit);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        try {
            Member member = template.queryForObject(sql, param, memberRowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Member> findByEmailAndPasswd(String email, String passwd) {
        String sql = "SELECT * FROM member WHERE email = :email AND passwd = :passwd";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("passwd", passwd);
        
        try {
            Member member = template.queryForObject(sql, param, memberRowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findAllWithPaging(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member 
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findByRegion(Long region) {
        String sql = """
            SELECT * FROM member 
            WHERE region = :region 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("region", region);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Member> findByGubun(Long gubun) {
        String sql = """
            SELECT * FROM member 
            WHERE gubun = :gubun 
            ORDER BY cdate DESC
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gubun", gubun);
        
        return template.query(sql, param, memberRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM member WHERE email = :email";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        int count = template.queryForObject(sql, param, Integer.class);
        return count > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByRegion(Long region) {
        String sql = "SELECT COUNT(*) FROM member WHERE region = :region";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("region", region);
        
        return template.queryForObject(sql, param, Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByGubun(Long gubun) {
        String sql = "SELECT COUNT(*) FROM member WHERE gubun = :gubun";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("gubun", gubun);
        
        return template.queryForObject(sql, param, Integer.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> findEmailByPhoneAndBirth(String phone, String birth) {
        String sql = """
            SELECT email FROM member 
            WHERE tel = :phone AND 
                  TO_CHAR(birth_date, 'YYYY-MM-DD') = :birth
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("phone", phone)
                .addValue("birth", birth);
        
        try {
            String email = template.queryForObject(sql, param, String.class);
            return Optional.ofNullable(email);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public int countByKeyword(String keyword) {
        String sql = "SELECT COUNT(*) FROM member WHERE email LIKE :kw OR nickname LIKE :kw";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("kw", "%" + keyword + "%");
        return template.queryForObject(sql, param, Integer.class);
    }

    @Override
    public List<Member> findByKeywordWithPaging(String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT * FROM member
            WHERE email LIKE :kw OR nickname LIKE :kw
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("kw", "%" + keyword + "%")
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, memberRowMapper);
    }
}
