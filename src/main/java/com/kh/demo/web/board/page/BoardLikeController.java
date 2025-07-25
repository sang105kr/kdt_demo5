package com.kh.demo.web.board.page;

import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.svc.BoardSVC;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.common.session.LoginMember;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 게시글 좋아요/싫어요 컨트롤러
 * - 좋아요/싫어요 처리
 * - 좋아요/싫어요 취소
 */
@Slf4j
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardLikeController extends BaseController {
    
    private final BoardSVC boardSVC;

    /**
     * 좋아요 처리
     * POST /board/{id}/like
     */
    @PostMapping("/{id}/like")
    @ResponseBody
    public Map<String, Object> likeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 권한 확인
            if (!isLoggedIn(session)) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            
            Optional<Boards> boardOpt = boardSVC.findById(id);
            if (boardOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "게시글을 찾을 수 없습니다.");
                return response;
            }
            
            // 좋아요 처리
            String email = getLoginEmail(session);
            boolean result = boardSVC.likeBoard(id, email);
            
            // 업데이트된 게시글 조회
            Optional<Boards> updatedBoardOpt = boardSVC.findById(id);
            Boards updatedBoard = updatedBoardOpt.orElse(null);
            
            response.put("success", result);
            response.put("message", result ? "좋아요가 처리되었습니다." : "좋아요 처리에 실패했습니다.");
            response.put("likeCount", updatedBoard != null ? updatedBoard.getLikeCount() : 0);
            response.put("dislikeCount", updatedBoard != null ? updatedBoard.getDislikeCount() : 0);
            
        } catch (Exception e) {
            log.error("좋아요 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "좋아요 처리 중 오류가 발생했습니다.");
        }
        
        return response;
    }

    /**
     * 좋아요 취소
     * POST /board/{id}/unlike
     */
    @PostMapping("/{id}/unlike")
    @ResponseBody
    public Map<String, Object> unlikeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 권한 확인
            if (!isLoggedIn(session)) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            
            Optional<Boards> boardOpt = boardSVC.findById(id);
            if (boardOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "게시글을 찾을 수 없습니다.");
                return response;
            }
            
            // 좋아요 취소 처리
            String email = getLoginEmail(session);
            boolean result = boardSVC.cancelBoardLike(id, email);
            
            // 업데이트된 게시글 조회
            Optional<Boards> updatedBoardOpt = boardSVC.findById(id);
            Boards updatedBoard = updatedBoardOpt.orElse(null);
            
            response.put("success", result);
            response.put("message", result ? "좋아요가 취소되었습니다." : "좋아요 취소에 실패했습니다.");
            response.put("likeCount", updatedBoard != null ? updatedBoard.getLikeCount() : 0);
            response.put("dislikeCount", updatedBoard != null ? updatedBoard.getDislikeCount() : 0);
            
        } catch (Exception e) {
            log.error("좋아요 취소 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "좋아요 취소 중 오류가 발생했습니다.");
        }
        
        return response;
    }

    /**
     * 싫어요 처리
     * POST /board/{id}/dislike
     */
    @PostMapping("/{id}/dislike")
    @ResponseBody
    public Map<String, Object> dislikeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 권한 확인
            if (!isLoggedIn(session)) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            
            Optional<Boards> boardOpt = boardSVC.findById(id);
            if (boardOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "게시글을 찾을 수 없습니다.");
                return response;
            }
            
            // 싫어요 처리
            String email = getLoginEmail(session);
            boolean result = boardSVC.dislikeBoard(id, email);
            
            // 업데이트된 게시글 조회
            Optional<Boards> updatedBoardOpt = boardSVC.findById(id);
            Boards updatedBoard = updatedBoardOpt.orElse(null);
            
            response.put("success", result);
            response.put("message", result ? "싫어요가 처리되었습니다." : "싫어요 처리에 실패했습니다.");
            response.put("likeCount", updatedBoard != null ? updatedBoard.getLikeCount() : 0);
            response.put("dislikeCount", updatedBoard != null ? updatedBoard.getDislikeCount() : 0);
            
        } catch (Exception e) {
            log.error("싫어요 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "싫어요 처리 중 오류가 발생했습니다.");
        }
        
        return response;
    }

    /**
     * 싫어요 취소
     * POST /board/{id}/undislike
     */
    @PostMapping("/{id}/undislike")
    @ResponseBody
    public Map<String, Object> undislikeBoard(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 권한 확인
            if (!isLoggedIn(session)) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            
            Optional<Boards> boardOpt = boardSVC.findById(id);
            if (boardOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "게시글을 찾을 수 없습니다.");
                return response;
            }
            
            // 싫어요 취소 처리
            String email = getLoginEmail(session);
            boolean result = boardSVC.cancelBoardDislike(id, email);
            
            // 업데이트된 게시글 조회
            Optional<Boards> updatedBoardOpt = boardSVC.findById(id);
            Boards updatedBoard = updatedBoardOpt.orElse(null);
            
            response.put("success", result);
            response.put("message", result ? "싫어요가 취소되었습니다." : "싫어요 취소에 실패했습니다.");
            response.put("likeCount", updatedBoard != null ? updatedBoard.getLikeCount() : 0);
            response.put("dislikeCount", updatedBoard != null ? updatedBoard.getDislikeCount() : 0);
            
        } catch (Exception e) {
            log.error("싫어요 취소 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "싫어요 취소 중 오류가 발생했습니다.");
        }
        
        return response;
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