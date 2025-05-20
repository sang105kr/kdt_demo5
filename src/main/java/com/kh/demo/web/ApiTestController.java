package com.kh.demo.web;

import com.kh.demo.web.api.ApiResponse;
import com.kh.demo.web.api.ApiResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

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

  // ResponseEntity 용도 : 응답메세지의 상태라인,헤더,바디 를 커스터마이징 할수 있다.
  @GetMapping("/t6")
  public ResponseEntity<List<Person>> t6() {
    List<Person> persons = List.of(
        new Person("홍길동", 30),
        new Person("홍길서", 40),
        new Person("홍길남", 40)
    );
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("key","value");
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(httpHeaders).body(persons);
    return ResponseEntity.ok(persons);
  }
  @GetMapping("/t7")
  public ResponseEntity<ApiResponse<List<Person>>> t7() {
    List<Person> persons = List.of(
        new Person("홍길동", 30),
        new Person("홍길서", 40),
        new Person("홍길남", 40)
    );
    ApiResponse<List<Person>> listApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, persons);
    return ResponseEntity.ok(listApiResponse);
  }
}
