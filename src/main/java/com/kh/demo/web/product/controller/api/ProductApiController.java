package com.kh.demo.web.product.controller.api;

import com.kh.demo.domain.product.svc.ProductSearchService;
import com.kh.demo.web.common.controller.api.BaseApiController;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.web.product.controller.page.dto.ProductDetailDTO;
import com.kh.demo.web.product.controller.page.dto.ProductListDTO;
import com.kh.demo.web.product.controller.page.dto.SearchCriteria;
import com.kh.demo.web.product.controller.page.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 고객용 상품 REST API 컨트롤러
 * - 고객 UI에서 사용하는 AJAX 기능 제공
 * - 자동완성, 실시간 검색, 무한 스크롤 등
 * - ProductSearchService 사용 (Elasticsearch + Oracle 통합)
 * - JSON 응답 형식
 * 
 * vs AdminApiProductController: 고객용 vs 관리자용
 */
@Slf4j
@RequestMapping("/api/products")
@RestController
@RequiredArgsConstructor
public class ProductApiController extends BaseApiController {

    private final ProductSearchService productSearchService;

    /**
     * 자동완성 API (고객용)
     * - 검색창에서 실시간 자동완성 제공
     * GET /api/products/autocomplete?prefix=삼성
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(@RequestParam String prefix) {
        log.info("고객용 자동완성 요청: prefix={}", prefix);
        
        try {
            List<String> results = productSearchService.autocomplete(prefix);
            ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("자동완성 검색 실패", e);
            ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.of());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 실시간 검색 API (고객용)
     * - 검색창에서 실시간 검색 결과 제공
     * GET /api/products/search/live?keyword=노트북&limit=5
     */
    @GetMapping("/search/live")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> liveSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("고객용 실시간 검색 요청: keyword={}, limit={}", keyword, limit);
        
        try {
            SearchCriteria criteria = SearchCriteria.of(keyword, null, null, null, null, null, null, 1, limit);
            SearchResult<ProductListDTO> searchResult = productSearchService.search(criteria);
            
            ApiResponse<List<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchResult.getItems());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("실시간 검색 실패", e);
            ApiResponse<List<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.of());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 통합 검색 API (고객용)
     * - 고객용 상품 목록 페이지의 AJAX 검색
     * GET /api/products/search?keyword=노트북&category=전자제품&page=1&size=12
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResult<ProductListDTO>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        log.info("고객용 통합 검색 요청 - keyword: {}, category: {}, minPrice: {}, maxPrice: {}, minRating: {}, sortBy: {}, sortOrder: {}, page: {}, size: {}", 
                keyword, category, minPrice, maxPrice, minRating, sortBy, sortOrder, page, size);
        
        try {
            SearchCriteria criteria = SearchCriteria.of(keyword, category, minPrice, maxPrice, 
                                                       minRating, sortBy, sortOrder, page, size);
            SearchResult<ProductListDTO> searchResult = productSearchService.search(criteria);
            
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("고객용 통합 검색 실패", e);
            SearchResult<ProductListDTO> emptyResult = SearchResult.empty(size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, emptyResult);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 카테고리별 상품 조회 API (고객용)
     * - 카테고리 페이지의 AJAX 호출
     * GET /api/products/category/전자제품?page=1&size=12
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<SearchResult<ProductListDTO>>> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        log.info("고객용 카테고리별 상품 조회 요청 - category: {}, page: {}, size: {}", category, page, size);
        
        try {
            SearchResult<ProductListDTO> searchResult = productSearchService.searchByCategory(category, page, size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 실패", e);
            SearchResult<ProductListDTO> emptyResult = SearchResult.empty(size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, emptyResult);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 키워드 검색 API (고객용)
     * - 검색 결과 페이지의 AJAX 호출
     * GET /api/products/search/keyword?keyword=노트북&page=1&size=12
     */
    @GetMapping("/search/keyword")
    public ResponseEntity<ApiResponse<SearchResult<ProductListDTO>>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        log.info("고객용 키워드 검색 요청 - keyword: {}, page: {}, size: {}", keyword, page, size);
        
        try {
            SearchResult<ProductListDTO> searchResult = productSearchService.searchByKeyword(keyword, page, size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("키워드 검색 실패", e);
            SearchResult<ProductListDTO> emptyResult = SearchResult.empty(size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, emptyResult);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 가격 범위 검색 API (고객용)
     * - 필터링 기능의 AJAX 호출
     * GET /api/products/search/price?minPrice=100000&maxPrice=500000&page=1&size=12
     */
    @GetMapping("/search/price")
    public ResponseEntity<ApiResponse<SearchResult<ProductListDTO>>> searchByPriceRange(
            @RequestParam Long minPrice,
            @RequestParam Long maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        log.info("고객용 가격 범위 검색 요청 - minPrice: {}, maxPrice: {}, page: {}, size: {}", minPrice, maxPrice, page, size);
        
        try {
            SearchResult<ProductListDTO> searchResult = productSearchService.searchByPriceRange(minPrice, maxPrice, page, size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("가격 범위 검색 실패", e);
            SearchResult<ProductListDTO> emptyResult = SearchResult.empty(size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, emptyResult);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 평점 검색 API (고객용)
     * - 필터링 기능의 AJAX 호출
     * GET /api/products/search/rating?minRating=4.0&page=1&size=12
     */
    @GetMapping("/search/rating")
    public ResponseEntity<ApiResponse<SearchResult<ProductListDTO>>> searchByRating(
            @RequestParam Double minRating,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        log.info("고객용 평점 검색 요청 - minRating: {}, page: {}, size: {}", minRating, page, size);
        
        try {
            SearchResult<ProductListDTO> searchResult = productSearchService.searchByRating(minRating, page, size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("평점 검색 실패", e);
            SearchResult<ProductListDTO> emptyResult = SearchResult.empty(size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, emptyResult);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 상세 정보 API (고객용)
     * - 상품 상세 페이지의 AJAX 호출
     * GET /api/products/{productId}/detail
     */
    @GetMapping("/{productId}/detail")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetail(@PathVariable Long productId) {
        log.info("고객용 상품 상세 정보 요청 - productId: {}", productId);
        
        try {
            ProductDetailDTO productDetail = productSearchService.getProductDetail(productId);
            
            if (productDetail == null) {
                ApiResponse<ProductDetailDTO> response = ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            ApiResponse<ProductDetailDTO> response = ApiResponse.of(ApiResponseCode.SUCCESS, productDetail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품 상세 정보 조회 실패", e);
            ApiResponse<ProductDetailDTO> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 인기 검색어 API (고객용)
     * - 검색 페이지의 인기 검색어 표시
     * GET /api/products/popular-keywords
     */
    @GetMapping("/popular-keywords")
    public ResponseEntity<ApiResponse<List<String>>> getPopularKeywords() {
        log.info("고객용 인기 검색어 요청");
        
        try {
            List<String> popularKeywords = productSearchService.getPopularKeywords();
            ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.SUCCESS, popularKeywords);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("인기 검색어 조회 실패", e);
            ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.of());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 검색 히스토리 API (고객용)
     * - 로그인 사용자의 검색 히스토리
     * GET /api/products/search-history?memberId=123
     */
    @GetMapping("/search-history")
    public ResponseEntity<ApiResponse<List<String>>> getSearchHistory(@RequestParam Long memberId) {
        log.info("고객용 검색 히스토리 요청 - memberId: {}", memberId);
        
        try {
            List<String> searchHistory = productSearchService.getSearchHistory(memberId);
            ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchHistory);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("검색 히스토리 조회 실패", e);
            ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.of());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 검색 히스토리 저장 API (고객용)
     * - 검색 시 히스토리 저장
     * POST /api/products/search-history
     */
    @PostMapping("/search-history")
    public ResponseEntity<ApiResponse<String>> saveSearchHistory(
            @RequestParam String keyword,
            @RequestParam Long memberId) {
        log.info("고객용 검색 히스토리 저장 요청 - keyword: {}, memberId: {}", keyword, memberId);
        
        try {
            productSearchService.saveSearchHistory(keyword, memberId);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "검색 히스토리가 저장되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("검색 히스토리 저장 실패", e);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "검색 히스토리 저장에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 무한 스크롤 API (고객용)
     * - 상품 목록의 무한 스크롤 기능
     * GET /api/products/infinite-scroll?page=2&size=12
     */
    @GetMapping("/infinite-scroll")
    public ResponseEntity<ApiResponse<SearchResult<ProductListDTO>>> infiniteScroll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        log.info("고객용 무한 스크롤 요청 - page: {}, size: {}", page, size);
        
        try {
            SearchCriteria criteria = SearchCriteria.of(null, null, null, null, null, null, null, page, size);
            SearchResult<ProductListDTO> searchResult = productSearchService.search(criteria);
            
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.SUCCESS, searchResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("무한 스크롤 데이터 조회 실패", e);
            SearchResult<ProductListDTO> emptyResult = SearchResult.empty(size);
            ApiResponse<SearchResult<ProductListDTO>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, emptyResult);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 