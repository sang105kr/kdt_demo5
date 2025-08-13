package com.kh.demo.web.notice.service;

import com.kh.demo.web.notice.dao.NoticeDao;
import com.kh.demo.web.notice.dto.NoticeDto;
import com.kh.demo.web.notice.dto.NoticeSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {
    
    private final NoticeDao noticeDao;
    
    @Override
    public List<NoticeDto> getNotices(NoticeSearchDto searchDto) {
        log.info("공지사항 목록 조회: {}", searchDto);
        return noticeDao.findNotices(searchDto);
    }
    
    @Override
    public int getNoticeCount(NoticeSearchDto searchDto) {
        log.info("공지사항 총 개수 조회: {}", searchDto);
        return noticeDao.countNotices(searchDto);
    }
    
    @Override
    @Transactional
    public Optional<NoticeDto> getNoticeById(Long noticeId) {
        log.info("공지사항 상세 조회: noticeId={}", noticeId);
        
        Optional<NoticeDto> notice = noticeDao.findNoticeById(noticeId);
        
        if (notice.isPresent()) {
            // 조회수 증가
            noticeDao.incrementViewCount(noticeId);
            log.info("공지사항 조회수 증가: noticeId={}", noticeId);
        }
        
        return notice;
    }
    
    @Override
    @Transactional
    public Long createNotice(NoticeDto noticeDto) {
        log.info("공지사항 등록: {}", noticeDto);
        
        // 기본값 설정
        if (noticeDto.getViewCount() == null) {
            noticeDto.setViewCount(0);
        }
        if (noticeDto.getStatusId() == null) {
            noticeDto.setStatusId(1L); // 활성 상태
        }
        
        Long noticeId = noticeDao.insertNotice(noticeDto);
        log.info("공지사항 등록 완료: noticeId={}", noticeId);
        
        return noticeId;
    }
    
    @Override
    @Transactional
    public boolean updateNotice(NoticeDto noticeDto) {
        log.info("공지사항 수정: {}", noticeDto);
        
        // 기존 공지사항 존재 확인
        Optional<NoticeDto> existingNotice = noticeDao.findNoticeById(noticeDto.getNoticeId());
        if (existingNotice.isEmpty()) {
            log.warn("수정할 공지사항이 존재하지 않음: noticeId={}", noticeDto.getNoticeId());
            return false;
        }
        
        int result = noticeDao.updateNotice(noticeDto);
        boolean success = result > 0;
        
        if (success) {
            log.info("공지사항 수정 완료: noticeId={}", noticeDto.getNoticeId());
        } else {
            log.warn("공지사항 수정 실패: noticeId={}", noticeDto.getNoticeId());
        }
        
        return success;
    }
    
    @Override
    @Transactional
    public boolean deleteNotice(Long noticeId) {
        log.info("공지사항 삭제: noticeId={}", noticeId);
        
        // 기존 공지사항 존재 확인
        Optional<NoticeDto> existingNotice = noticeDao.findNoticeById(noticeId);
        if (existingNotice.isEmpty()) {
            log.warn("삭제할 공지사항이 존재하지 않음: noticeId={}", noticeId);
            return false;
        }
        
        int result = noticeDao.deleteNotice(noticeId);
        boolean success = result > 0;
        
        if (success) {
            log.info("공지사항 삭제 완료: noticeId={}", noticeId);
        } else {
            log.warn("공지사항 삭제 실패: noticeId={}", noticeId);
        }
        
        return success;
    }
    
    @Override
    public List<NoticeDto> getImportantNotices(int limit) {
        log.info("중요/고정 공지사항 목록 조회: limit={}", limit);
        return noticeDao.findImportantNotices(limit);
    }
    
    @Override
    public int getNoticeCountByCategory(Long categoryId) {
        log.info("카테고리별 공지사항 개수 조회: categoryId={}", categoryId);
        return noticeDao.countNoticesByCategory(categoryId);
    }
}
