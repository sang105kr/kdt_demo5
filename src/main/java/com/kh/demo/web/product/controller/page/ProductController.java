package com.kh.demo.web.product.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.product.svc.ProductSearchService;
import com.kh.demo.domain.product.svc.ProductService;

import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.domain.review.vo.ReviewDetailVO;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.product.controller.page.dto.ProductDetailDTO;
import com.kh.demo.web.product.controller.page.dto.ProductListDTO;
import com.kh.demo.web.product.controller.page.dto.SearchCriteria;
import com.kh.demo.web.product.controller.page.dto.SearchResult;
import com.kh.demo.web.review.controller.page.form.ReviewCommentForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 고객용 상품 페이지 컨트롤러
 * - 웹 페이지 렌더링 (Thymeleaf)
 * - 고객 중심 기능 (검색, 상세보기, 자동완성)
 * - 세션 기반 사용자 정보 처리
 * - 다국어 지원
 */
@Slf4j
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {

    private final ProductSearchService productSearchService;
    private final ProductService productService;
    private final ReviewService reviewService;

    private final MessageSource messageSource;
    private final CodeSVC codeSVC;
    private final OrderService orderService;

    /**
     * Member 객체에서 memberId 추출
     */
    private Long extractMemberId(Object loginMember) {
        if (loginMember instanceof LoginMember) {
            return ((LoginMember) loginMember).getMemberId();
        }
        return null;
    }

    /**
     * 통합 상품 목록/검색 페이지
     * Elasticsearch 우선, 실패 시 Oracle fallback
     */
    @GetMapping({"", "/list"})
    public String productList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            HttpSession session) {
        
        log.info("상품 목록/검색 페이지 요청 - keyword: {}, category: {}, minPrice: {}, maxPrice: {}, minRating: {}, sortBy: {}, sortOrder: {}, page: {}, size: {}", 
                keyword, category, minPrice, maxPrice, minRating, sortBy, sortOrder, page, size);
        
        try {
            // 검색 조건 생성
            SearchCriteria criteria = SearchCriteria.of(keyword, category, minPrice, maxPrice, 
                                                       minRating, sortBy, sortOrder, page, size);
            
            // 통합 검색 실행
            SearchResult<ProductListDTO> searchResult = productSearchService.search(criteria);
            
            // 모델에 데이터 추가
            model.addAttribute("products", searchResult.getItems());
            model.addAttribute("searchResult", searchResult);
            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("minRating", minRating);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortOrder", sortOrder);
            
            // 인기 검색어 추가
            model.addAttribute("popularKeywords", productSearchService.getPopularKeywords());
            
            // 새로운 DTO 구조에서 카테고리명이 없는 경우를 위한 fallback 처리
            // ProductListDTO에서 categoryName이 설정되지 않은 경우에만 categoryNames Map 사용
            // 이는 Elasticsearch에서 categoryName이 저장되지 않은 기존 데이터를 위한 처리
            
            // 검색 히스토리 추가 (로그인 사용자)
            Object loginMember = getLoginMember(session);
            if (loginMember != null) {
                // Member 객체에서 memberId 추출
                Long memberId = extractMemberId(loginMember);
                if (memberId != null) {
                    model.addAttribute("searchHistory", productSearchService.getSearchHistory(memberId));
                }
            }
            
            // 검색 히스토리 저장
            if (keyword != null && !keyword.trim().isEmpty() && loginMember != null) {
                Long memberId = extractMemberId(loginMember);
                if (memberId != null) {
                    productSearchService.saveSearchHistory(keyword.trim(), memberId);
                }
            }
            
            model.addAttribute("title", messageSource.getMessage("product.list.title", null, LocaleContextHolder.getLocale()));
            model.addAttribute("use_banner", true);
            model.addAttribute("banner", messageSource.getMessage("product.list.banner", null, LocaleContextHolder.getLocale()));
            
            return "products/list";
            
        } catch (Exception e) {
            log.error("상품 목록/검색 중 오류 발생", e);
            model.addAttribute("errorMessage", messageSource.getMessage("product.list.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("products", List.of());
            model.addAttribute("searchResult", SearchResult.empty(size));
            return "products/list";
        }
    }

    /**
     * 상품 상세 페이지 (Oracle + Elasticsearch 통합)
     */
    @GetMapping("/{productId}")
    public String productDetail(@PathVariable Long productId, 
                               @RequestParam(defaultValue = "1") int reviewPage,
                               @RequestParam(defaultValue = "1") int qnaPage,
                               Model model,
                               HttpSession session) {
        log.info("상품 상세 조회 요청 - productId: {}, reviewPage: {}", productId, reviewPage);
        
        try {
            // 통합 상품 상세 정보 조회
            ProductDetailDTO productDetail = productSearchService.getProductDetail(productId);
            
            if (productDetail == null) {
                model.addAttribute("errorMessage", messageSource.getMessage("product.detail.notfound", null, LocaleContextHolder.getLocale()));
                return "redirect:/products";
            }
            
            // 관련 상품 조회 (같은 카테고리)
            String categoryCode = codeSVC.getCodeValue("PRODUCT_CATEGORY", productDetail.getCategoryId());
            SearchResult<ProductListDTO> relatedProducts = productSearchService.searchByCategory(
                categoryCode, 1, 4);
            
            // 리뷰 페이징 처리 (페이지당 10개)
            Long activeStatusId = codeSVC.getCodeId("REVIEW_STATUS", "ACTIVE");
            List<Review> allReviews = reviewService.findByProductIdAndStatus(productId, activeStatusId);
            int totalReviewCount = allReviews.size();
            
            // 페이징 계산
            int reviewPageSize = 10;
            int reviewOffset = (reviewPage - 1) * reviewPageSize;
            List<Review> pagedReviews = allReviews.stream()
                .skip(reviewOffset)
                .limit(reviewPageSize)
                .toList();
            
            // ReviewDetailVO로 변환하여 member 정보 포함
            List<ReviewDetailVO> reviewDetails = pagedReviews.stream()
                .map(review -> reviewService.findReviewDetailById(review.getReviewId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
            
            // Pagination 객체 생성
            Pagination reviewPagination = new Pagination(reviewPage, reviewPageSize, totalReviewCount);
            
            // Q&A 데이터 조회 (임시로 빈 리스트)
            List<Object> qnas = List.of();
            Long totalQnaCount = 0L;
            Pagination qnaPagination = new Pagination(qnaPage, 10, 0);
            
            // 구매 여부 확인
            boolean isPurchased = false;
            boolean hasReviewed = false;
            Object loginMember = getLoginMember(session);
            if (loginMember != null) {
                Long memberId = extractMemberId(loginMember);
                if (memberId != null) {
                    isPurchased = orderService.isProductPurchasedByMember(productId, memberId);
                    hasReviewed = reviewService.hasUserReviewedProduct(memberId, productId);
                }
            }
            log.info("reviews={}",reviewDetails);
            model.addAttribute("product", productDetail);
            model.addAttribute("relatedProducts", relatedProducts.getItems());
            model.addAttribute("reviews", reviewDetails);
            model.addAttribute("reviewPagination", reviewPagination);
            model.addAttribute("qnas", qnas);
            model.addAttribute("qnaPagination", qnaPagination);
            model.addAttribute("qnaCount", totalQnaCount);
            model.addAttribute("isPurchased", isPurchased);
            model.addAttribute("hasReviewed", hasReviewed);
            model.addAttribute("title", productDetail.getPname());
            model.addAttribute("use_banner", false);
            model.addAttribute("commentForm", new ReviewCommentForm());

            // 카테고리명 추가 (하위 카테고리에서 검색)
            Code categoryCodeObj = codeSVC.getCodeList("PRODUCT_CATEGORY").stream()
                .filter(code -> "Y".equals(code.getUseYn()) && code.getPcode() != null)
                .filter(cat -> cat.getCode().equals(categoryCode))
                .findFirst()
                .orElse(null);
            
            if (categoryCodeObj != null) {
                model.addAttribute("categoryName", categoryCodeObj.getDecode());
                model.addAttribute("categoryCodeId", categoryCodeObj.getCodeId());
            } else {
                model.addAttribute("categoryName", categoryCode);
                model.addAttribute("categoryCodeId", productDetail.getCategoryId());
            }
            
            return "products/detail";
            
        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", messageSource.getMessage("product.detail.error", null, LocaleContextHolder.getLocale()));
            return "redirect:/products";
        }
    }

    /**
     * 카테고리별 상품 조회
     */
    @GetMapping("/category/{categoryId}")
    public String productsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        log.info("카테고리별 상품 조회 요청 - categoryId: {}, page: {}, size: {}", categoryId, page, size);
        
        try {
            // codeId로 카테고리 정보 조회
            String categoryDecode = codeSVC.getCodeDecode("PRODUCT_CATEGORY", categoryId);
            if (categoryDecode == null) {
                log.warn("존재하지 않는 카테고리 ID: {}", categoryId);
                return "redirect:/products";
            }
            
            // 카테고리 코드 값 조회
            String categoryCodeValue = codeSVC.getCodeValue("PRODUCT_CATEGORY", categoryId);
            
            SearchResult<ProductListDTO> searchResult = productSearchService.search(
                SearchCriteria.of(null, categoryCodeValue, null, null, null, "created_date", "desc", page, size)
            );
            
            model.addAttribute("products", searchResult.getItems());
            model.addAttribute("searchResult", searchResult);
            model.addAttribute("categoryName", categoryDecode);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("title", categoryDecode + " 상품");
            model.addAttribute("use_banner", true);
            model.addAttribute("banner", categoryDecode + " 카테고리 상품을 확인해보세요!");
            
            return "products/list";
            
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 중 오류 발생", e);
            return "redirect:/products";
        }
    }

    /**
     * 자동완성 API
     */
    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam String prefix) {
        log.info("자동완성 요청 - prefix: {}", prefix);
        List<String> suggestions = productSearchService.autocomplete(prefix);
        
        // 하이라이팅 적용
        return suggestions.stream()
                .map(suggestion -> highlightText(suggestion, prefix))
                .collect(Collectors.toList());
    }
    
    /**
     * 텍스트 하이라이팅
     */
    private String highlightText(String text, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return text;
        }
        
        String lowerText = text.toLowerCase();
        String lowerPrefix = prefix.toLowerCase();
        
        if (lowerText.contains(lowerPrefix)) {
            int startIndex = lowerText.indexOf(lowerPrefix);
            int endIndex = startIndex + prefix.length();
            
            String before = text.substring(0, startIndex);
            String highlighted = text.substring(startIndex, endIndex);
            String after = text.substring(endIndex);
            
            return before + "<b>" + highlighted + "</b>" + after;
        }
        
        return text;
    }
} 