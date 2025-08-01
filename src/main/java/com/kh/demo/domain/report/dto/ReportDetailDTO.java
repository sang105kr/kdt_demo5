package com.kh.demo.domain.report.dto;

import com.kh.demo.domain.report.entity.Report;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailDTO {
    // Report 엔티티의 모든 필드
    private Long reportId;           // 신고 ID
    private Long reporterId;         // 신고자 ID
    private String targetType;       // 신고 대상 타입 (REVIEW, COMMENT, MEMBER)
    private Long targetId;           // 신고 대상 ID
    private Long categoryId;         // 신고 카테고리 ID
    private String reason;           // 신고 사유
    private String evidence;         // 증거 자료
    private String status;           // 상태 (PENDING, PROCESSING, RESOLVED, REJECTED)
    private String adminNotes;       // 관리자 메모
    private Long resolvedBy;         // 처리자 ID
    private LocalDateTime resolvedAt; // 처리일시
    private LocalDateTime cdate;     // 생성일시
    private LocalDateTime udate;     // 수정일시
    
    // JOIN 결과 필드들
    private String reporterName;     // 신고자 이름
    private String categoryName;     // 카테고리명 (code.decode)
    private String resolverName;     // 처리자 이름
    private String targetContent;    // 신고 대상 내용 (미리보기용)
    
    // Report 엔티티로부터 DTO 생성하는 정적 메서드
    public static ReportDetailDTO fromReport(Report report) {
        ReportDetailDTO dto = new ReportDetailDTO();
        dto.setReportId(report.getReportId());
        dto.setReporterId(report.getReporterId());
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setCategoryId(report.getCategoryId());
        dto.setReason(report.getReason());
        dto.setEvidence(report.getEvidence());
        dto.setStatus(report.getStatus());
        dto.setAdminNotes(report.getAdminNotes());
        dto.setResolvedBy(report.getResolvedBy());
        dto.setResolvedAt(report.getResolvedAt());
        dto.setCdate(report.getCdate());
        dto.setUdate(report.getUdate());
        return dto;
    }
} 