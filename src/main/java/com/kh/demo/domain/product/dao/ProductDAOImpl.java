package com.kh.demo.domain.product.dao;

import com.kh.demo.domain.product.entity.Products;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 상품 데이터 접근 객체 구현체
 * NamedJdbcTemplate을 사용하여 상품의 CRUD 및 검색 기능을 구현합니다.
 * 
 * @author KDT
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class ProductDAOImpl implements ProductDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private RowMapper<Products> doRowMapper() {
        return (rs, rowNum) -> {
            Products products = new Products();
            products.setProductId(rs.getLong("product_id"));
            products.setPname(rs.getString("pname"));
            products.setDescription(rs.getString("description"));
            products.setPrice(rs.getInt("price"));
            products.setRating(rs.getDouble("rating"));
            products.setCategory(rs.getString("category"));
            products.setCdate(rs.getObject("cdate", LocalDateTime.class));
            products.setUdate(rs.getObject("udate", LocalDateTime.class));
            return products;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long save(Products products) {
        String sql = """
            INSERT INTO products (product_id, pname, description, price, rating, category, cdate, udate) 
            VALUES (seq_product_id.nextval, :pname, :description, :price, :rating, :category, SYSTIMESTAMP, SYSTIMESTAMP) 
            """;

        SqlParameterSource param = new BeanPropertySqlParameterSource(products);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"product_id"});
        Number productIdNumber = keyHolder.getKey();
        if (productIdNumber == null) {
            throw new IllegalStateException("Failed to retrieve generated product_id");
        }
        return productIdNumber.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Products> findAll() {
        String sql = """
            SELECT product_id, pname, description, price, rating, category, cdate, udate
            FROM products 
            ORDER BY product_id DESC 
            """;

        return template.query(sql, doRowMapper());
    }

    /**
     * 페이징 조회 (BaseDAO에는 없으므로 별도 메서드로 구현)
     */
    public List<Products> findAllWithPaging(int pageNo, int numOfRows) {
        String sql = """
            SELECT product_id, pname, description, price, rating, category, cdate, udate
            FROM products 
            ORDER BY product_id DESC 
            OFFSET (:pageNo - 1) * :numOfRows ROWS 
            FETCH NEXT :numOfRows ROWS ONLY 
            """;

        Map<String, Integer> param = Map.of("pageNo", pageNo, "numOfRows", numOfRows);
        return template.query(sql, param, doRowMapper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(product_id) FROM products ";
        SqlParameterSource param = new MapSqlParameterSource();
        Long count = template.queryForObject(sql, param, Long.class);
        return count != null ? count.intValue() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Products> findById(Long productId) {
        String sql = """
            SELECT product_id, pname, description, price, rating, category, cdate, udate
            FROM products 
            WHERE product_id = :productId 
            """;

        SqlParameterSource param = new MapSqlParameterSource().addValue("productId", productId);

        try {
            Products products = template.queryForObject(sql, param, doRowMapper());
            return Optional.of(products);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateById(Long productId, Products products) {
        String sql = """
            UPDATE products 
            SET pname = :pname, description = :description, price = :price, 
                rating = :rating, category = :category, udate = SYSTIMESTAMP
            WHERE product_id = :productId 
            """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("pname", products.getPname())
                .addValue("description", products.getDescription())
                .addValue("price", products.getPrice())
                .addValue("rating", products.getRating())
                .addValue("category", products.getCategory())
                .addValue("productId", productId);

        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteById(Long productId) {
        String sql = """
            DELETE FROM products 
            WHERE product_id = :productId 
            """;

        Map<String, Long> param = Map.of("productId", productId);
        return template.update(sql, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return 0;
        String sql = """
            DELETE FROM products 
            WHERE product_id IN (:productIds) 
            """;

        Map<String, List<Long>> param = Map.of("productIds", productIds);
        return template.update(sql, param);
    }
} 