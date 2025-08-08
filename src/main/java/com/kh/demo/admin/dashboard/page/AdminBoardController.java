package com.kh.demo.admin.dashboard.page;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.svc.BoardSVC;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class AdminBoardController extends BaseController {

    private final BoardSVC boardSVC;
    private final CodeSVC codeSVC;
    
    private static final int PAGE_SIZE = 10;

    /**
     * 게시판 카테고리 목록을 모든 요청에 자동으로 추가
     */
    @ModelAttribute("boardCategories")
    public List<Code> boardCategories() {
        return codeSVC.getCodeList("BOARD");
    }

    /**
     * 게시판 카테고리명 매핑을 모든 요청에 자동으로 추가
     */
    @ModelAttribute("categoryNames")
    public Map<Long, String> categoryNames() {
        return codeSVC.getCodeList("BOARD").stream()
            .collect(Collectors.toMap(Code::getCodeId, Code::getDecode));
    }

    /**
     * 게시판 목록 페이지
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int pageNo,
                      @RequestParam(required = false) Long categoryId,
                      Model model, HttpSession session) {
        log.info("관리자 게시판 목록 페이지 요청 - pageNo: {}, categoryId: {}", pageNo, categoryId);
        
        List<Boards> boards;
        int totalCount;
        Pagination pagination;
        
        if (categoryId != null) {
            // 카테고리별 조회
            totalCount = boardSVC.countByBcategory(categoryId);
            pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
            boards = boardSVC.findByBcategoryWithPaging(categoryId, pageNo, PAGE_SIZE);
            model.addAttribute("selectedCategory", categoryId);
        } else {
            // 전체 조회
            totalCount = boardSVC.countAll();
            pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
            boards = boardSVC.findAllWithPaging(pageNo, PAGE_SIZE);
        }
        
        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pagination", pagination);
        addAuthInfoToModel(model, session);
        return "admin/board/list";
    }

    /**
     * 게시글 상세 페이지
     */
    @GetMapping("/{boardId}")
    public String detail(@PathVariable Long boardId, Model model, HttpSession session) {
        log.info("관리자 게시글 상세 페이지 요청 - boardId: {}", boardId);
        
        Optional<Boards> boardOpt = boardSVC.findById(boardId);
        if (boardOpt.isEmpty()) {
            model.addAttribute("errorMessage", "게시글을 찾을 수 없습니다.");
            return "redirect:/admin/board";
        }
        
        model.addAttribute("board", boardOpt.get());
        addAuthInfoToModel(model, session);
        return "admin/board/detail";
    }

    /**
     * 게시글 삭제 처리
     */
    @PostMapping("/{boardId}/delete")
    public String delete(@PathVariable Long boardId, 
                        @RequestParam(defaultValue = "1") int pageNo,
                        @RequestParam(required = false) Long categoryId,
                        Model model, HttpSession session) {
        log.info("관리자 게시글 삭제 요청 - boardId: {}", boardId);
        
        try {
            boardSVC.deleteById(boardId);
            model.addAttribute("message", "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("게시글 삭제 실패: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "게시글 삭제 중 오류가 발생했습니다.");
        }
        
        // 삭제 후 목록으로 리다이렉트
        String redirectUrl = "/admin/board?pageNo=" + pageNo;
        if (categoryId != null) {
            redirectUrl += "&categoryId=" + categoryId;
        }
        return "redirect:" + redirectUrl;
    }
} 