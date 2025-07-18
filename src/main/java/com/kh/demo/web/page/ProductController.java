package com.kh.demo.web.page;

import com.kh.demo.domain.product.svc.ProductSearchService;
import com.kh.demo.web.dto.ProductDetailDTO;
import com.kh.demo.web.dto.ProductListDTO;
import com.kh.demo.web.dto.SearchCriteria;
import com.kh.demo.web.dto.SearchResult;
import com.kh.demo.web.page.form.login.LoginMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.List;

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
    private final MessageSource messageSource;

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
    @GetMapping("")
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
    public String productDetail(@PathVariable Long productId, Model model) {
        log.info("상품 상세 조회 요청 - productId: {}", productId);
        
        try {
            // 통합 상품 상세 정보 조회
            ProductDetailDTO productDetail = productSearchService.getProductDetail(productId);
            
            if (productDetail == null) {
                model.addAttribute("errorMessage", messageSource.getMessage("product.detail.notfound", null, LocaleContextHolder.getLocale()));
                return "redirect:/products";
            }
            
            // 관련 상품 조회 (같은 카테고리)
            SearchResult<ProductListDTO> relatedProducts = productSearchService.searchByCategory(
                productDetail.getCategory(), 1, 4);
            
            model.addAttribute("product", productDetail);
            model.addAttribute("relatedProducts", relatedProducts.getItems());
            model.addAttribute("title", productDetail.getPname());
            model.addAttribute("use_banner", false);
            
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
    @GetMapping("/category/{category}")
    public String productsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        log.info("카테고리별 상품 조회 요청 - category: {}, page: {}, size: {}", category, page, size);
        
        try {
            SearchResult<ProductListDTO> searchResult = productSearchService.searchByCategory(category, page, size);
            
            model.addAttribute("products", searchResult.getItems());
            model.addAttribute("searchResult", searchResult);
            model.addAttribute("category", category);
            
            String categoryName = messageSource.getMessage("product.category." + category, null, LocaleContextHolder.getLocale());
            model.addAttribute("categoryName", categoryName);
            model.addAttribute("title", categoryName);
            model.addAttribute("use_banner", true);
            model.addAttribute("banner", messageSource.getMessage("product.category.banner", new Object[]{categoryName}, LocaleContextHolder.getLocale()));
            
            return "products/list";
            
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", messageSource.getMessage("product.category.error", null, LocaleContextHolder.getLocale()));
            return "redirect:/products";
        }
    }

    /**
     * 자동완성 API (ProductApiController 호출)
     */
    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam String prefix) {
        try {
            // ProductApiController의 자동완성 API 호출
            return productSearchService.autocomplete(prefix);
        } catch (Exception e) {
            log.error("자동완성 검색 실패", e);
            return List.of();
        }
    }
} 