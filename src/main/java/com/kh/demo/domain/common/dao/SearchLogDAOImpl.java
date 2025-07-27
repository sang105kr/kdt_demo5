package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.SearchLog;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 검색 로그 데이터 접근 객체 구현체
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class SearchLogDAOImpl implements SearchLogDAO {

    private final NamedParameterJdbcTemplate template;

    // RowMapper 정의
    private final RowMapper<SearchLog> searchLogRowMapper = (ResultSet rs, int rowNum) -> {
        SearchLog searchLog = new SearchLog();
        searchLog.setSearchLogId(rs.getLong("search_log_id"));
        searchLog.setMemberId(rs.getObject("member_id", Long.class));
        searchLog.setKeyword(rs.getString("keyword"));
        searchLog.setSearchTypeId(rs.getObject("search_type_id", Long.class));
        searchLog.setResultCount(rs.getObject("result_count", Integer.class));
        searchLog.setSearchIp(rs.getString("search_ip"));
        searchLog.setCdate(rs.getObject("cdate", LocalDateTime.class));
        searchLog.setUdate(rs.getObject("udate", LocalDateTime.class));
        return searchLog;
    };

    @Override
    public Long save(SearchLog entity) {
        String sql = """
            INSERT INTO search_logs (
                search_log_id, member_id, keyword, search_type_id, 
                result_count, search_ip, cdate, udate
            ) VALUES (
                seq_search_log_id.NEXTVAL, :memberId, :keyword, :searchTypeId, 
                :resultCount, :searchIp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
            """;
        
        SqlParameterSource param = new BeanPropertySqlParameterSource(entity);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        template.update(sql, param, keyHolder, new String[]{"search_log_id"});
        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<SearchLog> findById(Long id) {
        String sql = """
            SELECT search_log_id, member_id, keyword, search_type_id, 
                   result_count, search_ip, cdate, udate
            FROM search_logs 
            WHERE search_log_id = :searchLogId
            """;
        
        try {
            SearchLog searchLog = template.queryForObject(sql, 
                Map.of("searchLogId", id), searchLogRowMapper);
            return Optional.of(searchLog);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SearchLog> findAll() {
        String sql = """
            SELECT search_log_id, member_id, keyword, search_type_id, 
                   result_count, search_ip, cdate, udate
            FROM search_logs 
            ORDER BY cdate DESC
            """;
        
        return template.query(sql, searchLogRowMapper);
    }

    @Override
    public int updateById(Long id, SearchLog entity) {
        String sql = """
            UPDATE search_logs 
            SET keyword = :keyword,
                search_type_id = :searchTypeId,
                result_count = :resultCount,
                search_ip = :searchIp,
                udate = CURRENT_TIMESTAMP
            WHERE search_log_id = :searchLogId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("searchLogId", id)
            .addValue("keyword", entity.getKeyword())
            .addValue("searchTypeId", entity.getSearchTypeId())
            .addValue("resultCount", entity.getResultCount())
            .addValue("searchIp", entity.getSearchIp());
        
        return template.update(sql, param);
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM search_logs WHERE search_log_id = :searchLogId";
        return template.update(sql, Map.of("searchLogId", id));
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM search_logs";
        Integer count = template.queryForObject(sql, Map.of(), Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public List<SearchLog> findAllWithOffset(int offset, int limit) {
        String sql = """
            SELECT search_log_id, member_id, keyword, search_type_id, 
                   result_count, search_ip, cdate, udate
            FROM search_logs 
            ORDER BY cdate DESC
            OFFSET :offset ROWS 
            FETCH FIRST :limit ROWS ONLY
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("offset", offset)
            .addValue("limit", limit);
        
        return template.query(sql, param, searchLogRowMapper);
    }

    @Override
    public Long saveSearchLog(Long memberId, String keyword, String searchType, Integer resultCount, String searchIp) {
        // 검색 타입 코드를 code_id로 변환
        Long searchTypeId = getSearchTypeCodeId(searchType != null ? searchType : "PRODUCT");
        
        String sql = """
            INSERT INTO search_logs (
                search_log_id, member_id, keyword, search_type_id, 
                result_count, search_ip, cdate
            ) VALUES (
                seq_search_log_id.NEXTVAL, :memberId, :keyword, :searchTypeId, 
                :resultCount, :searchIp, CURRENT_TIMESTAMP
            )
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("keyword", keyword != null ? keyword.trim() : null)
            .addValue("searchTypeId", searchTypeId)
            .addValue("resultCount", resultCount != null ? resultCount : 0)
            .addValue("searchIp", searchIp);
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder, new String[]{"search_log_id"});
        
        return keyHolder.getKey().longValue();
    }
    
    /**
     * 검색 타입 코드를 code_id로 변환
     */
    private Long getSearchTypeCodeId(String searchType) {
        String sql = """
            SELECT code_id 
            FROM code 
            WHERE gcode = 'SEARCH_TYPE' 
              AND code = :searchType
              AND use_yn = 'Y'
            """;
        
        try {
            return template.queryForObject(sql, Map.of("searchType", searchType), Long.class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("검색 타입 코드를 찾을 수 없음: {}, 기본값 PRODUCT 사용", searchType);
            // 기본값으로 PRODUCT 사용
            return template.queryForObject(sql, Map.of("searchType", "PRODUCT"), Long.class);
        }
    }

    @Override
    public List<String> getPopularKeywordsFromOracle(int days, int limit) {
        String sql = """
            SELECT keyword
            FROM search_logs 
            WHERE cdate >= TRUNC(SYSDATE) - :days
              AND keyword IS NOT NULL
              AND TRIM(keyword) != ''
            GROUP BY keyword
            ORDER BY COUNT(*) DESC, keyword ASC
            FETCH FIRST :limit ROWS ONLY
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("days", days)
            .addValue("limit", limit);
        
        return template.queryForList(sql, param, String.class);
    }

    @Override
    public List<String> getPopularKeywordsFromOracle(int limit) {
        return getPopularKeywordsFromOracle(7, limit); // 기본 7일
    }

    @Override
    public List<String> getMemberSearchHistory(Long memberId, int limit) {
        String sql = """
            SELECT keyword
            FROM search_logs 
            WHERE member_id = :memberId
              AND TRIM(keyword) IS NOT NULL
            GROUP BY keyword
            ORDER BY MAX(cdate) DESC
            FETCH FIRST :limit ROWS ONLY
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("limit", limit);
        
        return template.queryForList(sql, param, String.class);
    }

    @Override
    public int deleteOldSearchLogs(int daysToKeep) {
        String sql = """
            DELETE FROM search_logs 
            WHERE cdate < TRUNC(SYSDATE) - :daysToKeep
            """;
        
        return template.update(sql, Map.of("daysToKeep", daysToKeep));
    }

    @Override
    public int mergeDuplicateSearchLogs() {
        // 같은 사용자의 같은 키워드를 하나로 합치고 최신 날짜로 유지
        String sql = """
            DELETE FROM search_logs s1
            WHERE s1.search_log_id NOT IN (
                SELECT MAX(s2.search_log_id)
                FROM search_logs s2
                WHERE s2.member_id = s1.member_id 
                  AND s2.keyword = s1.keyword
                  AND s2.cdate >= TRUNC(SYSDATE) - 1  -- 최근 1일 내
            )
            AND EXISTS (
                SELECT 1 FROM search_logs s3
                WHERE s3.member_id = s1.member_id
                  AND s3.keyword = s1.keyword
                  AND s3.search_log_id != s1.search_log_id
                  AND s3.cdate >= TRUNC(SYSDATE) - 1
            )
            """;
        
        return template.update(sql, Map.of());
    }

    @Override
    public Map<String, Integer> getSearchStatsByPeriod(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT keyword, COUNT(*) as search_count
            FROM search_logs
            WHERE cdate BETWEEN :fromDate AND :toDate
              AND keyword IS NOT NULL
              AND TRIM(keyword) != ''
            GROUP BY keyword
            ORDER BY search_count DESC
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("fromDate", Timestamp.valueOf(from))
            .addValue("toDate", Timestamp.valueOf(to));
        
        return template.query(sql, param, rs -> {
            Map<String, Integer> stats = new LinkedHashMap<>();
            while (rs.next()) {
                stats.put(rs.getString("keyword"), rs.getInt("search_count"));
            }
            return stats;
        });
    }

    @Override
    public int getKeywordSearchCount(String keyword, int days) {
        String sql = """
            SELECT COUNT(*) 
            FROM search_logs
            WHERE keyword = :keyword
              AND cdate >= TRUNC(SYSDATE) - :days
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("keyword", keyword)
            .addValue("days", days);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public int getMemberSearchCount(Long memberId, int days) {
        String sql = """
            SELECT COUNT(*) 
            FROM search_logs
            WHERE member_id = :memberId
              AND cdate >= TRUNC(SYSDATE) - :days
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("days", days);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public int getSearchCountByType(String searchType, int days) {
        String sql = """
            SELECT COUNT(*) 
            FROM search_logs sl
            JOIN code c ON sl.search_type_id = c.code_id
            WHERE c.gcode = 'SEARCH_TYPE'
              AND c.code = :searchType
              AND c.use_yn = 'Y'
              AND sl.cdate >= TRUNC(SYSDATE) - :days
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("searchType", searchType)
            .addValue("days", days);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public int getTotalSearchCountByDays(int days) {
        String sql = """
            SELECT COUNT(*) 
            FROM search_logs
            WHERE cdate >= TRUNC(SYSDATE) - :days
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("days", days);
        
        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public int clearMemberSearchHistory(Long memberId) {
        String sql = """
            DELETE FROM search_logs 
            WHERE member_id = :memberId
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("memberId", memberId);
        
        return template.update(sql, param);
    }
    
    @Override
    public int deleteMemberSearchHistoryItem(Long memberId, String keyword) {
        String sql = """
            DELETE FROM search_logs 
            WHERE member_id = :memberId 
              AND keyword = :keyword
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("memberId", memberId)
            .addValue("keyword", keyword);
        
        return template.update(sql, param);
    }
} 