package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberDAOImpl implements MemberDAO{

  private final NamedParameterJdbcTemplate template;

  @Override
  public Member insertMember(Member member) {
    // 1) sql 준비
    StringBuffer sql = new StringBuffer();
    sql.append("insert into member (member_id,email,passwd,tel,nickname,gender,hobby,region) ");
    sql.append("values(member_member_id_seq.nextval, :email,:passwd,:tel,:nickname,:gender,:hobby,:region) ");

    // 2) sql 실행
    SqlParameterSource param = new BeanPropertySqlParameterSource(member);
    //KeyHolder 의 역할 : insert쿼리를 실행한 후 데이터베이스에서 자동 생성된 키(pk,시퀀스)만을 반환 하는데 사용
    KeyHolder keyHolder = new GeneratedKeyHolder();
    int rows = template.update(sql.toString(), param, keyHolder, new String[]{"member_id"});

    long memberId = ((Number) keyHolder.getKeys().get("member_id")).longValue();

    return findByMemberId(memberId).get();
  }

  @Override
  public boolean isExist(String email) {
    StringBuffer sql = new StringBuffer();
    sql.append("select count(*) ");
    sql.append("  from member ");
    sql.append(" where email = :email ");

    Map<String,String> param = Map.of("email",email);
    Integer cntOfRec = template.queryForObject(sql.toString(), param, Integer.class);

    return (cntOfRec == 1) ? true : false;
  }

  @Override
  public Optional<Member> findByMemberId(Long memberId) {
    StringBuffer sql = new StringBuffer();
    sql.append("select  member_id, ");
    sql.append("        email, ");
    sql.append("        passwd, ");
    sql.append("        tel, ");
    sql.append("        nickname, ");
    sql.append("        gender, ");
    sql.append("        hobby, ");
    sql.append("        region, ");
    sql.append("        gubun, ");
    sql.append("        pic, ");
    sql.append("        cdate, ");
    sql.append("        udate ");
    sql.append(" from member ");
    sql.append("where member_id = :memberId ");

    Map<String,Long> param = Map.of("memberId",memberId);
    try {
      Member member = template.queryForObject(
          sql.toString(), param, BeanPropertyRowMapper.newInstance(Member.class));

      return Optional.of(member);

    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();

    }
  }

  @Override
  public Optional<Member> findByEmail(String email) {
    StringBuffer sql = new StringBuffer();
    sql.append("select  member_id, ");
    sql.append("        email, ");
    sql.append("        passwd, ");
    sql.append("        tel, ");
    sql.append("        nickname, ");
    sql.append("        gender, ");
    sql.append("        hobby, ");
    sql.append("        region, ");
    sql.append("        gubun, ");
    sql.append("        pic, ");
    sql.append("        cdate, ");
    sql.append("        udate ");
    sql.append(" from member ");
    sql.append("where email = :email ");

    Map<String,String> param = Map.of("email",email);
    try {
      Member member = template.queryForObject(
          sql.toString(), param,BeanPropertyRowMapper.newInstance(Member.class));

      return Optional.of(member);

    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();

    }
  }


}
