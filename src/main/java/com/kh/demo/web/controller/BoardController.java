package com.kh.demo.web.controller;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.domain.board.svc.BoardSVC;
import com.kh.demo.domain.board.svc.RboardSVC;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.controller.form.board.DetailForm;
import com.kh.demo.web.controller.form.board.ReplyForm;
import com.kh.demo.web.controller.form.board.SaveForm;
import com.kh.demo.web.controller.form.board.UpdateForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

@Slf4j
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController extends BaseController {
    private final BoardSVC boardSVC;
    private final RboardSVC rboardSVC;
    private final CodeSVC codeSVC;
    
    private static final int PAGE_SIZE = 10; // 페이지당 게시글 수
    private static final int REPLY_PAGE_SIZE = 10; // 페이지당 댓글 수

    // 통합 게시글 목록 (카테고리 필터 + 페이징)
    @GetMapping
    public String list(@RequestParam(required = false) Long category,
                      @RequestParam(required = false) String search,
                      @RequestParam(defaultValue = "1", name = "pageNo") int pageNo, 
                      Model model) {
        
        List<Boards> boards;
        int totalCount;
        Pagination pagination;
        
        // 카테고리별 또는 전체 게시글 조회 (검색 포함)
        if (search != null && !search.trim().isEmpty()) {
            // 검색이 있는 경우
            if (category != null) {
                // 카테고리 + 검색
                totalCount = boardSVC.countByBcategoryAndTitleContaining(category, search.trim());
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findByBcategoryAndTitleContainingWithPaging(category, search.trim(), pageNo, PAGE_SIZE);
            } else {
                // 전체 + 검색
                totalCount = boardSVC.countByTitleContaining(search.trim());
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findByTitleContainingWithPaging(search.trim(), pageNo, PAGE_SIZE);
            }
        } else {
            // 검색이 없는 경우
            if (category != null) {
                // 카테고리별 조회
                totalCount = boardSVC.countByBcategory(category);
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findByBcategoryWithPaging(category, pageNo, PAGE_SIZE);
            } else {
                // 전체 조회
                totalCount = boardSVC.countAll();
                pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
                boards = boardSVC.findAllWithPaging(pageNo, PAGE_SIZE);
                log.info("전체조회!!={},{},{}", totalCount,pagination,boards);
            }
        }
        
        // 각 게시글의 좋아요/싫어요 수를 Map으로 관리
        Map<Long, Map<String, Integer>> likeDislikeMap = new HashMap<>();
        for (Boards board : boards) {
            Map<String, Integer> counts = new HashMap<>();
            counts.put("like", board.getLikeCount() != null ? board.getLikeCount() : 0);
            counts.put("dislike", board.getDislikeCount() != null ? board.getDislikeCount() : 0);
            likeDislikeMap.put(board.getBoardId(), counts);
        }
        
        model.addAttribute("list", boards);
        model.addAttribute("likeDislikeMap", likeDislikeMap);
        model.addAttribute("pagination", pagination);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchKeyword", search);
        log.info("[BoardController] totalCount={}, totalPages={}, boards.size={}", totalCount, pagination.getTotalPages(), boards.size());
        log.info("[Pagination Debug] totalCount={}, totalPages={}, startPage={}, endPage={}, hasNextBlock={}, pageNo={}",
    pagination.getTotalCount(), pagination.getTotalPages(), pagination.getStartPage(), pagination.getEndPage(), pagination.getHasNextBlock(), pagination.getPageNo());
        return "board/list";
    }

    // 게시글 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, 
                        Model model, HttpSession session) throws SQLException {
        Boards board = boardSVC.findById(id).orElse(null);
        if (board == null) {
            return "redirect:/board";
        }
        
        // 게시글 정보 설정
        DetailForm detailForm = new DetailForm();
        detailForm.setBoardId(board.getBoardId());
        detailForm.setBcategory(board.getBcategory());
        detailForm.setTitle(board.getTitle());
        detailForm.setNickname(board.getNickname());
        detailForm.setEmail(board.getEmail());
        detailForm.setBcontent(board.getBcontent() != null ? board.getBcontent() : "");
        detailForm.setCdate(board.getCdate());
        detailForm.setUdate(board.getUdate());
        detailForm.setLikeCount(board.getLikeCount());
        detailForm.setDislikeCount(board.getDislikeCount());
        
        // 댓글은 AJAX로 로드하므로 서버에서 렌더링하지 않음
        
        model.addAttribute("detailForm", detailForm);
        return "board/detail";
    }

    // 게시글 등록 폼
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        SaveForm saveForm = new SaveForm();
        // 세션에서 닉네임/이메일 자동 주입
        Object loginMemberObj = getLoginMember(session);
        if (loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember loginMember) {
            saveForm.setNickname(loginMember.getNickname());
            saveForm.setEmail(loginMember.getEmail());
        }
        model.addAttribute("saveForm", saveForm);
        return "board/add";
    }

    // 게시글 등록 처리
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("saveForm") SaveForm form,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      HttpSession session,
                      Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        // 세션에서 닉네임/이메일 강제 주입(보안)
        Object loginMemberObj = getLoginMember(session);
        if (loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember loginMember) {
            form.setNickname(loginMember.getNickname());
            form.setEmail(loginMember.getEmail());
        }
        if (bindingResult.hasErrors()) {
            // 폼 검증 실패 시 세션 정보를 다시 설정
            model.addAttribute("saveForm", form);
            return "board/add";
        }
        Boards board = new Boards();
        board.setBcategory(form.getBcategory());
        board.setTitle(form.getTitle());
        board.setNickname(form.getNickname());
        board.setEmail(form.getEmail());
        board.setBcontent(form.getBcontent());
        boardSVC.save(board);
        redirectAttributes.addFlashAttribute("msg", "등록되었습니다.");
        return "redirect:/board";
    }

    // 게시글 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) throws SQLException {
        Boards board = boardSVC.findById(id).orElse(null);
        if (board == null) {
            return "redirect:/board";
        }
        
        // FlashAttribute에서 폼 데이터가 있으면 사용, 없으면 새로 생성
        UpdateForm updateForm = (UpdateForm) model.getAttribute("updateForm");
        if (updateForm == null) {
            updateForm = new UpdateForm();
            updateForm.setBcategory(board.getBcategory());
            updateForm.setTitle(board.getTitle());
            updateForm.setNickname(board.getNickname());
            updateForm.setEmail(board.getEmail());
            updateForm.setBcontent(board.getBcontent() != null ? board.getBcontent() : "");
            updateForm.setCdate(board.getCdate());
            updateForm.setUdate(board.getUdate());
        }
        
        // 게시판 카테고리 데이터 추가
        List<Code> boardCategories = codeSVC.findByGcode("BOARD");
        model.addAttribute("boardCategories", boardCategories);
        model.addAttribute("updateForm", updateForm);
        model.addAttribute("boardId", id);
        return "board/edit";
    }

    // 답글 작성 폼
    @GetMapping("/{id}/reply")
    public String replyForm(@PathVariable Long id, Model model, HttpSession session) throws SQLException {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        // 원글 정보 조회
        Boards originalPost = boardSVC.findById(id).orElse(null);
        if (originalPost == null) {
            return "redirect:/board";
        }
        
        // 답글 폼 생성
        SaveForm replyForm = new SaveForm();
        
        // 세션에서 닉네임/이메일 자동 주입
        Object loginMemberObj = getLoginMember(session);
        if (loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember loginMember) {
            replyForm.setNickname(loginMember.getNickname());
            replyForm.setEmail(loginMember.getEmail());
        }
        
        // 원글과 동일한 카테고리로 자동 설정
        replyForm.setBcategory(originalPost.getBcategory());
        
        // 게시판 카테고리 데이터 추가
        List<Code> boardCategories = codeSVC.findByGcode("BOARD");
        
        model.addAttribute("originalPost", originalPost);
        model.addAttribute("replyForm", replyForm);
        model.addAttribute("boardCategories", boardCategories);
        return "board/reply";
    }

    // 게시글 수정 처리
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                      @Valid @ModelAttribute("updateForm") UpdateForm form,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        if (bindingResult.hasErrors()) {
            // 폼 검증 실패 시 redirect를 사용하여 GET 요청으로 다시 처리
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateForm", bindingResult);
            redirectAttributes.addFlashAttribute("updateForm", form);
            return "redirect:/board/" + id + "/edit";
        }
        
        // Use the targeted update method that only updates user-modifiable fields
        boardSVC.updateContent(id, form.getBcategory(), form.getTitle(), 
                              form.getEmail(), form.getNickname(), form.getBcontent());
        redirectAttributes.addFlashAttribute("msg", "수정되었습니다.");
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // 로그인 체크
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/board/" + id;
        }
        
        // 게시글 작성자 확인
        Boards board = boardSVC.findById(id).orElse(null);
        if (board == null) {
            redirectAttributes.addFlashAttribute("msg", "게시글을 찾을 수 없습니다.");
            return "redirect:/board";
        }
        
        Object loginMemberObj = getLoginMember(session);
        if (!(loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember)) {
            redirectAttributes.addFlashAttribute("msg", "로그인 정보가 올바르지 않습니다.");
            return "redirect:/board/" + id;
        }
        
        com.kh.demo.web.controller.form.login.LoginMember loginMember = 
            (com.kh.demo.web.controller.form.login.LoginMember) loginMemberObj;
        
        if (!loginMember.getEmail().equals(board.getEmail())) {
            redirectAttributes.addFlashAttribute("msg", "삭제 권한이 없습니다.");
            return "redirect:/board/" + id;
        }
        
        boardSVC.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "삭제되었습니다.");
        return "redirect:/board";
    }
    
    // 게시글 답글 등록
    @PostMapping("/{id}/reply")
    public String addBoardReply(@PathVariable Long id,
                               @Valid @ModelAttribute("replyForm") SaveForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession session,
                               Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        // 세션에서 닉네임/이메일 강제 주입(보안)
        Object loginMemberObj = getLoginMember(session);
        if (loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember loginMember) {
            form.setNickname(loginMember.getNickname());
            form.setEmail(loginMember.getEmail());
        }
        
        if (bindingResult.hasErrors()) {
            // 폼 검증 실패 시 원글 정보와 함께 다시 표시
            Boards originalPost = boardSVC.findById(id).orElse(null);
            if (originalPost != null) {
                List<Code> boardCategories = codeSVC.findByGcode("BOARD");
                model.addAttribute("originalPost", originalPost);
                model.addAttribute("boardCategories", boardCategories);
            }
            return "board/reply";
        }
        
        // 원글 정보 조회
        Boards originalPost = boardSVC.findById(id).orElse(null);
        if (originalPost == null) {
            redirectAttributes.addFlashAttribute("msg", "원글을 찾을 수 없습니다.");
            return "redirect:/board";
        }
        
        // 답글 생성
        Boards reply = new Boards();
        reply.setBcategory(form.getBcategory());
        reply.setTitle(form.getTitle());
        reply.setNickname(form.getNickname());
        reply.setEmail(form.getEmail());
        reply.setBcontent(form.getBcontent());
        
        // 부모 게시글 ID 설정 (서비스에서 계층 구조 자동 계산)
        reply.setPboardId(originalPost.getBoardId());
        
        boardSVC.save(reply);
        redirectAttributes.addFlashAttribute("msg", "답글이 등록되었습니다.");
        return "redirect:/board/" + id;
    }
    
    // 댓글 등록
    @PostMapping("/{boardId}/comment")
    public String addReply(@PathVariable Long boardId,
                          @Valid @ModelAttribute("replyForm") ReplyForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("replyForm", form);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.replyForm", bindingResult);
            return "redirect:/board/" + boardId;
        }
        
        // 로그인 체크 (세션에서 사용자 정보 가져오기)
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/board/" + boardId;
        }
        
        Object loginMemberObj = getLoginMember(session);
        if (!(loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember)) {
            redirectAttributes.addFlashAttribute("msg", "로그인 정보가 올바르지 않습니다.");
            return "redirect:/board/" + boardId;
        }
        
        com.kh.demo.web.controller.form.login.LoginMember loginMember = 
            (com.kh.demo.web.controller.form.login.LoginMember) loginMemberObj;
        String email = loginMember.getEmail();
        String nickname = loginMember.getNickname();
        
        Replies reply = new Replies();
        reply.setBoardId(boardId);
        reply.setEmail(email);
        reply.setNickname(nickname);
        reply.setRcontent(form.getRcontent());
        
        // 대댓글인 경우
        if (form.getParentId() != null) {
            reply.setParentId(form.getParentId());
            reply.setRgroup(form.getRgroup());
            reply.setRstep(form.getRstep() + 1);
            reply.setRindent(form.getRindent() + 1);
        } else {
            // 최상위 댓글인 경우
            reply.setParentId(null);
            reply.setRgroup(null); // DAO에서 자동 생성
            reply.setRstep(0);
            reply.setRindent(0);
        }
        
        reply.setStatus("A"); // 활성 상태
        
        rboardSVC.save(reply);
        redirectAttributes.addFlashAttribute("msg", "댓글이 등록되었습니다.");
        return "redirect:/board/" + boardId;
    }
    
    // 댓글 삭제
    @PostMapping("/{boardId}/reply/{replyId}/delete")
    public String deleteReply(@PathVariable Long boardId,
                             @PathVariable Long replyId,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        // 로그인 체크
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/board/" + boardId;
        }
        
        Object loginMemberObj = getLoginMember(session);
        if (!(loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember)) {
            redirectAttributes.addFlashAttribute("msg", "로그인 정보가 올바르지 않습니다.");
            return "redirect:/board/" + boardId;
        }
        
        com.kh.demo.web.controller.form.login.LoginMember loginMember = 
            (com.kh.demo.web.controller.form.login.LoginMember) loginMemberObj;
        String email = loginMember.getEmail();
        
        // 댓글 작성자 확인
        Replies reply = rboardSVC.findById(replyId).orElse(null);
        if (reply == null || !email.equals(reply.getEmail())) {
            redirectAttributes.addFlashAttribute("msg", "삭제 권한이 없습니다.");
            return "redirect:/board/" + boardId;
        }
        
        rboardSVC.deleteById(replyId);
        redirectAttributes.addFlashAttribute("msg", "댓글이 삭제되었습니다.");
        return "redirect:/board/" + boardId;
    }

    // 게시글 좋아요
    @PostMapping("/{id}/like")
    @ResponseBody
    public Map<String, Object> likeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (!isLoggedIn(session)) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }
        
        Object loginMemberObj = getLoginMember(session);
        if (!(loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember)) {
            response.put("success", false);
            response.put("message", "로그인 정보가 올바르지 않습니다.");
            return response;
        }
        
        com.kh.demo.web.controller.form.login.LoginMember loginMember = 
            (com.kh.demo.web.controller.form.login.LoginMember) loginMemberObj;
        
        try {
            boolean result = boardSVC.likeBoard(id, loginMember.getEmail());
            if (result) {
                Boards board = boardSVC.findById(id).orElse(null);
                response.put("success", true);
                response.put("message", "좋아요가 등록되었습니다.");
                response.put("likeCount", board != null ? board.getLikeCount() : 0);
                response.put("dislikeCount", board != null ? board.getDislikeCount() : 0);
            } else {
                response.put("success", false);
                response.put("message", "이미 좋아요를 눌렀습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return response;
    }
    
    // 게시글 좋아요 취소
    @PostMapping("/{id}/unlike")
    @ResponseBody
    public Map<String, Object> unlikeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (!isLoggedIn(session)) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }
        
        Object loginMemberObj = getLoginMember(session);
        if (!(loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember)) {
            response.put("success", false);
            response.put("message", "로그인 정보가 올바르지 않습니다.");
            return response;
        }
        
        com.kh.demo.web.controller.form.login.LoginMember loginMember = 
            (com.kh.demo.web.controller.form.login.LoginMember) loginMemberObj;
        
        try {
            boolean result = boardSVC.cancelBoardLike(id, loginMember.getEmail());
            if (result) {
                Boards board = boardSVC.findById(id).orElse(null);
                response.put("success", true);
                response.put("message", "좋아요가 취소되었습니다.");
                response.put("likeCount", board != null ? board.getLikeCount() : 0);
                response.put("dislikeCount", board != null ? board.getDislikeCount() : 0);
            } else {
                response.put("success", false);
                response.put("message", "좋아요를 누르지 않았습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return response;
    }
    
    // 게시글 싫어요
    @PostMapping("/{id}/dislike")
    @ResponseBody
    public Map<String, Object> dislikeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (!isLoggedIn(session)) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }
        
        Object loginMemberObj = getLoginMember(session);
        if (!(loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember)) {
            response.put("success", false);
            response.put("message", "로그인 정보가 올바르지 않습니다.");
            return response;
        }
        
        com.kh.demo.web.controller.form.login.LoginMember loginMember = 
            (com.kh.demo.web.controller.form.login.LoginMember) loginMemberObj;
        
        try {
            boolean result = boardSVC.dislikeBoard(id, loginMember.getEmail());
            if (result) {
                Boards board = boardSVC.findById(id).orElse(null);
                response.put("success", true);
                response.put("message", "싫어요가 등록되었습니다.");
                response.put("likeCount", board != null ? board.getLikeCount() : 0);
                response.put("dislikeCount", board != null ? board.getDislikeCount() : 0);
            } else {
                response.put("success", false);
                response.put("message", "이미 싫어요를 눌렀습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return response;
    }
    
    // 게시글 싫어요 취소
    @PostMapping("/{id}/undislike")
    @ResponseBody
    public Map<String, Object> undislikeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (!isLoggedIn(session)) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }
        
        Object loginMemberObj = getLoginMember(session);
        if (!(loginMemberObj instanceof com.kh.demo.web.controller.form.login.LoginMember)) {
            response.put("success", false);
            response.put("message", "로그인 정보가 올바르지 않습니다.");
            return response;
        }
        
        com.kh.demo.web.controller.form.login.LoginMember loginMember = 
            (com.kh.demo.web.controller.form.login.LoginMember) loginMemberObj;
        
        try {
            boolean result = boardSVC.cancelBoardDislike(id, loginMember.getEmail());
            if (result) {
                Boards board = boardSVC.findById(id).orElse(null);
                response.put("success", true);
                response.put("message", "싫어요가 취소되었습니다.");
                response.put("likeCount", board != null ? board.getLikeCount() : 0);
                response.put("dislikeCount", board != null ? board.getDislikeCount() : 0);
            } else {
                response.put("success", false);
                response.put("message", "싫어요를 누르지 않았습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return response;
    }
}

