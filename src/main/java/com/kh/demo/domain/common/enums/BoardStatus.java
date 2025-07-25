package com.kh.demo.domain.common.enums;

/**
 * 게시판 상태 열거형
 */
public enum BoardStatus {
    ACTIVE("A", "활성"),
    DELETED("D", "삭제"),
    INACTIVE("I", "비활성");
    
    private final String code;
    private final String description;
    
    BoardStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static BoardStatus fromCode(String code) {
        for (BoardStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown board status code: " + code);
    }
} 