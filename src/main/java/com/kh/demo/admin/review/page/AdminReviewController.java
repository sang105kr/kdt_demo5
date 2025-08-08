package com.kh.demo.admin.review.page;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.domain.review.svc.ReviewCommentService;
import com.kh.demo.domain.report.svc.ReportService;
import com.kh.demo.domain.report.dto.ReportDetailDTO;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController extends BaseController {
    private final ReviewService reviewService;
    private final ReviewCommentService reviewCommentService;
    private final CodeSVC codeSVC;
    private final ReportService reportService;

    /**
     * 리뷰 목록 페이지
     * GET /admin/reviews
     */
    @GetMapping
    public String reviewList(@RequestParam(defaultValue = "1") int pageNo,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "") String status,
                           @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "") String startDate,
                           @RequestParam(defaultValue = "") String endDate,
                           Model model, HttpSession session) {
        
        log.info("리뷰 목록 조회 시작 - pageNo: {}, pageSize: {}, status: {}, keyword: {}", 
                 pageNo, pageSize, status, keyword);
        
        // 페이징 처리
        pageSize = Math.min(pageSize, 50); // 최대 50개로 제한
        int offset = (pageNo - 1) * pageSize;
        
        List<Review> reviews;
        int totalCount;
        
        if (!status.isEmpty()) {
            Long statusId = codeSVC.getCodeId("REVIEW_STATUS", status);
            reviews = reviewService.findByStatusWithPaging(statusId, pageNo, pageSize);
            totalCount = reviewService.countByStatus(statusId);
        } else if (!keyword.isEmpty()) {
            reviews = reviewService.findByKeywordWithPaging(keyword, pageNo, pageSize);
            totalCount = reviewService.countByKeyword(keyword);
        } else if (!startDate.isEmpty() || !endDate.isEmpty()) {
            reviews = reviewService.findByDateRangeWithPaging(startDate, endDate, pageNo, pageSize);
            totalCount = reviewService.countByDateRange(startDate, endDate);
        } else {
            reviews = reviewService.findAllWithPaging(pageNo, pageSize);
            totalCount = reviewService.countAll();
        }

        // 페이징 객체 생성
        Pagination pagination = new Pagination(pageNo, pageSize, totalCount);
        
        // 리뷰 상태 코드 목록
        List<Code> reviewStatusCodes = codeSVC.getCodeList("REVIEW_STATUS");
        
        // 리뷰 상태명 매핑
        Map<Long, String> reviewStatusMap = codeSVC.getCodeDecodeMap("REVIEW_STATUS");

        log.info("리뷰 목록 조회 완료 - totalCount: {}, reviews.size: {}", totalCount, reviews.size());

        model.addAttribute("reviews", reviews);
        model.addAttribute("pagination", pagination);
        model.addAttribute("reviewStatusCodes", reviewStatusCodes);
        model.addAttribute("reviewStatusMap", reviewStatusMap);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("title", "리뷰 관리");
        model.addAttribute("use_banner", false);

        addAuthInfoToModel(model, session);
        return "admin/reviews/list";
    }

    @GetMapping("/{reviewId}")
    public String reviewDetail(@PathVariable Long reviewId, Model model, HttpSession session) {
        addAuthInfoToModel(model, session);
        Optional<Review> reviewOpt = reviewService.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            model.addAttribute("errorMessage", "리뷰를 찾을 수 없습니다.");
            return "admin/reviews/reviewDetail";
        }
        Review review = reviewOpt.get();
                    String statusDecode = codeSVC.getCodeDecode("REVIEW_STATUS", review.getStatusId());
        // 캐시에서 코드 정보 가져오기
        Map<Long, String> reviewStatusMap = codeSVC.getCodeDecodeMap("REVIEW_STATUS");
        Map<Long, String> reviewStatusValueMap = codeSVC.getCodeDecodeMap("REVIEW_STATUS");
        List<ReviewComment> comments = reviewCommentService.findByReviewId(reviewId);
        List<String> commentStatusDecodes = comments.stream()
                .map(c -> codeSVC.getCodeDecode("REVIEW_COMMENT_STATUS", c.getStatusId()))
                .toList();
        Map<Long, String> reviewCommentStatusMap = codeSVC.getCodeDecodeMap("REVIEW_COMMENT_STATUS");
        Map<Long, String> reviewCommentStatusValueMap = codeSVC.getCodeDecodeMap("REVIEW_COMMENT_STATUS");
        List<ReportDetailDTO> reportList = reportService.findDetailsByTarget("REVIEW", reviewId);
        model.addAttribute("review", review);
        model.addAttribute("statusDecode", statusDecode);
        model.addAttribute("reviewStatusMap", reviewStatusMap);
        model.addAttribute("reviewStatusValueMap", reviewStatusValueMap);
        model.addAttribute("comments", comments);
        model.addAttribute("commentStatusDecodes", commentStatusDecodes);
        model.addAttribute("reviewCommentStatusMap", reviewCommentStatusMap);
        model.addAttribute("reviewCommentStatusValueMap", reviewCommentStatusValueMap);
        model.addAttribute("reportList", reportList);
        return "admin/reviews/reviewDetail";
    }

    @PostMapping("/{reviewId}/status")
    public String updateReviewStatus(@PathVariable Long reviewId, @RequestParam Long status, RedirectAttributes redirectAttributes) {
        int result = reviewService.updateStatus(reviewId, status);
        if (result > 0) {
            redirectAttributes.addFlashAttribute("message", "리뷰 상태가 변경되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 상태 변경에 실패했습니다.");
        }
        return "redirect:/admin/reviews/" + reviewId;
    }

    @PostMapping("/comments/{commentId}/status")
    public String updateCommentStatus(@PathVariable Long commentId, @RequestParam Long status, RedirectAttributes redirectAttributes) {
        int result = reviewCommentService.updateStatus(commentId, status);
        // 댓글이 속한 리뷰로 리다이렉트
        Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
        Long reviewId = commentOpt.map(ReviewComment::getReviewId).orElse(null);
        if (result > 0) {
            redirectAttributes.addFlashAttribute("message", "댓글 상태가 변경되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 상태 변경에 실패했습니다.");
        }
        return reviewId != null ? "redirect:/admin/reviews/" + reviewId : "redirect:/admin";
    }
} 