package com.kh.demo.web.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class AddressPageController {

    /**
     * 주소 검색 페이지 (팝업용)
     */
    @GetMapping("/api/address/search-page")
    public String searchPage() {
        log.info("주소 검색 페이지 요청");
        return "common/addressSearch";
    }
} 