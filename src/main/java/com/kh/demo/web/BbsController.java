package com.kh.demo.web;
import com.kh.demo.domain.bbs.svc.BbsSVC;
import com.kh.demo.domain.entity.Bbs;
import com.kh.demo.web.form.bbs.DetailForm;
import com.kh.demo.web.form.bbs.SaveForm;
import com.kh.demo.web.form.bbs.UpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

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

  //게시글 등록처리   POST http://localhost:9080/bbs/add
  @PostMapping("/add")
  public String add(
      @Valid @ModelAttribute SaveForm saveForm,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    //1) 유효성 체크
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "bbs/addForm";
    }

    //2) 게시글 등록처리
    Bbs bbs = new Bbs();
    BeanUtils.copyProperties(saveForm, bbs); // 필드명이 같은 속성값을 복사
    Long bid = bbsSVC.save(bbs);

    // 리다이렉트 경로 변수 지정
    redirectAttributes.addAttribute("id", bid);
    return "redirect:/bbs/{id}";     //302 GET http://localhost:9080/bbs/{id}
  }

  //게시글 목록     GET  http://localhost:9080/bbs
  @GetMapping
  public String findAll(Model model) {
    List<Bbs> bbsList = bbsSVC.findAll();
    model.addAttribute("list", bbsList);
    return "bbs/all";
  }

  //게시글 조회화면  GET http://localhost:9080/bbs/{id}
  @GetMapping("/{id}")
  public String findById(
      @PathVariable("id") Long id,
      Model model
      ) {

    Optional<Bbs> optionalBbs = bbsSVC.findById(id);
    Bbs bbs = optionalBbs.orElseThrow();

    DetailForm detailForm = new DetailForm();
    BeanUtils.copyProperties(bbs, detailForm);

    log.info("detailForm={}", detailForm);
    model.addAttribute("detailForm", detailForm);

    return "bbs/detailForm";
  }

  //게시글 수정화면  GET http://localhost:9080/bbs/{id}/edit
  @GetMapping("/{id}/edit")
  public String updateForm(
      @PathVariable("id") Long id,
      Model model) {

    Optional<Bbs> optionalBbs = bbsSVC.findById(id);
    Bbs bbs = optionalBbs.orElseThrow();

    UpdateForm updateForm = new UpdateForm();
    BeanUtils.copyProperties(bbs, updateForm);
    model.addAttribute("updateForm", updateForm);

    return "bbs/updateForm";
  }

  //게시글 수정처리  Post http://localhost:9080/bbs/{id}/edit
  @PostMapping("/{id}/edit")
  public String update(
      @PathVariable("id") Long id,
      @Valid @ModelAttribute UpdateForm updateForm,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes){

    //1) 유효성 처리
    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "bbs/updateForm";
    }
    //2) 수정 처리
    Bbs bbs = new Bbs();
    BeanUtils.copyProperties(updateForm, bbs);
    int updatedRows = bbsSVC.updateById(id, bbs);

    //3) 조회화면으로 리다이렉트
    redirectAttributes.addAttribute("id", id);
    return "redirect:/bbs/{id}";   // 302 GET http://localhost:9080/bbs/{id}
  }


  //게시글 삭제    GET http://localhost:9080/bbs/{id}/del
  @GetMapping("/{id}/del")
  public String deleteById(@PathVariable("id") Long id) {

    int deletedRow = bbsSVC.deleteById(id);

    return "redirect:/bbs";    // 302 GET http://localhost:9080/bbs
  }
}
