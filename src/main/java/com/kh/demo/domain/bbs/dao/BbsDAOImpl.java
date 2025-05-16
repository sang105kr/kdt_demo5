package com.kh.demo.domain.bbs.dao;

import com.kh.demo.domain.entity.Bbs;
import com.kh.demo.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
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
public class BbsDAOImpl implements BbsDAO{

  private final NamedParameterJdbcTemplate template;

  @Override
  public Long save(Bbs bbs) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO BBS(bbs_id, title, content, writer) ");
    sql.append("VALUES (bbs_bbs_id_seq.nextval, :title, :content, :writer) ");

    //BeanPropertySqlParameterSource : 자바객체 필드명과 SQL파라미터명이 같을때 자동 매칭함.
    SqlParameterSource param = new BeanPropertySqlParameterSource(bbs);

    // template.update()가 수행된 레코드의 특정 컬럼값을 읽어오는 용도
    KeyHolder keyHolder = new GeneratedKeyHolder();
    long rows = template.update(sql.toString(),param, keyHolder, new String[]{"bbs_id"} );
    //log.info("rows={}",rows);

    return ((Number)keyHolder.getKeys().get("bbs_id")).longValue();
  }

  //수동매핑
  private RowMapper<Bbs> doRowMapper(){

    return (rs, rowNum)->{
      Bbs bbs = new Bbs();
      bbs.setBbsId(rs.getLong("bbs_id"));
      bbs.setTitle(rs.getString("title"));
      bbs.setContent(rs.getString("content"));
      bbs.setWriter(rs.getString("writer"));
      bbs.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
      bbs.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
      return bbs;
    };
  }
  @Override
  public List<Bbs> findAll() {
    //sql
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT bbs_id, title, content, writer, created_at, updated_at ");
    sql.append("  FROM bbs ");

    //db요청
    //case1) 수동 매핑
    List<Bbs> list = template.query(sql.toString(), doRowMapper());
    //case2) 자동 매핑
//    List<Bbs> list = template.query(sql.toString(), BeanPropertyRowMapper.newInstance(Bbs.class));

    return list;
  }

  @Override
  public Optional<Bbs> findById(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT bbs_id, title, content, writer, created_at, updated_at ");
    sql.append("  FROM bbs ");
    sql.append(" WHERE bbs_id = :id ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("id",id);

    Bbs bbs = null;
    try {
      bbs = template.queryForObject(sql.toString(), param, BeanPropertyRowMapper.newInstance(Bbs.class));
    } catch (EmptyResultDataAccessException e) { //template.queryForObject() : 레코드를 못찾으면 예외 발생
      return Optional.empty();
    }

    return Optional.of(bbs);
  }

  @Override
  public int deleteById(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM BBS ");
    sql.append(" WHERE bbs_id = :id ");

    Map<String, Long> param = Map.of("id",id);
    int rows = template.update(sql.toString(), param); //삭제된 행의 수 반환
    return rows;
  }

  @Override
  public int deleteByIds(List<Long> ids) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM BBS ");
    sql.append(" WHERE bbs_id IN (:ids) ");

    Map<String, List<Long>> param = Map.of("ids",ids);
    int rows = template.update(sql.toString(), param); //삭제한 행의 수 반환
    return rows;
  }

  @Override
  public int updateById(Long id, Bbs bbs) {
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE BBS ");
    sql.append("SET title = :title, content = :content, writer = :writer, ");
    sql.append("    updated_at = systimestamp ");
    sql.append("WHERE bbs_id = :id ");

    //수동매핑
    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("title", bbs.getTitle())
        .addValue("content", bbs.getContent())
        .addValue("writer", bbs.getWriter())
        .addValue("id", id);

    int rows = template.update(sql.toString(), param); // 수정된 행의 수 반환

    return rows;
  }
}
