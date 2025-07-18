package com.kh.demo.domain.shared.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 모든 DTO의 기본 클래스
 * 공통 필드들을 정의
 */
@Data
public abstract class BaseDTO {
    private LocalDateTime cdate;    // 생성일시
    private LocalDateTime udate;    // 수정일시
    
    /**
     * 엔티티에서 DTO로 변환 시 공통 필드 설정
     * @param cdate 생성일시
     * @param udate 수정일시
     */
    protected void setCommonFields(LocalDateTime cdate, LocalDateTime udate) {
        this.cdate = cdate;
        this.udate = udate;
    }
} 