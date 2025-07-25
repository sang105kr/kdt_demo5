package com.kh.demo.domain.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 구분 enum
 * 코드 테이블의 MEMBER 그룹과 매핑
 */
@Getter
@RequiredArgsConstructor
public enum MemberType {
    
    NORMAL(2L, "NORMAL", "일반회원"),
    VIP(3L, "VIP", "우수회원"), 
    ADMIN1(4L, "ADMIN1", "관리자1"),
    ADMIN2(5L, "ADMIN2", "관리자2");
    
    private final Long codeId;
    private final String code;
    private final String displayName;
    
    /**
     * 코드 ID로 MemberType 찾기
     */
    public static MemberType fromCodeId(Long codeId) {
        for (MemberType type : values()) {
            if (type.getCodeId().equals(codeId)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown member type codeId: " + codeId);
    }
    
    /**
     * 코드값으로 MemberType 찾기
     */
    public static MemberType fromCode(String code) {
        for (MemberType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown member type code: " + code);
    }
    
    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return this == ADMIN1 || this == ADMIN2;
    }
    
    /**
     * VIP 회원 여부 확인
     */
    public boolean isVip() {
        return this == VIP;
    }
} 