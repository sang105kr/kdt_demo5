package com.kh.demo.domain.report.dao;

import com.kh.demo.domain.report.entity.Report;
import com.kh.demo.domain.common.base.BaseDAO;

import java.util.List;

public interface ReportDAO extends BaseDAO<Report, Long> {
    
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
    int countByStatus(String status);
    
    int countByTargetType(String targetType);
    
    int countByTarget(String targetType, Long targetId);
    
    // 신고 상태 업데이트
    int updateStatus(Long reportId, String status, Long resolverId);
    
    // 관리자 메모 업데이트
    int updateAdminNotes(Long reportId, String adminNotes);
    
    // 신고 통계 업데이트
    int updateReportStatistics(String targetType, Long targetId);
    
    // 자동 조치 규칙 조회
    List<Report> findTargetsForAutoAction(String targetType, int threshold);
} 