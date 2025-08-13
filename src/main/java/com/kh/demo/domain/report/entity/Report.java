package com.kh.demo.domain.report.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Report extends BaseEntity {
    private Long reportId;           // 신고 ID
    private Long reporterId;         // 신고자 ID
    private Long targetTypeId;       // 신고 대상 타입 (code_id 참조, gcode='REPORT_TARGET_TYPE')
    private Long targetId;           // 신고 대상 ID
    private Long categoryId;         // 신고 카테고리 ID (code_id 참조, gcode='REPORT_CATEGORY')
    private String reason;           // 신고 사유
    private String evidence;         // 증거 자료
    private Long statusId;           // 상태 (code_id 참조, gcode='REPORT_STATUS')
    private String adminNotes;       // 관리자 메모
    private Long resolvedBy;         // 처리자 ID
    private LocalDateTime resolvedAt; // 처리일시
} 