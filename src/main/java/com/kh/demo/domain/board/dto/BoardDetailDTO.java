package com.kh.demo.domain.board.dto;

import com.kh.demo.domain.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 게시판 상세 정보 DTO
 * Boards 엔티티와 Code 테이블을 조인한 결과
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BoardDetailDTO extends BaseDTO {
    
    // Boards 기본 정보
    private Long boardId;
    private String title;
    private String email;
    private String nickname;
    private Integer hit;
    private String bcontent;
    private Long pboardId;
    private Long bgroup;
    private Integer step;
    private Integer bindent;
    private Integer likeCount;
    private Integer dislikeCount;
    
    // 코드 참조 필드들
    private Long bcategory;         // 카테고리 코드 ID
    private Long statusId;          // 상태 코드 ID
    
    // 코드 decode 값들 (조인으로 조회)
    private String categoryCode;    // 카테고리 코드 (SPRING, REACT, etc.)
    private String categoryName;    // 카테고리명 (Spring, React, etc.)
    private String statusCode;      // 상태 코드 (ACTIVE, HIDDEN, DELETED)
    private String statusName;      // 상태명 (활성, 숨김, 삭제)
    
    /**
     * 답글 여부 확인
     */
    public boolean isReply() {
        return step != null && step > 0;
    }
    
    /**
     * 원글 여부 확인
     */
    public boolean isOriginal() {
        return step == null || step == 0;
    }
    
    /**
     * 좋아요 비율 계산 (%)
     */
    public double getLikePercentage() {
        int total = (likeCount != null ? likeCount : 0) + (dislikeCount != null ? dislikeCount : 0);
        if (total == 0) return 0.0;
        return ((double) (likeCount != null ? likeCount : 0) / total) * 100;
    }
    
    /**
     * 인기 게시글 여부 (좋아요 10개 이상 또는 조회수 100 이상)
     */
    public boolean isPopular() {
        return (likeCount != null && likeCount >= 10) || (hit != null && hit >= 100);
    }
    
    /**
     * 답글 들여쓰기용 공백 문자열 생성
     */
    public String getIndentSpaces() {
        if (bindent == null || bindent == 0) return "";
        return "　".repeat(bindent); // 전각 공백 사용
    }
}