package com.kh.demo.web;

import com.kh.demo.domain.entity.Product;
import com.kh.demo.domain.product.svc.ProductSVC;
import com.kh.demo.web.api.ApiResponse;
import com.kh.demo.web.api.ApiResponseCode;
import com.kh.demo.web.api.product.SaveApi;
import com.kh.demo.web.api.product.UpdateApi;
import com.kh.demo.web.api.product.ProductApi;
import com.kh.demo.web.exception.BusinessValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.naming.Binding;
import java.util.*;

@Slf4j
@RequestMapping("/api/products")
@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class ApiProductController {

  private final ProductSVC productSVC;

  //상품 생성      //   POST    /products  =>      POST http://localhost:9080/api/products
  @PostMapping
  //@RequestBody : 요청메세지 body에 포함된 json포멧 문자열을 java 객체로 변환
  public ResponseEntity<ApiResponse<Product>> add(
      @RequestBody @Valid SaveApi saveApi
  ) {
    log.info("saveApi={}", saveApi);
    
    //1) 글로벌 오류 체크 : 상품수량 * 상품가격 이 1000만원이 넘는지 체크
    final int MAX_TOTAL_AMOUNT = 10_000_000;
    validateProductTotalAmount(saveApi, MAX_TOTAL_AMOUNT);

    Product product = new Product();
    BeanUtils.copyProperties(saveApi, product);

    Long id = productSVC.save(product);
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();

    ApiResponse<Product> productApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, findedProduct);

    return ResponseEntity.status(HttpStatus.CREATED).body(productApiResponse);
  }

  private void validateProductTotalAmount(SaveApi saveApi, int MAX_TOTAL_AMOUNT) {
    // 글로벌오류(비즈니스 유효성 검증): 수량 * 가격이 천만원을 초과하는지 검사
    long totalAmount = saveApi.getPrice() * saveApi.getQuantity();
    if (totalAmount > MAX_TOTAL_AMOUNT) {
      Map<String, String> details = new HashMap<>();
      details.put("global", "상품의 총 금액(가격 * 수량)이 천만원을 초과할 수 없습니다.");
      details.put("totalAmount", String.valueOf(totalAmount));
      details.put("maxAmount", String.valueOf(MAX_TOTAL_AMOUNT));

      throw new BusinessValidationException("상품 유효성 검증 실패", details);
    }
  }

  //상품 조회      //   GET     /products/{id} =>  GET http://localhost:9080/api/products/{id}
  @GetMapping("/{id}")
//  @ResponseBody   // 응답메세지 body에 자바 객체를 json포맷 문자열로 변환
  public ResponseEntity<ApiResponse<ProductApi>> findById(@PathVariable("id") Long id) {

    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();

    ProductApi productApi = new ProductApi();
    org.springframework.beans.BeanUtils.copyProperties(findedProduct, productApi);

    ApiResponse<ProductApi> productApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, productApi);

    return ResponseEntity.ok(productApiResponse);
  }

  //상품 수정      //   PATCH   /products/{id} =>  PATCH http://localhost:9080/api/products/{id}
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<Product>> updateById(
      @PathVariable("id") Long id,
      @RequestBody @Valid UpdateApi updateApi // 요청메세지의 json포맷의 문자열을 자바 객체로 변환
      ) {

    //1) 상품조회 
    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow(
        ()->new NoSuchElementException("상품번호 : " + id + " 를 찾을 수 없습니다.")
    );  // 찾고자하는 상품이 없으면 NoSuchElementException 예외발생

    //2) 상품수정
    Product product = new Product();
    BeanUtils.copyProperties(updateApi, product);
    int updatedRow = productSVC.updateById(id, product);

    //3) 수정된상품 조회
    optionalProduct = productSVC.findById(id);
    Product updatedProduct = optionalProduct.orElseThrow();
    
    //4) REST API 응답 표준 메시지 생성
    ApiResponse<Product> productApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, updatedProduct);

    //5) HTTP 응답 메세지 생성
    return ResponseEntity.ok(productApiResponse);
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
    log.info("pageNo={},numOfRows={}", pageNo, numOfRows);
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

  //전체 건수 가져오기      //   GET   /products/totCnt =>  GET http://localhost:9080/api/products/totCnt
  @GetMapping("/totCnt")
  public ResponseEntity<ApiResponse<Integer>> totalCount() {

    int totalCount = productSVC.getTotalCount();
    ApiResponse<Integer> productApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);

    return ResponseEntity.ok(productApiResponse);  //상태코드 200, 응답메세지Body:productApiResponse객채가 json포맷 문자열로 변환됨
  }
}
