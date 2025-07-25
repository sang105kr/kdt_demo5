package com.kh.demo.web.common.controller.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST API 표준 응답 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;                    // 응답 코드
    private String message;                 // 응답 메시지
    private T data;                        // 응답 데이터
    private LocalDateTime timestamp;        // 응답 시간
    private Paging paging;                  // 페이징 정보 (선택)
    private Map<String, Object> details;    // 상세 정보 (선택)

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> of(ApiResponseCode code, T data) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), data, LocalDateTime.now(), null, null);
    }

    /**
     * 페이징 응답 생성
     */
    public static <T> ApiResponse<T> of(ApiResponseCode code, T data, Paging paging) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), data, LocalDateTime.now(), paging, null);
    }

    /**
     * 상세 정보 포함 응답 생성
     */
    public static <T> ApiResponse<T> withDetails(ApiResponseCode code, Map<String, Object> details, T data) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), data, LocalDateTime.now(), null, details);
    }

    /**
     * 페이징 정보 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Paging {
        private int pageNo;           // 현재 페이지 번호
        private int numOfRows;        // 페이지당 행 수
        private int totalCount;       // 전체 행 수
        private int totalPages;       // 전체 페이지 수
        private boolean hasNext;      // 다음 페이지 존재 여부
        private boolean hasPrev;      // 이전 페이지 존재 여부

        public Paging(int pageNo, int numOfRows, int totalCount) {
            this.pageNo = pageNo;
            this.numOfRows = numOfRows;
            this.totalCount = totalCount;
            this.totalPages = (int) Math.ceil((double) totalCount / numOfRows);
            this.hasNext = pageNo < totalPages;
            this.hasPrev = pageNo > 1;
        }
    }
} 