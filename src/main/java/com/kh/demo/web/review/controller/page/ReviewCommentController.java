package com.kh.demo.web.review.controller.page;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewCommentService;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.web.review.controller.page.form.ReviewCommentForm;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.domain.common.svc.CodeSVC;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@RequestMapping("/reviews")
@Controller
@RequiredArgsConstructor
public class ReviewCommentController {

    private final ReviewCommentService reviewCommentService;
    private final ReviewService reviewService;
    private final MessageSource messageSource;
    private final CodeSVC codeSVC;

    // 공개 리뷰 댓글 작성
    @PostMapping("/{reviewId}/comments")
    public String writeComment(@PathVariable Long reviewId, 
                              @Valid @ModelAttribute ReviewCommentForm commentForm,
                              BindingResult bindingResult, 
                              HttpSession session, 
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 내용을 입력해주세요.");
            return "redirect:/reviews/" + reviewId;
        }

        try {
            // 리뷰 존재 여부 확인
            Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
            Optional<Review> reviewOpt = reviewService.findByIdAndStatus(reviewId, activeStatusId);
            if (reviewOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 리뷰입니다.");
                return "redirect:/reviews";
            }

            ReviewComment comment = new ReviewComment();
            comment.setReviewId(reviewId);
            comment.setContent(commentForm.getContent());
            Long commentActiveStatusId = codeSVC.getCodeId("REVIEW_COMMENT_STATUS", "ACTIVE");
            comment.setStatus(commentActiveStatusId);

            ReviewComment savedComment = reviewCommentService.createComment(comment, loginMember.getMemberId());
            
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 작성되었습니다.");
            return "redirect:/reviews/" + reviewId;
            
        } catch (Exception e) {
            log.error("댓글 작성 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 작성에 실패했습니다.");
            return "redirect:/reviews/" + reviewId;
        }
    }

    // 댓글 수정
    @PostMapping("/comments/{commentId}/edit")
    public String editComment(@PathVariable Long commentId, 
                             @Valid @ModelAttribute ReviewCommentForm commentForm,
                             BindingResult bindingResult, 
                             HttpSession session, 
                             Model model,
                             RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 내용을 입력해주세요.");
            return "redirect:/reviews";
        }

        try {
            // 댓글 존재 여부 및 권한 확인
            Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
            if (commentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 댓글입니다.");
                return "redirect:/reviews";
            }

            ReviewComment existingComment = commentOpt.get();
            if (!existingComment.getMemberId().equals(loginMember.getMemberId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "댓글 작성자만 수정할 수 있습니다.");
                return "redirect:/reviews";
            }

            ReviewComment comment = new ReviewComment();
            comment.setContent(commentForm.getContent());

            int result = reviewCommentService.updateComment(commentId, comment, loginMember.getMemberId());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "댓글이 수정되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "댓글 수정에 실패했습니다.");
            }
            
            return "redirect:/reviews/" + existingComment.getReviewId();
            
        } catch (Exception e) {
            log.error("댓글 수정 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 수정에 실패했습니다.");
            return "redirect:/reviews";
        }
    }

    // 댓글 삭제
    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId, 
                               HttpSession session, 
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/login";
        }

        try {
            // 댓글 존재 여부 및 권한 확인
            Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
            if (commentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 댓글입니다.");
                return "redirect:/reviews";
            }

            ReviewComment existingComment = commentOpt.get();
            if (!existingComment.getMemberId().equals(loginMember.getMemberId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "댓글 작성자만 삭제할 수 있습니다.");
                return "redirect:/reviews";
            }

            int result = reviewCommentService.deleteComment(commentId, loginMember.getMemberId());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "댓글이 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "댓글 삭제에 실패했습니다.");
            }
            
            return "redirect:/reviews/" + existingComment.getReviewId();
            
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 삭제에 실패했습니다.");
            return "redirect:/reviews";
        }
    }

    // 댓글 신고
    @PostMapping("/comments/{commentId}/report")
    public String reportComment(@PathVariable Long commentId, 
                               HttpSession session, 
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/login";
        }

        try {
            // 댓글 존재 여부 확인
            Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
            if (commentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 댓글입니다.");
                return "redirect:/reviews";
            }

            ReviewComment existingComment = commentOpt.get();
            
            // 자신의 댓글은 신고할 수 없음
            if (existingComment.getMemberId().equals(loginMember.getMemberId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "자신의 댓글은 신고할 수 없습니다.");
                return "redirect:/reviews/" + existingComment.getReviewId();
            }

            int result = reviewCommentService.reportComment(commentId, loginMember.getMemberId());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "댓글이 신고되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "댓글 신고에 실패했습니다.");
            }
            
            return "redirect:/reviews/" + existingComment.getReviewId();
            
        } catch (Exception e) {
            log.error("댓글 신고 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 신고에 실패했습니다.");
            return "redirect:/reviews";
        }
    }
} 