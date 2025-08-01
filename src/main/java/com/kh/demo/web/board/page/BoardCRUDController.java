package com.kh.demo.web.board.page;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.svc.BoardSVC;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.board.page.form.DetailForm;
import com.kh.demo.web.board.page.form.SaveForm;
import com.kh.demo.web.board.page.form.UpdateForm;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 게시글 CRUD 컨트롤러
 * - 게시글 작성
 * - 게시글 조회
 * - 게시글 수정
 * - 게시글 삭제
 */
@Slf4j
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardCRUDController extends BaseController {
    
    private final BoardSVC boardSVC;
    private final CodeSVC codeSVC;


    /**
     * 게시글 상세 조회
     * GET /board/{id}
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, 
                        Model model, HttpSession session) throws SQLException {
        
        // 게시글 조회
        Optional<Boards> boardOpt = boardSVC.findById(id);
        if (boardOpt.isEmpty()) {
            return "redirect:/board";
        }
        
        Boards board = boardOpt.get();
        
        // 조회수 증가 (실제 구현에서는 별도 메서드 필요)
        // boardSVC.incrementHit(id);
        
        // DetailForm으로 변환
        DetailForm detailForm = new DetailForm();
        detailForm.setBoardId(board.getBoardId());
        detailForm.setBcategory(board.getBcategory());
        detailForm.setTitle(board.getTitle());
        detailForm.setBcontent(board.getBcontent());
        detailForm.setEmail(board.getEmail());
        detailForm.setNickname(board.getNickname());
        detailForm.setLikeCount(board.getLikeCount());
        detailForm.setDislikeCount(board.getDislikeCount());
        detailForm.setCdate(board.getCdate());
        detailForm.setUdate(board.getUdate());
        
        // 카테고리 정보 추가
        String categoryName = codeSVC.getCodeDecode("BOARD", board.getBcategory());
        if (categoryName == null) {
            categoryName = "알 수 없음";
        }
        
        model.addAttribute("detailForm", detailForm);
        model.addAttribute("categoryName", categoryName);
        
        return "board/detail";
    }

    /**
     * 게시글 작성 폼
     * GET /board/add
     */
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        // 권한 확인
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        // 게시글 카테고리 목록 조회
        List<Code> boardCategories = codeSVC.getCodeList("BOARD");
        
        // 로그인 사용자 정보 가져오기
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        
        SaveForm saveForm = new SaveForm();
        saveForm.setNickname(loginMember.getNickname());
        saveForm.setEmail(loginMember.getEmail());
        
        model.addAttribute("saveForm", saveForm);
        model.addAttribute("boardCategories", boardCategories);
        model.addAttribute("loginMember", loginMember);
        return "board/add";
    }

    /**
     * 게시글 작성 처리
     * POST /board/add
     */
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("saveForm") SaveForm form,
                     BindingResult bindingResult,
                     RedirectAttributes redirectAttributes,
                     HttpSession session,
                     Model model) {
        
        // 권한 확인
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            // 오류 발생 시 카테고리 목록과 로그인 정보 다시 추가
            List<Code> boardCategories = codeSVC.getCodeList("BOARD");
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            
            model.addAttribute("boardCategories", boardCategories);
            model.addAttribute("loginMember", loginMember);
            return "board/add";
        }
        
        try {
            Boards board = new Boards();
            board.setBcategory(form.getBcategory());
            board.setTitle(form.getTitle());
            board.setBcontent(form.getBcontent());
            board.setEmail(form.getEmail());
            board.setNickname(form.getNickname());
            
            Long boardId = boardSVC.save(board);
            
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 등록되었습니다.");
            return "redirect:/board/" + boardId;
            
        } catch (Exception e) {
            bindingResult.reject("save.error", "게시글 저장 중 오류가 발생했습니다.");
            
            // 예외 발생 시에도 카테고리 목록과 로그인 정보 다시 추가
            List<Code> boardCategories = codeSVC.getCodeList("BOARD");
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            
            model.addAttribute("boardCategories", boardCategories);
            model.addAttribute("loginMember", loginMember);
            return "board/add";
        }
    }

    /**
     * 게시글 수정 폼
     * GET /board/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) throws SQLException {
        Optional<Boards> boardOpt = boardSVC.findById(id);
        if (boardOpt.isEmpty()) {
            return "redirect:/board";
        }
        
        Boards board = boardOpt.get();
        UpdateForm updateForm = new UpdateForm();
        updateForm.setBcategory(board.getBcategory());
        updateForm.setTitle(board.getTitle());
        updateForm.setBcontent(board.getBcontent());
        updateForm.setNickname(board.getNickname());
        updateForm.setEmail(board.getEmail());
        
        // 카테고리 정보 추가
        String categoryName = codeSVC.getCodeDecode("BOARD", board.getBcategory());
        if (categoryName == null) {
            categoryName = "알 수 없음";
        }
        
        // 게시글 카테고리 목록 조회
        List<Code> boardCategories = codeSVC.getCodeList("BOARD");
        
        model.addAttribute("updateForm", updateForm);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("boardId", id);
        model.addAttribute("cdate", board.getCdate());
        model.addAttribute("udate", board.getUdate());
        model.addAttribute("boardCategories", boardCategories);
        return "board/edit";
    }

    /**
     * 게시글 수정 처리
     * POST /board/{id}/edit
     */
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                      @Valid @ModelAttribute("updateForm") UpdateForm form,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        
        if (bindingResult.hasErrors()) {
            return "board/edit";
        }
        
        try {
            Optional<Boards> boardOpt = boardSVC.findById(id);
            if (boardOpt.isEmpty()) {
                return "redirect:/board";
            }
            
            Boards board = boardOpt.get();
            board.setBcategory(form.getBcategory());
            board.setTitle(form.getTitle());
            board.setBcontent(form.getBcontent());
            
            boardSVC.update(board);
            
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 수정되었습니다.");
            return "redirect:/board/" + id;
            
        } catch (Exception e) {
            bindingResult.reject("update.error", "게시글 수정 중 오류가 발생했습니다.");
            return "board/edit";
        }
    }

    /**
     * 게시글 삭제
     * POST /board/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            Optional<Boards> boardOpt = boardSVC.findById(id);
            if (boardOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
                return "redirect:/board";
            }
            
            Boards board = boardOpt.get();
            
            // 권한 확인 (작성자만 삭제 가능)
            if (!board.getEmail().equals(getLoginEmail(session))) {
                redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
                return "redirect:/board/" + id;
            }
            
            boardSVC.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "게시글 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/board";
    }

    /**
     * 로그인 여부 확인
     */
    protected boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loginMember") != null;
    }

    /**
     * 로그인한 사용자 이메일 조회
     */
    protected String getLoginEmail(HttpSession session) {
        Object loginMember = session.getAttribute("loginMember");
        if (loginMember instanceof LoginMember) {
            return ((LoginMember) loginMember).getEmail();
        }
        return null;
    }
} 