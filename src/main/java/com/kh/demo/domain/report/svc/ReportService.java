package com.kh.demo.domain.report.svc;

import com.kh.demo.domain.report.entity.Report;
import com.kh.demo.domain.common.base.BaseSVC;

import java.util.List;
import java.util.Map;

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
    void executeAutoActions();
    
    // 신고 카테고리 목록 조회 (code 테이블에서 조회)
    List<Map<String, Object>> getReportCategories();
    
    // 신고 검증 (중복 신고 방지)
    boolean isAlreadyReported(String targetType, Long targetId, Long reporterId);
} 