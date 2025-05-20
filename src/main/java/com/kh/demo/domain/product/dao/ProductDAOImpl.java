package com.kh.demo.domain.product.dao;

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
@RequiredArgsConstructor
@Repository
class ProductDAOImpl implements ProductDAO{

  private final NamedParameterJdbcTemplate template;

//  //필드 주입
//  @Autowired
//  NamedParameterJdbcTemplate template;

  //생성자 주입
//  public ProductDAOImpl(NamedParameterJdbcTemplate template){
//    this.template = template;
//  }
  //수동매핑
  private RowMapper<Product> doRowMapper(){

    return (rs, rowNum)->{
      Product product = new Product();
      product.setProductId(rs.getLong("product_id"));
      product.setPname(rs.getString("pname"));
      product.setQuantity(rs.getLong("quantity"));
      product.setPrice(rs.getLong("price"));
      return product;
    };
  }

  /**
   * 상품 등록
   * @param product
   * @return 상품번호
   */
  @Override
  public Long save(Product product) {
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO product (product_id,pname,quantity,price) ");
    sql.append("     VALUES (PRODUCT_PRODUCT_ID_SEQ.nextval,:pname,:quantity,:price) ");

    //BeanPropertySqlParameterSource : 자바객체 필드명과 SQL파라미터명이 같을때 자동 매칭함.
    SqlParameterSource param = new BeanPropertySqlParameterSource(product);

    // template.update()가 수행된 레코드의 특정 컬럼값을 읽어오는 용도
    KeyHolder keyHolder = new GeneratedKeyHolder();
    long rows = template.update(sql.toString(),param, keyHolder, new String[]{"product_id"} );
    //log.info("rows={}",rows);

    Number pidNumber = (Number)keyHolder.getKeys().get("product_id");
    long pid = pidNumber.longValue();
    return pid;
  }

  /**
   * 상품 목록
   * @return 상품 목록
   */
  @Override
  public List<Product> findAll() {
    //sql
    StringBuffer sql = new StringBuffer();
    sql.append("  SELECT product_id, pname, quantity, price ");
    sql.append("    FROM product ");
    sql.append("order BY product_id desc ");

    //db요청
    List<Product> list = template.query(sql.toString(), doRowMapper());

    return list;
  }

  //상품목록 - 페이징
  @Override
  public List<Product> findAll(int pageNo, int numOfRows) {
    //sql
    StringBuffer sql = new StringBuffer();
    sql.append("  SELECT product_id,pname,quantity,price ");
    sql.append("    FROM product ");
    sql.append("ORDER BY product_id asc ");
    sql.append("  OFFSET (:pageNo -1) * :numOfRows ROWS ");
    sql.append("FETCH NEXT :numOfRows ROWS only ");

    Map<String, Integer> map = Map.of("pageNo", pageNo, "numOfRows", numOfRows);
    List<Product> list = template.query(sql.toString(), map, doRowMapper());

    return list;
  }

  //상품 총 건수
  @Override
  public int getTotalCount() {
    String sql = "select count(product_id) from product ";

    SqlParameterSource param = new MapSqlParameterSource();
    int i = template.queryForObject(sql, param, Integer.class);

    return i;
  }

  /**
   * 상품조회
   * @param id 상품번호
   * @return 상품정보
   */
  @Override
  public Optional<Product> findById(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT product_id, pname, quantity, price ");
    sql.append("  FROM product ");
    sql.append(" WHERE product_id = :id ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("id",id);

    Product product = null;
    try {
      product = template.queryForObject(sql.toString(), param, BeanPropertyRowMapper.newInstance(Product.class));
    } catch (EmptyResultDataAccessException e) { //template.queryForObject() : 레코드를 못찾으면 예외 발생
      return Optional.empty();
    }

    return Optional.of(product);
  }

  /**
   * 상품삭제(단건)
   * @param id 상품번호
   * @return 삭제건수
   */
  @Override
  public int deleteById(Long id) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM product ");
    sql.append(" WHERE product_id = :id ");
    //case1)
//    SqlParameterSource param = new MapSqlParameterSource().addValue("id",id);
    //case2)
    Map<String, Long> param = Map.of("id",id);
    int rows = template.update(sql.toString(), param); //삭제된 행의 수 반환
    return rows;
  }

  /**
   * 상품삭제(여러건)
   * @param ids 상품번호s
   * @return 삭제건수
   */
  @Override
  public int deleteByIds(List<Long> ids) {
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM product ");
    sql.append(" WHERE product_id IN ( :ids ) ");

    Map<String, List<Long>> param = Map.of("ids",ids);
    int rows = template.update(sql.toString(), param); //삭제한 행의 수 반환
    return rows;
  }

  /**
   * 상품 수정
   * @param productId 상품번호
   * @param product 상품정보
   * @return 상품 수정 건수
   */
  @Override
  public int updateById(Long productId, Product product) {
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE product ");
    sql.append("   SET pname = :pname, quantity = :quantity, price = :price ");
    sql.append(" WHERE product_id = :productId ");

    //수동매핑
    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("pname", product.getPname())
        .addValue("quantity", product.getQuantity())
        .addValue("price", product.getPrice())
        .addValue("productId", productId);

    int rows = template.update(sql.toString(), param); // 수정된 행의 수 반환

    return rows;
  }
}
