package com.kh.demo.web.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController extends BaseController {

  @GetMapping("/")
  public String home() {
    // 단일 홈페이지로 통합 - 조건부 렌더링은 템플릿에서 처리
    return "index";
  }
} 