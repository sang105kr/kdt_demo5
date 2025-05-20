package com.kh.demo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/test/api")
public class ApiTestController {

  @AllArgsConstructor
  @Getter
  class Person{
    private String name;
    private int age;
  }

  //문자열
  @GetMapping("/t1")
  public String t1(){
    String name = "홍길동";
    return name;
  }
  //사용자 정의 객체
  @GetMapping("/t2")
  public Person t2() {
    Person person = new Person("홍길동",30);
    return person;
  }

  @GetMapping("/t3")
  public List<Person> t3() {
    List<Person> persons = List.of(
        new Person("홍길동", 30),
        new Person("홍길서", 40),
        new Person("홍길남", 40)
    );
    return persons;
  }

  @GetMapping("/t4")
  public Set<Person> t4() {

    Set<Person> persons = Set.of(
        new Person("홍길동", 30),
        new Person("홍길서", 40),
        new Person("홍길남", 40)
    );
    return persons;
  }
  @GetMapping("/t5")
  public Map<Integer, Person> t5() {
    Map<Integer, Person> persons = Map.of(
        1,new Person("홍길동", 30),
        2,new Person("홍길서", 40),
        3,new Person("홍길남", 40)
    );
    return persons;
  }


}
