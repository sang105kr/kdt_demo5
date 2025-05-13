package com.kh.demo.web;

import com.kh.demo.domain.entity.Product;
import com.kh.demo.domain.product.svc.ProductSVC;
import com.kh.demo.web.form.product.DetailForm;
import com.kh.demo.web.form.product.SaveForm;
import com.kh.demo.web.form.product.UpdateForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/products")       // GET http://localhost:9080/products
@RequiredArgsConstructor
public class ProductController {

  final private ProductSVC productSVC;

//  ProductController(ProductSVC productSVC){
//    this.productSVC = productSVC;
//  }

  //목록
  @GetMapping       // GET  http://localhost:9080/products
  public String findAll(Model model) {
    List<Product> list = productSVC.findAll();
    model.addAttribute("list", list);
    return "product/all";   //view
  }

  //상품등록화면
  @GetMapping("/add")       // GET  http://localhost:9080/products/add
  public String addForm(Model model){
    model.addAttribute("saveForm",new SaveForm());
    return "product/add";  //view
  }


  //상품등록처리
  @PostMapping("/add")      // POST http://localhost:9080/products/add
  public String add(
      //case1)
//      @RequestParam("pname") String pname,
//      @RequestParam("price") Long price,
//      @RequestParam("quantity") Long quantity

      // @Valid : 유효성체크 활성화
      // @ModelAttribute : form객체를 모델객체에 추가하여 view에서 참조할수 있도록 함.
      @Valid @ModelAttribute SaveForm saveForm,
      BindingResult bindingResult,  //BindingResult :1.요청데이터  2.검증 결과를 담는 객체
      RedirectAttributes redirectAttributes,
      Model model
  ){
//      log.info("pname={},price={},quantity={}",pname,price,quantity);
    log.info("pname={},price={},quantity={}",saveForm.getPname(),saveForm.getPrice(),saveForm.getQuantity());

    //1)유효성 체크
    //1-1) 어노테이션 기반의 필드 검증
    if(bindingResult.hasErrors()){
      log.info("bindingResult={}", bindingResult);
      return "product/add";
    }
    //1-2) 코드기반 검증 : 필드 , 글로벌 오류(필드 2개이상)
    //1-2-1) 필드 오류 : 상품수량 100 초과 불가
//    if (saveForm.getQuantity() > 100) {
//      bindingResult.rejectValue("quantity","product","상품수량 100 초과 불가!");
//    }
    
    //1-2-2) 글로벌오류 : 총액(상품수량 * 단가) 1000만원 초과 불과
    if(saveForm.getPrice() * saveForm.getQuantity() > 10_000_000) {
      bindingResult.reject("totalPrice","총액(상품수량 * 단가) 1000만원 초과 불가!");
    }

    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "product/add";
    }

    //2)정상로직
    Product product = new Product();
    product.setPname(saveForm.getPname());
    product.setQuantity(saveForm.getQuantity());
    product.setPrice(saveForm.getPrice());

    Long pid = productSVC.save(product);
    redirectAttributes.addAttribute("id",pid);
    return "redirect:/products/{id}"; //http 응답메세지  상태라인 : 302
                                      //               응답헤더 -> location : http://localhost:9080/products/2
                                      //http 요청메세지  요청라인 : GET http://localhost:9080/products/2
  }

  //상품조회(단건)
  @GetMapping("/{id}")      // GET http://localhost:9080/products/2?name=홍길동&age=20
  public String findById(
      @PathVariable("id") Long id,        // 경로변수 값을 읽어올때
      Model model
//      @RequestParam("name") String name,  // 쿼리파라미터 값을 읽어올때
//      @RequestParam("age") Long age
      ){

    log.info("id={}",id);
//    log.info("name={}",name);
//    log.info("age={}",age);

    Optional<Product> optionalProduct = productSVC.findById(id);
    Product findedProduct = optionalProduct.orElseThrow();

    DetailForm detailForm = new DetailForm();
    detailForm.setProductId(findedProduct.getProductId());
    detailForm.setPname(findedProduct.getPname());
    detailForm.setQuantity(findedProduct.getQuantity());
    detailForm.setPrice(findedProduct.getPrice());

    model.addAttribute("detailForm",detailForm);

    return "product/detailForm";   //상품상세화면
  }

  //상품삭제(단건)
  //  @GetMapping("/del?id=상품번호")   // GET http://localhost:9080/products/del?pid=상품번호
  @GetMapping("/{id}/del")   // GET http://localhost:9080/products/상품아이디/del
  public String deleteById(
      //@RequestParm("id") Long productId
      @PathVariable("id") Long productId) {

    int rows = productSVC.deleteById(productId);

    return "redirect:/products";      // 302 get redirectUrl: http://localhost:9080/products
  }

  //상품삭제(여러건)
  @PostMapping("/del")      // POST http://localhost:9080/products/del
  public String deleteByIds(@RequestParam("productIds") List<Long> productIds) {

    log.info("productIds={}", productIds);

    int rows = productSVC.deleteByIds(productIds);
    log.info("상품정보 {}-건 삭제됨!", rows);
    return "redirect:/products";
  }

  //상품수정화면
  @GetMapping("/{id}/edit")         // GET http://localhost:9080/2/edit
  public String updateForm(
      @PathVariable("id") Long productId,
      Model model
  ) {
    //1) 유효성체크
    //2) 상품조회
    Optional<Product> optionalProduct = productSVC.findById(productId);
    Product findedProduct = optionalProduct.orElseThrow();

    UpdateForm updateForm = new UpdateForm();
    updateForm.setProductId(findedProduct.getProductId());
    updateForm.setPname(findedProduct.getPname());
    updateForm.setQuantity(findedProduct.getQuantity());
    updateForm.setPrice(findedProduct.getPrice());

    model.addAttribute("updateForm",updateForm);
    return "product/updateForm";
  }

  //상품수정처리
  @PostMapping("/{id}/edit")         // POST http://localhost:9080/2/edit
  public String updateById(
      @PathVariable("id") Long productId,
      UpdateForm updateForm,
      RedirectAttributes redirectAttributes
      ){
    log.info("id={}", productId);
    log.info("updateForm={}",updateForm);

    Product product = new Product();
    product.setProductId(updateForm.getProductId());
    product.setPname(updateForm.getPname());
    product.setQuantity(updateForm.getQuantity());
    product.setPrice(updateForm.getPrice());

    int rows = productSVC.updateById(productId, product);

    redirectAttributes.addAttribute("id",productId);
    return "redirect:/products/{id}";  // 302 get redirectUrl-> http://localhost/products/id
  }

  //
  @ResponseBody
  @GetMapping("/test1")   // GET http://localhost:9080/products/test1
  public String test1() {
    return "test1";
  }

}
