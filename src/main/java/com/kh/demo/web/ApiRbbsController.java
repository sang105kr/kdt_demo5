package com.kh.demo.web;

import com.kh.demo.domain.bbs.svc.RbbsSVC;
import com.kh.demo.domain.entity.Rbbs;
import com.kh.demo.web.api.ApiResponse;
import com.kh.demo.web.api.ApiResponseCode;
import com.kh.demo.web.api.rbbs.SaveApi;
import com.kh.demo.web.api.rbbs.UpdateApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RequestMapping("/api/rbbs")
@RestController 
@RequiredArgsConstructor
public class ApiRbbsController {

  private final RbbsSVC rbbsSVC;

  //댓글 생성      //   POST    /api/rbbs  =>      POST http://localhost:9080/api/rbbs
  @PostMapping
  public ResponseEntity<ApiResponse<Rbbs>> add(
      @RequestBody @Valid SaveApi saveApi
  ) {
    log.info("saveApi={}", saveApi);
    
    Rbbs rbbs = new Rbbs();
    BeanUtils.copyProperties(saveApi, rbbs);

    Rbbs insertedRbbs = rbbsSVC.save(rbbs);
    ApiResponse<Rbbs> rbbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, insertedRbbs);

    return ResponseEntity.status(HttpStatus.CREATED).body(rbbsApiResponse);
  }

  //댓글 조회      //   GET     /rbbss/{id} =>  GET http://localhost:9080/api/rbbss/{id}
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Rbbs>> findById(@PathVariable("id") Long id) {

    Optional<Rbbs> optionalRbbs = rbbsSVC.findById(id);
    Rbbs findedRbbs = optionalRbbs.orElseThrow();  // 찾고자하는 댓글 없으면 NoSuchElementException 예외발생

    ApiResponse<Rbbs> rbbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, findedRbbs);

    return ResponseEntity.ok(rbbsApiResponse);  //상태코드 200, 응답메세지Body:RbbsApiResponse객채가 json포맷 문자열로 변환됨
  }

  //댓글 수정      //   PATCH   /rbbss/{id} =>  PATCH http://localhost:9080/api/rbbss/{id}
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<Rbbs>> updateById(
      @PathVariable("id") Long id,
      @RequestBody @Valid UpdateApi updateApi
      ) {

    //1) 댓글조회
    Optional<Rbbs> optionalRbbs = rbbsSVC.findById(id);
    Rbbs findedRbbs = optionalRbbs.orElseThrow(
        ()->new NoSuchElementException("댓글번호 : " + id + " 를 찾을 수 없습니다.")
    );  // 찾고자하는 댓글이 없으면 NoSuchElementException 예외발생

    //2) 댓글수정
    Rbbs rbbs = new Rbbs();
    BeanUtils.copyProperties(updateApi, rbbs);
    int updatedRow = rbbsSVC.updateById(id, rbbs);

    //3) 수정된상품 조회
    optionalRbbs = rbbsSVC.findById(id);
    Rbbs updatedRbbs = optionalRbbs.orElseThrow();
    
    //4) REST API 응답 표준 메시지 생성
    ApiResponse<Rbbs> rbbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, updatedRbbs);

    //5) HTTP 응답 메세지 생성
    return ResponseEntity.ok(rbbsApiResponse);
  }

  //댓글 삭제      //   DELETE  /rbbss/{id} =>  DELETE http://localhost:9080/api/rbbss/{id}
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Rbbs>> deleteById(@PathVariable("id") Long id) {
    //1) 댓글조회
    Optional<Rbbs> optionalRbbs = rbbsSVC.findById(id);
    Rbbs findedRbbs = optionalRbbs.orElseThrow(
        ()->new NoSuchElementException("댓글번호 : " + id + " 를 찾을 수 없습니다.")
    );  // 찾고자하는 상품이 없으면 NoSuchElementException 예외발생

    //2) 댓글 삭제
    int deletedRow = rbbsSVC.deleteById(id);
    
    //3) REST API 표준 응답 생성
    ApiResponse<Rbbs> rbbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, findedRbbs);

    //4) HTTP응답 메세지 생성
    return ResponseEntity.ok(rbbsApiResponse);
  }

  //상품 목록      //   GET     /Rbbss      =>  GET http://localhost:9080/api/Rbbss
  @GetMapping
//  @ResponseBody
  public ResponseEntity<ApiResponse<List<Rbbs>>> findAll() {

    List<Rbbs> list = rbbsSVC.findAll();
    ApiResponse<List<Rbbs>> listApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, list);

    return ResponseEntity.ok(listApiResponse);
  }

  //상품 목록-페이징      //   GET     /rbbss      =>  GET http://localhost:9080/api/rbbss/paging?pageNo=1&numOfRows=10
  @GetMapping("/paging")
  public ResponseEntity<ApiResponse<List<Rbbs>>> findAll(
      @RequestParam(value="pageNo", defaultValue = "1") Integer pageNo,
      @RequestParam(value="numOfRows", defaultValue = "10") Integer numOfRows
  ) {
    log.info("pageNo={},numOfRows={}", pageNo, numOfRows);
    //상품목록 가져오기
    List<Rbbs> list = rbbsSVC.findAll(pageNo, numOfRows);
    //상품 총건수 가져오기
    int totalCount = rbbsSVC.getTotalCount();
    //REST API 표준 응답 만들기
    ApiResponse<List<Rbbs>> listApiResponse = ApiResponse.of(
        ApiResponseCode.SUCCESS,
        list,
        new ApiResponse.Paging(pageNo, numOfRows, totalCount)
    );
    return ResponseEntity.ok(listApiResponse);
  }

  //전체 건수 가져오기      //   GET   /Rbbss/totCnt =>  GET http://localhost:9080/api/rbbss/totCnt
  @GetMapping("/totCnt")
  public ResponseEntity<ApiResponse<Integer>> totalCount() {

    int totalCount = rbbsSVC.getTotalCount();
    ApiResponse<Integer> RbbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);

    return ResponseEntity.ok(RbbsApiResponse);  //상태코드 200, 응답메세지Body:RbbsApiResponse객채가 json포맷 문자열로 변환됨
  }
}
