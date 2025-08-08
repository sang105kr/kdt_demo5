package com.kh.demo.domain.report.svc;

import com.kh.demo.domain.report.entity.Report;
import com.kh.demo.domain.report.dto.ReportDetailDTO;
import com.kh.demo.domain.common.base.BaseSVC;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReportService extends BaseSVC<Report, Long> {
    
    // 신고 생성
    Report createReport(Report report, Long reporterId);
    
    // 신고 처리
    int processReport(Long reportId, String status, String adminNotes, Long adminId);
    
    // 신고 상태별 목록 조회
    List<Report> findByStatus(String status);
    
    // 신고 타입별 목록 조회
    List<Report> findByTargetType(String targetType);
    
    // 특정 대상의 신고 목록 조회
    List<Report> findByTarget(String targetType, Long targetId);
    
    // 신고자별 신고 목록 조회
    List<Report> findByReporterId(Long reporterId);
    
    // 처리자별 신고 목록 조회
    List<Report> findByResolverId(Long resolverId);
    
    // 신고 통계 조회
    Map<String, Object> getReportStatistics();
    
    // 자동 조치 실행
    int executeAutoActions();
    
    // 신고 카테고리 목록 조회 (code 테이블에서 조회)
    List<Map<String, Object>> getReportCategories();
    
    // 신고 검증 (중복 신고 방지)
    boolean isAlreadyReported(String targetType, Long targetId, Long reporterId);
    
    // 상태별 신고 개수 조회
    int countByStatus(String status);
    
    // === DTO 기반 서비스 메서드들 ===
    
    // 신고 상세 정보 조회 (JOIN 포함)
    Optional<ReportDetailDTO> findDetailById(Long reportId);
    
    // 모든 신고 상세 목록 조회 (JOIN 포함)
    List<ReportDetailDTO> findAllDetails();
    
    // 상태별 신고 상세 목록 조회 (JOIN 포함)
    List<ReportDetailDTO> findDetailsByStatus(String status);
    
    // 대상 타입별 신고 상세 목록 조회 (JOIN 포함)
    List<ReportDetailDTO> findDetailsByTargetType(String targetType);
    
    // 특정 대상의 신고 상세 목록 조회 (JOIN 포함)
    List<ReportDetailDTO> findDetailsByTarget(String targetType, Long targetId);
    
    // 신고자별 신고 상세 목록 조회 (JOIN 포함)
    List<ReportDetailDTO> findDetailsByReporterId(Long reporterId);
    
    // 처리자별 신고 상세 목록 조회 (JOIN 포함)
    List<ReportDetailDTO> findDetailsByResolverId(Long resolverId);
    
    // 페이징된 신고 상세 목록 조회 (JOIN 포함)
    List<ReportDetailDTO> findDetailsWithOffset(int offset, int limit);
    
    // === 시스템 알림 관련 메서드들 ===
    
    // 긴급 신고 개수 조회 (CRITICAL 카테고리)
    int countCriticalReports();
    
    // 시스템 알림 개수 조회 (긴급 신고 + 특별 이슈)
    int getSystemAlertCount();
    
    // === 페이징 및 검색 메서드들 ===
    
    // 상태별 신고 상세 목록 조회 (페이징)
    List<ReportDetailDTO> findDetailsByStatusWithPaging(String status, int pageNo, int pageSize);
    
    // 대상 타입별 신고 상세 목록 조회 (페이징)
    List<ReportDetailDTO> findDetailsByTargetTypeWithPaging(String targetType, int pageNo, int pageSize);
    
    // 키워드 검색 신고 상세 목록 조회 (페이징)
    List<ReportDetailDTO> findDetailsByKeywordWithPaging(String keyword, int pageNo, int pageSize);
    
    // 날짜 범위별 신고 상세 목록 조회 (페이징)
    List<ReportDetailDTO> findDetailsByDateRangeWithPaging(String startDate, String endDate, int pageNo, int pageSize);
    
    // 대상 타입별 신고 개수 조회
    int countByTargetType(String targetType);
    
    // 키워드 검색 신고 개수 조회
    int countByKeyword(String keyword);
    
    // 날짜 범위별 신고 개수 조회
    int countByDateRange(String startDate, String endDate);
    
    // 신고 통계 조회 (기간 지정)
    Map<String, Object> getReportStatistics(int days);
} 