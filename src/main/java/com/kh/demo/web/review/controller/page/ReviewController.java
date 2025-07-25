package com.kh.demo.web.review.controller.page;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.product.entity.Products;

import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.web.review.controller.page.form.ReviewCommentForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping("/reviews")
@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final MessageSource messageSource;
    private final CodeSVC codeSVC;

    // 상품별 리뷰 목록 페이지
    @GetMapping("/product/{productId}")
    public String productReviews(@PathVariable Long productId, 
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        try {
            // 상품 정보 조회
            Optional<Products> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                String errorMessage = messageSource.getMessage("product.not.found", null, null);
                model.addAttribute("errorMessage", errorMessage);
                return "redirect:/products";
            }

            Products product = productOpt.get();
            
            // 상품의 리뷰 목록 조회 (공개 상태만)
            Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
            List<Review> reviews = reviewService.findByProductIdAndStatus(productId, activeStatusId);
            
            // 평균 평점 계산 (BigDecimal을 double로 변환)
            double averageRating = reviews.stream()
                .mapToDouble(review -> review.getRating().doubleValue())
                .average()
                .orElse(0.0);

            model.addAttribute("product", product);
            model.addAttribute("reviews", reviews);
            model.addAttribute("reviewCount", reviews.size());
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            
            // 카테고리명 추가 (하위 카테고리에서 검색)
            String categoryName = codeSVC.findActiveSubCodesByGcode("PRODUCT_CATEGORY").stream()
                .filter(cat -> cat.getCode().equals(product.getCategory()))
                .map(cat -> cat.getDecode())
                .findFirst()
                .orElse(product.getCategory());
            model.addAttribute("categoryName", categoryName);
            
            return "reviews/productReviews";
        } catch (Exception e) {
            log.error("상품 리뷰 목록 조회 실패", e);
            String errorMessage = messageSource.getMessage("review.list.failed", null, null);
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:/products/" + productId;
        }
    }

    // 공개 리뷰 상세 페이지
    @GetMapping("/{reviewId}")
    public String reviewDetail(@PathVariable Long reviewId, Model model) {
        try {
            // 리뷰 조회 (공개 상태만)
            Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
            Optional<Review> reviewOpt = reviewService.findByIdAndStatus(reviewId, activeStatusId);
            if (reviewOpt.isEmpty()) {
                String errorMessage = messageSource.getMessage("review.not.found", null, null);
                model.addAttribute("errorMessage", errorMessage);
                return "redirect:/products";
            }

            Review review = reviewOpt.get();
            
            // 상품 정보 조회
            Optional<Products> productOpt = productService.findById(review.getProductId());
            if (productOpt.isEmpty()) {
                String errorMessage = messageSource.getMessage("product.not.found", null, null);
                model.addAttribute("errorMessage", errorMessage);
                return "redirect:/products";
            }

            Products product = productOpt.get();
            
            // 리뷰 댓글 목록 조회
            List<ReviewComment> comments = reviewService.findCommentsByReviewId(reviewId);
            
            // 댓글 작성 폼 추가
            ReviewCommentForm commentForm = new ReviewCommentForm();
            commentForm.setReviewId(reviewId);
            
            model.addAttribute("review", review);
            model.addAttribute("product", product);
            model.addAttribute("comments", comments);
            model.addAttribute("commentCount", comments.size());
            
            // 카테고리명 추가 (하위 카테고리에서 검색)
            String categoryName = codeSVC.findActiveSubCodesByGcode("PRODUCT_CATEGORY").stream()
                .filter(cat -> cat.getCode().equals(product.getCategory()))
                .map(cat -> cat.getDecode())
                .findFirst()
                .orElse(product.getCategory());
            model.addAttribute("categoryName", categoryName);
            
            return "reviews/reviewDetail";
        } catch (Exception e) {
            log.error("리뷰 상세 조회 실패", e);
            String errorMessage = messageSource.getMessage("review.detail.failed", null, null);
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:/products";
        }
    }
} 