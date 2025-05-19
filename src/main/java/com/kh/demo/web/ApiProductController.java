package com.kh.demo.web;

import com.kh.demo.domain.entity.Product;
import com.kh.demo.domain.product.svc.ProductSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping("/api/products")
@Controller
@RequiredArgsConstructor
public class ApiProductController {

  private final ProductSVC productSVC;

  //상품 생성      //   POST    /products  =>      POST http://localhost:9080/api/products

  //상품 조회      //   GET     /products/{id} =>  GET http://localhost:9080/api/products/{id}
  @GetMapping("/{id}")
  @ResponseBody   // 응답메세지 바디에 메소드 반환타입의 객체를 json포맷으로 변환하여 반영함.
  public Product findById(@PathVariable("id") Long id) {
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();
    return findedProduct;
  }


  //상품 수정      //   PATCH   /products/{id} =>  PATCH http://localhost:9080/api/products/{id}

  //상품 삭제      //   DELETE  /products/{id} =>  DELETE http://localhost:9080/api/products/{id}

  //상품 목록      //   GET     /products      =>  GET http://localhost:9080/api/products
  @GetMapping
  @ResponseBody
  public List<Product> findAll() {
    List<Product> list = productSVC.findAll();
    return list;
  }
}
