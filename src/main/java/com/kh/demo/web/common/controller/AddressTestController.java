package com.kh.demo.web.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class AddressTestController {

    /**
     * 주소 검색 테스트 페이지
     */
    @GetMapping("/test/address")
    public String addressTest() {
        log.info("주소 검색 테스트 페이지 요청");
        return "common/addressTest";
    }
} 