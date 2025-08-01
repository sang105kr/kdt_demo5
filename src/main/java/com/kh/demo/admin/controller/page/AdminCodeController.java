package com.kh.demo.admin.controller.page;

import com.kh.demo.admin.form.code.CodeAddForm;
import com.kh.demo.admin.form.code.CodeEditForm;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 코드 관리 페이지 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/codes")
@Controller
public class AdminCodeController extends BaseController {

    private final CodeSVC codeSVC;

    /**
     * 코드 목록 페이지
     */
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String gcode,
            @RequestParam(defaultValue = "") String searchText,
            Pageable pageable,
            Model model,
            HttpSession session) {
        
        log.debug("코드 목록 조회 - gcode: {}, searchText: {}", gcode, searchText);
        
        addAuthInfoToModel(model, session);
        
        // 페이징된 코드 목록 조회
        Page<Code> codes = codeSVC.findCodesWithPaging(gcode, searchText, pageable);
        
        // 그룹코드 목록 조회 (필터링용)
        List<String> gcodes = codeSVC.getAllGcodes();
        
        model.addAttribute("codes", codes);
        model.addAttribute("gcodes", gcodes);
        model.addAttribute("currentGcode", gcode);
        model.addAttribute("searchText", searchText);
        
        return "admin/code/list";
    }

    /**
     * 코드 상세 페이지
     */
    @GetMapping("/{codeId}")
    public String detail(@PathVariable Long codeId, Model model, HttpSession session) {
        log.debug("코드 상세 조회 - codeId: {}", codeId);
        
        addAuthInfoToModel(model, session);
        
        // 기존 코드 조회 (캐시에서 찾기)
        Code code = null;
        for (String gcode : codeSVC.getAllGcodes()) {
            List<Code> codes = codeSVC.getCodeList(gcode);
            code = codes.stream()
                .filter(c -> c.getCodeId().equals(codeId))
                .findFirst()
                .orElse(null);
            if (code != null) break;
        }
        
        // 하위 코드 목록 조회 (캐시에서 찾기)
        List<Code> subCodes = new ArrayList<>();
        for (String gcode : codeSVC.getAllGcodes()) {
            List<Code> codes = codeSVC.getCodeList(gcode);
            List<Code> foundSubCodes = codes.stream()
                .filter(c -> codeId.equals(c.getPcode()))
                .collect(Collectors.toList());
            subCodes.addAll(foundSubCodes);
        }
        
        model.addAttribute("code", code);
        model.addAttribute("subCodes", subCodes);
        
        return "admin/code/detail";
    }

    /**
     * 코드 등록 폼 페이지
     */
    @GetMapping("/add")
    public String addForm(
            @RequestParam(required = false) String gcode,
            @RequestParam(required = false) Long pcode,
            Model model,
            HttpSession session) {
        
        log.debug("코드 등록 폼 - gcode: {}, pcode: {}", gcode, pcode);
        
        addAuthInfoToModel(model, session);
        
        CodeAddForm form = new CodeAddForm();
        if (gcode != null) {
            form.setGcode(gcode);
        }
        if (pcode != null) {
            form.setPcode(pcode);
        }
        
        // 그룹코드 목록 조회
        List<String> gcodes = codeSVC.getAllGcodes();
        
        // 상위코드 목록 조회 (루트 레벨 코드들)
        List<Code> parentCodes = codeSVC.findRootCodes();
        
        model.addAttribute("form", form);
        model.addAttribute("gcodes", gcodes);
        model.addAttribute("parentCodes", parentCodes);
        
        return "admin/code/addForm";
    }

    /**
     * 코드 수정 폼 페이지
     */
    @GetMapping("/{codeId}/edit")
    public String editForm(@PathVariable Long codeId, Model model, HttpSession session) {
        log.debug("코드 수정 폼 - codeId: {}", codeId);
        
        addAuthInfoToModel(model, session);
        
        // 기존 코드 조회 (캐시에서 찾기)
        Code code = null;
        for (String gcode : codeSVC.getAllGcodes()) {
            List<Code> codes = codeSVC.getCodeList(gcode);
            code = codes.stream()
                .filter(c -> c.getCodeId().equals(codeId))
                .findFirst()
                .orElse(null);
            if (code != null) break;
        }
        
        CodeEditForm form = new CodeEditForm();
        form.setCodeId(code.getCodeId());
        form.setGcode(code.getGcode());
        form.setCode(code.getCode());
        form.setDecode(code.getDecode());
        form.setPcode(code.getPcode());
        form.setSortOrder(code.getSortOrder());
        form.setUseYn(code.getUseYn());
        
        // 그룹코드 목록 조회
        List<String> gcodes = codeSVC.getAllGcodes();
        
        // 상위코드 목록 조회 (자기 자신 제외)
        List<Code> parentCodes = codeSVC.findRootCodesExcluding(codeId);
        
        model.addAttribute("form", form);
        model.addAttribute("gcodes", gcodes);
        model.addAttribute("parentCodes", parentCodes);
        
        return "admin/code/editForm";
    }
} 