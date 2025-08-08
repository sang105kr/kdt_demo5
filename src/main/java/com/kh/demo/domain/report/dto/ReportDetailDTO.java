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
    private Long targetTypeId;       // 신고 대상 타입 ID (code_id 참조)
    private Long targetId;           // 신고 대상 ID
    private Long categoryId;         // 신고 카테고리 ID (code_id 참조)
    private String reason;           // 신고 사유
    private String evidence;         // 증거 자료
    private Long statusId;           // 상태 ID (code_id 참조)
    private String adminNotes;       // 관리자 메모
    private Long resolvedBy;         // 처리자 ID
    private LocalDateTime resolvedAt; // 처리일시
    private LocalDateTime cdate;     // 생성일시
    private LocalDateTime udate;     // 수정일시
    
    // 코드 decode 값들 (조인으로 조회)
    private String targetTypeCode;   // 대상 타입 코드 (REVIEW, COMMENT, MEMBER)
    private String targetTypeName;   // 대상 타입명 (리뷰, 댓글, 회원)
    private String categoryCode;     // 카테고리 코드 (SPAM_AD, ABUSE, etc.)
    private String categoryName;     // 카테고리명 (스팸/광고, 욕설/비방, etc.)
    private String statusCode;       // 상태 코드 (PENDING, PROCESSING, RESOLVED, REJECTED)
    private String statusName;       // 상태명 (대기, 처리중, 해결됨, 거부됨)
    
    // JOIN 결과 필드들
    private String reporterName;     // 신고자 이름
    private String resolverName;     // 처리자 이름
    private String targetContent;    // 신고 대상 내용 (미리보기용)
    
    // Report 엔티티로부터 DTO 생성하는 정적 메서드
    public static ReportDetailDTO fromReport(Report report) {
        ReportDetailDTO dto = new ReportDetailDTO();
        dto.setReportId(report.getReportId());
        dto.setReporterId(report.getReporterId());
        dto.setTargetTypeId(report.getTargetTypeId());
        dto.setTargetId(report.getTargetId());
        dto.setCategoryId(report.getCategoryId());
        dto.setReason(report.getReason());
        dto.setEvidence(report.getEvidence());
        dto.setStatusId(report.getStatusId());
        dto.setAdminNotes(report.getAdminNotes());
        dto.setResolvedBy(report.getResolvedBy());
        dto.setResolvedAt(report.getResolvedAt());
        dto.setCdate(report.getCdate());
        dto.setUdate(report.getUdate());
        return dto;
    }
    
    /**
     * 처리 완료 여부
     */
    public boolean isResolved() {
        return "RESOLVED".equals(statusCode);
    }
    
    /**
     * 처리 대기 여부
     */
    public boolean isPending() {
        return "PENDING".equals(statusCode);
    }
    
    /**
     * 처리중 여부
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(statusCode);
    }
} 