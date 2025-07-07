package com.kh.demo.domain.products.dao;

import com.kh.demo.domain.entity.Products;
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
public class ProductRepositoryImpl implements ProductRepository {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private RowMapper<Products> doRowMapper() {
        return (rs, rowNum) -> {
            Products products = new Products();
            products.setProductId(rs.getLong("product_id"));
            products.setPname(rs.getString("pname"));
            products.setDescription(rs.getString("description"));
            products.setPrice(rs.getLong("price"));
            products.setRating(rs.getDouble("rating"));
            products.setCategory(rs.getString("category"));
            return products;
        };
    }

    /**
     * 상품 등록
     */
    @Override
    public Long save(Products products) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO products (product_id, pname, description, price, rating, category) ");
        sql.append("VALUES (products_product_id_seq.nextval, :pname, :description, :price, :rating, :category) ");

        SqlParameterSource param = new BeanPropertySqlParameterSource(products);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        template.update(sql.toString(), param, keyHolder, new String[]{"product_id"});
        
        Number productIdNumber = (Number) keyHolder.getKeys().get("product_id");
        return productIdNumber.longValue();
    }

    /**
     * 상품 목록 조회
     */
    @Override
    public List<Products> findAll() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT product_id, pname, description, price, rating, category ");
        sql.append("FROM products ");
        sql.append("ORDER BY product_id DESC ");

        return template.query(sql.toString(), doRowMapper());
    }

    /**
     * 상품 목록 조회 (페이징)
     */
    @Override
    public List<Products> findAll(int pageNo, int numOfRows) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT product_id, pname, description, price, rating, category ");
        sql.append("FROM products ");
        sql.append("ORDER BY product_id DESC ");
        sql.append("OFFSET (:pageNo - 1) * :numOfRows ROWS ");
        sql.append("FETCH NEXT :numOfRows ROWS ONLY ");

        Map<String, Integer> param = Map.of("pageNo", pageNo, "numOfRows", numOfRows);
        return template.query(sql.toString(), param, doRowMapper());
    }

    /**
     * 상품 총 건수
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(product_id) FROM products ";
        SqlParameterSource param = new MapSqlParameterSource();
        return template.queryForObject(sql, param, Integer.class);
    }

    /**
     * 상품 조회
     */
    @Override
    public Optional<Products> findById(Long productId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT product_id, pname, description, price, rating, category ");
        sql.append("FROM products ");
        sql.append("WHERE product_id = :productId ");

        SqlParameterSource param = new MapSqlParameterSource().addValue("productId", productId);

        try {
            Products products = template.queryForObject(sql.toString(), param, BeanPropertyRowMapper.newInstance(Products.class));
            return Optional.of(products);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 상품 수정
     */
    @Override
    public int updateById(Long productId, Products products) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE products ");
        sql.append("SET pname = :pname, description = :description, price = :price, ");
        sql.append("    rating = :rating, category = :category ");
        sql.append("WHERE product_id = :productId ");

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("pname", products.getPname())
                .addValue("description", products.getDescription())
                .addValue("price", products.getPrice())
                .addValue("rating", products.getRating())
                .addValue("category", products.getCategory())
                .addValue("productId", productId);

        return template.update(sql.toString(), param);
    }

    /**
     * 상품 삭제
     */
    @Override
    public int deleteById(Long productId) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM products ");
        sql.append("WHERE product_id = :productId ");

        Map<String, Long> param = Map.of("productId", productId);
        return template.update(sql.toString(), param);
    }

    /**
     * 상품 삭제 (여러건)
     */
    @Override
    public int deleteByIds(List<Long> productIds) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM products ");
        sql.append("WHERE product_id IN (:productIds) ");

        Map<String, List<Long>> param = Map.of("productIds", productIds);
        return template.update(sql.toString(), param);
    }
}
