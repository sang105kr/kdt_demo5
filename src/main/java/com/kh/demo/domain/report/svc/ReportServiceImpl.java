package com.kh.demo.domain.report.svc;

import com.kh.demo.domain.report.dao.ReportDAO;
import com.kh.demo.domain.report.entity.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private final ReportDAO reportDAO;

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
        // 통계 쿼리 구현 필요
        throw new UnsupportedOperationException("getReportStatistics not implemented");
    }

    @Override
    public List<Map<String, Object>> getReportCategories() {
        // 카테고리 쿼리 구현 필요
        throw new UnsupportedOperationException("getReportCategories not implemented");
    }

    @Override
    @Transactional
    public void executeAutoActions() {
        // 자동조치 로직은 별도 구현 필요
        log.info("Auto-action execution triggered");
    }

    @Override
    public Long save(Report report) {
        return reportDAO.save(report);
    }

    @Override
    public List<Report> findAll(int pageNo, int numOfRows) {
        // 페이징 구현 필요 (DAO에 없으면 NotImplementedException)
        throw new UnsupportedOperationException("Paging not implemented");
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
        Long id = reportDAO.save(report);
        return reportDAO.findById(id).orElseThrow();
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
        // 중복 신고 여부 확인 쿼리 필요 (임시 false)
        return false;
    }

    // 기타 ReportService 메서드 구현 필요시 추가
} 