package com.kh.demo.web.notice.controller.page;

import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.notice.dto.NoticeDto;
import com.kh.demo.web.notice.dto.NoticeSearchDto;
import com.kh.demo.web.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    
    private final NoticeService noticeService;
    private final CodeSVC codeSVC;
    
    /**
     * 공지사항 메인 페이지 (목록으로 리다이렉트)
     */
    @GetMapping
    public String index() {
        return "redirect:/notice/list";
    }
    
    /**
     * 공지사항 목록 페이지
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                      @RequestParam(defaultValue = "10") int pageSize,
                      @RequestParam(defaultValue = "all") String searchType,
                      @RequestParam(defaultValue = "") String searchKeyword,
                      @RequestParam(required = false) Long categoryId,
                      @RequestParam(defaultValue = "cdate") String sortBy,
                      @RequestParam(defaultValue = "desc") String sortOrder,
                      Model model) {
        
        NoticeSearchDto searchDto = new NoticeSearchDto();
        searchDto.setSearchType(searchType);
        searchDto.setSearchKeyword(searchKeyword);
        searchDto.setCategoryId(categoryId);
        searchDto.setPage(page);
        searchDto.setPageSize(pageSize);
        searchDto.setSortBy(sortBy);
        searchDto.setSortOrder(sortOrder);
        log.info("공지사항 목록 조회: page={}, pageSize={}, searchType={}, searchKeyword={}, categoryId={}, sortBy={}, sortOrder={}", 
                page, pageSize, searchType, searchKeyword, categoryId, sortBy, sortOrder);
        
        // 공지사항 목록 조회
        List<NoticeDto> notices = noticeService.getNotices(searchDto);
        int totalCount = noticeService.getNoticeCount(searchDto);
        
        log.info("조회된 공지사항 수: {}, 총 개수: {}", notices.size(), totalCount);
        
        // 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);
        
        // 카테고리 목록 조회 (CodeSVC 사용)
        List<Code> categories = codeSVC.getCodeList("NOTICE_CATEGORY");
        
        // 모델에 데이터 추가
        model.addAttribute("notices", notices);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("categories", categories);
        
        return "notice/list";
    }
    
    /**
     * 공지사항 상세 페이지
     */
    @GetMapping("/{noticeId}")
    public String detail(@PathVariable Long noticeId, Model model) {
        log.info("공지사항 상세 조회: noticeId={}", noticeId);
        
        Optional<NoticeDto> notice = noticeService.getNoticeById(noticeId);
        
        if (notice.isEmpty()) {
            log.warn("공지사항을 찾을 수 없음: noticeId={}", noticeId);
            return "redirect:/notice";
        }
        
        model.addAttribute("notice", notice.get());
        return "notice/detail";
    }
    
    /**
     * 중요/고정 공지사항 목록 조회 (메인 페이지용)
     */
    @GetMapping("/important")
    @ResponseBody
    public List<NoticeDto> getImportantNotices(@RequestParam(defaultValue = "5") int limit) {
        log.info("중요/고정 공지사항 목록 조회: limit={}", limit);
        return noticeService.getImportantNotices(limit);
    }
}
