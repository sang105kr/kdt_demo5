package com.kh.demo.web.notice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {
    
    private Long noticeId;           // 공지사항 ID
    private Long categoryId;         // 카테고리 ID
    private String categoryName;     // 카테고리명
    private String title;            // 제목
    private String content;          // 내용
    private Long authorId;           // 작성자 ID
    private String authorName;       // 작성자명
    private Integer viewCount;       // 조회수
    private String isImportant;      // 중요공지 여부 (Y/N)
    private String isFixed;          // 상단고정 여부 (Y/N)
    private LocalDate startDate;     // 공지 시작일
    private LocalDate endDate;       // 공지 종료일
    private Long statusId;           // 상태 ID
    private String statusName;       // 상태명
    private LocalDateTime cdate;     // 생성일시
    private LocalDateTime udate;     // 수정일시
    
    // 생성자 (등록용)
    public NoticeDto(Long categoryId, String title, String content, Long authorId, 
                    String isImportant, String isFixed, LocalDate startDate, LocalDate endDate) {
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.isImportant = isImportant;
        this.isFixed = isFixed;
        this.startDate = startDate;
        this.endDate = endDate;
        this.viewCount = 0;
        this.statusId = 1L; // 기본값: 활성
    }
    
    // 생성자 (수정용)
    public NoticeDto(Long noticeId, Long categoryId, String title, String content, 
                    String isImportant, String isFixed, LocalDate startDate, LocalDate endDate) {
        this.noticeId = noticeId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.isImportant = isImportant;
        this.isFixed = isFixed;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
