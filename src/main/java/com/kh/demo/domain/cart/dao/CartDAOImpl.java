package com.kh.demo.domain.cart.dao;

import com.kh.demo.domain.cart.entity.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CartDAOImpl implements CartDAO {
    
    private final NamedParameterJdbcTemplate template;
    
    @Override
    public List<CartItem> findByMemberId(Long memberId) {
        String sql = """
            SELECT ci.cart_item_id, ci.member_id, ci.product_id, ci.quantity, 
                   ci.unit_price, ci.total_price, ci.cdate, ci.udate,
                   p.product_id as p_product_id, p.pname, p.description, p.price, 
                   p.stock_quantity, p.rating, p.category, p.cdate as p_cdate, p.udate as p_udate
            FROM cart_items ci
            LEFT JOIN products p ON ci.product_id = p.product_id
            WHERE ci.member_id = :memberId
            ORDER BY ci.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.query(sql, params, getCartItemRowMapper());
    }
    
    @Override
    public Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId) {
        String sql = """
            SELECT ci.cart_item_id, ci.member_id, ci.product_id, ci.quantity, 
                   ci.unit_price, ci.total_price, ci.cdate, ci.udate,
                   p.product_id as p_product_id, p.pname, p.description, p.price, 
                   p.stock_quantity, p.rating, p.category, p.cdate as p_cdate, p.udate as p_udate
            FROM cart_items ci
            LEFT JOIN products p ON ci.product_id = p.product_id
            WHERE ci.member_id = :memberId AND ci.product_id = :productId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("productId", productId);
        
        List<CartItem> results = template.query(sql, params, getCartItemRowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public int countByMemberId(Long memberId) {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE member_id = :memberId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.queryForObject(sql, params, Integer.class);
    }
    
    @Override
    public Long getTotalAmountByMemberId(Long memberId) {
        String sql = "SELECT COALESCE(SUM(total_price), 0) FROM cart_items WHERE member_id = :memberId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.queryForObject(sql, params, Long.class);
    }
    
    @Override
    public int deleteByMemberId(Long memberId) {
        String sql = "DELETE FROM cart_items WHERE member_id = :memberId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.update(sql, params);
    }
    
    @Override
    public int updateQuantity(Long cartItemId, Integer quantity) {
        String sql = """
            UPDATE cart_items 
            SET quantity = :quantity, 
                total_price = unit_price * :quantity,
                udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId)
                .addValue("quantity", quantity);
        
        return template.update(sql, params);
    }
    
    @Override
    public int updatePrice(Long cartItemId, Long unitPrice) {
        String sql = """
            UPDATE cart_items 
            SET unit_price = :unitPrice, 
                total_price = :unitPrice * quantity,
                udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId)
                .addValue("unitPrice", unitPrice);
        
        return template.update(sql, params);
    }
    
    @Override
    public Long save(CartItem cartItem) {
        if (cartItem.getCartItemId() == null) {
            return insert(cartItem);
        } else {
            update(cartItem);
            return cartItem.getCartItemId();
        }
    }
    
    @Override
    public Optional<CartItem> findById(Long cartItemId) {
        String sql = """
            SELECT ci.cart_item_id, ci.member_id, ci.product_id, ci.quantity, 
                   ci.unit_price, ci.total_price, ci.cdate, ci.udate,
                   p.product_id as p_product_id, p.pname, p.description, p.price, 
                   p.stock_quantity, p.rating, p.category, p.cdate as p_cdate, p.udate as p_udate
            FROM cart_items ci
            LEFT JOIN products p ON ci.product_id = p.product_id
            WHERE ci.cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId);
        
        List<CartItem> results = template.query(sql, params, getCartItemRowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<CartItem> findAll() {
        String sql = """
            SELECT ci.cart_item_id, ci.member_id, ci.product_id, ci.quantity, 
                   ci.unit_price, ci.total_price, ci.cdate, ci.udate,
                   p.product_id as p_product_id, p.pname, p.description, p.price, 
                   p.stock_quantity, p.rating, p.category, p.cdate as p_cdate, p.udate as p_udate
            FROM cart_items ci
            LEFT JOIN products p ON ci.product_id = p.product_id
            ORDER BY ci.cdate DESC
            """;
        
        return template.query(sql, getCartItemRowMapper());
    }
    
    @Override
    public int updateById(Long cartItemId, CartItem cartItem) {
        String sql = """
            UPDATE cart_items 
            SET member_id = :memberId, product_id = :productId, quantity = :quantity,
                unit_price = :unitPrice, total_price = :totalPrice, udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId)
                .addValue("memberId", cartItem.getMemberId())
                .addValue("productId", cartItem.getProductId())
                .addValue("quantity", cartItem.getQuantity())
                .addValue("unitPrice", cartItem.getUnitPrice())
                .addValue("totalPrice", cartItem.getTotalPrice());
        
        return template.update(sql, params);
    }
    
    @Override
    public int deleteById(Long cartItemId) {
        String sql = "DELETE FROM cart_items WHERE cart_item_id = :cartItemId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId);
        
        return template.update(sql, params);
    }
    
    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM cart_items";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
    
    private Long insert(CartItem cartItem) {
        String sql = """
            INSERT INTO cart_items (member_id, product_id, quantity, unit_price, total_price, cdate, udate)
            VALUES (:memberId, :productId, :quantity, :unitPrice, :totalPrice, SYSDATE, SYSDATE)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", cartItem.getMemberId())
                .addValue("productId", cartItem.getProductId())
                .addValue("quantity", cartItem.getQuantity())
                .addValue("unitPrice", cartItem.getUnitPrice())
                .addValue("totalPrice", cartItem.getTotalPrice());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder, new String[]{"cart_item_id"});
        
        return keyHolder.getKey().longValue();
    }
    
    private void update(CartItem cartItem) {
        String sql = """
            UPDATE cart_items 
            SET member_id = :memberId, product_id = :productId, quantity = :quantity,
                unit_price = :unitPrice, total_price = :totalPrice, udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItem.getCartItemId())
                .addValue("memberId", cartItem.getMemberId())
                .addValue("productId", cartItem.getProductId())
                .addValue("quantity", cartItem.getQuantity())
                .addValue("unitPrice", cartItem.getUnitPrice())
                .addValue("totalPrice", cartItem.getTotalPrice());
        
        template.update(sql, params);
    }
    
    private org.springframework.jdbc.core.RowMapper<CartItem> getCartItemRowMapper() {
        return (rs, rowNum) -> {
            CartItem cartItem = new CartItem();
            cartItem.setCartItemId(rs.getLong("cart_item_id"));
            cartItem.setMemberId(rs.getLong("member_id"));
            cartItem.setProductId(rs.getLong("product_id"));
            cartItem.setQuantity(rs.getInt("quantity"));
            cartItem.setUnitPrice(rs.getBigDecimal("unit_price"));
            cartItem.setTotalPrice(rs.getBigDecimal("total_price"));
            cartItem.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
            cartItem.setUdate(rs.getTimestamp("udate").toLocalDateTime());
            
            // 상품 정보 매핑
            if (rs.getLong("p_product_id") != 0) {
                com.kh.demo.domain.product.entity.Products product = new com.kh.demo.domain.product.entity.Products();
                product.setProductId(rs.getLong("p_product_id"));
                product.setPname(rs.getString("pname"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getInt("price"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                product.setRating(rs.getDouble("rating"));
                product.setCategory(rs.getString("category"));
                product.setCdate(rs.getTimestamp("p_cdate").toLocalDateTime());
                product.setUdate(rs.getTimestamp("p_udate").toLocalDateTime());
                
                cartItem.setProduct(product);
            }
            
            return cartItem;
        };
    }
} 