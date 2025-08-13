package com.kh.demo.domain.faq.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * FAQ 엔티티
 * - 테이블 구조와 정확히 동일하게 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Faq extends BaseEntity {
    
    private Long faqId;                    // FAQ ID
    private Long categoryId;               // FAQ 카테고리 (code_id 참조)
    private String question;               // 질문
    private String answer;                 // 답변
    private String keywords;               // 검색 키워드
    private Integer viewCount;             // 조회수
    private Integer helpfulCount;          // 도움됨 수
    private Integer unhelpfulCount;        // 도움안됨 수
    private Integer sortOrder;             // 정렬순서
    private String isActive;               // 활성화 여부 (Y/N)
    private Long adminId;                  // 작성자 관리자 ID
    
    // 기본값 설정
    public void setDefaultValues() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.helpfulCount == null) {
            this.helpfulCount = 0;
        }
        if (this.unhelpfulCount == null) {
            this.unhelpfulCount = 0;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        if (this.isActive == null) {
            this.isActive = "Y";
        }
    }
}
