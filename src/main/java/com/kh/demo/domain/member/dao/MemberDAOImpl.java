package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.shared.base.BaseDAO;
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

import java.sql.Blob;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
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
        member.setHobby(rs.getString("hobby"));
        member.setRegion(rs.getLong("region"));
        member.setGubun(rs.getLong("gubun"));
        member.setPic(rs.getBlob("pic"));
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
            INSERT INTO member (member_id, email, passwd, tel, nickname, gender, hobby, region, gubun, pic, cdate, udate)
            VALUES (seq_member_id.nextval, :email, :passwd, :tel, :nickname, :gender, :hobby, :region, :gubun, :pic, SYSTIMESTAMP, SYSTIMESTAMP)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, new BeanPropertySqlParameterSource(member), keyHolder);
        
        return keyHolder.getKey().longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateById(Long id, Member member) {
        String sql = """
            UPDATE member 
            SET email = :email, passwd = :passwd, tel = :tel, nickname = :nickname, 
                gender = :gender, hobby = :hobby, region = :region, gubun = :gubun, 
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
                .addValue("hobby", member.getHobby())
                .addValue("region", member.getRegion())
                .addValue("gubun", member.getGubun())
                .addValue("pic", member.getPic());
        
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
    public List<Member> findAllWithPaging(int offset, int limit) {
        String sql = """
            SELECT * FROM (
                SELECT a.*, ROWNUM rnum FROM (
                    SELECT * FROM member ORDER BY cdate DESC
                ) a WHERE ROWNUM <= :limit
            ) WHERE rnum > :offset
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", offset + limit);
        
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
}
