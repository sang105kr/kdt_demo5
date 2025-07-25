package com.kh.demo.admin.controller.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

  @GetMapping("/camping")
  public String camping() {
    return "admin/dashboard/camping";
  }
}