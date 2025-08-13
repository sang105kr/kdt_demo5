package com.kh.demo.web.notice.service;

import com.kh.demo.web.notice.dto.NoticeDto;
import com.kh.demo.web.notice.dto.NoticeSearchDto;

import java.util.List;
import java.util.Optional;

public interface NoticeService {
    
    /**
     * 공지사항 목록 조회 (페이징)
     */
    List<NoticeDto> getNotices(NoticeSearchDto searchDto);
    
    /**
     * 공지사항 총 개수 조회
     */
    int getNoticeCount(NoticeSearchDto searchDto);
    
    /**
     * 공지사항 상세 조회 (조회수 증가 포함)
     */
    Optional<NoticeDto> getNoticeById(Long noticeId);
    
    /**
     * 공지사항 등록
     */
    Long createNotice(NoticeDto noticeDto);
    
    /**
     * 공지사항 수정
     */
    boolean updateNotice(NoticeDto noticeDto);
    
    /**
     * 공지사항 삭제
     */
    boolean deleteNotice(Long noticeId);
    
    /**
     * 중요/고정 공지사항 목록 조회 (메인 페이지용)
     */
    List<NoticeDto> getImportantNotices(int limit);
    
    /**
     * 카테고리별 공지사항 개수 조회
     */
    int getNoticeCountByCategory(Long categoryId);
}
