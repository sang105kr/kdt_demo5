package com.kh.demo.domain.faq.dao;

import com.kh.demo.domain.faq.entity.Faq;
import com.kh.demo.domain.faq.dto.FaqDTO;
import com.kh.demo.domain.common.base.BaseDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
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
public class FaqDAOImpl implements FaqDAO {

    private final NamedParameterJdbcTemplate template;

    // FAQ RowMapper 정의
    private final RowMapper<Faq> faqRowMapper = (rs, rowNum) -> {
        Faq faq = new Faq();
        faq.setFaqId(rs.getLong("faq_id"));
        faq.setCategoryId(rs.getLong("category_id"));
        faq.setQuestion(rs.getString("question"));
        faq.setAnswer(rs.getString("answer"));
        faq.setKeywords(rs.getString("keywords"));
        faq.setViewCount(rs.getInt("view_count"));
        faq.setHelpfulCount(rs.getInt("helpful_count"));
        faq.setUnhelpfulCount(rs.getInt("unhelpful_count"));
        faq.setSortOrder(rs.getInt("sort_order"));
        faq.setIsActive(rs.getString("is_active"));
        faq.setAdminId(rs.getObject("admin_id", Long.class));
        faq.setCdate(rs.getObject("cdate", java.time.LocalDateTime.class));
        faq.setUdate(rs.getObject("udate", java.time.LocalDateTime.class));
        return faq;
    };

    // FAQ DTO RowMapper 정의 (조인 데이터 포함)
    private final RowMapper<FaqDTO> faqDTORowMapper = (rs, rowNum) -> {
        FaqDTO faqDTO = new FaqDTO();
        faqDTO.setFaqId(rs.getLong("faq_id"));
        faqDTO.setCategoryId(rs.getLong("category_id"));
        faqDTO.setQuestion(rs.getString("question"));
        faqDTO.setAnswer(rs.getString("answer"));
        faqDTO.setKeywords(rs.getString("keywords"));
        faqDTO.setViewCount(rs.getInt("view_count"));
        faqDTO.setHelpfulCount(rs.getInt("helpful_count"));
        faqDTO.setUnhelpfulCount(rs.getInt("unhelpful_count"));
        faqDTO.setSortOrder(rs.getInt("sort_order"));
        faqDTO.setIsActive(rs.getString("is_active"));
        faqDTO.setAdminId(rs.getObject("admin_id", Long.class));
        faqDTO.setCdate(rs.getObject("cdate", java.time.LocalDateTime.class));
        faqDTO.setUdate(rs.getObject("udate", java.time.LocalDateTime.class));
        
        // 조인으로 가져온 데이터
        try {
            faqDTO.setCategoryName(rs.getString("category_name"));
        } catch (Exception e) {
            faqDTO.setCategoryName(null);
        }
        
        try {
            faqDTO.setAdminNickname(rs.getString("admin_nickname"));
        } catch (Exception e) {
            faqDTO.setAdminNickname(null);
        }
        
        return faqDTO;
    };

    @Override
    public Long save(Faq faq) {
        String sql = """
            INSERT INTO faq (
                faq_id, category_id, question, answer, keywords, 
                view_count, helpful_count, unhelpful_count, sort_order, 
                is_active, admin_id, cdate, udate
            ) VALUES (
                seq_faq_id.NEXTVAL, :categoryId, :question, :answer, :keywords,
                :viewCount, :helpfulCount, :unhelpfulCount, :sortOrder,
                :isActive, :adminId, SYSTIMESTAMP, SYSTIMESTAMP
            )
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("categoryId", faq.getCategoryId())
            .addValue("question", faq.getQuestion())
            .addValue("answer", faq.getAnswer())
            .addValue("keywords", faq.getKeywords())
            .addValue("viewCount", faq.getViewCount())
            .addValue("helpfulCount", faq.getHelpfulCount())
            .addValue("unhelpfulCount", faq.getUnhelpfulCount())
            .addValue("sortOrder", faq.getSortOrder())
            .addValue("isActive", faq.getIsActive())
            .addValue("adminId", faq.getAdminId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder, new String[]{"faq_id"});
        
        return keyHolder.getKey().longValue();
    }

    @Override
    public int updateById(Long faqId, Faq faq) {
        String sql = """
            UPDATE faq SET 
                category_id = :categoryId,
                question = :question,
                answer = :answer,
                keywords = :keywords,
                view_count = :viewCount,
                helpful_count = :helpfulCount,
                unhelpful_count = :unhelpfulCount,
                sort_order = :sortOrder,
                is_active = :isActive,
                admin_id = :adminId,
                udate = SYSTIMESTAMP
            WHERE faq_id = :faqId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("faqId", faqId)
            .addValue("categoryId", faq.getCategoryId())
            .addValue("question", faq.getQuestion())
            .addValue("answer", faq.getAnswer())
            .addValue("keywords", faq.getKeywords())
            .addValue("viewCount", faq.getViewCount())
            .addValue("helpfulCount", faq.getHelpfulCount())
            .addValue("unhelpfulCount", faq.getUnhelpfulCount())
            .addValue("sortOrder", faq.getSortOrder())
            .addValue("isActive", faq.getIsActive())
            .addValue("adminId", faq.getAdminId());

        return template.update(sql, params);
    }

    @Override
    public int deleteById(Long faqId) {
        String sql = "DELETE FROM faq WHERE faq_id = :faqId";
        MapSqlParameterSource params = new MapSqlParameterSource("faqId", faqId);
        return template.update(sql, params);
    }

    @Override
    public Optional<Faq> findById(Long faqId) {
        String sql = "SELECT * FROM faq WHERE faq_id = :faqId";
        MapSqlParameterSource params = new MapSqlParameterSource("faqId", faqId);
        
        try {
            Faq faq = template.queryForObject(sql, params, faqRowMapper);
            return Optional.ofNullable(faq);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Faq> findAll() {
        String sql = "SELECT * FROM faq ORDER BY sort_order ASC, cdate DESC";
        return template.query(sql, faqRowMapper);
    }

    @Override
    public List<Faq> findByCategoryId(Long categoryId, int offset, int limit) {
        String sql = """
            SELECT * FROM faq 
            WHERE category_id = :categoryId AND is_active = 'Y'
            ORDER BY sort_order ASC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("categoryId", categoryId)
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, faqRowMapper);
    }

    @Override
    public int countByCategoryId(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM faq WHERE category_id = :categoryId AND is_active = 'Y'";
        MapSqlParameterSource params = new MapSqlParameterSource("categoryId", categoryId);
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Faq> findByKeyword(String keyword, int offset, int limit) {
        String sql = """
            SELECT * FROM faq 
            WHERE (question LIKE :keyword OR answer LIKE :keyword OR keywords LIKE :keyword) 
            AND is_active = 'Y'
            ORDER BY sort_order ASC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("keyword", "%" + keyword + "%")
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, faqRowMapper);
    }

    @Override
    public int countByKeyword(String keyword) {
        String sql = """
            SELECT COUNT(*) FROM faq 
            WHERE (question LIKE :keyword OR answer LIKE :keyword OR keywords LIKE :keyword) 
            AND is_active = 'Y'
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("keyword", "%" + keyword + "%");
        return template.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Faq> findActiveWithPaging(int offset, int limit) {
        String sql = """
            SELECT * FROM faq 
            WHERE is_active = 'Y'
            ORDER BY sort_order ASC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, faqRowMapper);
    }

    @Override
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM faq WHERE is_active = 'Y'";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public List<Faq> findAllBySortOrder(int offset, int limit) {
        String sql = """
            SELECT * FROM faq 
            WHERE is_active = 'Y'
            ORDER BY sort_order ASC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, faqRowMapper);
    }

    @Override
    public List<Faq> findAllByViewCount(int offset, int limit) {
        String sql = """
            SELECT * FROM faq 
            WHERE is_active = 'Y'
            ORDER BY view_count DESC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, faqRowMapper);
    }

    @Override
    public int incrementViewCount(Long faqId) {
        String sql = "UPDATE faq SET view_count = view_count + 1 WHERE faq_id = :faqId";
        MapSqlParameterSource params = new MapSqlParameterSource("faqId", faqId);
        return template.update(sql, params);
    }

    @Override
    public int incrementHelpfulCount(Long faqId) {
        String sql = "UPDATE faq SET helpful_count = helpful_count + 1 WHERE faq_id = :faqId";
        MapSqlParameterSource params = new MapSqlParameterSource("faqId", faqId);
        return template.update(sql, params);
    }

    @Override
    public int incrementUnhelpfulCount(Long faqId) {
        String sql = "UPDATE faq SET unhelpful_count = unhelpful_count + 1 WHERE faq_id = :faqId";
        MapSqlParameterSource params = new MapSqlParameterSource("faqId", faqId);
        return template.update(sql, params);
    }

    @Override
    public int updateActiveStatus(Long faqId, String isActive) {
        String sql = "UPDATE faq SET is_active = :isActive, udate = SYSTIMESTAMP WHERE faq_id = :faqId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("faqId", faqId)
            .addValue("isActive", isActive);
        return template.update(sql, params);
    }

    @Override
    public int updateSortOrder(Long faqId, Integer sortOrder) {
        String sql = "UPDATE faq SET sort_order = :sortOrder, udate = SYSTIMESTAMP WHERE faq_id = :faqId";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("faqId", faqId)
            .addValue("sortOrder", sortOrder);
        return template.update(sql, params);
    }

    // DTO 조회 메서드들 (조인 데이터 포함)
    public Optional<FaqDTO> findByIdWithJoin(Long faqId) {
        String sql = """
            SELECT f.*, c.decode as category_name, m.nickname as admin_nickname
            FROM faq f
            LEFT JOIN code c ON f.category_id = c.code_id
            LEFT JOIN member m ON f.admin_id = m.member_id
            WHERE f.faq_id = :faqId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("faqId", faqId);
        
        try {
            FaqDTO faqDTO = template.queryForObject(sql, params, faqDTORowMapper);
            return Optional.ofNullable(faqDTO);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<FaqDTO> findActiveWithJoin(int offset, int limit) {
        String sql = """
            SELECT f.*, c.decode as category_name, m.nickname as admin_nickname
            FROM faq f
            LEFT JOIN code c ON f.category_id = c.code_id
            LEFT JOIN member m ON f.admin_id = m.member_id
            WHERE f.is_active = 'Y'
            ORDER BY f.sort_order ASC, f.cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, faqDTORowMapper);
    }

    // BaseDAO 추상 메서드 구현
    @Override
    public List<Faq> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT * FROM faq 
            ORDER BY sort_order ASC, cdate DESC
            OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, params, faqRowMapper);
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM faq";
        return template.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }
}
