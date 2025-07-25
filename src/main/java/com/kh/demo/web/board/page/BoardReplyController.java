package com.kh.demo.web.board.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.domain.board.svc.BoardSVC;
import com.kh.demo.domain.board.svc.RboardSVC;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.web.board.page.form.ReplyForm;
import com.kh.demo.web.board.page.form.SaveForm;
import com.kh.demo.web.common.controller.page.BaseController;
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
 * 게시글 댓글 관리 컨트롤러
 * - 댓글 작성
 * - 댓글 삭제
 */
@Slf4j
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardReplyController extends BaseController {
    
    private final BoardSVC boardSVC;
    private final RboardSVC rboardSVC;

    @ModelAttribute("boardCategories")
    public List<Code> boardCategories() {
        return codeSVC.findActiveSubCodesByGcode("BOARD");
    }

    /**
     * 댓글 작성 폼
     * GET /board/{id}/reply
     */
    @GetMapping("/{id}/reply")
    public String replyForm(@PathVariable Long id, Model model, HttpSession session) throws SQLException {
        // 권한 확인
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Optional<Boards> boardOpt = boardSVC.findById(id);
        if (boardOpt.isEmpty()) {
            return "redirect:/board";
        }
        
        Boards board = boardOpt.get();
        model.addAttribute("board", board);
        model.addAttribute("replyForm", new ReplyForm());
        return "board/replyForm";
    }

    /**
     * 게시글 답글 작성
     * POST /board/{id}/reply
     */
    @PostMapping("/{id}/reply")
    public String addBoardReply(@PathVariable Long id,
                               @Valid @ModelAttribute("replyForm") SaveForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession session,
                               Model model) {
        
        // 권한 확인
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            return "board/replyForm";
        }
        
        try {
            Optional<Boards> parentBoardOpt = boardSVC.findById(id);
            if (parentBoardOpt.isEmpty()) {
                return "redirect:/board";
            }
            
            Boards parentBoard = parentBoardOpt.get();
            
            // 답글 생성
            Boards replyBoard = new Boards();
            replyBoard.setBcategory(parentBoard.getBcategory());
            replyBoard.setTitle(form.getTitle());
            replyBoard.setBcontent(form.getBcontent());
            replyBoard.setEmail(getLoginEmail(session));
            replyBoard.setBgroup(parentBoard.getBgroup());
            replyBoard.setStep(parentBoard.getStep() + 1);
            replyBoard.setBindent(parentBoard.getBindent() + 1);
            
            Long replyId = boardSVC.save(replyBoard);
            
            redirectAttributes.addFlashAttribute("message", "답글이 성공적으로 등록되었습니다.");
            return "redirect:/board/" + replyId;
            
        } catch (Exception e) {
            bindingResult.reject("reply.error", "답글 저장 중 오류가 발생했습니다.");
            return "board/replyForm";
        }
    }

    /**
     * 댓글 작성
     * POST /board/{boardId}/comment
     */
    @PostMapping("/{boardId}/comment")
    public String addReply(@PathVariable Long boardId,
                          @Valid @ModelAttribute("replyForm") ReplyForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        
        // 권한 확인
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            return "redirect:/board/" + boardId;
        }
        
        try {
            // 게시글 존재 확인
            Optional<Boards> boardOpt = boardSVC.findById(boardId);
            if (boardOpt.isEmpty()) {
                return "redirect:/board";
            }
            
            // 댓글 생성
            Replies reply = new Replies();
            reply.setBoardId(boardId);
            reply.setRcontent(form.getRcontent());
            reply.setEmail(getLoginEmail(session));
            
            Long replyId = rboardSVC.save(reply);
            
            redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 등록되었습니다.");
            return "redirect:/board/" + boardId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "댓글 저장 중 오류가 발생했습니다.");
            return "redirect:/board/" + boardId;
        }
    }

    /**
     * 댓글 삭제
     * POST /board/{boardId}/reply/{replyId}/delete
     */
    @PostMapping("/{boardId}/reply/{replyId}/delete")
    public String deleteReply(@PathVariable Long boardId,
                            @PathVariable Long replyId,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        
        try {
            Optional<Replies> replyOpt = rboardSVC.findById(replyId);
            if (replyOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "댓글을 찾을 수 없습니다.");
                return "redirect:/board/" + boardId;
            }
            
            Replies reply = replyOpt.get();
            
            // 권한 확인 (작성자만 삭제 가능)
            if (!reply.getEmail().equals(getLoginEmail(session))) {
                redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
                return "redirect:/board/" + boardId;
            }
            
            rboardSVC.deleteById(replyId);
            redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "댓글 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/board/" + boardId;
    }

    /**
     * 로그인 여부 확인
     */
    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loginMember") != null;
    }

    /**
     * 로그인한 사용자 이메일 조회
     */
    public String getLoginEmail(HttpSession session) {
        Object loginMember = session.getAttribute("loginMember");
        if (loginMember instanceof LoginMember) {
            return ((LoginMember) loginMember).getEmail();
        }
        return null;
    }
} 