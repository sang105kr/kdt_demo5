package com.kh.demo.admin;

import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminHomeController extends BaseController {

  @GetMapping
  public String admin(Model model, HttpSession session){
    addAuthInfoToModel(model, session);
    return "admin/adminHome";
  }
}
