package com.kh.demo.web;

import com.kh.demo.domain.entity.Product;
import com.kh.demo.domain.product.svc.ProductSVC;
import com.kh.demo.web.api.SaveApi;
import com.kh.demo.web.api.UpdateApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping("/api/products")
@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class ApiProductController {

  private final ProductSVC productSVC;

  //상품 생성      //   POST    /products  =>      POST http://localhost:9080/api/products
  @PostMapping
  public Product add(@RequestBody SaveApi saveApi) {

    Product product = new Product();
    BeanUtils.copyProperties(saveApi, product);

    Long id = productSVC.save(product);
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();
    return findedProduct;
  }
  //상품 조회      //   GET     /products/{id} =>  GET http://localhost:9080/api/products/{id}
  @GetMapping("/{id}")
//  @ResponseBody   // 응답메세지 바디에 메소드 반환타입의 객체를 json포맷으로 변환하여 반영함.
  public Product findById(@PathVariable("id") Long id) {
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();
    return findedProduct;
  }

  //상품 수정      //   PATCH   /products/{id} =>  PATCH http://localhost:9080/api/products/{id}
  @PatchMapping("/{id}")
  public Product updateById(
      @PathVariable("id") Long id,
      @RequestBody UpdateApi updateApi // 요청메세지의 json포맷의 문자열을 자바 객체로 변환
      ) {

    Product product = new Product();
    BeanUtils.copyProperties(updateApi, product);
    int updatedRow = productSVC.updateById(id, product);
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();
    return findedProduct;
  }

  //상품 삭제      //   DELETE  /products/{id} =>  DELETE http://localhost:9080/api/products/{id}
  @DeleteMapping("/{id}")
  public String deleteById(@PathVariable("id") Long id) {

    int deletedRow = productSVC.deleteById(id);

    return deletedRow == 1 ? "OK" : "NOK";
  }

  //상품 목록      //   GET     /products      =>  GET http://localhost:9080/api/products
  @GetMapping
//  @ResponseBody
  public List<Product> findAll() {
    List<Product> list = productSVC.findAll();
    return list;
  }
}
