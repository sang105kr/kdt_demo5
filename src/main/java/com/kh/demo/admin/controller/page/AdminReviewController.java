package com.kh.demo.admin.controller.page;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.domain.review.svc.ReviewCommentService;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.review.dao.ReviewReportDAO;
import com.kh.demo.domain.review.entity.ReviewReport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    private final ReviewService reviewService;
    private final ReviewCommentService reviewCommentService;
    private final CodeSVC codeSVC;
    private final ReviewReportDAO reviewReportDAO;

    @GetMapping("/{reviewId}")
    public String reviewDetail(@PathVariable Long reviewId, Model model) {
        Optional<Review> reviewOpt = reviewService.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            model.addAttribute("errorMessage", "리뷰를 찾을 수 없습니다.");
            return "admin/reviewDetail";
        }
        Review review = reviewOpt.get();
        String statusDecode = codeSVC.getDecodeById(review.getStatus());
        List<Code> reviewStatusCodes = codeSVC.findByGcode("REVIEW_STATUS");
        List<ReviewComment> comments = reviewCommentService.findByReviewId(reviewId);
        List<String> commentStatusDecodes = comments.stream()
                .map(c -> codeSVC.getDecodeById(c.getStatus()))
                .toList();
        List<Code> reviewCommentStatusCodes = codeSVC.findByGcode("REVIEW_COMMENT_STATUS");
        List<ReviewReport> reportList = reviewReportDAO.findByReviewId(reviewId);
        model.addAttribute("review", review);
        model.addAttribute("statusDecode", statusDecode);
        model.addAttribute("reviewStatusCodes", reviewStatusCodes);
        model.addAttribute("comments", comments);
        model.addAttribute("commentStatusDecodes", commentStatusDecodes);
        model.addAttribute("reviewCommentStatusCodes", reviewCommentStatusCodes);
        model.addAttribute("reportList", reportList);
        return "admin/reviewDetail";
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