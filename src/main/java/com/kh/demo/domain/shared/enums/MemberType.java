package com.kh.demo.domain.shared.enums;

/**
 * 회원 타입 열거형
 */
public enum MemberType {
    ADMIN("M01A", "관리자"),
    USER("M02U", "일반회원"),
    PREMIUM("M03P", "프리미엄회원");
    
    private final String code;
    private final String description;
    
    MemberType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static MemberType fromCode(String code) {
        for (MemberType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown member type code: " + code);
    }
} 