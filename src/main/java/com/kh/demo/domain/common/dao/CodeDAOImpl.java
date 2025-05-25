package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.CodeId;
import com.kh.demo.domain.entity.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CodeDAOImpl implements CodeDAO {

  private final NamedParameterJdbcTemplate template;

  @Override
  public List<Code> loadCodes(CodeId pcodeId) {
    StringBuffer sql = new StringBuffer();
    sql.append("select code_id,decode ");
    sql.append("  from code ");
    sql.append(" where pcode_id = :pcodeId ");
    sql.append("   and useyn = 'Y' ");

    Map<String,String> param = Map.of("pcodeId",pcodeId.name());

    List<Code> codes = template.query(
        sql.toString(),
        param,
        BeanPropertyRowMapper.newInstance(Code.class));

    return codes;
  }
}
