package com.kh.demo.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 검색 결과 DTO
 */
@Data
@Builder
public class SearchResult<T> {
    private List<T> items;           // 검색 결과 목록
    private int totalCount;          // 전체 개수
    private int currentPage;         // 현재 페이지
    private int totalPages;          // 전체 페이지 수
    private int pageSize;            // 페이지 크기
    private boolean hasNext;         // 다음 페이지 존재 여부
    private boolean hasPrevious;     // 이전 페이지 존재 여부
    private String searchSource;     // 검색 소스 (elasticsearch, oracle)
    private long searchTime;         // 검색 소요 시간 (ms)
    
    public static <T> SearchResult<T> of(List<T> items, int totalCount, int currentPage, int pageSize, String searchSource, long searchTime) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        return SearchResult.<T>builder()
                .items(items)
                .totalCount(totalCount)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .hasNext(currentPage < totalPages)
                .hasPrevious(currentPage > 1)
                .searchSource(searchSource)
                .searchTime(searchTime)
                .build();
    }
    
    public static <T> SearchResult<T> empty(int pageSize) {
        return SearchResult.<T>builder()
                .items(List.of())
                .totalCount(0)
                .currentPage(1)
                .totalPages(0)
                .pageSize(pageSize)
                .hasNext(false)
                .hasPrevious(false)
                .searchSource("none")
                .searchTime(0)
                .build();
    }
} 