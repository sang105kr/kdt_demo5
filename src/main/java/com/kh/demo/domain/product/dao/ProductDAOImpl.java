package com.kh.demo.domain.product.dao;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.common.exception.ErrorCode;
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 상품 DAO 구현체
 * - NamedParameterJdbcTemplate 사용
 * - Oracle 시퀀스 기반 키 생성
 * - RowMapper를 통한 결과 매핑
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class ProductDAOImpl implements ProductDAO {

    private final NamedParameterJdbcTemplate template;

    private RowMapper<Products> doRowMapper() {
        return (ResultSet rs, int rowNum) -> {
            Products products = new Products();
            products.setProductId(rs.getLong("product_id"));
            products.setPname(rs.getString("pname"));
            products.setDescription(rs.getString("description"));
            products.setPrice(rs.getInt("price"));
            products.setRating(rs.getObject("rating", Double.class));
            products.setCategory(rs.getString("category"));
            products.setStockQuantity(rs.getObject("stock_quantity", Integer.class));
            products.setCdate(rs.getObject("cdate", java.time.LocalDateTime.class));
            products.setUdate(rs.getObject("udate", java.time.LocalDateTime.class));
            return products;
        };
    }

    @Override
    public Long save(Products products) {
        String sql = """
            INSERT INTO products (product_id, pname, description, price, category, stock_quantity, cdate, udate) 
            VALUES (seq_product_id.nextval, :pname, :description, :price, :category, :stockQuantity, SYSTIMESTAMP, SYSTIMESTAMP) 
            """;

        SqlParameterSource param = new BeanPropertySqlParameterSource(products);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"product_id"});
        Number productIdNumber = keyHolder.getKey();
        if (productIdNumber == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("operation", "product_save");
            throw ErrorCode.INTERNAL_SERVER_ERROR.toException(details);
        }
        return productIdNumber.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Products> findAll() {
        String sql = """
            SELECT product_id, pname, description, price, rating, category, stock_quantity, cdate, udate
            FROM products 
            ORDER BY product_id DESC 
            """;

        return template.query(sql, doRowMapper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Products> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT product_id, pname, description, price, rating, category, stock_quantity, cdate, udate
            FROM products 
            ORDER BY product_id DESC 
            OFFSET :offset ROWS 
            FETCH FIRST :limit ROWS ONLY 
            """;

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", limit);
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
            SELECT product_id, pname, description, price, rating, category, stock_quantity, cdate, udate
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
                rating = :rating, category = :category, stock_quantity = :stockQuantity, udate = SYSTIMESTAMP
            WHERE product_id = :productId 
            """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("pname", products.getPname())
                .addValue("description", products.getDescription())
                .addValue("price", products.getPrice())
                .addValue("rating", products.getRating())
                .addValue("category", products.getCategory())
                .addValue("stockQuantity", products.getStockQuantity())
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Products> findByPname(String pname) {
        String sql = """
            SELECT product_id, pname, description, price, rating, category, stock_quantity, cdate, udate
            FROM products 
            WHERE pname = :pname 
            ORDER BY product_id DESC 
            """;

        Map<String, String> param = Map.of("pname", pname);
        return template.query(sql, param, doRowMapper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Products> searchByKeyword(String keyword, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT product_id, pname, description, price, rating, category, stock_quantity, cdate, udate
            FROM products 
            WHERE pname LIKE '%' || :keyword || '%' 
               OR description LIKE '%' || :keyword || '%'
            ORDER BY product_id DESC 
            OFFSET :offset ROWS 
            FETCH FIRST :pageSize ROWS ONLY 
            """;

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("keyword", keyword)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, doRowMapper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByKeyword(String keyword) {
        String sql = """
            SELECT COUNT(product_id) 
            FROM products 
            WHERE pname LIKE '%' || :keyword || '%' 
               OR description LIKE '%' || :keyword || '%'
            """;

        Map<String, String> param = Map.of("keyword", keyword);
        Long count = template.queryForObject(sql, param, Long.class);
        return count != null ? count.intValue() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Products> findByCategory(String category, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        String sql = """
            SELECT product_id, pname, description, price, rating, category, stock_quantity, cdate, udate
            FROM products 
            WHERE category = :category 
            ORDER BY product_id DESC 
            OFFSET :offset ROWS 
            FETCH FIRST :pageSize ROWS ONLY 
            """;
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("category", category)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);
        return template.query(sql, param, doRowMapper());
    }

    /**
     * {@inheritDoc}
     */
        @Override
    public int countByCategory(String category) {
        String sql = """
            SELECT COUNT(product_id) 
            FROM products 
            WHERE category = :category 
            """;

        Map<String, String> param = Map.of("category", category);
        Long count = template.queryForObject(sql, param, Long.class);
        return count != null ? count.intValue() : 0;
    }
    
    /**
     * 재고 차감
     */
    @Override
    public int decreaseStock(Long productId, Integer quantity) {
        String sql = """
            UPDATE products 
            SET stock_quantity = stock_quantity - :quantity, udate = SYSTIMESTAMP
            WHERE product_id = :productId 
            AND stock_quantity >= :quantity
            """;

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("quantity", quantity);

        return template.update(sql, param);
    }
    
    /**
     * 재고 증가
     */
    @Override
    public int increaseStock(Long productId, Integer quantity) {
        String sql = """
            UPDATE products 
            SET stock_quantity = stock_quantity + :quantity, udate = SYSTIMESTAMP
            WHERE product_id = :productId
            """;

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("quantity", quantity);

        return template.update(sql, param);
    }
} 