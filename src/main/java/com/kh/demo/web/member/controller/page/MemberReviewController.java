package com.kh.demo.web.member.controller.page;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewCommentService;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.review.controller.page.form.ReviewCommentForm;
import com.kh.demo.web.review.controller.page.form.ReviewForm;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * 회원 리뷰 관리 컨트롤러
 * - 리뷰 작성/수정/삭제
 * - 리뷰 댓글 관리
 */
@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberReviewController extends BaseController {

    private final ReviewService reviewService;
    private final ReviewCommentService reviewCommentService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CodeSVC codeSVC;

    /**
     * 리뷰 내역 조회
     */
    @GetMapping("/mypage/reviews")
    public String reviewHistory(HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Review> reviews = reviewService.findByMemberId(loginMember.getMemberId());
        model.addAttribute("reviews", reviews);
        
        return "member/review/reviewHistory";
    }

    /**
     * 리뷰 상세 조회
     */
    @GetMapping("/mypage/reviews/{reviewId}")
    public String reviewDetail(@PathVariable Long reviewId, HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
        Optional<Review> reviewOpt = reviewService.findByIdAndStatus(reviewId, activeStatusId);
        if (reviewOpt.isEmpty()) {
            return "redirect:/member/mypage/reviews";
        }
        
        Review review = reviewOpt.get();
        if (!review.getMemberId().equals(loginMember.getMemberId())) {
            return "redirect:/member/mypage/reviews";
        }

        // 상품 정보 조회
        Products product = productService.findById(review.getProductId()).orElse(null);
        
        // 리뷰 댓글 조회
        List<ReviewComment> comments = reviewCommentService.findByReviewId(reviewId);

        // 상태 decode 추가
        String statusDecode = codeSVC.getDecodeById(review.getStatus());
        List<String> commentStatusDecodes = comments.stream()
            .map(c -> codeSVC.getDecodeById(c.getStatus()))
            .toList();
        model.addAttribute("statusDecode", statusDecode);
        model.addAttribute("commentStatusDecodes", commentStatusDecodes);

        model.addAttribute("review", review);
        model.addAttribute("product", product);
        model.addAttribute("comments", comments);
        model.addAttribute("commentForm", new ReviewCommentForm());
        
        return "member/review/reviewDetail";
    }

    /**
     * 리뷰 작성 폼
     */
    @GetMapping("/mypage/reviews/write")
    public String reviewWriteForm(@RequestParam Long productId, @RequestParam Long orderId, 
                                HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 상품 정보 조회
        Products product = productService.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/member/mypage/orders";
        }

        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setProductId(productId);
        reviewForm.setOrderId(orderId);

        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("product", product);
        model.addAttribute("orderId", orderId);
        
        return "member/review/reviewWriteForm";
    }

    /**
     * 리뷰 작성 처리
     */
    @PostMapping("/mypage/reviews/write")
    public String reviewWrite(@Valid @ModelAttribute ReviewForm reviewForm, 
                            BindingResult bindingResult, HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        log.info("reviewForm={}", reviewForm);
        if (loginMember == null) {
            return "redirect:/login";
        }

        // product, orderId를 항상 모델에 추가
        Products product = null;
        if (reviewForm.getProductId() != null) {
            product = productService.findById(reviewForm.getProductId()).orElse(null);
        }
        if (product == null) {
            model.addAttribute("errorMessage", "상품 정보를 찾을 수 없습니다.");
        }
        model.addAttribute("product", product);
        model.addAttribute("orderId", reviewForm.getOrderId());

        if (bindingResult.hasErrors()) {
            return "member/review/reviewWriteForm";
        }

        try {
            // 주문 상태 확인
            Long deliveredStatusId = codeSVC.getCodeId("ORDER_STATUS", "DELIVERED");
            if (deliveredStatusId == null) {
                model.addAttribute("errorMessage", "주문 상태 코드를 찾을 수 없습니다.");
                return "member/review/reviewWriteForm";
            }
            
            // 주문 정보 조회
            Optional<Order> orderOpt = orderService.findByOrderId(reviewForm.getOrderId());
            if (orderOpt.isEmpty()) {
                model.addAttribute("errorMessage", "존재하지 않는 주문입니다.");
                return "member/review/reviewWriteForm";
            }
            
            Order order = orderOpt.get();
            if (!order.getOrderStatusId().equals(deliveredStatusId)) {
                model.addAttribute("errorMessage", "배송완료된 주문만 리뷰를 작성할 수 있습니다. 현재 주문 상태를 확인해주세요.");
                return "member/review/reviewWriteForm";
            }
            
            // 이미 리뷰가 작성되었는지 확인 (주문 ID + 상품 ID로 체크)
            Optional<Review> existingReview = reviewService.findByOrderIdAndProductId(reviewForm.getOrderId(), reviewForm.getProductId());
            if (existingReview.isPresent()) {
                model.addAttribute("errorMessage", "이미 해당 상품에 대한 리뷰가 작성된 주문입니다.");
                return "member/review/reviewWriteForm";
            }
            
            Review review = new Review();
            BeanUtils.copyProperties(reviewForm, review);
            review.setMemberId(loginMember.getMemberId());
            
            // 리뷰 상태를 ACTIVE로 설정
            Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
            review.setStatus(activeStatusId);
            
            // 초기값 설정
            review.setHelpfulCount(0);
            review.setReportCount(0);
            
            Review savedReview = reviewService.createReview(review);
            
            return "redirect:/member/mypage/reviews/" + savedReview.getReviewId();
        } catch (Exception e) {
            log.error("리뷰 작성 중 오류 발생", e);
            model.addAttribute("errorMessage", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "member/review/reviewWriteForm";
        }
    }

    /**
     * 리뷰 수정 폼
     */
    @GetMapping("/mypage/reviews/{reviewId}/edit")
    public String reviewEditForm(@PathVariable Long reviewId, HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
        Optional<Review> reviewOpt = reviewService.findByIdAndStatus(reviewId, activeStatusId);
        if (reviewOpt.isEmpty()) {
            return "redirect:/member/mypage/reviews";
        }
        
        Review review = reviewOpt.get();
        if (!review.getMemberId().equals(loginMember.getMemberId())) {
            return "redirect:/member/mypage/reviews";
        }

        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setProductId(review.getProductId());
        reviewForm.setRating(review.getRating());
        reviewForm.setContent(review.getContent());

        // 상품 정보 조회
        Products product = productService.findById(review.getProductId()).orElse(null);

        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("review", review);
        model.addAttribute("product", product);
        
        return "member/review/reviewEditForm";
    }

    /**
     * 리뷰 수정 처리
     */
    @PostMapping("/mypage/reviews/{reviewId}/edit")
    public String reviewEdit(@PathVariable Long reviewId, @Valid @ModelAttribute ReviewForm reviewForm,
                           BindingResult bindingResult, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
        Optional<Review> reviewOpt = reviewService.findByIdAndStatus(reviewId, activeStatusId);
        if (reviewOpt.isEmpty()) {
            return "redirect:/member/mypage/reviews";
        }
        
        Review review = reviewOpt.get();
        if (!review.getMemberId().equals(loginMember.getMemberId())) {
            return "redirect:/member/mypage/reviews";
        }

        if (bindingResult.hasErrors()) {
            return "member/review/reviewEditForm";
        }

        try {
            Review updateReview = new Review();
            updateReview.setRating(reviewForm.getRating());
            updateReview.setContent(reviewForm.getContent());
            
            int result = reviewService.updateReview(reviewId, updateReview, loginMember.getMemberId());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("message", "리뷰가 성공적으로 수정되었습니다.");
                return "redirect:/member/mypage/reviews/" + reviewId;
            } else {
                bindingResult.reject("review.error", "리뷰 수정에 실패했습니다.");
                return "member/review/reviewEditForm";
            }
        } catch (Exception e) {
            bindingResult.reject("review.error", "리뷰 수정 중 오류가 발생했습니다.");
            return "member/review/reviewEditForm";
        }
    }

    /**
     * 리뷰 삭제
     */
    @PostMapping("/mypage/reviews/{reviewId}/delete")
    public String reviewDelete(@PathVariable Long reviewId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
        Optional<Review> reviewOpt = reviewService.findByIdAndStatus(reviewId, activeStatusId);
        if (reviewOpt.isEmpty()) {
            return "redirect:/member/mypage/reviews";
        }
        
        Review review = reviewOpt.get();
        if (!review.getMemberId().equals(loginMember.getMemberId())) {
            return "redirect:/member/mypage/reviews";
        }

        try {
            int result = reviewService.deleteReview(reviewId, loginMember.getMemberId(), false);
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("message", "리뷰가 성공적으로 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "리뷰 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "리뷰 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/member/mypage/reviews";
    }

    /**
     * 리뷰 댓글 작성
     */
    @PostMapping("/mypage/reviews/{reviewId}/comments")
    public String reviewCommentWrite(@PathVariable Long reviewId, @Valid @ModelAttribute ReviewCommentForm commentForm,
                                   BindingResult bindingResult, HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "redirect:/member/mypage/reviews/" + reviewId;
        }

        try {
            ReviewComment comment = new ReviewComment();
            comment.setReviewId(reviewId);
            comment.setMemberId(loginMember.getMemberId());
            comment.setContent(commentForm.getContent());
            
            ReviewComment savedComment = reviewCommentService.createComment(comment, loginMember.getMemberId());
            
            return "redirect:/member/mypage/reviews/" + reviewId;
        } catch (Exception e) {
            bindingResult.reject("comment.error", "댓글 작성 중 오류가 발생했습니다.");
            return "member/review/reviewDetail";
        }
    }

    /**
     * 리뷰 댓글 수정
     */
    @PostMapping("/mypage/reviews/comments/{commentId}/edit")
    public String reviewCommentEdit(@PathVariable Long commentId, @Valid @ModelAttribute ReviewCommentForm commentForm,
                                  BindingResult bindingResult, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "redirect:/member/mypage/reviews";
        }

        try {
            ReviewComment updateComment = new ReviewComment();
            updateComment.setContent(commentForm.getContent());
            
            int result = reviewCommentService.updateComment(commentId, updateComment, loginMember.getMemberId());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 수정되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "댓글 수정에 실패했습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "댓글 수정 중 오류가 발생했습니다.");
        }
        
        return "redirect:/member/mypage/reviews";
    }

    /**
     * 리뷰 댓글 삭제
     */
    @PostMapping("/mypage/reviews/comments/{commentId}/delete")
    public String reviewCommentDelete(@PathVariable Long commentId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }



        try {
            int result = reviewCommentService.deleteComment(commentId, loginMember.getMemberId());
            
            if (result > 0) {
                redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "댓글 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "댓글 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/member/mypage/reviews";
    }
} 