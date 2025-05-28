package com.kh.demo.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/test")
public class TestController {

  @GetMapping("/text")
  public String text(Model model) {

    @AllArgsConstructor
    @Getter
    class Person{
      private String name;
      private int age;
    }

    List<Person> users = new ArrayList<>();
    users.add(new Person("홍길동1",10));
    users.add(new Person("홍길동2",20));
    users.add(new Person("홍길동3",30));
    users.add(new Person("홍길동4",40));
    users.add(new Person("홍길동5",50));
    users.add(new Person("홍길동6",60));

    model.addAttribute("users",users);
    return "test/text";
  }

}
