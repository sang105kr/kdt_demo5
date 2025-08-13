package com.kh.demo.web.notice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeSearchDto {
    
    private String searchType;       // 검색 타입 (title, content, all)
    private String searchKeyword;    // 검색 키워드
    private Long categoryId;         // 카테고리 ID
    private String isImportant;      // 중요공지 여부 (Y/N)
    private String isFixed;          // 상단고정 여부 (Y/N)
    private Long statusId;           // 상태 ID
    private Integer page;            // 페이지 번호
    private Integer pageSize;        // 페이지 크기
    private String sortBy;           // 정렬 기준 (cdate, view_count, title)
    private String sortOrder;        // 정렬 순서 (asc, desc)
    
    // 기본값 설정
    public NoticeSearchDto(String searchType, String searchKeyword, Long categoryId) {
        this.searchType = searchType;
        this.searchKeyword = searchKeyword;
        this.categoryId = categoryId;
        this.page = 1;
        this.pageSize = 10;
        this.sortBy = "cdate";
        this.sortOrder = "desc";
        this.statusId = 1L; // 기본값: 활성
    }
    
    // 페이징 계산
    public int getOffset() {
        return (page - 1) * pageSize;
    }
    
    // 검색 조건이 있는지 확인
    public boolean hasSearchCondition() {
        return (searchKeyword != null && !searchKeyword.trim().isEmpty()) ||
               (categoryId != null) ||
               (isImportant != null && !isImportant.trim().isEmpty()) ||
               (isFixed != null && !isFixed.trim().isEmpty());
    }
}
