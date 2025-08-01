package com.kh.demo.domain.report.svc;

import com.kh.demo.domain.report.dao.ReportDAO;
import com.kh.demo.domain.report.entity.Report;
import com.kh.demo.domain.report.dto.ReportDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private final ReportDAO reportDAO;
    private final NamedParameterJdbcTemplate template;

    @Override
    public Optional<Report> findById(Long id) {
        return reportDAO.findById(id);
    }

    @Override
    public List<Report> findAll() {
        return reportDAO.findAll();
    }

    @Override
    public List<Report> findByStatus(String status) {
        return reportDAO.findByStatus(status);
    }

    @Override
    public List<Report> findByTargetType(String targetType) {
        return reportDAO.findByTargetType(targetType);
    }

    @Override
    public List<Report> findByTarget(String targetType, Long targetId) {
        return reportDAO.findByTarget(targetType, targetId);
    }

    @Override
    @Transactional
    public int processReport(Long reportId, String status, String adminNotes, Long resolverId) {
        int result = reportDAO.updateStatus(reportId, status, resolverId);
        if (adminNotes != null && !adminNotes.isEmpty()) {
            reportDAO.updateAdminNotes(reportId, adminNotes);
        }
        return result;
    }

    @Override
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 각 상태별 신고 개수 조회
            int totalReports = reportDAO.getTotalCount();
            int pendingReports = reportDAO.countByStatus("PENDING");
            int processingReports = reportDAO.countByStatus("PROCESSING");
            int resolvedReports = reportDAO.countByStatus("RESOLVED");
            int rejectedReports = reportDAO.countByStatus("REJECTED");
            
            statistics.put("totalReports", totalReports);
            statistics.put("pendingReports", pendingReports);
            statistics.put("processingReports", processingReports);
            statistics.put("resolvedReports", resolvedReports);
            statistics.put("rejectedReports", rejectedReports);
            
        } catch (Exception e) {
            log.error("신고 통계 조회 실패", e);
            // 기본값 설정
            statistics.put("totalReports", 0);
            statistics.put("pendingReports", 0);
            statistics.put("processingReports", 0);
            statistics.put("resolvedReports", 0);
            statistics.put("rejectedReports", 0);
        }
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getReportCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        
        try {
            // code 테이블에서 REPORT_CATEGORY 그룹의 카테고리 조회
            String sql = """
                SELECT code_id as categoryId, decode as categoryName, 
                       CASE 
                           WHEN code = 'ABUSE' THEN '욕설, 비방, 폭력적 내용'
                           WHEN code = 'SPAM_AD' THEN '상업적 광고나 스팸성 내용'
                           WHEN code = 'PORN' THEN '음란물 또는 부적절한 성적 내용'
                           WHEN code = 'PRIVACY' THEN '개인정보 노출 또는 침해'
                           WHEN code = 'COPYRIGHT' THEN '타인의 저작물 무단 사용'
                           WHEN code = 'FALSE_INFO' THEN '사기, 허위 정보'
                           WHEN code = 'ETC' THEN '기타 부적절한 내용'
                           ELSE '기타 부적절한 내용'
                       END as description
                FROM code 
                WHERE gcode = 'REPORT_CATEGORY' AND use_yn = 'Y'
                ORDER BY sort_order, code_id
                """;
            
            List<Map<String, Object>> results = template.queryForList(sql, new MapSqlParameterSource());
            
            for (Map<String, Object> row : results) {
                Map<String, Object> category = new HashMap<>();
                category.put("categoryId", row.get("categoryId"));
                category.put("categoryName", row.get("categoryName"));
                category.put("description", row.get("description"));
                categories.add(category);
            }
            
        } catch (Exception e) {
            log.error("신고 카테고리 조회 실패", e);
            // 기본값 반환
            Map<String, Object> defaultCategory = new HashMap<>();
            defaultCategory.put("categoryId", 1L);
            defaultCategory.put("categoryName", "부적절한 내용");
            defaultCategory.put("description", "욕설, 폭력, 성적 내용 등");
            categories.add(defaultCategory);
        }
        
        return categories;
    }

    @Override
    @Transactional
    public void executeAutoActions() {
        // 자동 조치 로직 구현
        log.info("자동 조치 실행");
    }

    @Override
    public Long save(Report report) {
        return reportDAO.save(report);
    }

    @Override
    public List<Report> findAll(int pageNo, int numOfRows) {
        int offset = (pageNo - 1) * numOfRows;
        return reportDAO.findAllWithOffset(offset, numOfRows);
    }

    @Override
    public int updateById(Long id, Report report) {
        return reportDAO.updateById(id, report);
    }

    @Override
    public int deleteById(Long id) {
        return reportDAO.deleteById(id);
    }

    @Override
    public int getTotalCount() {
        return reportDAO.getTotalCount();
    }

    @Override
    public Report createReport(Report report, Long reporterId) {
        report.setReporterId(reporterId);
        report.setStatus("PENDING");
        Long reportId = reportDAO.save(report);
        report.setReportId(reportId);
        return report;
    }

    @Override
    public List<Report> findByReporterId(Long reporterId) {
        return reportDAO.findByReporterId(reporterId);
    }

    @Override
    public List<Report> findByResolverId(Long resolverId) {
        return reportDAO.findByResolverId(resolverId);
    }

    @Override
    public boolean isAlreadyReported(String targetType, Long targetId, Long reporterId) {
        return reportDAO.countByTarget(targetType, targetId) > 0;
    }

    @Override
    public int countByStatus(String status) {
        return reportDAO.countByStatus(status);
    }

    // === DTO 기반 서비스 메서드들 ===

    @Override
    public Optional<ReportDetailDTO> findDetailById(Long reportId) {
        return reportDAO.findDetailById(reportId);
    }

    @Override
    public List<ReportDetailDTO> findAllDetails() {
        return reportDAO.findAllDetails();
    }

    @Override
    public List<ReportDetailDTO> findDetailsByStatus(String status) {
        return reportDAO.findDetailsByStatus(status);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByTargetType(String targetType) {
        return reportDAO.findDetailsByTargetType(targetType);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByTarget(String targetType, Long targetId) {
        return reportDAO.findDetailsByTarget(targetType, targetId);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByReporterId(Long reporterId) {
        return reportDAO.findDetailsByReporterId(reporterId);
    }

    @Override
    public List<ReportDetailDTO> findDetailsByResolverId(Long resolverId) {
        return reportDAO.findDetailsByResolverId(resolverId);
    }

    @Override
    public List<ReportDetailDTO> findDetailsWithOffset(int offset, int limit) {
        return reportDAO.findDetailsWithOffset(offset, limit);
    }
    
    // === 시스템 알림 관련 메서드들 ===
    
    @Override
    public int countCriticalReports() {
        // CRITICAL 카테고리의 신고 개수 조회
        // 실제로는 code 테이블에서 CRITICAL 카테고리 ID를 조회해야 함
        return reportDAO.countByCategory("CRITICAL");
    }
    
    @Override
    public int getSystemAlertCount() {
        // 시스템 알림 = 긴급 신고 + 특별 이슈
        int criticalReports = countCriticalReports();
        
        // TODO: 추가 시스템 이슈 체크
        // - 서버 리소스 부족
        // - 데이터베이스 연결 문제  
        // - 보안 이슈
        // - 긴급 공지사항
        
        return criticalReports;
    }
} 