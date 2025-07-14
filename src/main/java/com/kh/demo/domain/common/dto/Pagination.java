package com.kh.demo.domain.common.dto;

import lombok.Data;

/**
 * 페이징 정보 DTO
 */
@Data
public class Pagination {
    private Integer pageNo;         // 현재 페이지 번호
    private Integer numOfRows;      // 페이지당 행 수
    private Integer totalCount;     // 전체 행 수
    private Integer totalPages;     // 전체 페이지 수
    private Integer startRow;       // 시작 행 번호
    private Integer endRow;         // 끝 행 번호
    private Boolean hasNext;        // 다음 페이지 존재 여부
    private Boolean hasPrev;        // 이전 페이지 존재 여부
    private Integer blockSize;      // 한 블록에 보여줄 페이지 수
    private Integer startPage;      // 현재 블록의 시작 페이지 번호
    private Integer endPage;        // 현재 블록의 끝 페이지 번호
    private Boolean hasPrevBlock;   // 이전 블록 존재 여부
    private Boolean hasNextBlock;   // 다음 블록 존재 여부
    
    public Pagination(Integer pageNo, Integer numOfRows, Integer totalCount) {
        this(pageNo, numOfRows, totalCount, 10); // blockSize 기본값 10
    }
    public Pagination(Integer pageNo, Integer numOfRows, Integer totalCount, Integer blockSize) {
        this.pageNo = pageNo;
        this.numOfRows = numOfRows;
        this.totalCount = totalCount;
        this.blockSize = blockSize;
        this.totalPages = (int) Math.ceil((double) totalCount / numOfRows);
        this.startRow = Math.max(0, (pageNo - 1) * numOfRows);
        this.endRow = Math.min(pageNo * numOfRows, totalCount);
//        this.hasPrev = totalPages > 0 && pageNo > 1;
//        this.hasNext = totalPages > 0 && pageNo < totalPages;
        // 블록 계산
        int currentBlock = (int) Math.ceil((double) pageNo / blockSize);
        this.startPage = (currentBlock - 1) * blockSize + 1;
        this.endPage = Math.min(startPage + blockSize - 1, totalPages);
        this.hasPrevBlock = startPage != 1 ? true : false;
        this.hasNextBlock = totalCount > endPage * blockSize ? true : false;
    }
} 