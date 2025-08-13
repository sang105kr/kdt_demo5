package com.kh.demo.domain.common.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * 모든 엔티티의 기본 클래스
 * 공통 필드들을 정의
 */
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity {
    private LocalDateTime cdate;    // 생성일시
    private LocalDateTime udate;    // 수정일시
} 