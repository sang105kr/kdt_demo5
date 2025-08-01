package com.kh.demo.admin.controller.page;

import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController extends BaseController {

  @GetMapping("/camping")
  public String camping(Model model, HttpSession session) {
    addAuthInfoToModel(model, session);
    return "admin/dashboard/camping";
  }
}