package com.kh.demo.web;
import com.kh.demo.domain.bbs.svc.BbsSVC;
import com.kh.demo.web.form.bbs.SaveForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/bbs")
@RequiredArgsConstructor
public class BbsController {

  private final BbsSVC bbsSVC;

  //게시글 등록화면   GET  http://localhost:9080/bbs/add
  @GetMapping("/add")
  public String addForm(Model model) {
    model.addAttribute("saveForm", new SaveForm());
    return "bbs/addForm";
  }

  //게시글등록처리   POST http://localhost:9080/bbs/add
  @PostMapping("/add")
  public String add(RedirectAttributes redirectAttributes) {


//    redirectAttributes("id", bid);
    return "redirect:/bbs/{id}";     //302 GET http://localhost:9080/bbs/{id}
  }

  //게시글 목록     GET  http://localhost:9080/bbs
  @GetMapping
  public String findAll() {

    return "bbs/all";
  }

  //게시글 조회화면  GET http://localhost:9080/bbs/{id}
  @GetMapping("/bbs/{id}")
  public String findById(@PathVariable("id") Long id) {

    return "bbs/detailForm";
  }

  //게시글 수정화면  GET http://localhost:9080/bbs/{id}/edit
  @GetMapping("/{id}/edit")
  public String updateForm(@PathVariable("id") Long id) {

    return "bbs/updateForm";
  }

  //게시글 수정처리  Post http://localhost:9080/bbs/{id}/edit
  @PostMapping("/{id}/edit")
  public String update(@PathVariable("id") Long id, RedirectAttributes redirectAttributes){

    redirectAttributes.addAttribute("id", id);
    return "redirect:/bbs/{id}";   // 302 GET http://localhost:9080/bbs/{id}
  }

}
