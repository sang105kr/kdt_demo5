package com.kh.demo.web.page;

import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.common.svc.UploadFileSVC;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.web.page.form.product.DetailForm;
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

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class CustomerProductController extends BaseController {

    private final ProductService productService;
    private final UploadFileSVC uploadFileSVC;
    private final MessageSource messageSource;

    /**
     * 상품 검색 페이지
     */
    @GetMapping("/search")
    public String searchForm(Model model) {
        log.info("상품 검색 페이지 요청");
        
        model.addAttribute("title", messageSource.getMessage("product.search.title", null, LocaleContextHolder.getLocale()));
        model.addAttribute("use_banner", true);
        model.addAttribute("banner", messageSource.getMessage("product.search.banner", null, LocaleContextHolder.getLocale()));
        
        return "products/search";
    }

    /**
     * 메인 상품 목록 페이지
     */
    @GetMapping("")
    public String productList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        log.info("메인 상품 목록 페이지 요청 - page: {}, size: {}", page, size);
        
        try {
            List<Products> products = productService.getAllProducts(page, size);
            int totalCount = productService.getTotalProductCount();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            model.addAttribute("products", products);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("size", size);
            
            model.addAttribute("title", messageSource.getMessage("product.list.title", null, LocaleContextHolder.getLocale()));
            model.addAttribute("use_banner", true);
            model.addAttribute("banner", messageSource.getMessage("product.list.banner", null, LocaleContextHolder.getLocale()));
            
            return "products/search-results";
            
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", messageSource.getMessage("product.list.error", null, LocaleContextHolder.getLocale()));
            return "products/search";
        }
    }

    /**
     * 상품 검색 결과
     */
    @GetMapping("/search-results")
    public String searchResults(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        log.info("상품 검색 결과 요청 - keyword: {}, category: {}, page: {}, size: {}", keyword, category, page, size);
        
        try {
            List<Products> products;
            int totalCount;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 키워드 검색
                products = productService.searchProductsByKeyword(keyword, page, size);
                totalCount = productService.countProductsByKeyword(keyword);
            } else if (category != null && !category.trim().isEmpty()) {
                // 카테고리 검색
                products = productService.getProductsByCategory(category, page, size);
                totalCount = productService.countProductsByCategory(category);
            } else {
                // 전체 상품 조회
                products = productService.getAllProducts(page, size);
                totalCount = productService.getTotalProductCount();
            }
            
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            model.addAttribute("products", products);
            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("size", size);
            
            model.addAttribute("title", messageSource.getMessage("product.search.results.title", null, LocaleContextHolder.getLocale()));
            model.addAttribute("use_banner", true);
            model.addAttribute("banner", messageSource.getMessage("product.search.results.banner", null, LocaleContextHolder.getLocale()));
            
            return "products/search-results";
            
        } catch (Exception e) {
            log.error("상품 검색 중 오류 발생", e);
            model.addAttribute("errorMessage", messageSource.getMessage("product.search.error", null, LocaleContextHolder.getLocale()));
            return "products/search";
        }
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    public String productDetail(@PathVariable Long productId, Model model) {
        log.info("상품 상세 조회 요청 - productId: {}", productId);
        
        try {
            Products product = productService.getProductById(productId);
            if (product == null) {
                model.addAttribute("errorMessage", messageSource.getMessage("product.detail.notfound", null, LocaleContextHolder.getLocale()));
                return "products/search";
            }
            
            // 상품 이미지 및 매뉴얼 파일 조회
            List<UploadFile> imageFiles = uploadFileSVC.findByCodeAndRid(7L, product.getProductId().toString()); // PRODUCT_IMAGE
            List<UploadFile> manualFiles = uploadFileSVC.findByCodeAndRid(8L, product.getProductId().toString()); // PRODUCT_MANUAL
            
            // 첫 번째 이미지와 매뉴얼 URL 설정
            String imageUrl = imageFiles.isEmpty() ? null : imageFiles.get(0).getStoreFilename();
            String manualUrl = manualFiles.isEmpty() ? null : manualFiles.get(0).getStoreFilename();
            
            DetailForm detailForm = new DetailForm();
            detailForm.setProductId(product.getProductId());
            detailForm.setPname(product.getPname());
            detailForm.setProductName(product.getPname());
            detailForm.setDescription(product.getDescription());
            detailForm.setPrice(product.getPrice());
            detailForm.setCategory(product.getCategory());
            detailForm.setRating(product.getRating());
            detailForm.setStockQuantity(product.getStockQuantity());
            detailForm.setImageUrl(imageUrl);
            detailForm.setManualUrl(manualUrl);
            
            model.addAttribute("product", product);
            model.addAttribute("detailForm", detailForm);
            model.addAttribute("title", product.getPname());
            model.addAttribute("use_banner", false);
            
            return "products/detail";
            
        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", messageSource.getMessage("product.detail.error", null, LocaleContextHolder.getLocale()));
            return "products/search";
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
            List<Products> products = productService.getProductsByCategory(category, page, size);
            int totalCount = productService.countProductsByCategory(category);
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            model.addAttribute("products", products);
            model.addAttribute("category", category);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("size", size);
            
            String categoryName = messageSource.getMessage("product.category." + category, null, LocaleContextHolder.getLocale());
            model.addAttribute("categoryName", categoryName);
            model.addAttribute("title", categoryName);
            model.addAttribute("use_banner", true);
            model.addAttribute("banner", messageSource.getMessage("product.category.banner", new Object[]{categoryName}, LocaleContextHolder.getLocale()));
            
            return "products/search-results";
            
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", messageSource.getMessage("product.category.error", null, LocaleContextHolder.getLocale()));
            return "products/search";
        }
    }
} 