package com.kh.demo.web.controller;

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
@RequestMapping("/product")
@Controller
@RequiredArgsConstructor
public class ProductController extends BaseController {

    /**
     * 상품 검색 페이지
     * GET /product/search
     */
    @GetMapping("/search")
    public String searchPage() {
        log.info("상품 검색 페이지 요청");
        return "product/search";
    }

    /**
     * 검색 결과 페이지
     * GET /product/search-results
     */
    @GetMapping("/search-results")
    public String searchResultsPage() {
        log.info("검색 결과 페이지 요청");
        return "product/search-results";
    }

    /**
     * 상품 상세 페이지
     * GET /product/detail
     */
    @GetMapping("/detail")
    public String productDetailPage() {
        log.info("상품 상세 페이지 요청");
        return "product/detail";
    }
} 