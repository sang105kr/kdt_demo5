package com.kh.demo.web.notice.dao;

import com.kh.demo.web.notice.dto.NoticeDto;
import com.kh.demo.web.notice.dto.NoticeSearchDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeDao {
    
    /**
     * 공지사항 목록 조회 (페이징)
     */
    List<NoticeDto> findNotices(NoticeSearchDto searchDto);
    
    /**
     * 공지사항 총 개수 조회
     */
    int countNotices(NoticeSearchDto searchDto);
    
    /**
     * 공지사항 상세 조회
     */
    Optional<NoticeDto> findNoticeById(Long noticeId);
    
    /**
     * 공지사항 등록
     */
    Long insertNotice(NoticeDto noticeDto);
    
    /**
     * 공지사항 수정
     */
    int updateNotice(NoticeDto noticeDto);
    
    /**
     * 공지사항 삭제
     */
    int deleteNotice(Long noticeId);
    
    /**
     * 조회수 증가
     */
    int incrementViewCount(Long noticeId);
    
    /**
     * 중요/고정 공지사항 목록 조회 (메인 페이지용)
     */
    List<NoticeDto> findImportantNotices(int limit);
    
    /**
     * 카테고리별 공지사항 개수 조회
     */
    int countNoticesByCategory(Long categoryId);
}
