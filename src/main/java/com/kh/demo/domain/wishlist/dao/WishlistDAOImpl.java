package com.kh.demo.domain.wishlist.dao;

import com.kh.demo.domain.wishlist.entity.Wishlist;
import com.kh.demo.domain.wishlist.dto.WishlistItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 위시리스트 Repository 구현체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WishlistDAOImpl implements WishlistDAO {

    private final NamedParameterJdbcTemplate template;

    @Override
    public List<WishlistItemDto> findWishlistItemsByMemberId(Long memberId) {
        String sql = """
            SELECT w.wishlist_id, w.member_id, w.product_id, w.cdate, w.udate,
                   p.pname as product_name, p.price as product_price, p.category as product_category,
                   m.nickname as member_nickname
            FROM wishlist w
            LEFT JOIN products p ON w.product_id = p.product_id
            LEFT JOIN member m ON w.member_id = m.member_id
            WHERE w.member_id = :memberId
            ORDER BY w.cdate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, params, this::mapRowDto);
    }

    @Override
    public List<WishlistItemDto> findWishlistItemsByMemberId(Long memberId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        
        String sql = """
            SELECT w.wishlist_id, w.member_id, w.product_id, w.cdate, w.udate,
                   p.pname as product_name, p.price as product_price, p.category as product_category,
                   m.nickname as member_nickname
            FROM wishlist w
            LEFT JOIN products p ON w.product_id = p.product_id
            LEFT JOIN member m ON w.member_id = m.member_id
            WHERE w.member_id = :memberId
            ORDER BY w.cdate DESC
            OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);

        return template.query(sql, params, this::mapRowDto);
    }

    @Override
    public Long save(Wishlist wishlist) {
        String sql = """
            INSERT INTO wishlist (wishlist_id, member_id, product_id, cdate, udate)
            VALUES (seq_wishlist_id.nextval, :memberId, :productId, :cdate, :udate)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", wishlist.getMemberId())
                .addValue("productId", wishlist.getProductId())
                .addValue("cdate", wishlist.getCdate())
                .addValue("udate", wishlist.getUdate());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder, new String[]{"wishlist_id"});

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<Wishlist> findById(Long wishlistId) {
        String sql = """
            SELECT wishlist_id, member_id, product_id, cdate, udate
            FROM wishlist
            WHERE wishlist_id = :wishlistId
            """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource("wishlistId", wishlistId);
            Wishlist wishlist = template.queryForObject(sql, params, this::mapRow);
            return Optional.of(wishlist);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Wishlist> findAll() {
        String sql = """
            SELECT wishlist_id, member_id, product_id, cdate, udate
            FROM wishlist
            ORDER BY cdate DESC
            """;

        return template.query(sql, this::mapRow);
    }

    @Override
    public int updateById(Long wishlistId, Wishlist wishlist) {
        String sql = """
            UPDATE wishlist
            SET member_id = :memberId,
                product_id = :productId,
                udate = :udate
            WHERE wishlist_id = :wishlistId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("wishlistId", wishlistId)
                .addValue("memberId", wishlist.getMemberId())
                .addValue("productId", wishlist.getProductId())
                .addValue("udate", LocalDateTime.now());

        return template.update(sql, params);
    }

    @Override
    public int deleteById(Long wishlistId) {
        String sql = "DELETE FROM wishlist WHERE wishlist_id = :wishlistId";
        MapSqlParameterSource params = new MapSqlParameterSource("wishlistId", wishlistId);
        return template.update(sql, params);
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM wishlist";
        Integer count = template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public List<Wishlist> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT wishlist_id, member_id, product_id, cdate, udate
            FROM wishlist
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", limit);

        return template.query(sql, params, this::mapRow);
    }

    // 편의 메서드들
    public void deleteAll() {
        String sql = "DELETE FROM wishlist";
        template.update(sql, new MapSqlParameterSource());
    }

    public boolean existsById(Long wishlistId) {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE wishlist_id = :wishlistId";
        MapSqlParameterSource params = new MapSqlParameterSource("wishlistId", wishlistId);
        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<Wishlist> findByMemberId(Long memberId) {
        String sql = """
            SELECT wishlist_id, member_id, product_id, cdate, udate
            FROM wishlist
            WHERE member_id = :memberId
            ORDER BY cdate DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return template.query(sql, params, this::mapRow);
    }

    @Override
    public List<Wishlist> findByMemberId(Long memberId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        
        String sql = """
            SELECT wishlist_id, member_id, product_id, cdate, udate
            FROM wishlist
            WHERE member_id = :memberId
            ORDER BY cdate DESC
            OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);

        return template.query(sql, params, this::mapRow);
    }

    @Override
    public int countByMemberId(Long memberId) {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE member_id = :memberId";
        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Wishlist findByMemberIdAndProductId(Long memberId, Long productId) {
        String sql = """
            SELECT wishlist_id, member_id, product_id, cdate, udate
            FROM wishlist
            WHERE member_id = :memberId AND product_id = :productId
            """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("memberId", memberId)
                    .addValue("productId", productId);
            return template.queryForObject(sql, params, this::mapRow);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean existsByMemberIdAndProductId(Long memberId, Long productId) {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE member_id = :memberId AND product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("productId", productId);
        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public Long addWishlist(Long memberId, Long productId) {
        // 중복 체크
        if (existsByMemberIdAndProductId(memberId, productId)) {
            throw new RuntimeException("이미 위시리스트에 추가된 상품입니다.");
        }

        Wishlist wishlist = Wishlist.builder()
                .memberId(memberId)
                .productId(productId)
                .cdate(LocalDateTime.now())
                .udate(LocalDateTime.now())
                .build();

        return save(wishlist);
    }

    @Override
    public int removeWishlist(Long memberId, Long productId) {
        String sql = "DELETE FROM wishlist WHERE member_id = :memberId AND product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("productId", productId);

        return template.update(sql, params);
    }

    @Override
    public int removeAllByMemberId(Long memberId) {
        String sql = "DELETE FROM wishlist WHERE member_id = :memberId";
        MapSqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return template.update(sql, params);
    }

    @Override
    public int removeAllByProductId(Long productId) {
        String sql = "DELETE FROM wishlist WHERE product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource("productId", productId);
        return template.update(sql, params);
    }

    /**
     * ResultSet을 Wishlist 객체로 매핑 (순수 엔티티)
     */
    private Wishlist mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Wishlist.builder()
                .wishlistId(rs.getLong("wishlist_id"))
                .memberId(rs.getLong("member_id"))
                .productId(rs.getLong("product_id"))
                .cdate(rs.getTimestamp("cdate").toLocalDateTime())
                .udate(rs.getTimestamp("udate").toLocalDateTime())
                .build();
    }

    /**
     * ResultSet을 WishlistItemDto 객체로 매핑 (조인 데이터 포함)
     */
    private WishlistItemDto mapRowDto(ResultSet rs, int rowNum) throws SQLException {
        return WishlistItemDto.builder()
                .wishlistId(rs.getLong("wishlist_id"))
                .memberId(rs.getLong("member_id"))
                .productId(rs.getLong("product_id"))
                .cdate(rs.getTimestamp("cdate").toLocalDateTime())
                .udate(rs.getTimestamp("udate").toLocalDateTime())
                .productName(rs.getString("product_name"))
                .productPrice(rs.getObject("product_price", Integer.class))
                .productCategory(rs.getString("product_category"))
                .memberNickname(rs.getString("member_nickname"))
                .build();
    }
} 