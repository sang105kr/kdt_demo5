package com.kh.demo.web.restcontroller;

import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.domain.product.search.document.ProductDocument;
import com.kh.demo.web.exception.BusinessValidationException;
import com.kh.demo.web.restcontroller.product.request.CreateReq;
import com.kh.demo.web.restcontroller.product.request.UpdateReq;
import com.kh.demo.web.restcontroller.product.response.ReadReq;
import com.kh.demo.web.restcontroller.validator.ProductValidator;
import com.kh.demo.web.restcontroller.converter.ProductConverter;
import com.kh.demo.web.restcontroller.dto.ApiResponse;
import com.kh.demo.web.restcontroller.dto.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Map;

/**
 * Products RESTful API 컨트롤러
 * Oracle + Elasticsearch 동기화 지원
 */
@Slf4j
@RequestMapping("/api/products")
@RestController
@RequiredArgsConstructor
public class ProductApiController extends BaseRestController {

    private final ProductService productService;
    private final ProductValidator productValidator;
    private final ProductConverter productConverter;

    /**
     * 상품 등록 (Oracle + Elasticsearch 동기화)
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReadReq>> add(@RequestBody @Valid CreateReq createReq) {
        log.info("상품 등록 요청: {}", createReq);
        
        Products products = new Products();
        BeanUtils.copyProperties(createReq, products);
        Long productId = productService.save(products);
        
        Optional<Products> optionalProduct = productService.findById(productId);
        Products savedProduct = optionalProduct.orElseThrow();
        ReadReq readReq = new ReadReq();
        BeanUtils.copyProperties(savedProduct, readReq);
        
        ApiResponse<ReadReq> response = ApiResponse.of(ApiResponseCode.SUCCESS, readReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품 조회 (Oracle)
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReadReq>> findById(@PathVariable("id") Long id) {
        log.info("상품 조회 요청: {}", id);

        Optional<Products> optionalProduct = productService.findById(id);
        Products product = optionalProduct.orElseThrow(
            () -> new NoSuchElementException("상품번호: " + id + "를 찾을 수 없습니다.")
        );

        ReadReq readReq = new ReadReq();
        BeanUtils.copyProperties(product, readReq);

        ApiResponse<ReadReq> response = ApiResponse.of(ApiResponseCode.SUCCESS, readReq);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 수정 (Oracle + Elasticsearch 동기화)
     * PATCH /api/products/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ReadReq>> updateById(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateReq updateReq) {
        log.info("상품 수정 요청: id={}, updateApi={}", id, updateReq);
        
        Products products = new Products();
        BeanUtils.copyProperties(updateReq, products);
        int updatedRows = productService.updateById(id, products);
        
        Optional<Products> optionalProduct = productService.findById(id);
        Products updatedProduct = optionalProduct.orElseThrow();
        ReadReq readReq = new ReadReq();
        BeanUtils.copyProperties(updatedProduct, readReq);
        
        ApiResponse<ReadReq> response = ApiResponse.of(ApiResponseCode.SUCCESS, readReq);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 삭제 (Oracle + Elasticsearch 동기화)
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ReadReq>> deleteById(@PathVariable("id") Long id) {
        log.info("상품 삭제 요청: {}", id);

        // 1) 상품 존재 여부 확인 및 삭제 (SVC에서 처리)
        Optional<Products> optionalProduct = productService.findById(id);
        Products product = optionalProduct.orElseThrow(
            () -> new NoSuchElementException("상품번호: " + id + "를 찾을 수 없습니다.")
        );

        // 2) 상품 삭제 (Oracle + Elasticsearch 동기화)
        int deletedRows = productService.deleteById(id);

        // 3) 엔티티 → 응답 DTO 변환
        ReadReq readReq = new ReadReq();
        BeanUtils.copyProperties(product, readReq);

        ApiResponse<ReadReq> response = ApiResponse.of(ApiResponseCode.SUCCESS, readReq);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 목록 조회 (Oracle)
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadReq>>> findAll() {
        log.info("상품 목록 조회 요청");

        List<Products> products = productService.findAll();
        List<ReadReq> responseList = products.stream().map(p -> {
            ReadReq ra = new ReadReq();
            BeanUtils.copyProperties(p, ra);
            return ra;
        }).toList();
        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 목록 조회 (페이징, Oracle)
     * GET /api/products/paging?pageNo=1&numOfRows=10
     */
    @GetMapping("/paging")
    public ResponseEntity<ApiResponse<List<ReadReq>>> findAll(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "numOfRows", defaultValue = "10") Integer numOfRows) {
        log.info("상품 목록 조회 요청 (페이징): pageNo={}, numOfRows={}", pageNo, numOfRows);

        List<Products> products = productService.findAll(pageNo, numOfRows);
        int totalCount = productService.getTotalCount();
        List<ReadReq> responseList = products.stream().map(p -> {
            ReadReq ra = new ReadReq();
            BeanUtils.copyProperties(p, ra);
            return ra;
        }).toList();
        ApiResponse<List<ReadReq>> response = ApiResponse.of(
            ApiResponseCode.SUCCESS,
            responseList,
            new ApiResponse.Paging(pageNo, numOfRows, totalCount)
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 총 건수 조회 (Oracle)
     * GET /api/products/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getTotalCount() {
        log.info("상품 총 건수 조회 요청");

        int totalCount = productService.getTotalCount();
        ApiResponse<Integer> response = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Elasticsearch 검색 API들
     */

    /**
     * 상품명으로 검색 (Elasticsearch)
     * GET /api/products/search/pname?keyword=노트북
     */
    @GetMapping("/search/pname")
    public ResponseEntity<ApiResponse<List<ReadReq>>> searchByPname(
            @RequestParam("keyword") String keyword) {
        log.info("상품명 검색 요청: keyword={}", keyword);

        List<ProductDocument> documents = productService.searchByPname(keyword);
        List<ReadReq> responseList = documents.stream().map(doc -> {
            ReadReq readReq = new ReadReq();
            BeanUtils.copyProperties(doc, readReq);
            return readReq;
        }).toList();

        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품설명으로 검색 (Elasticsearch)
     * GET /api/products/search/description?keyword=고성능
     */
    @GetMapping("/search/description")
    public ResponseEntity<ApiResponse<List<ReadReq>>> searchByDescription(
            @RequestParam("keyword") String keyword) {
        log.info("상품설명 검색 요청: keyword={}", keyword);

        List<ProductDocument> documents = productService.searchByDescription(keyword);
        List<ReadReq> responseList = documents.stream().map(doc -> {
            ReadReq readReq = new ReadReq();
            BeanUtils.copyProperties(doc, readReq);
            return readReq;
        }).toList();

        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 카테고리로 검색 (Elasticsearch)
     * GET /api/products/search/category?category=전자제품
     */
    @GetMapping("/search/category")
    public ResponseEntity<ApiResponse<List<ReadReq>>> searchByCategory(
            @RequestParam("category") String category) {
        log.info("카테고리 검색 요청: category={}", category);

        List<ProductDocument> documents = productService.searchByCategory(category);
        List<ReadReq> responseList = documents.stream().map(doc -> {
            ReadReq readReq = new ReadReq();
            BeanUtils.copyProperties(doc, readReq);
            return readReq;
        }).toList();

        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 가격 범위로 검색 (Elasticsearch)
     * GET /api/products/search/price?minPrice=100000&maxPrice=500000
     */
    @GetMapping("/search/price")
    public ResponseEntity<ApiResponse<List<ReadReq>>> searchByPriceRange(
            @RequestParam("minPrice") Long minPrice,
            @RequestParam("maxPrice") Long maxPrice) {
        log.info("가격 범위 검색 요청: minPrice={}, maxPrice={}", minPrice, maxPrice);

        List<ProductDocument> documents = productService.searchByPriceRange(minPrice, maxPrice);
        List<ReadReq> responseList = documents.stream().map(doc -> {
            ReadReq readReq = new ReadReq();
            BeanUtils.copyProperties(doc, readReq);
            return readReq;
        }).toList();

        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 평점 이상으로 검색 (Elasticsearch)
     * GET /api/products/search/rating?minRating=4.0
     */
    @GetMapping("/search/rating")
    public ResponseEntity<ApiResponse<List<ReadReq>>> searchByRating(
            @RequestParam("minRating") Double minRating) {
        log.info("평점 검색 요청: minRating={}", minRating);

        List<ProductDocument> documents = productService.searchByRating(minRating);
        List<ReadReq> responseList = documents.stream().map(doc -> {
            ReadReq readReq = new ReadReq();
            BeanUtils.copyProperties(doc, readReq);
            return readReq;
        }).toList();

        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 고급 검색 API들
     */

    /**
     * 하이라이팅된 설명 검색 (Elasticsearch)
     * GET /api/products/search/highlight/description?keyword=고성능
     */
    @GetMapping("/search/highlight/description")
    public ResponseEntity<ApiResponse<List<ReadReq>>> highlightDescription(
            @RequestParam("keyword") String keyword) {
        log.info("하이라이팅 설명 검색 요청: keyword={}", keyword);

        List<ProductDocument> documents = productService.highlightDescription(keyword);
        List<ReadReq> responseList = documents.stream().map(doc -> {
            ReadReq readReq = new ReadReq();
            BeanUtils.copyProperties(doc, readReq);
            return readReq;
        }).toList();

        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품명 자동완성 (Elasticsearch)
     * GET /api/products/autocomplete/pname?prefix=삼성
     */
    @GetMapping("/autocomplete/pname")
    public ResponseEntity<ApiResponse<List<String>>> autocompletePname(
            @RequestParam("prefix") String prefix) {
        log.info("자동완성 요청: prefix={}", prefix);

        List<String> results = productService.autocompletePname(prefix);
        ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * 고급 상품명 검색 (하이라이팅 포함, Elasticsearch)
     * GET /api/products/search/advanced/pname?pname=노트북
     */
    @GetMapping("/search/advanced/pname")
    public ResponseEntity<ApiResponse<List<ReadReq>>> searchProductsByPname(
            @RequestParam("pname") String pname) {
        log.info("고급 상품명 검색 요청: pname={}", pname);

        List<ProductDocument> documents = productService.searchProductsByPname(pname);
        List<ReadReq> responseList = documents.stream().map(doc -> {
            ReadReq readReq = new ReadReq();
            BeanUtils.copyProperties(doc, readReq);
            return readReq;
        }).toList();

        ApiResponse<List<ReadReq>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 데이터 관리 API들
     */

    /**
     * 전체 데이터 동기화 (Oracle → Elasticsearch)
     * POST /api/products/sync/elasticsearch
     */
    @PostMapping("/sync/elasticsearch")
    public ResponseEntity<ApiResponse<String>> syncAllToElasticsearch() {
        log.info("전체 데이터 Elasticsearch 동기화 요청");

        try {
            productService.syncAllToElasticsearch();
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "전체 데이터 동기화가 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("전체 데이터 동기화 실패", e);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "전체 데이터 동기화 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 데이터 개수 비교 (Oracle vs Elasticsearch)
     * GET /api/products/compare/count
     */
    @GetMapping("/compare/count")
    public ResponseEntity<ApiResponse<Object>> compareDataCount() {
        log.info("데이터 개수 비교 요청");

        try {
            productService.compareDataCount();
            
            // 실제 개수 정보도 반환
            long oracleCount = productService.getTotalCount();
            long elasticsearchCount = productService.searchByPname("").size(); // 간단한 방법으로 ES 개수 확인
            
            var countInfo = Map.of(
                "oracleCount", oracleCount,
                "elasticsearchCount", elasticsearchCount,
                "isSynchronized", oracleCount == elasticsearchCount
            );
            
            ApiResponse<Object> response = ApiResponse.of(ApiResponseCode.SUCCESS, countInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("데이터 개수 비교 실패", e);
            ApiResponse<Object> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "데이터 개수 비교 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 