package com.kh.demo.web.dashboard;

import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

  @GetMapping("/camping")
  public String camping(Model model, HttpSession session){
    addAuthInfoToModel(model, session);
    return "dashboard/camping";
  }
  @GetMapping("/pusanRestaurants")
  public String pusanRestaurants(Model model, HttpSession session){
    addAuthInfoToModel(model, session);
    return "dashboard/pusanRestaurants";
  }

}
