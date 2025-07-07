package com.kh.demo.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Products 웹 페이지 컨트롤러
 * 검색, 검색 결과, 상품 상세 페이지 처리
 */
@Slf4j
@RequestMapping("/products")
@Controller
@RequiredArgsConstructor
public class ProductsController {

    /**
     * 상품 검색 페이지
     * GET /products/search
     */
    @GetMapping("/search")
    public String searchPage() {
        log.info("상품 검색 페이지 요청");
        return "products/search";
    }

    /**
     * 검색 결과 페이지
     * GET /products/search-results
     */
    @GetMapping("/search-results")
    public String searchResultsPage() {
        log.info("검색 결과 페이지 요청");
        return "products/search-results";
    }

    /**
     * 상품 상세 페이지
     * GET /products/detail
     */
    @GetMapping("/detail")
    public String productDetailPage() {
        log.info("상품 상세 페이지 요청");
        return "products/detail";
    }
} 