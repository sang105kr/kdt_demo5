package com.kh.demo.admin.controller.api;

import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.web.common.controller.api.BaseApiController;
import com.kh.demo.web.common.converter.ProductConverter;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import com.kh.demo.web.common.validator.ProductValidator;
import com.kh.demo.web.product.controller.api.request.CreateRequest;
import com.kh.demo.web.product.controller.api.response.ReadResponse;
import com.kh.demo.web.product.controller.api.request.UpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 관리자용 상품 REST API 컨트롤러
 * - 시스템 통합용 API (외부 시스템, 모바일 앱)
 * - 관리자 대시보드 AJAX 호출
 * - CRUD 작업 (생성, 조회, 수정, 삭제)
 * - Elasticsearch 검색 API
 * - 데이터 동기화 관리
 * - JSON 응답 형식
 * 
 * vs AdminProductController: REST API vs 웹 UI
 */
@Slf4j
@RequestMapping("/api/admin/products")
@RestController
@RequiredArgsConstructor
public class AdminApiProductController extends BaseApiController {

    private final ProductService productService;
    private final ProductValidator productValidator;
    private final ProductConverter productConverter;

    /**
     * 상품 등록 (Oracle + Elasticsearch 동기화)
     * - 외부 시스템 연동용
     * - 관리자 대시보드 AJAX 호출용
     * - 파일 업로드는 별도 API 필요
     * POST /api/admin/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReadResponse>> add(@RequestBody @Valid CreateRequest createRequest) {
        log.info("관리자 API 상품 등록 요청: {}", createRequest);
        
        Products products = new Products();
        BeanUtils.copyProperties(createRequest, products);
        Long productId = productService.save(products);
        
        Optional<Products> optionalProduct = productService.findById(productId);
        Products savedProduct = optionalProduct.orElseThrow();
        ReadResponse readResponse = new ReadResponse();
        BeanUtils.copyProperties(savedProduct, readResponse);
        
        ApiResponse<ReadResponse> response = ApiResponse.of(ApiResponseCode.SUCCESS, readResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품 조회 (Oracle)
     * GET /api/admin/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReadResponse>> findById(@PathVariable("id") Long id) {
        log.info("관리자 API 상품 조회 요청: {}", id);

        Optional<Products> optionalProduct = productService.findById(id);
        Products product = optionalProduct.orElseThrow(
            () -> new NoSuchElementException("상품번호: " + id + "를 찾을 수 없습니다.")
        );

        ReadResponse readResponse = new ReadResponse();
        BeanUtils.copyProperties(product, readResponse);

        ApiResponse<ReadResponse> response = ApiResponse.of(ApiResponseCode.SUCCESS, readResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 수정 (Oracle + Elasticsearch 동기화)
     * PATCH /api/admin/products/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ReadResponse>> updateById(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateRequest updateRequest) {
        log.info("관리자 API 상품 수정 요청: id={}, updateApi={}", id, updateRequest);
        
        Products products = new Products();
        BeanUtils.copyProperties(updateRequest, products);
        int updatedRows = productService.updateById(id, products);
        
        Optional<Products> optionalProduct = productService.findById(id);
        Products updatedProduct = optionalProduct.orElseThrow();
        ReadResponse readResponse = new ReadResponse();
        BeanUtils.copyProperties(updatedProduct, readResponse);
        
        ApiResponse<ReadResponse> response = ApiResponse.of(ApiResponseCode.SUCCESS, readResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 삭제 (Oracle + Elasticsearch 동기화)
     * DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ReadResponse>> deleteById(@PathVariable("id") Long id) {
        log.info("관리자 API 상품 삭제 요청: {}", id);

        // 1) 상품 존재 여부 확인 및 삭제 (SVC에서 처리)
        Optional<Products> optionalProduct = productService.findById(id);
        Products product = optionalProduct.orElseThrow(
            () -> new NoSuchElementException("상품번호: " + id + "를 찾을 수 없습니다.")
        );

        // 2) 상품 삭제 (Oracle + Elasticsearch 동기화)
        int deletedRows = productService.deleteById(id);

        // 3) 엔티티 → 응답 DTO 변환
        ReadResponse readResponse = new ReadResponse();
        BeanUtils.copyProperties(product, readResponse);

        ApiResponse<ReadResponse> response = ApiResponse.of(ApiResponseCode.SUCCESS, readResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 목록 조회 (Oracle)
     * GET /api/admin/products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadResponse>>> findAll() {
        log.info("관리자 API 상품 목록 조회 요청");

        List<Products> products = productService.findAll();
        List<ReadResponse> responseList = products.stream().map(p -> {
            ReadResponse ra = new ReadResponse();
            BeanUtils.copyProperties(p, ra);
            return ra;
        }).toList();
        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 목록 조회 (페이징, Oracle)
     * GET /api/admin/products/paging?pageNo=1&numOfRows=10
     */
    @GetMapping("/paging")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> findAll(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "numOfRows", defaultValue = "10") Integer numOfRows) {
        log.info("관리자 API 상품 목록 조회 요청 (페이징): pageNo={}, numOfRows={}", pageNo, numOfRows);

        List<Products> products = productService.findAll(pageNo, numOfRows);
        int totalCount = productService.getTotalCount();
        List<ReadResponse> responseList = products.stream().map(p -> {
            ReadResponse ra = new ReadResponse();
            BeanUtils.copyProperties(p, ra);
            return ra;
        }).toList();
        ApiResponse<List<ReadResponse>> response = ApiResponse.of(
            ApiResponseCode.SUCCESS,
            responseList,
            new ApiResponse.Paging(pageNo, numOfRows, totalCount)
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 총 건수 조회 (Oracle)
     * GET /api/admin/products/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getTotalCount() {
        log.info("관리자 API 상품 총 건수 조회 요청");

        int totalCount = productService.getTotalCount();
        ApiResponse<Integer> response = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);
        return ResponseEntity.ok(response);
    }

    /**
     * 파일 업로드 API (관리자용)
     * - 외부 시스템에서 파일 업로드 시 사용
     * - AdminProductController의 파일 업로드와 동일한 로직
     * POST /api/admin/products/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType) {
        log.info("관리자 API 파일 업로드 요청: filename={}, fileType={}", file.getOriginalFilename(), fileType);
        
        try {
            // TODO: 파일 업로드 로직 구현
            // AdminProductController의 processUploadFiles 로직 활용
            String uploadedFilename = "uploaded_" + file.getOriginalFilename();
            
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, uploadedFilename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("파일 업로드 실패", e);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Elasticsearch 검색 API들 (관리자용)
     * - 개별 검색 기능 제공
     * - 시스템 통합용
     */

    /**
     * 상품명으로 검색 (Elasticsearch) - 관리자용
     * GET /api/admin/products/search/pname?keyword=노트북
     */
    @GetMapping("/search/pname")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> searchByPname(
            @RequestParam("keyword") String keyword) {
        log.info("관리자 API 상품명 검색 요청: keyword={}", keyword);

        List<com.kh.demo.domain.product.search.document.ProductDocument> documents = productService.searchByPname(keyword);
        List<ReadResponse> responseList = documents.stream().map(doc -> {
            ReadResponse readResponse = new ReadResponse();
            BeanUtils.copyProperties(doc, readResponse);
            return readResponse;
        }).toList();

        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품설명으로 검색 (Elasticsearch)
     * GET /api/admin/products/search/description?keyword=고성능
     */
    @GetMapping("/search/description")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> searchByDescription(
            @RequestParam("keyword") String keyword) {
        log.info("관리자 API 상품설명 검색 요청: keyword={}", keyword);

        List<com.kh.demo.domain.product.search.document.ProductDocument> documents = productService.searchByDescription(keyword);
        List<ReadResponse> responseList = documents.stream().map(doc -> {
            ReadResponse readResponse = new ReadResponse();
            BeanUtils.copyProperties(doc, readResponse);
            return readResponse;
        }).toList();

        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 카테고리로 검색 (Elasticsearch)
     * GET /api/admin/products/search/category?category=전자제품
     */
    @GetMapping("/search/category")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> searchByCategory(
            @RequestParam("category") String category) {
        log.info("관리자 API 카테고리 검색 요청: category={}", category);

        List<com.kh.demo.domain.product.search.document.ProductDocument> documents = productService.searchByCategory(category);
        List<ReadResponse> responseList = documents.stream().map(doc -> {
            ReadResponse readResponse = new ReadResponse();
            BeanUtils.copyProperties(doc, readResponse);
            return readResponse;
        }).toList();

        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 가격 범위로 검색 (Elasticsearch)
     * GET /api/admin/products/search/price?minPrice=100000&maxPrice=500000
     */
    @GetMapping("/search/price")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> searchByPriceRange(
            @RequestParam("minPrice") Long minPrice,
            @RequestParam("maxPrice") Long maxPrice) {
        log.info("관리자 API 가격 범위 검색 요청: minPrice={}, maxPrice={}", minPrice, maxPrice);

        List<com.kh.demo.domain.product.search.document.ProductDocument> documents = productService.searchByPriceRange(minPrice, maxPrice);
        List<ReadResponse> responseList = documents.stream().map(doc -> {
            ReadResponse readResponse = new ReadResponse();
            BeanUtils.copyProperties(doc, readResponse);
            return readResponse;
        }).toList();

        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 평점 이상으로 검색 (Elasticsearch)
     * GET /api/admin/products/search/rating?minRating=4.0
     */
    @GetMapping("/search/rating")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> searchByRating(
            @RequestParam("minRating") Double minRating) {
        log.info("관리자 API 평점 검색 요청: minRating={}", minRating);

        List<com.kh.demo.domain.product.search.document.ProductDocument> documents = productService.searchByRating(minRating);
        List<ReadResponse> responseList = documents.stream().map(doc -> {
            ReadResponse readResponse = new ReadResponse();
            BeanUtils.copyProperties(doc, readResponse);
            return readResponse;
        }).toList();

        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 고급 검색 API들
     */

    /**
     * 하이라이팅된 설명 검색 (Elasticsearch)
     * GET /api/admin/products/search/highlight/description?keyword=고성능
     */
    @GetMapping("/search/highlight/description")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> highlightDescription(
            @RequestParam("keyword") String keyword) {
        log.info("관리자 API 하이라이팅 설명 검색 요청: keyword={}", keyword);

        List<com.kh.demo.domain.product.search.document.ProductDocument> documents = productService.highlightDescription(keyword);
        List<ReadResponse> responseList = documents.stream().map(doc -> {
            ReadResponse readResponse = new ReadResponse();
            BeanUtils.copyProperties(doc, readResponse);
            return readResponse;
        }).toList();

        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품명 자동완성 (Elasticsearch)
     * GET /api/admin/products/autocomplete/pname?prefix=삼성
     */
    @GetMapping("/autocomplete/pname")
    public ResponseEntity<ApiResponse<List<String>>> autocompletePname(
            @RequestParam("prefix") String prefix) {
        log.info("관리자 API 자동완성 요청: prefix={}", prefix);

        List<String> results = productService.autocompletePname(prefix);
        ApiResponse<List<String>> response = ApiResponse.of(ApiResponseCode.SUCCESS, results);
        return ResponseEntity.ok(response);
    }

    /**
     * 고급 상품명 검색 (하이라이팅 포함, Elasticsearch)
     * GET /api/admin/products/search/advanced/pname?pname=노트북
     */
    @GetMapping("/search/advanced/pname")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> searchProductsByPname(
            @RequestParam("pname") String pname) {
        log.info("관리자 API 고급 상품명 검색 요청: pname={}", pname);

        List<com.kh.demo.domain.product.search.document.ProductDocument> documents = productService.searchByPname(pname, 20); // 20개 결과 반환
        List<ReadResponse> responseList = documents.stream().map(doc -> {
            ReadResponse readResponse = new ReadResponse();
            BeanUtils.copyProperties(doc, readResponse);
            return readResponse;
        }).toList();

        ApiResponse<List<ReadResponse>> response = ApiResponse.of(ApiResponseCode.SUCCESS, responseList);
        return ResponseEntity.ok(response);
    }

    /**
     * 통합 상품 검색 API (관리자용)
     * - 고객용 ProductController와 유사한 기능
     * - 관리자 대시보드나 시스템 통합용
     * GET /api/admin/products/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ReadResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        log.info("관리자 API 통합 상품 검색 요청 - keyword: {}, category: {}, minPrice: {}, maxPrice: {}, minRating: {}, sort: {}, page: {}, size: {}", 
                keyword, category, minPrice, maxPrice, minRating, sort, page, size);
        
        try {
            List<com.kh.demo.domain.product.search.document.ProductDocument> productDocuments;
            
            // 기본 검색
            if (keyword != null && !keyword.trim().isEmpty()) {
                productDocuments = productService.searchByPname(keyword.trim());
            } else if (category != null && !category.trim().isEmpty()) {
                productDocuments = productService.searchByCategory(category.trim());
            } else {
                productDocuments = productService.findAllProducts(); // 전체 조회
            }
            
            // 가격 필터 적용
            if (minPrice != null || maxPrice != null) {
                productDocuments = productDocuments.stream()
                        .filter(doc -> {
                            if (minPrice != null && doc.getPrice() < minPrice) return false;
                            if (maxPrice != null && doc.getPrice() > maxPrice) return false;
                            return true;
                        })
                        .collect(Collectors.toList());
            }
            
            // 평점 필터 적용
            if (minRating != null) {
                productDocuments = productDocuments.stream()
                        .filter(doc -> doc.getRating() != null && doc.getRating() >= minRating)
                        .collect(Collectors.toList());
            }
            
            // 정렬 적용
            switch (sort) {
                case "price-low":
                    productDocuments.sort((a, b) -> Long.compare(a.getPrice(), b.getPrice()));
                    break;
                case "price-high":
                    productDocuments.sort((a, b) -> Long.compare(b.getPrice(), a.getPrice()));
                    break;
                case "rating":
                    productDocuments.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
                    break;
                case "newest":
                default:
                    // ProductDocument에는 cdate 필드가 없으므로 productId로 정렬 (최신 등록순)
                    productDocuments.sort((a, b) -> Long.compare(b.getProductId(), a.getProductId()));
                    break;
            }
            
            int totalCount = productDocuments.size();
            
            // 페이징 처리
            int startIndex = (page - 1) * size;
            
            // startIndex가 totalCount보다 크면 빈 리스트 반환
            if (startIndex >= totalCount) {
                List<ReadResponse> emptyResponse = List.of();
                ApiResponse<List<ReadResponse>> response = ApiResponse.of(
                    ApiResponseCode.SUCCESS,
                    emptyResponse,
                    new ApiResponse.Paging(page, size, totalCount)
                );
                return ResponseEntity.ok(response);
            }
            
            int endIndex = Math.min(startIndex + size, totalCount);
            List<com.kh.demo.domain.product.search.document.ProductDocument> pagedDocuments = productDocuments.subList(startIndex, endIndex);
            
            // ProductDocument를 ReadReq로 변환하고 이미지 정보 추가
            List<ReadResponse> responseList = pagedDocuments.stream().map(doc -> {
                ReadResponse readResponse = new ReadResponse();
                BeanUtils.copyProperties(doc, readResponse);
                
                // 이미지 파일 정보 추가
                try {
                    List<UploadFile> imageFiles = productService.findProductImages(doc.getProductId());
                    if (!imageFiles.isEmpty()) {
                        readResponse.setImageUrl("/uploads/" + imageFiles.get(0).getStoreFilename());
                    }
                } catch (Exception e) {
                    log.warn("상품 {} 이미지 조회 실패: {}", doc.getProductId(), e.getMessage());
                }
                
                return readResponse;
            }).collect(Collectors.toList());
            
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            ApiResponse<List<ReadResponse>> response = ApiResponse.of(
                ApiResponseCode.SUCCESS,
                responseList,
                new ApiResponse.Paging(page, size, totalCount)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관리자 API 통합 상품 검색 중 오류 발생", e);
            ApiResponse<List<ReadResponse>> response = ApiResponse.of(
                ApiResponseCode.INTERNAL_SERVER_ERROR,
                List.of()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 데이터 관리 API들
     */

    /**
     * 전체 데이터 동기화 (Oracle → Elasticsearch)
     * POST /api/admin/products/sync/elasticsearch
     */
    @PostMapping("/sync/elasticsearch")
    public ResponseEntity<ApiResponse<String>> syncAllToElasticsearch() {
        log.info("관리자 API 전체 데이터 Elasticsearch 동기화 요청");

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
     * GET /api/admin/products/compare/count
     */
    @GetMapping("/compare/count")
    public ResponseEntity<ApiResponse<Object>> compareDataCount() {
        log.info("관리자 API 데이터 개수 비교 요청");

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