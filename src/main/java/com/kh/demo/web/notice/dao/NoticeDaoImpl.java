package com.kh.demo.web.notice.dao;

import com.kh.demo.web.notice.dto.NoticeDto;
import com.kh.demo.web.notice.dto.NoticeSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NoticeDaoImpl implements NoticeDao {
    
    private final NamedParameterJdbcTemplate template;
    
    // RowMapper 정의
    private final RowMapper<NoticeDto> noticeRowMapper = BeanPropertyRowMapper.newInstance(NoticeDto.class);
    
    @Override
    public List<NoticeDto> findNotices(NoticeSearchDto searchDto) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT n.notice_id, n.category_id, c.decode as category_name, ");
        sql.append("       n.title, n.content, n.author_id, m.nickname as author_name, ");
        sql.append("       n.view_count, n.is_important, n.is_fixed, ");
        sql.append("       n.start_date, n.end_date, n.status_id, s.decode as status_name, ");
        sql.append("       n.cdate, n.udate ");
        sql.append("FROM notices n ");
        sql.append("LEFT JOIN code c ON n.category_id = c.code_id ");
        sql.append("LEFT JOIN member m ON n.author_id = m.member_id ");
        sql.append("LEFT JOIN code s ON n.status_id = s.code_id ");
        sql.append("WHERE 1=1 ");
        sql.append("AND n.status_id = (SELECT code_id FROM code WHERE gcode = 'NOTICE_STATUS' AND code = 'ACTIVE') ");
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        
        // 검색 조건 추가
        if (searchDto.getSearchKeyword() != null && !searchDto.getSearchKeyword().trim().isEmpty()) {
            if ("title".equals(searchDto.getSearchType())) {
                sql.append("AND n.title LIKE :searchKeyword ");
                param.addValue("searchKeyword", "%" + searchDto.getSearchKeyword() + "%");
            } else if ("content".equals(searchDto.getSearchType())) {
                sql.append("AND n.content LIKE :searchKeyword ");
                param.addValue("searchKeyword", "%" + searchDto.getSearchKeyword() + "%");
            } else {
                sql.append("AND (n.title LIKE :searchKeyword OR n.content LIKE :searchKeyword) ");
                param.addValue("searchKeyword", "%" + searchDto.getSearchKeyword() + "%");
            }
        }
        
        if (searchDto.getCategoryId() != null) {
            sql.append("AND n.category_id = :categoryId ");
            param.addValue("categoryId", searchDto.getCategoryId());
        }
        
        if (searchDto.getIsImportant() != null && !searchDto.getIsImportant().trim().isEmpty()) {
            sql.append("AND n.is_important = :isImportant ");
            param.addValue("isImportant", searchDto.getIsImportant());
        }
        
        if (searchDto.getIsFixed() != null && !searchDto.getIsFixed().trim().isEmpty()) {
            sql.append("AND n.is_fixed = :isFixed ");
            param.addValue("isFixed", searchDto.getIsFixed());
        }
        
        // status_id 필터링 제거 (위에서 ACTIVE 상태로 고정)
        // if (searchDto.getStatusId() != null) {
        //     sql.append("AND n.status_id = :statusId ");
        //     param.addValue("statusId", searchDto.getStatusId());
        // }
        
        // 정렬 조건
        sql.append("ORDER BY ");
        if ("view_count".equals(searchDto.getSortBy())) {
            sql.append("n.view_count ");
        } else if ("title".equals(searchDto.getSortBy())) {
            sql.append("n.title ");
        } else {
            sql.append("n.is_important DESC, n.is_fixed DESC, n.cdate ");
        }
        sql.append(searchDto.getSortOrder().toUpperCase());
        
        // 페이징
        sql.append(" OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY");
        param.addValue("offset", searchDto.getOffset());
        param.addValue("pageSize", searchDto.getPageSize());
        
        log.info("실행될 SQL: {}", sql.toString());
        log.info("파라미터: {}", param.getValues());
        
        return template.query(sql.toString(), param, noticeRowMapper);
    }
    
    @Override
    public int countNotices(NoticeSearchDto searchDto) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(*) FROM notices n ");
        sql.append("WHERE 1=1 ");
        sql.append("AND n.status_id = (SELECT code_id FROM code WHERE gcode = 'NOTICE_STATUS' AND code = 'ACTIVE') ");
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        
        // 검색 조건 추가
        if (searchDto.getSearchKeyword() != null && !searchDto.getSearchKeyword().trim().isEmpty()) {
            if ("title".equals(searchDto.getSearchType())) {
                sql.append("AND n.title LIKE :searchKeyword ");
                param.addValue("searchKeyword", "%" + searchDto.getSearchKeyword() + "%");
            } else if ("content".equals(searchDto.getSearchType())) {
                sql.append("AND n.content LIKE :searchKeyword ");
                param.addValue("searchKeyword", "%" + searchDto.getSearchKeyword() + "%");
            } else {
                sql.append("AND (n.title LIKE :searchKeyword OR n.content LIKE :searchKeyword) ");
                param.addValue("searchKeyword", "%" + searchDto.getSearchKeyword() + "%");
            }
        }
        
        if (searchDto.getCategoryId() != null) {
            sql.append("AND n.category_id = :categoryId ");
            param.addValue("categoryId", searchDto.getCategoryId());
        }
        
        if (searchDto.getIsImportant() != null && !searchDto.getIsImportant().trim().isEmpty()) {
            sql.append("AND n.is_important = :isImportant ");
            param.addValue("isImportant", searchDto.getIsImportant());
        }
        
        if (searchDto.getIsFixed() != null && !searchDto.getIsFixed().trim().isEmpty()) {
            sql.append("AND n.is_fixed = :isFixed ");
            param.addValue("isFixed", searchDto.getIsFixed());
        }
        
        // status_id 필터링 제거 (위에서 ACTIVE 상태로 고정)
        // if (searchDto.getStatusId() != null) {
        //     sql.append("AND n.status_id = :statusId ");
        //     param.addValue("statusId", searchDto.getStatusId());
        // }
        
        log.info("COUNT SQL: {}", sql.toString());
        log.info("COUNT 파라미터: {}", param.getValues());
        
        return template.queryForObject(sql.toString(), param, Integer.class);
    }
    
    @Override
    public Optional<NoticeDto> findNoticeById(Long noticeId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT n.notice_id, n.category_id, c.decode as category_name, ");
        sql.append("       n.title, n.content, n.author_id, m.nickname as author_name, ");
        sql.append("       n.view_count, n.is_important, n.is_fixed, ");
        sql.append("       n.start_date, n.end_date, n.status_id, s.decode as status_name, ");
        sql.append("       n.cdate, n.udate ");
        sql.append("FROM notices n ");
        sql.append("LEFT JOIN code c ON n.category_id = c.code_id ");
        sql.append("LEFT JOIN member m ON n.author_id = m.member_id ");
        sql.append("LEFT JOIN code s ON n.status_id = s.code_id ");
        sql.append("WHERE n.notice_id = :noticeId");
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("noticeId", noticeId);
        
        try {
            NoticeDto notice = template.queryForObject(sql.toString(), param, noticeRowMapper);
            return Optional.ofNullable(notice);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Long insertNotice(NoticeDto noticeDto) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO notices (notice_id, category_id, title, content, author_id, ");
        sql.append("                     view_count, is_important, is_fixed, start_date, end_date, status_id) ");
        sql.append("VALUES (seq_notice_id.nextval, :categoryId, :title, :content, :authorId, ");
        sql.append("        :viewCount, :isImportant, :isFixed, :startDate, :endDate, :statusId)");
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("categoryId", noticeDto.getCategoryId());
        param.addValue("title", noticeDto.getTitle());
        param.addValue("content", noticeDto.getContent());
        param.addValue("authorId", noticeDto.getAuthorId());
        param.addValue("viewCount", noticeDto.getViewCount());
        param.addValue("isImportant", noticeDto.getIsImportant());
        param.addValue("isFixed", noticeDto.getIsFixed());
        param.addValue("startDate", noticeDto.getStartDate());
        param.addValue("endDate", noticeDto.getEndDate());
        param.addValue("statusId", noticeDto.getStatusId());
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql.toString(), param, keyHolder, new String[]{"notice_id"});
        
        return keyHolder.getKey().longValue();
    }
    
    @Override
    public int updateNotice(NoticeDto noticeDto) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE notices ");
        sql.append("SET category_id = :categoryId, title = :title, content = :content, ");
        sql.append("    is_important = :isImportant, is_fixed = :isFixed, ");
        sql.append("    start_date = :startDate, end_date = :endDate, udate = SYSTIMESTAMP ");
        sql.append("WHERE notice_id = :noticeId");
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("noticeId", noticeDto.getNoticeId());
        param.addValue("categoryId", noticeDto.getCategoryId());
        param.addValue("title", noticeDto.getTitle());
        param.addValue("content", noticeDto.getContent());
        param.addValue("isImportant", noticeDto.getIsImportant());
        param.addValue("isFixed", noticeDto.getIsFixed());
        param.addValue("startDate", noticeDto.getStartDate());
        param.addValue("endDate", noticeDto.getEndDate());
        
        return template.update(sql.toString(), param);
    }
    
    @Override
    public int deleteNotice(Long noticeId) {
        String sql = "DELETE FROM notices WHERE notice_id = :noticeId";
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("noticeId", noticeId);
        
        return template.update(sql, param);
    }
    
    @Override
    public int incrementViewCount(Long noticeId) {
        String sql = "UPDATE notices SET view_count = view_count + 1 WHERE notice_id = :noticeId";
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("noticeId", noticeId);
        
        return template.update(sql, param);
    }
    
    @Override
    public List<NoticeDto> findImportantNotices(int limit) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT n.notice_id, n.category_id, c.decode as category_name, ");
        sql.append("       n.title, n.content, n.author_id, m.nickname as author_name, ");
        sql.append("       n.view_count, n.is_important, n.is_fixed, ");
        sql.append("       n.start_date, n.end_date, n.status_id, s.decode as status_name, ");
        sql.append("       n.cdate, n.udate ");
        sql.append("FROM notices n ");
        sql.append("LEFT JOIN code c ON n.category_id = c.code_id ");
        sql.append("LEFT JOIN member m ON n.author_id = m.member_id ");
        sql.append("LEFT JOIN code s ON n.status_id = s.code_id ");
        sql.append("WHERE n.status_id = (SELECT code_id FROM code WHERE gcode = 'NOTICE_STATUS' AND code = 'ACTIVE') ");
        sql.append("AND (n.is_important = 'Y' OR n.is_fixed = 'Y') ");
        sql.append("ORDER BY n.is_important DESC, n.is_fixed DESC, n.cdate DESC ");
        sql.append("FETCH FIRST :limit ROWS ONLY");
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("limit", limit);
        
        return template.query(sql.toString(), param, noticeRowMapper);
    }
    
    @Override
    public int countNoticesByCategory(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM notices WHERE category_id = :categoryId AND status_id = (SELECT code_id FROM code WHERE gcode = 'NOTICE_STATUS' AND code = 'ACTIVE')";
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("categoryId", categoryId);
        
        return template.queryForObject(sql, param, Integer.class);
    }
}
