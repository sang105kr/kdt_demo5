package com.kh.demo.domain.bbs.dao;

import com.kh.demo.domain.entity.Product;
import com.kh.demo.domain.entity.Rbbs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RbbsDAOImpl implements RbbsDAO{

  private final NamedParameterJdbcTemplate template;

  @Override
  public Rbbs save(Rbbs rbbs) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO RBBS(rbbs_id, bbs_id, content, writer) ");
    sql.append("VALUES (rbbs_rbbs_id_seq.nextval, :bbsId, :content, :writer) ");

    //BeanPropertySqlParameterSource : 자바객체 필드명과 SQL파라미터명이 같을때 자동 매칭함.
    SqlParameterSource param = new BeanPropertySqlParameterSource(rbbs);

    // template.update()가 수행된 레코드의 특정 컬럼값을 읽어오는 용도
    KeyHolder keyHolder = new GeneratedKeyHolder();
    long rows = template.update(sql.toString(),param, keyHolder, new String[]{"rbbs_id"} );
    //log.info("rows={}",rows);

    long rbbsId = ((Number) keyHolder.getKeys().get("rbbs_id")).longValue();
    return findById(rbbsId).get();
  }

  @Override
  public List<Rbbs> findAll() {
    //sql
    StringBuffer sql = new StringBuffer();
    sql.append("  SELECT rbbs_id, bbs_id, content, writer, created_at, updated_at ");
    sql.append("    FROM rbbs ");
    sql.append("ORDER BY bbs_id DESC ");

    //db요청
    List<Rbbs> list = template.query(sql.toString(), BeanPropertyRowMapper.newInstance(Rbbs.class));
    return list;
  }

  //댓글 목록 - 페이징
  @Override
  public List<Rbbs> findAll(int pageNo, int numOfRows) {
    //sql
    StringBuffer sql = new StringBuffer();
    sql.append("  SELECT rbbs_id,bbs_id,content,writer,created_at,updated_at ");
    sql.append("    FROM rbbs ");
    sql.append("ORDER BY rbbs_id DESC ");
    sql.append("  OFFSET (:pageNo -1) * :numOfRows ROWS ");
    sql.append("FETCH NEXT :numOfRows ROWS only ");

    Map<String, Integer> map = Map.of("pageNo", pageNo, "numOfRows", numOfRows);
    List<Rbbs> list = template.query(sql.toString(), map, BeanPropertyRowMapper.newInstance(Rbbs.class));

    return list;
  }

  //상품 총 건수
  @Override
  public int getTotalCount() {
    String sql = "select count(rbbs_id) from rbbs ";

    SqlParameterSource param = new MapSqlParameterSource();
    int i = template.queryForObject(sql, param, Integer.class);

    return i;
  }

  @Override
  public Optional<Rbbs> findById(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT rbbs_id, bbs_id, content, writer, created_at, updated_at ");
    sql.append("  FROM rbbs ");
    sql.append(" WHERE rbbs_id = :id ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("id",id);

    Rbbs rbbs = null;
    try {
      rbbs = template.queryForObject(sql.toString(), param, BeanPropertyRowMapper.newInstance(Rbbs.class));
    } catch (EmptyResultDataAccessException e) { //template.queryForObject() : 레코드를 못찾으면 예외 발생
      return Optional.empty();
    }

    return Optional.of(rbbs);
  }

  @Override
  public int deleteById(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM RBBS ");
    sql.append(" WHERE rbbs_id = :id ");

    Map<String, Long> param = Map.of("id",id);
    int rows = template.update(sql.toString(), param); //삭제된 행의 수 반환
    return rows;
  }

  @Override
  public int updateById(Long id, Rbbs rbbs) {
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE RBBS ");
    sql.append("SET content = :content, ");
    sql.append("    updated_at = systimestamp ");
    sql.append("WHERE rbbs_id = :id ");

    //수동매핑
    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("content", rbbs.getContent())
        .addValue("writer", rbbs.getWriter())
        .addValue("id", id);

    int rows = template.update(sql.toString(), param); // 수정된 행의 수 반환

    return rows;
  }
}
