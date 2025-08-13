package com.kh.demo.domain.faq.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * FAQ DTO
 * - 조인 데이터를 포함한 FAQ 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaqDTO {
    
    private Long faqId;                    // FAQ ID
    private Long categoryId;               // FAQ 카테고리 ID
    private String question;               // 질문
    private String answer;                 // 답변
    private String keywords;               // 검색 키워드
    private Integer viewCount;             // 조회수
    private Integer helpfulCount;          // 도움됨 수
    private Integer unhelpfulCount;        // 도움안됨 수
    private Integer sortOrder;             // 정렬순서
    private String isActive;               // 활성화 여부 (Y/N)
    private Long adminId;                  // 작성자 관리자 ID
    private LocalDateTime cdate;           // 생성일시
    private LocalDateTime udate;           // 수정일시
    
    // 조인으로 가져올 데이터
    private String categoryName;           // 카테고리명
    private String adminNickname;          // 관리자 닉네임
}
