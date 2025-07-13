package com.kh.demo.domain.common.dto;

import lombok.Data;

/**
 * 페이징 정보 DTO
 */
@Data
public class Pagination {
    private Integer pageNo;        // 현재 페이지 번호
    private Integer numOfRows;     // 페이지당 행 수
    private Integer totalCount;    // 전체 행 수
    private Integer totalPages;    // 전체 페이지 수
    private Integer startRow;      // 시작 행 번호
    private Integer endRow;        // 끝 행 번호
    private Boolean hasNext;       // 다음 페이지 존재 여부
    private Boolean hasPrev;       // 이전 페이지 존재 여부
    
    public Pagination(Integer pageNo, Integer numOfRows, Integer totalCount) {
        this.pageNo = pageNo;
        this.numOfRows = numOfRows;
        this.totalCount = totalCount;
        this.totalPages = (int) Math.ceil((double) totalCount / numOfRows);
        this.startRow = (pageNo - 1) * numOfRows + 1;
        this.endRow = Math.min(pageNo * numOfRows, totalCount);
        this.hasNext = pageNo < totalPages;
        this.hasPrev = pageNo > 1;
    }
} 