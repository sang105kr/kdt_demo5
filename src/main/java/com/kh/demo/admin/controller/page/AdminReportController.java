package com.kh.demo.admin.controller.page;

import com.kh.demo.domain.report.entity.Report;
import com.kh.demo.domain.report.svc.ReportService;
import com.kh.demo.common.session.LoginMember;
import jakarta.servlet.http.HttpSession;
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
public class AdminReportController {

    private final ReportService reportService;

    /**
     * 신고 카테고리를 모든 요청에 자동으로 추가
     */
    @ModelAttribute("categories")
    public List<Map<String, Object>> reportCategories() {
        return reportService.getReportCategories();
    }

    /**
     * 신고 목록 페이지
     * GET /admin/reports
     */
    @GetMapping
    public String reportList(@RequestParam(defaultValue = "PENDING") String status,
                            @RequestParam(defaultValue = "") String targetType,
                            Model model, HttpSession session) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || (loginMember.getGubun() != 4 && loginMember.getGubun() != 5)) {
            return "redirect:/login";
        }

        List<Report> reports;
        if (!targetType.isEmpty()) {
            reports = reportService.findByTargetType(targetType);
        } else {
            reports = reportService.findByStatus(status);
        }

        // 신고 통계
        Map<String, Object> statistics = reportService.getReportStatistics();

        model.addAttribute("reports", reports);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentTargetType", targetType);
        model.addAttribute("title", "신고 관리");
        model.addAttribute("use_banner", false);

        return "admin/reports/list";
    }

    // 신고 상세 페이지
    @GetMapping("/{reportId}")
    public String reportDetail(@PathVariable Long reportId, Model model, HttpSession session) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || (loginMember.getGubun() != 4 && loginMember.getGubun() != 5)) {
            return "redirect:/login";
        }

        Report report = reportService.findById(reportId).orElse(null);
        if (report == null) {
            return "redirect:/admin/reports";
        }

        // 관련 신고들 (같은 대상)
        List<Report> relatedReports = reportService.findByTarget(report.getTargetType(), report.getTargetId());

        model.addAttribute("report", report);
        model.addAttribute("relatedReports", relatedReports);
        model.addAttribute("title", "신고 상세");
        model.addAttribute("use_banner", false);

        return "admin/reports/detail";
    }

    // 신고 처리
    @PostMapping("/{reportId}/process")
    public String processReport(@PathVariable Long reportId,
                               @RequestParam String status,
                               @RequestParam(required = false) String adminNotes,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || (loginMember.getGubun() != 4 && loginMember.getGubun() != 5)) {
            return "redirect:/login";
        }

        try {
            int result = reportService.processReport(reportId, status, adminNotes, loginMember.getMemberId());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "신고가 처리되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "신고 처리에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("신고 처리 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "신고 처리 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/reports/" + reportId;
    }

    // 자동 조치 실행
    @PostMapping("/auto-actions")
    public String executeAutoActions(HttpSession session, RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || (loginMember.getGubun() != 4 && loginMember.getGubun() != 5)) {
            return "redirect:/login";
        }

        try {
            reportService.executeAutoActions();
            redirectAttributes.addFlashAttribute("successMessage", "자동 조치가 실행되었습니다.");
        } catch (Exception e) {
            log.error("자동 조치 실행 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "자동 조치 실행 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/reports";
    }

    // 신고 통계 페이지
    @GetMapping("/statistics")
    public String reportStatistics(Model model, HttpSession session) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || (loginMember.getGubun() != 4 && loginMember.getGubun() != 5)) {
            return "redirect:/login";
        }

        Map<String, Object> statistics = reportService.getReportStatistics();

        model.addAttribute("statistics", statistics);
        model.addAttribute("title", "신고 통계");
        model.addAttribute("use_banner", false);

        return "admin/reports/statistics";
    }
} 