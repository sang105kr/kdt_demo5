package com.kh.demo.admin.report.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.report.dto.ReportDetailDTO;
import com.kh.demo.domain.report.svc.ReportService;
import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/admin/reports")
@Controller
@RequiredArgsConstructor
public class AdminReportController extends BaseController {

    private final ReportService reportService;
    private final CodeSVC codeSVC;
    
    // 권한 상수 정의 - CodeSVC로 동적 확인
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 신고 카테고리를 모든 요청에 자동으로 추가
     */
    @ModelAttribute("categories")
    public List<Map<String, Object>> reportCategories() {
        return reportService.getReportCategories();
    }

    /**
     * 관리자 권한 체크
     */
    private boolean hasAdminPermission(LoginMember loginMember) {
        if (loginMember == null) {
            log.warn("로그인 멤버가 null입니다.");
            return false;
        }
        
        try {
            // CodeSVC를 사용하여 관리자 권한 확인
            Long admin1CodeId = codeSVC.getCodeId("MEMBER_GUBUN", "ADMIN1");
            Long admin2CodeId = codeSVC.getCodeId("MEMBER_GUBUN", "ADMIN2");
            
            log.info("권한 체크 - memberId: {}, gubun: {}, admin1CodeId: {}, admin2CodeId: {}", 
                     loginMember.getMemberId(), loginMember.getGubun(), admin1CodeId, admin2CodeId);
            
            boolean isAdmin = loginMember.getGubun() == admin1CodeId || 
                             loginMember.getGubun() == admin2CodeId;
            
            log.info("관리자 권한 체크 결과: {}", isAdmin);
            return isAdmin;
        } catch (Exception e) {
            log.error("관리자 권한 체크 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 신고 목록 페이지
     * GET /admin/reports
     */
    @GetMapping
    public String reportList(@RequestParam(defaultValue = "1") int pageNo,
                            @RequestParam(defaultValue = "10") int pageSize,
                            @RequestParam(defaultValue = "PENDING") String status,
                            @RequestParam(defaultValue = "") String targetType,
                            @RequestParam(defaultValue = "") String keyword,
                            @RequestParam(defaultValue = "") String startDate,
                            @RequestParam(defaultValue = "") String endDate,
                            Model model, HttpSession session) {
        
        log.info("신고 목록 조회 시작 - pageNo: {}, pageSize: {}, status: {}, targetType: {}, keyword: {}", 
                 pageNo, pageSize, status, targetType, keyword);
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (!hasAdminPermission(loginMember)) {
            log.warn("권한 없는 사용자 신고 목록 접근 시도 - memberId: {}", 
                     loginMember != null ? loginMember.getMemberId() : "null");
            return "redirect:/login";
        }

        // 페이징 처리
        pageSize = Math.min(pageSize, 50); // 최대 50개로 제한
        int offset = (pageNo - 1) * pageSize;
        
        List<ReportDetailDTO> reports;
        int totalCount;
        
        if (!targetType.isEmpty()) {
            reports = reportService.findDetailsByTargetTypeWithPaging(targetType, pageNo, pageSize);
            totalCount = reportService.countByTargetType(targetType);
        } else if (!keyword.isEmpty()) {
            reports = reportService.findDetailsByKeywordWithPaging(keyword, pageNo, pageSize);
            totalCount = reportService.countByKeyword(keyword);
        } else if (!startDate.isEmpty() || !endDate.isEmpty()) {
            reports = reportService.findDetailsByDateRangeWithPaging(startDate, endDate, pageNo, pageSize);
            totalCount = reportService.countByDateRange(startDate, endDate);
        } else {
            reports = reportService.findDetailsByStatusWithPaging(status, pageNo, pageSize);
            totalCount = reportService.countByStatus(status);
        }

        // 신고 통계
        Map<String, Object> statistics = reportService.getReportStatistics();
        
        // 페이징 객체 생성
        Pagination pagination = new Pagination(pageNo, pageSize, totalCount);

        log.info("신고 목록 조회 완료 - totalCount: {}, reports.size: {}", totalCount, reports.size());

        model.addAttribute("reports", reports);
        model.addAttribute("pagination", pagination);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentTargetType", targetType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("title", "신고 관리");
        model.addAttribute("use_banner", false);

        addAuthInfoToModel(model, session);
        return "admin/reports/list";
    }

    /**
     * 신고 상세 페이지
     * GET /admin/reports/{reportId}
     */
    @GetMapping("/{reportId}")
    public String reportDetail(@PathVariable Long reportId, Model model, HttpSession session) {
        
        log.info("신고 상세 조회 시작 - reportId: {}", reportId);
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (!hasAdminPermission(loginMember)) {
            log.warn("권한 없는 사용자 신고 상세 접근 시도 - memberId: {}, reportId: {}", 
                     loginMember != null ? loginMember.getMemberId() : "null", reportId);
            return "redirect:/login";
        }

        ReportDetailDTO report = reportService.findDetailById(reportId).orElse(null);
        if (report == null) {
            log.warn("존재하지 않는 신고 조회 시도 - reportId: {}", reportId);
            addAuthInfoToModel(model, session);
            return "redirect:/admin/reports";
        }

        // 관련 신고들 (같은 대상)
        List<ReportDetailDTO> relatedReports = reportService.findDetailsByTarget(report.getTargetTypeCode(), report.getTargetId());

        log.info("신고 상세 조회 완료 - reportId: {}, relatedReports.size: {}", reportId, relatedReports.size());

        model.addAttribute("report", report);
        model.addAttribute("relatedReports", relatedReports);
        model.addAttribute("title", "신고 상세");
        model.addAttribute("use_banner", false);

        addAuthInfoToModel(model, session);
        return "admin/reports/detail";
    }

    /**
     * 신고 처리 (AJAX)
     * POST /admin/reports/{reportId}/process
     */
    @PostMapping("/{reportId}/process")
    @ResponseBody
    public Map<String, Object> processReportAjax(@PathVariable Long reportId,
                                                 @Valid @RequestParam String status,
                                                 @RequestParam(required = false) String adminNotes,
                                                 HttpSession session) {
        
        log.info("신고 처리 시작 - reportId: {}, status: {}, adminNotes: {}", reportId, status, adminNotes);
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (!hasAdminPermission(loginMember)) {
            log.warn("권한 없는 사용자 신고 처리 시도 - memberId: {}, reportId: {}", 
                     loginMember != null ? loginMember.getMemberId() : "null", reportId);
            return Map.of("success", false, "message", "권한이 없습니다.");
        }

        try {
            int result = reportService.processReport(reportId, status, adminNotes, loginMember.getMemberId());
            
            if (result > 0) {
                log.info("신고 처리 성공 - reportId: {}, status: {}, adminId: {}", 
                         reportId, status, loginMember.getMemberId());
                return Map.of("success", true, "message", "신고가 처리되었습니다.");
            } else {
                log.warn("신고 처리 실패 - reportId: {}, status: {}", reportId, status);
                return Map.of("success", false, "message", "신고 처리에 실패했습니다.");
            }
            
        } catch (IllegalArgumentException e) {
            log.error("신고 처리 중 잘못된 파라미터 - reportId: {}, error: {}", reportId, e.getMessage());
            return Map.of("success", false, "message", "잘못된 요청입니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("신고 처리 중 오류 발생 - reportId: {}", reportId, e);
            return Map.of("success", false, "message", "신고 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 자동 조치 실행
     * POST /admin/reports/auto-actions
     */
    @PostMapping("/auto-actions")
    public String executeAutoActions(HttpSession session, RedirectAttributes redirectAttributes) {
        
        log.info("자동 조치 실행 시작");
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (!hasAdminPermission(loginMember)) {
            log.warn("권한 없는 사용자 자동 조치 실행 시도 - memberId: {}", 
                     loginMember != null ? loginMember.getMemberId() : "null");
            return "redirect:/login";
        }

        try {
            int processedCount = reportService.executeAutoActions();
            log.info("자동 조치 실행 완료 - 처리된 신고 수: {}", processedCount);
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("자동 조치가 실행되었습니다. (처리된 신고: %d건)", processedCount));
        } catch (Exception e) {
            log.error("자동 조치 실행 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "자동 조치 실행 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/reports";
    }

    /**
     * 신고 통계 페이지
     * GET /admin/reports/statistics
     */
    @GetMapping("/statistics")
    public String reportStatistics(@RequestParam(defaultValue = "30") int days,
                                  Model model, HttpSession session) {
        
        log.info("신고 통계 조회 시작 - days: {}", days);
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (!hasAdminPermission(loginMember)) {
            log.warn("권한 없는 사용자 신고 통계 접근 시도 - memberId: {}", 
                     loginMember != null ? loginMember.getMemberId() : "null");
            return "redirect:/login";
        }

        Map<String, Object> statistics = reportService.getReportStatistics(days);

        log.info("신고 통계 조회 완료 - days: {}", days);

        model.addAttribute("statistics", statistics);
        model.addAttribute("days", days);
        model.addAttribute("title", "신고 통계");
        model.addAttribute("use_banner", false);

        addAuthInfoToModel(model, session);
        return "admin/reports/statistics";
    }
} 