package com.kh.demo.web.restcontroller.board;

import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.domain.board.svc.RboardSVC;
import com.kh.demo.web.restcontroller.board.request.ReplyCreateRequest;
import com.kh.demo.web.restcontroller.board.request.ReplyUpdateRequest;
import com.kh.demo.web.restcontroller.board.response.ReplyResponse;
import com.kh.demo.web.restcontroller.dto.ApiResponse;
import com.kh.demo.web.restcontroller.dto.ApiResponseCode;
import com.kh.demo.web.session.SessionConst;
import com.kh.demo.web.controller.form.login.LoginMember;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class RepliesRestController {
    private final RboardSVC rboardSVC;

    // 댓글 등록
    @PostMapping
    public ApiResponse<Long> createReply(@Valid @RequestBody ReplyCreateRequest req, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, null);
        }
        String email = loginMember.getEmail();
        String nickname = loginMember.getNickname();
        Replies reply = new Replies();
        reply.setBoardId(req.getBoardId());
        reply.setEmail(email);
        reply.setNickname(nickname);
        reply.setRcontent(req.getRcontent());
        // 댓글/답글 구분 및 계층 처리
        if (req.getParentId() != null) {
            // 답글: 부모 댓글 정보 필요
            Replies parent = rboardSVC.findById(req.getParentId()).orElse(null);
            if (parent == null) {
                return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
            }
            reply.setParentId(parent.getReplyId());
            reply.setRgroup(parent.getRgroup());
            reply.setRstep(parent.getRstep() + 1);
            reply.setRindent(parent.getRindent() + 1);
        } else {
            // 최상위 댓글
            reply.setParentId(null);
            reply.setRgroup(null); // DAO에서 reply_id로 rgroup 설정
            reply.setRstep(0);
            reply.setRindent(0);
        }
        reply.setStatus("A");
        Long replyId = rboardSVC.save(reply);
        return ApiResponse.of(ApiResponseCode.SUCCESS, replyId);
    }

    // 댓글 수정
    @PatchMapping
    public ApiResponse<Integer> updateReply(@Valid @RequestBody ReplyUpdateRequest req, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, null);
        }
        
        Replies reply = rboardSVC.findById(req.getReplyId()).orElse(null);
        if (reply == null) {
            return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
        }
        
        if (!reply.getEmail().equals(loginMember.getEmail())) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, null);
        }
        
        reply.setRcontent(req.getRcontent());
        int updated = rboardSVC.update(reply);
        return ApiResponse.of(ApiResponseCode.SUCCESS, updated);
    }

    // 댓글 삭제
    @DeleteMapping("/{replyId}")
    public ApiResponse<Integer> deleteReply(@PathVariable Long replyId, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, null);
        }
        
        Replies reply = rboardSVC.findById(replyId).orElse(null);
        if (reply == null) {
            return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
        }
        
        if (!reply.getEmail().equals(loginMember.getEmail())) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, null);
        }
        
        int deleted = rboardSVC.deleteById(replyId);
        return ApiResponse.of(ApiResponseCode.SUCCESS, deleted);
    }

    // 게시글별 댓글/대댓글 목록 (계층형, 페이징)
    @GetMapping
    public ApiResponse<List<ReplyResponse>> getReplies(
            @RequestParam Long boardId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Replies> replies = rboardSVC.findByBoardIdWithPaging(boardId, pageNo, pageSize);
        List<ReplyResponse> result = replies.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.of(ApiResponseCode.SUCCESS, result);
    }

    // 단일 댓글 조회
    @GetMapping("/{replyId}")
    public ApiResponse<ReplyResponse> getReply(@PathVariable Long replyId) {
        Replies reply = rboardSVC.findById(replyId).orElseThrow(() -> new IllegalArgumentException("댓글 없음"));
        return ApiResponse.of(ApiResponseCode.SUCCESS, toResponse(reply));
    }

    // 댓글 좋아요
    @PostMapping("/{replyId}/like")
    public ApiResponse<Map<String, Object>> likeReply(@PathVariable Long replyId, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, null);
        }
        
        try {
            boolean result = rboardSVC.likeReply(replyId, loginMember.getEmail());
            if (result) {
                // 업데이트된 댓글 정보 조회
                Replies reply = rboardSVC.findById(replyId).orElse(null);
                if (reply != null) {
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("likeCount", reply.getLikeCount() != null ? reply.getLikeCount() : 0);
                    responseData.put("dislikeCount", reply.getDislikeCount() != null ? reply.getDislikeCount() : 0);
                    return ApiResponse.of(ApiResponseCode.SUCCESS, responseData);
                }
            }
            return ApiResponse.of(ApiResponseCode.SUCCESS, Map.of("likeCount", 0, "dislikeCount", 0));
        } catch (Exception e) {
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
        }
    }
    
    // 댓글 좋아요 취소
    @PostMapping("/{replyId}/unlike")
    public ApiResponse<Boolean> unlikeReply(@PathVariable Long replyId, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, false);
        }
        
        try {
            boolean result = rboardSVC.cancelReplyLike(replyId, loginMember.getEmail());
            return ApiResponse.of(ApiResponseCode.SUCCESS, result);
        } catch (Exception e) {
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false);
        }
    }
    
    // 댓글 싫어요
    @PostMapping("/{replyId}/dislike")
    public ApiResponse<Map<String, Object>> dislikeReply(@PathVariable Long replyId, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, null);
        }
        
        try {
            boolean result = rboardSVC.dislikeReply(replyId, loginMember.getEmail());
            if (result) {
                // 업데이트된 댓글 정보 조회
                Replies reply = rboardSVC.findById(replyId).orElse(null);
                if (reply != null) {
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("likeCount", reply.getLikeCount() != null ? reply.getLikeCount() : 0);
                    responseData.put("dislikeCount", reply.getDislikeCount() != null ? reply.getDislikeCount() : 0);
                    return ApiResponse.of(ApiResponseCode.SUCCESS, responseData);
                }
            }
            return ApiResponse.of(ApiResponseCode.SUCCESS, Map.of("likeCount", 0, "dislikeCount", 0));
        } catch (Exception e) {
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
        }
    }
    
    // 댓글 싫어요 취소
    @PostMapping("/{replyId}/undislike")
    public ApiResponse<Boolean> undislikeReply(@PathVariable Long replyId, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ApiResponse.of(ApiResponseCode.FORBIDDEN, false);
        }
        
        try {
            boolean result = rboardSVC.cancelReplyDislike(replyId, loginMember.getEmail());
            return ApiResponse.of(ApiResponseCode.SUCCESS, result);
        } catch (Exception e) {
            return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false);
        }
    }

    // Entity -> Response 변환
    private ReplyResponse toResponse(Replies r) {
        ReplyResponse res = new ReplyResponse();
        res.setReplyId(r.getReplyId());
        res.setBoardId(r.getBoardId());
        res.setEmail(r.getEmail());
        res.setNickname(r.getNickname());
        res.setRcontent(r.getRcontent());
        res.setParentId(r.getParentId());
        
        // 부모 댓글의 닉네임 조회
        if (r.getParentId() != null) {
            try {
                Replies parent = rboardSVC.findById(r.getParentId()).orElse(null);
                if (parent != null) {
                    res.setParentNickname(parent.getNickname());
                }
            } catch (Exception e) {
                // 부모 댓글 조회 실패 시 무시
            }
        }
        
        res.setRgroup(r.getRgroup());
        res.setRstep(r.getRstep());
        res.setRindent(r.getRindent());
        res.setLikeCount(r.getLikeCount());
        res.setDislikeCount(r.getDislikeCount());
        res.setStatus(r.getStatus());
        res.setCdate(r.getCdate());
        res.setUdate(r.getUdate());
        return res;
    }
} 