package com.kh.demo.domain.common.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Code extends BaseEntity {
    private Long codeId;        // 코드 시퀀스 (기본키)
    private String gcode;       // 코드 그룹(분류)
    private String code;        // 코드값
    private String decode;      // 코드명(한글)
    private Long pcode;         // 상위코드 시퀀스
    private String codePath;    // 코드 경로 (성능 향상)
    private Integer codeLevel;  // 코드 레벨
    private Integer sortOrder;  // 정렬순서
    private String useYn;       // 사용여부
} 