package com.kh.demo.web.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

  @GetMapping("/camping")
  public String camping(){
    return "dashboard/camping";
  }
  @GetMapping("/pusanRestaurants")
  public String pusanRestaurants(){
    return "dashboard/pusanRestaurants";
  }

}
