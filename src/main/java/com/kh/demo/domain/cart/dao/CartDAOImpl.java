package com.kh.demo.domain.cart.dao;

import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.cart.dto.CartItemDTO;
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
    public List<CartItemDTO> findDTOByMemberId(Long memberId) {
        String sql = """
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity,
                   ci.sale_price, ci.original_price, ci.discount_rate,
                   (ci.sale_price * ci.quantity) as total_price, ci.cdate, ci.udate,
                   p.product_id as p_product_id, p.pname, p.description, p.price,
                   p.stock_quantity, p.rating, cat.decode as category, p.cdate as p_cdate, p.udate as p_udate
            FROM cart_items ci
            INNER JOIN cart c ON ci.cart_id = c.cart_id
            LEFT JOIN products p ON ci.product_id = p.product_id
            LEFT JOIN code cat ON p.category_id = cat.code_id
            WHERE c.member_id = :memberId
            ORDER BY ci.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.query(sql, params, getCartItemDTORowMapper());
    }
    
    @Override
    public Optional<CartItemDTO> findDTOByMemberIdAndProductId(Long memberId, Long productId) {
        String sql = """
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity,
                   ci.sale_price, ci.original_price, ci.discount_rate,
                   (ci.sale_price * ci.quantity) as total_price, ci.cdate, ci.udate,
                   p.product_id as p_product_id, p.pname, p.description, p.price,
                   p.stock_quantity, p.rating, cat.decode as category, p.cdate as p_cdate, p.udate as p_udate
            FROM cart_items ci
            INNER JOIN cart c ON ci.cart_id = c.cart_id
            LEFT JOIN products p ON ci.product_id = p.product_id
            LEFT JOIN code cat ON p.category_id = cat.code_id
            WHERE c.member_id = :memberId AND ci.product_id = :productId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("productId", productId);
        
        List<CartItemDTO> results = template.query(sql, params, getCartItemDTORowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<CartItem> findByMemberId(Long memberId) {
        String sql = """
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity,
                   ci.sale_price, ci.original_price, ci.discount_rate,
                   (ci.sale_price * ci.quantity) as total_price, ci.cdate, ci.udate
            FROM cart_items ci
            INNER JOIN cart c ON ci.cart_id = c.cart_id
            WHERE c.member_id = :memberId
            ORDER BY ci.cdate DESC
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.query(sql, params, getCartItemRowMapper());
    }
    
    @Override
    public Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId) {
        String sql = """
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity,
                   ci.sale_price, ci.original_price, ci.discount_rate,
                   (ci.sale_price * ci.quantity) as total_price, ci.cdate, ci.udate
            FROM cart_items ci
            INNER JOIN cart c ON ci.cart_id = c.cart_id
            WHERE c.member_id = :memberId AND ci.product_id = :productId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("productId", productId);
        
        List<CartItem> results = template.query(sql, params, getCartItemRowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public int countByMemberId(Long memberId) {
        String sql = """
            SELECT COUNT(*) 
            FROM cart_items ci
            INNER JOIN cart c ON ci.cart_id = c.cart_id
            WHERE c.member_id = :memberId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.queryForObject(sql, params, Integer.class);
    }
    
    @Override
    public Long getTotalAmountByMemberId(Long memberId) {
        String sql = """
            SELECT COALESCE(SUM(ci.sale_price * ci.quantity), 0) 
            FROM cart_items ci
            INNER JOIN cart c ON ci.cart_id = c.cart_id
            WHERE c.member_id = :memberId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.queryForObject(sql, params, Long.class);
    }
    
    @Override
    public int deleteByMemberId(Long memberId) {
        String sql = """
            DELETE FROM cart_items 
            WHERE cart_id IN (
                SELECT cart_id FROM cart WHERE member_id = :memberId
            )
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        return template.update(sql, params);
    }
    
    @Override
    public int updateQuantity(Long cartItemId, Integer quantity) {
        String sql = """
            UPDATE cart_items 
            SET quantity = :quantity, 
                udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId)
                .addValue("quantity", quantity);
        
        return template.update(sql, params);
    }
    
    @Override
    public int updatePriceInfo(Long cartItemId, Long salePrice, Long originalPrice, Double discountRate) {
        String sql = """
            UPDATE cart_items 
            SET sale_price = :salePrice, 
                original_price = :originalPrice, 
                discount_rate = :discountRate, 
                udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId)
                .addValue("salePrice", salePrice)
                .addValue("originalPrice", originalPrice)
                .addValue("discountRate", discountRate);
        
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
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity,
                   ci.sale_price, ci.original_price, ci.discount_rate,
                   (ci.sale_price * ci.quantity) as total_price, ci.cdate, ci.udate
            FROM cart_items ci
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
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity,
                   ci.sale_price, ci.original_price, ci.discount_rate,
                   (ci.sale_price * ci.quantity) as total_price, ci.cdate, ci.udate
            FROM cart_items ci
            ORDER BY ci.cdate DESC
            """;
        
        return template.query(sql, getCartItemRowMapper());
    }
    
    @Override
    public int updateById(Long cartItemId, CartItem cartItem) {
        String sql = """
            UPDATE cart_items 
            SET product_id = :productId, quantity = :quantity, udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId)
                .addValue("productId", cartItem.getProductId())
                .addValue("quantity", cartItem.getQuantity());
        
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

    @Override
    public List<CartItem> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT cart_item_id, cart_id, product_id, quantity,
                   sale_price, original_price, discount_rate,
                   (sale_price * quantity) as total_price, cdate, udate
            FROM cart_items
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH FIRST :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, getCartItemRowMapper());
    }
    
    @Override
    public Optional<Long> findCartIdByMemberId(Long memberId) {
        String sql = "SELECT cart_id FROM cart WHERE member_id = :memberId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        try {
            Long cartId = template.queryForObject(sql, params, Long.class);
            return Optional.ofNullable(cartId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<Long> findMemberIdByCartId(Long cartId) {
        String sql = "SELECT member_id FROM cart WHERE cart_id = :cartId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartId", cartId);
        
        try {
            Long memberId = template.queryForObject(sql, params, Long.class);
            return Optional.ofNullable(memberId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Long createCart(Long memberId) {
        String sql = """
            INSERT INTO cart (member_id, cdate, udate)
            VALUES (:memberId, SYSDATE, SYSDATE)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder, new String[]{"cart_id"});
        
        return keyHolder.getKey().longValue();
    }
    
    private Long insert(CartItem cartItem) {
        String sql = """
            INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
            VALUES (seq_cart_item_id.nextval, :cartId, :productId, :quantity, :salePrice, :originalPrice, :discountRate, SYSDATE, SYSDATE)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartId", cartItem.getCartId())
                .addValue("productId", cartItem.getProductId())
                .addValue("quantity", cartItem.getQuantity())
                .addValue("salePrice", cartItem.getSalePrice())
                .addValue("originalPrice", cartItem.getOriginalPrice())
                .addValue("discountRate", cartItem.getDiscountRate());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder, new String[]{"cart_item_id"});
        
        return keyHolder.getKey().longValue();
    }
    
    private void update(CartItem cartItem) {
        String sql = """
            UPDATE cart_items 
            SET product_id = :productId, quantity = :quantity, 
                sale_price = :salePrice, original_price = :originalPrice, discount_rate = :discountRate, 
                udate = SYSDATE
            WHERE cart_item_id = :cartItemId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItem.getCartItemId())
                .addValue("productId", cartItem.getProductId())
                .addValue("quantity", cartItem.getQuantity())
                .addValue("salePrice", cartItem.getSalePrice())
                .addValue("originalPrice", cartItem.getOriginalPrice())
                .addValue("discountRate", cartItem.getDiscountRate());
        
        template.update(sql, params);
    }
    
    private org.springframework.jdbc.core.RowMapper<CartItem> getCartItemRowMapper() {
        return (rs, rowNum) -> {
            CartItem cartItem = new CartItem();
            cartItem.setCartItemId(rs.getLong("cart_item_id"));
            cartItem.setCartId(rs.getLong("cart_id"));
            cartItem.setProductId(rs.getLong("product_id"));
            cartItem.setQuantity(rs.getInt("quantity"));
            cartItem.setSalePrice(rs.getBigDecimal("sale_price"));
            cartItem.setOriginalPrice(rs.getBigDecimal("original_price"));
            cartItem.setDiscountRate(rs.getBigDecimal("discount_rate"));
            cartItem.setTotalPrice(rs.getBigDecimal("total_price"));
            cartItem.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
            cartItem.setUdate(rs.getTimestamp("udate").toLocalDateTime());
            
            return cartItem;
        };
    }
    
    private org.springframework.jdbc.core.RowMapper<CartItemDTO> getCartItemDTORowMapper() {
        return (rs, rowNum) -> {
            CartItemDTO cartItemDTO = new CartItemDTO();
            cartItemDTO.setCartItemId(rs.getLong("cart_item_id"));
            cartItemDTO.setCartId(rs.getLong("cart_id"));
            cartItemDTO.setProductId(rs.getLong("product_id"));
            cartItemDTO.setQuantity(rs.getInt("quantity"));
            cartItemDTO.setSalePrice(rs.getBigDecimal("sale_price"));
            cartItemDTO.setOriginalPrice(rs.getBigDecimal("original_price"));
            cartItemDTO.setDiscountRate(rs.getBigDecimal("discount_rate"));
            cartItemDTO.setTotalPrice(rs.getBigDecimal("total_price"));
            cartItemDTO.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
            cartItemDTO.setUdate(rs.getTimestamp("udate").toLocalDateTime());
            
            // 상품 정보 매핑 (평면화)
            if (rs.getLong("p_product_id") != 0) {
                cartItemDTO.setPname(rs.getString("pname"));
                cartItemDTO.setDescription(rs.getString("description"));
                cartItemDTO.setProductPrice(rs.getInt("price"));
                cartItemDTO.setStockQuantity(rs.getInt("stock_quantity"));
                cartItemDTO.setRating(rs.getDouble("rating"));
                cartItemDTO.setCategory(rs.getString("category"));
            }
            
            return cartItemDTO;
        };
    }
} 