package com.kh.demo.admin.notice.page;

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
@RequestMapping("/admin/notice")
public class NoticeAdminController {
    
    private final NoticeService noticeService;
    private final CodeSVC codeSVC;
    
    /**
     * 관리자 공지사항 목록 페이지
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                      @RequestParam(defaultValue = "10") int pageSize,
                      @RequestParam(required = false) String searchType,
                      @RequestParam(required = false) String searchKeyword,
                      @RequestParam(required = false) Long categoryId,
                      @RequestParam(required = false) String isImportant,
                      @RequestParam(required = false) String isFixed,
                      @RequestParam(required = false) Long statusId,
                      @RequestParam(defaultValue = "cdate") String sortBy,
                      @RequestParam(defaultValue = "desc") String sortOrder,
                      Model model) {
        
        log.info("관리자 공지사항 목록 조회: page={}, pageSize={}, searchType={}, searchKeyword={}, categoryId={}, isImportant={}, isFixed={}, statusId={}, sortBy={}, sortOrder={}",
                page, pageSize, searchType, searchKeyword, categoryId, isImportant, isFixed, statusId, sortBy, sortOrder);
        
        // 검색 조건 설정
        NoticeSearchDto searchDto = new NoticeSearchDto();
        searchDto.setPage(page);
        searchDto.setPageSize(pageSize);
        searchDto.setSearchType(searchType);
        searchDto.setSearchKeyword(searchKeyword);
        searchDto.setCategoryId(categoryId);
        searchDto.setIsImportant(isImportant);
        searchDto.setIsFixed(isFixed);
        searchDto.setStatusId(statusId);
        searchDto.setSortBy(sortBy);
        searchDto.setSortOrder(sortOrder);
        
        // 공지사항 목록 조회
        List<NoticeDto> notices = noticeService.getNotices(searchDto);
        int totalCount = noticeService.getNoticeCount(searchDto);
        
        // 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);
        
        // 카테고리와 상태 목록 조회
        List<Code> categories = codeSVC.getCodeList("NOTICE_CATEGORY");
        List<Code> statuses = codeSVC.getCodeList("NOTICE_STATUS");
        
        // 통계 정보 계산
        int importantCount = (int) notices.stream().filter(n -> "Y".equals(n.getIsImportant())).count();
        int fixedCount = (int) notices.stream().filter(n -> "Y".equals(n.getIsFixed())).count();
        int totalViews = notices.stream().mapToInt(n -> n.getViewCount() != null ? n.getViewCount() : 0).sum();
        
        // 모델에 데이터 추가
        model.addAttribute("notices", notices);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("categories", categories);
        model.addAttribute("statuses", statuses);
        model.addAttribute("importantCount", importantCount);
        model.addAttribute("fixedCount", fixedCount);
        model.addAttribute("totalViews", totalViews);
        
        return "admin/notice/list";
    }
    
    /**
     * 관리자 공지사항 상세 페이지
     */
    @GetMapping("/{noticeId}")
    public String detail(@PathVariable Long noticeId, Model model) {
        log.info("관리자 공지사항 상세 조회: noticeId={}", noticeId);
        
        Optional<NoticeDto> notice = noticeService.getNoticeById(noticeId);
        
        if (notice.isEmpty()) {
            log.warn("공지사항을 찾을 수 없음: noticeId={}", noticeId);
            return "redirect:/admin/notice";
        }
        
        model.addAttribute("notice", notice.get());
        return "admin/notice/detail";
    }
    
    /**
     * 공지사항 작성 페이지
     */
    @GetMapping("/write")
    public String writeForm(Model model) {
        log.info("공지사항 작성 페이지");
        
        // 카테고리와 상태 목록 조회
        List<Code> categories = codeSVC.getCodeList("NOTICE_CATEGORY");
        List<Code> statuses = codeSVC.getCodeList("NOTICE_STATUS");
        
        model.addAttribute("categories", categories);
        model.addAttribute("statuses", statuses);
        
        return "admin/notice/write";
    }
    
    /**
     * 공지사항 등록
     */
    @PostMapping("/write")
    public String write(@ModelAttribute NoticeDto noticeDto,
                       @SessionAttribute(name = "loginMember", required = false) Object loginMember) {
        log.info("공지사항 등록: {}", noticeDto);
        
        // TODO: 로그인 체크 및 권한 확인
        // TODO: 작성자 ID 설정 (현재는 임시로 1L 사용)
        noticeDto.setAuthorId(1L);
        
        Long noticeId = noticeService.createNotice(noticeDto);
        log.info("공지사항 등록 완료: noticeId={}", noticeId);
        
        return "redirect:/admin/notice/" + noticeId;
    }
    
    /**
     * 공지사항 수정 페이지
     */
    @GetMapping("/{noticeId}/edit")
    public String editForm(@PathVariable Long noticeId, Model model) {
        log.info("공지사항 수정 페이지: noticeId={}", noticeId);
        
        Optional<NoticeDto> notice = noticeService.getNoticeById(noticeId);
        
        if (notice.isEmpty()) {
            log.warn("수정할 공지사항을 찾을 수 없음: noticeId={}", noticeId);
            return "redirect:/admin/notice";
        }
        
        // 카테고리와 상태 목록 조회
        List<Code> categories = codeSVC.getCodeList("NOTICE_CATEGORY");
        List<Code> statuses = codeSVC.getCodeList("NOTICE_STATUS");
        
        model.addAttribute("notice", notice.get());
        model.addAttribute("categories", categories);
        model.addAttribute("statuses", statuses);
        return "admin/notice/edit";
    }
    
    /**
     * 공지사항 수정
     */
    @PostMapping("/{noticeId}/edit")
    public String edit(@PathVariable Long noticeId,
                      @ModelAttribute NoticeDto noticeDto) {
        log.info("공지사항 수정: noticeId={}, {}", noticeId, noticeDto);
        
        noticeDto.setNoticeId(noticeId);
        
        boolean success = noticeService.updateNotice(noticeDto);
        
        if (success) {
            log.info("공지사항 수정 완료: noticeId={}", noticeId);
            return "redirect:/admin/notice/" + noticeId;
        } else {
            log.warn("공지사항 수정 실패: noticeId={}", noticeId);
            return "redirect:/admin/notice";
        }
    }
    
    /**
     * 공지사항 삭제
     */
    @PostMapping("/{noticeId}/delete")
    public String delete(@PathVariable Long noticeId) {
        log.info("공지사항 삭제: noticeId={}", noticeId);
        
        boolean success = noticeService.deleteNotice(noticeId);
        
        if (success) {
            log.info("공지사항 삭제 완료: noticeId={}", noticeId);
        } else {
            log.warn("공지사항 삭제 실패: noticeId={}", noticeId);
        }
        
        return "redirect:/admin/notice";
    }
}
