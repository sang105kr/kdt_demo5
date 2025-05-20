package com.kh.demo.web;

import com.kh.demo.domain.entity.Product;
import com.kh.demo.domain.product.svc.ProductSVC;
import com.kh.demo.web.api.ApiResponse;
import com.kh.demo.web.api.ApiResponseCode;
import com.kh.demo.web.api.product.SaveApi;
import com.kh.demo.web.api.product.UpdateApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RequestMapping("/api/products")
@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class ApiProductController {

  private final ProductSVC productSVC;

  //상품 생성      //   POST    /products  =>      POST http://localhost:9080/api/products
  @PostMapping
  //@RequestBody : 요청메세지 body에 포함된 json포멧 문자열을 java 객체로 변환
  public Product add(@RequestBody SaveApi saveApi) {
    log.info("saveApi={}", saveApi);

    Product product = new Product();
    BeanUtils.copyProperties(saveApi, product);

    Long id = productSVC.save(product);
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();
    return findedProduct;
  }
  //상품 조회      //   GET     /products/{id} =>  GET http://localhost:9080/api/products/{id}
  @GetMapping("/{id}")
//  @ResponseBody   // 응답메세지 body에 자바 객체를 json포맷 문자열로 변환
  public ResponseEntity<ApiResponse<Product>> findById(@PathVariable("id") Long id) {

    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();  // 찾고자하는 상품이 없으면 NoSuchElementException 예외발생

    ApiResponse<Product> productApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, findedProduct);

    return ResponseEntity.ok(productApiResponse);  //상태코드 200, 응답메세지Body:productApiResponse객채가 json포맷 문자열로 변환됨
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
  public ResponseEntity<ApiResponse<Product>> deleteById(@PathVariable("id") Long id) {
    //1) 상품조회
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow(
        ()->new NoSuchElementException("상품번호 : " + id + " 를 찾을 수 없습니다.")
    );  // 찾고자하는 상품이 없으면 NoSuchElementException 예외발생

    //2) 상품 삭제
    int deletedRow = productSVC.deleteById(id);
    
    //3) REST API 표준 응답 생성
    ApiResponse<Product> productApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, findedProduct);

    //4) HTTP응답 메세지 생성
    return ResponseEntity.ok(productApiResponse);
  }

  //상품 목록      //   GET     /products      =>  GET http://localhost:9080/api/products
  @GetMapping
//  @ResponseBody
  public ResponseEntity<ApiResponse<List<Product>>> findAll() {

    List<Product> list = productSVC.findAll();
    ApiResponse<List<Product>> listApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, list);

    return ResponseEntity.ok(listApiResponse);
  }

  //상품 목록-페이징      //   GET     /products      =>  GET http://localhost:9080/api/products/paging?pageNo=1&numOfRows=10
  @GetMapping("/paging")
//  @ResponseBody
  public ResponseEntity<ApiResponse<List<Product>>> findAll(
      @RequestParam(value="pageNo", defaultValue = "1") Integer pageNo,
      @RequestParam(value="numOfRows", defaultValue = "10") Integer numOfRows
  ) {

    //상품목록 가져오기
    List<Product> list = productSVC.findAll(pageNo, numOfRows);
    //상품 총건수 가져오기
    int totalCount = productSVC.getTotalCount();
    //REST API 표준 응답 만들기
    ApiResponse<List<Product>> listApiResponse = ApiResponse.of(
        ApiResponseCode.SUCCESS,
        list,
        new ApiResponse.Paging(pageNo, numOfRows, totalCount)
    );
    return ResponseEntity.ok(listApiResponse);
  }
}
