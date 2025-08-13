package com.kh.demo.domain.common.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 검색 로그 엔티티
 * 사용자의 검색 기록을 저장하여 인기검색어, 개인 검색 히스토리 기능을 제공
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SearchLog extends BaseEntity {
    
    private Long searchLogId;     // 검색 로그 ID (PK)
    private Long memberId;        // 회원 ID (로그인 사용자, NULL 허용)
    private String keyword;       // 검색 키워드
    private Long searchTypeId;    // 검색 타입 ID (code 테이블 참조)
    private Integer resultCount;  // 검색 결과 수
    private String searchIp;      // 검색 IP
} 