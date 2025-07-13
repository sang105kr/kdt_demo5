package com.kh.demo.web;

import com.kh.demo.domain.document.ProductDocument;
import com.kh.demo.domain.entity.Products;
import com.kh.demo.domain.products.svc.ProductSVC;
import com.kh.demo.web.api.ApiResponse;
import com.kh.demo.web.api.ApiResponseCode;
import com.kh.demo.web.exception.BusinessValidationException;
import com.kh.demo.web.api.products.SaveApi;
import com.kh.demo.web.api.products.UpdateApi;
import com.kh.demo.web.api.products.ProductsApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Products RESTful API 컨트롤러
 * Oracle + Elasticsearch 동기화 지원
 */
@Slf4j
@RequestMapping("/api/elk/products")
@RestController
@RequiredArgsConstructor
public class ApiProductsController {

    private final ProductSVC productService;

    /**
     * 상품 등록 (Oracle + Elasticsearch 동기화)
     * POST /api/elk/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductsApi>> add(@RequestBody @Valid SaveApi saveApi) {
        log.info("상품 등록 요청: {}", saveApi);
        // 비즈니스 유효성 검증: 가격이 1천만원 초과 금지
        if (saveApi.getPrice() != null) {
            if (saveApi.getPrice() > 10_000_000) {
                throw new BusinessValidationException("상품의 가격이 천만원을 초과할 수 없습니다.");
            }
        }
        Products products = new Products();
        BeanUtils.copyProperties(saveApi, products);
        Long productId = productService.save(products);
        Optional<Products> optionalProduct = productService.findById(productId);
        Products savedProduct = optionalProduct.orElseThrow();
        ProductsApi productsApi = new ProductsApi();
        BeanUtils.copyProperties(savedProduct, productsApi);
        ApiResponse<ProductsApi> response = ApiResponse.of(ApiResponseCode.SUCCESS, productsApi);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품 조회 (Oracle)
     * GET /api/elk/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductsApi>> findById(@PathVariable("id") Long id) {
        log.info("상품 조회 요청: {}", id);

        Optional<Products> optionalProduct = productService.findById(id);
        Products product = optionalProduct.orElseThrow(
            () -> new NoSuchElementException("상품번호: " + id + "를 찾을 수 없습니다.")
        );

        ProductsApi productsApi = new ProductsApi();
        BeanUtils.copyProperties(product, productsApi);

        ApiResponse<ProductsApi> response = ApiResponse.of(ApiResponseCode.SUCCESS, productsApi);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 수정 (Oracle + Elasticsearch 동기화)
     * PATCH /api/elk/products/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductsApi>> updateById(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateApi updateApi) {
        log.info("상품 수정 요청: id={}, updateApi={}", id, updateApi);
        Optional<Products> optionalProduct = productService.findById(id);
        optionalProduct.orElseThrow(
            () -> new NoSuchElementException("상품번호: " + id + "를 찾을 수 없습니다.")
        );
        // 비즈니스 유효성 검증: 가격이 1천만원 초과 금지
        if (updateApi.getPrice() != null) {
            if (updateApi.getPrice() > 10_000_000) {
                throw new BusinessValidationException("상품의 가격이 천만원을 초과할 수 없습니다.");
            }
        }
        Products products = new Products();
        BeanUtils.copyProperties(updateApi, products);
        int updatedRows = productService.updateById(id, products);
        optionalProduct = productService.findById(id);
        Products updatedProduct = optionalProduct.orElseThrow();
        ProductsApi productsApi = new ProductsApi();
        BeanUtils.copyProperties(updatedProduct, productsApi);
        ApiResponse<ProductsApi> response = ApiResponse.of(ApiResponseCode.SUCCESS, productsApi);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 삭제 (Oracle + Elasticsearch 동기화)
     * DELETE /api/elk/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductsApi>> deleteById(@PathVariable("id") Long id) {
        log.info("상품 삭제 요청: {}", id);

        // 1) 상품 존재 여부 확인
        Optional<Products> optionalProduct = productService.findById(id);
        Products product = optionalProduct.orElseThrow(
            () -> new NoSuchElementException("상품번호: " + id + "를 찾을 수 없습니다.")
        );

        // 2) 상품 삭제 (Oracle + Elasticsearch 동기화)
        int deletedRows = productService.deleteById(id);

        // 3) 엔티티 → 응답 DTO 변환
        ProductsApi productsApi = new ProductsApi();
        BeanUtils.copyProperties(product, productsApi);

        ApiResponse<ProductsApi> response = ApiResponse.of(ApiResponseCode.SUCCESS, productsApi);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 목록 조회 (Oracle)
     * GET /api/elk/products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductsApi>>> findAll() {
        log.info("상품 목록 조회 요청");

        List<Products> products = productService.findAll();
        List<ProductsApi> responseList = products.stream().map(p -> {
            ProductsApi ra = new ProductsApi();
            BeanUtils.copyProperties(p, ra);
            return ra;
        }).toList();
        ApiResponse<List<ProductsApi>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 목록 조회 (페이징, Oracle)
     * GET /api/elk/products/paging?pageNo=1&numOfRows=10
     */
    @GetMapping("/paging")
    public ResponseEntity<ApiResponse<List<ProductsApi>>> findAll(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "numOfRows", defaultValue = "10") Integer numOfRows) {
        log.info("상품 목록 조회 요청 (페이징): pageNo={}, numOfRows={}", pageNo, numOfRows);

        List<Products> products = productService.findAll(pageNo, numOfRows);
        int totalCount = productService.getTotalCount();
        List<ProductsApi> responseList = products.stream().map(p -> {
            ProductsApi ra = new ProductsApi();
            BeanUtils.copyProperties(p, ra);
            return ra;
        }).toList();
        ApiResponse<List<ProductsApi>> response = ApiResponse.of(
            ApiResponseCode.SUCCESS,
            responseList,
            new ApiResponse.Paging(pageNo, numOfRows, totalCount)
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 총 건수 조회 (Oracle)
     * GET /api/elk/products/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getTotalCount() {
        log.info("상품 총 건수 조회 요청");

        int totalCount = productService.getTotalCount();
        ApiResponse<Integer> response = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Elasticsearch 검색 - 상품명으로 검색
     * GET /api/elk/products/search/pname?keyword=노트북
     */
    @GetMapping("/search/pname")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchByPname(
            @RequestParam("keyword") String keyword) {
        log.info("상품명 검색 요청: {}", keyword);

        List<ProductDocument> results = productService.searchByPname(keyword);
        ApiResponse<List<ProductDocument>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * Elasticsearch 검색 - 상품설명으로 검색
     * GET /api/elk/products/search/description?keyword=AI
     */
    @GetMapping("/search/description")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchByDescription(
            @RequestParam("keyword") String keyword) {
        log.info("상품설명 검색 요청: {}", keyword);

        List<ProductDocument> results = productService.searchByDescription(keyword);
        ApiResponse<List<ProductDocument>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * Elasticsearch 검색 - 카테고리로 검색
     * GET /api/elk/products/search/category?category=전자제품
     */
    @GetMapping("/search/category")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchByCategory(
            @RequestParam("category") String category) {
        log.info("카테고리 검색 요청: {}", category);

        List<ProductDocument> results = productService.searchByCategory(category);
        ApiResponse<List<ProductDocument>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * Elasticsearch 검색 - 가격 범위로 검색
     * GET /api/elk/products/search/price?minPrice=100000&maxPrice=1000000
     */
    @GetMapping("/search/price")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchByPriceRange(
            @RequestParam("minPrice") Long minPrice,
            @RequestParam("maxPrice") Long maxPrice) {
        log.info("가격 범위 검색 요청: {} ~ {}", minPrice, maxPrice);

        List<ProductDocument> results = productService.searchByPriceRange(minPrice, maxPrice);
        ApiResponse<List<ProductDocument>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * Elasticsearch 검색 - 평점 이상으로 검색
     * GET /api/elk/products/search/rating?rating=4.5
     */
    @GetMapping("/search/rating")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchByRating(
            @RequestParam("rating") Double rating) {
        log.info("평점 검색 요청: {}", rating);

        List<ProductDocument> results = productService.searchByRating(rating);
        ApiResponse<List<ProductDocument>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 데이터 동기화 (Oracle → Elasticsearch)
     * POST /api/elk/products/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncAllToElasticsearch() {
        log.info("전체 데이터 동기화 요청");

        productService.syncAllToElasticsearch();

        ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "동기화가 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 데이터 개수 비교
     * GET /api/elk/products/compare
     */
    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<String>> compareDataCount() {
        log.info("데이터 개수 비교 요청");

        productService.compareDataCount();

        ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "데이터 개수 비교가 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 상품명 자동완성 (Elasticsearch)
     * GET /api/elk/products/autocomplete/pname?prefix=노
     */
    @GetMapping("/autocomplete/pname")
    public ResponseEntity<ApiResponse<List<String>>> autocompletePname(@RequestParam("prefix") String prefix) {
        log.info("상품명 자동완성 요청: {}", prefix);
        List<String> suggestions = productService.autocompletePname(prefix);
        ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.SUCCESS, suggestions);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품명으로 검색하여 상품 목록 반환 (Elasticsearch)
     * GET /api/elk/products/search/products?pname=노트북
     */
    @GetMapping("/search/products")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchProductsByPname(@RequestParam("pname") String pname) {
        log.info("상품명으로 상품 검색 요청: {}", pname);
        List<ProductDocument> results = productService.searchProductsByPname(pname);
        ApiResponse<List<ProductDocument>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품설명 하이라이팅 검색 (Elasticsearch)
     * GET /api/elk/products/search/description/highlight?keyword=AI
     */
    @GetMapping("/search/description/highlight")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> highlightDescription(@RequestParam("keyword") String keyword) {
        log.info("상품설명 하이라이팅 검색 요청: {}", keyword);
        List<ProductDocument> results = productService.highlightDescription(keyword);
        ApiResponse<List<ProductDocument>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }
} 