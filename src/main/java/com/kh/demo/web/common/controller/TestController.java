package com.kh.demo.web.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 테스트 페이지 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/test")
public class TestController {



    /**
     * 주소 검색 테스트 페이지
     */
    @GetMapping("/address")
    public String addressTest() {
        log.info("주소 검색 테스트 페이지 요청");
        return "common/addressTest";
    }
}
