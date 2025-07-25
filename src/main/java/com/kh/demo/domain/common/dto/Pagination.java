package com.kh.demo.domain.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 페이징 정보를 담는 DTO
 */
public class Pagination {
    private int pageNo;           // 현재 페이지 번호
    private int pageSize;         // 페이지당 항목 수
    private int totalCount;       // 전체 항목 수
    private int totalPages;       // 전체 페이지 수
    private int startPage;        // 시작 페이지 번호
    private int endPage;          // 끝 페이지 번호
    private boolean hasPrevBlock; // 이전 블록 존재 여부
    private boolean hasNextBlock; // 다음 블록 존재 여부
    private int blockSize = 10;   // 페이지 블록 크기

    public Pagination(int pageNo, int pageSize, int totalCount) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        this.blockSize = 10;
        this.startPage = ((pageNo - 1) / blockSize) * blockSize + 1;
        this.endPage = Math.min(startPage + blockSize - 1, totalPages);
        this.hasPrevBlock = startPage > 1;
        this.hasNextBlock = endPage < totalPages;
    }
    
    // Getter 메서드들
    public int getPageNo() { return pageNo; }
    public int getPageSize() { return pageSize; }
    public int getTotalCount() { return totalCount; }
    public int getTotalPages() { return totalPages; }
    public int getStartPage() { return startPage; }
    public int getEndPage() { return endPage; }
    public boolean getHasPrevBlock() { return hasPrevBlock; }
    public boolean getHasNextBlock() { return hasNextBlock; }
    public int getBlockSize() { return blockSize; }
    
    // Setter 메서드들
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public void setStartPage(int startPage) { this.startPage = startPage; }
    public void setEndPage(int endPage) { this.endPage = endPage; }
    public void setHasPrevBlock(boolean hasPrevBlock) { this.hasPrevBlock = hasPrevBlock; }
    public void setHasNextBlock(boolean hasNextBlock) { this.hasNextBlock = hasNextBlock; }
    public void setBlockSize(int blockSize) { this.blockSize = blockSize; }
} 