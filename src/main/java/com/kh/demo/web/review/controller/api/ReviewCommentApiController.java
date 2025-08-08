package com.kh.demo.web.review.controller.api;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.review.entity.Review;
import com.kh.demo.domain.review.entity.ReviewComment;
import com.kh.demo.domain.review.svc.ReviewCommentService;
import com.kh.demo.domain.review.svc.ReviewService;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 리뷰 댓글 API 컨트롤러
 * - REST API 엔드포인트
 * - JSON 응답
 * - 비동기 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewCommentApiController {

    private final ReviewCommentService reviewCommentService;
    private final ReviewService reviewService;

    /**
     * 리뷰 댓글 작성 API (대댓글 지원)
     */
    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> writeComment(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, errorData));
            }

            // 리뷰 존재 여부 확인
            Optional<Review> reviewOpt = reviewService.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "존재하지 않는 리뷰입니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, errorData));
            }

            // 댓글 내용 검증
            String content = (String) request.get("content");
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "댓글 내용을 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorData));
            }

            // 대댓글인 경우 부모 댓글 확인
            Long parentId = null;
            if (request.get("parentId") != null) {
                parentId = Long.valueOf(request.get("parentId").toString());
                Optional<ReviewComment> parentCommentOpt = reviewCommentService.findById(parentId);
                if (parentCommentOpt.isEmpty()) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("success", false);
                    errorData.put("message", "존재하지 않는 부모 댓글입니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, errorData));
                }
                
                // 부모 댓글이 같은 리뷰에 속하는지 확인
                ReviewComment parentComment = parentCommentOpt.get();
                if (!parentComment.getReviewId().equals(reviewId)) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("success", false);
                    errorData.put("message", "잘못된 부모 댓글입니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorData));
                }
            }

            // 댓글 생성
            ReviewComment comment = new ReviewComment();
            comment.setReviewId(reviewId);
            comment.setContent(content.trim());
            comment.setParentId(parentId);

            ReviewComment savedComment = reviewCommentService.createComment(comment, loginMember.getMemberId());
            
            log.info("리뷰 댓글 작성 성공: reviewId={}, commentId={}, parentId={}, memberId={}", 
                reviewId, savedComment.getCommentId(), parentId, loginMember.getMemberId());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", parentId != null ? "답글이 작성되었습니다." : "댓글이 작성되었습니다.");
            responseData.put("commentId", savedComment.getCommentId());
            responseData.put("content", savedComment.getContent());
            responseData.put("memberId", savedComment.getMemberId());
            responseData.put("parentId", savedComment.getParentId());
            responseData.put("cdate", savedComment.getCdate());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (Exception e) {
            log.error("리뷰 댓글 작성 실패: reviewId={}", reviewId, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "댓글 작성에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorData));
        }
    }

    /**
     * 대댓글 작성 API (별도 엔드포인트)
     */
    @PostMapping("/comments/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<Map<String, Object>>> writeReply(
            @PathVariable Long parentCommentId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, errorData));
            }

            // 부모 댓글 존재 여부 확인
            Optional<ReviewComment> parentCommentOpt = reviewCommentService.findById(parentCommentId);
            if (parentCommentOpt.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "존재하지 않는 댓글입니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, errorData));
            }

            ReviewComment parentComment = parentCommentOpt.get();

            // 댓글 내용 검증
            String content = (String) request.get("content");
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "답글 내용을 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorData));
            }

            // 대댓글 생성
            ReviewComment reply = new ReviewComment();
            reply.setReviewId(parentComment.getReviewId());
            reply.setContent(content.trim());
            reply.setParentId(parentCommentId);

            ReviewComment savedReply = reviewCommentService.createComment(reply, loginMember.getMemberId());
            
            log.info("대댓글 작성 성공: parentCommentId={}, replyId={}, memberId={}", 
                parentCommentId, savedReply.getCommentId(), loginMember.getMemberId());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "답글이 작성되었습니다.");
            responseData.put("commentId", savedReply.getCommentId());
            responseData.put("content", savedReply.getContent());
            responseData.put("memberId", savedReply.getMemberId());
            responseData.put("parentId", savedReply.getParentId());
            responseData.put("cdate", savedReply.getCdate());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (Exception e) {
            log.error("대댓글 작성 실패: parentCommentId={}", parentCommentId, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "답글 작성에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorData));
        }
    }

    /**
     * 리뷰 댓글 수정 API
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, errorData));
            }

            // 댓글 존재 여부 및 권한 확인
            Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
            if (commentOpt.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "존재하지 않는 댓글입니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, errorData));
            }

            ReviewComment existingComment = commentOpt.get();
            if (!existingComment.getMemberId().equals(loginMember.getMemberId())) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "댓글 작성자만 수정할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.of(ApiResponseCode.FORBIDDEN, errorData));
            }

            // 댓글 내용 검증
            String content = (String) request.get("content");
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "댓글 내용을 입력해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, errorData));
            }

            // 댓글 수정
            ReviewComment comment = new ReviewComment();
            comment.setContent(content.trim());

            int result = reviewCommentService.updateComment(commentId, comment, loginMember.getMemberId());
            
            if (result > 0) {
                log.info("리뷰 댓글 수정 성공: commentId={}, memberId={}", commentId, loginMember.getMemberId());
                            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "댓글이 수정되었습니다.");
            responseData.put("commentId", commentId);
            responseData.put("content", content.trim());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
            } else {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "댓글 수정에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorData));
            }

        } catch (Exception e) {
            log.error("리뷰 댓글 수정 실패: commentId={}", commentId, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "댓글 수정에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorData));
        }
    }

    /**
     * 리뷰 댓글 삭제 API
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteComment(
            @PathVariable Long commentId,
            HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, errorData));
            }

            // 댓글 존재 여부 및 권한 확인
            Optional<ReviewComment> commentOpt = reviewCommentService.findById(commentId);
            if (commentOpt.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "존재하지 않는 댓글입니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, errorData));
            }

            ReviewComment existingComment = commentOpt.get();
            if (!existingComment.getMemberId().equals(loginMember.getMemberId())) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "댓글 작성자만 삭제할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.of(ApiResponseCode.FORBIDDEN, errorData));
            }

            // 댓글 삭제
            int result = reviewCommentService.deleteComment(commentId, loginMember.getMemberId());
            
            if (result > 0) {
                log.info("리뷰 댓글 삭제 성공: commentId={}, memberId={}", commentId, loginMember.getMemberId());
                            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "댓글이 삭제되었습니다.");
            responseData.put("commentId", commentId);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
            } else {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "댓글 삭제에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorData));
            }

        } catch (Exception e) {
            log.error("리뷰 댓글 삭제 실패: commentId={}", commentId, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "댓글 삭제에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorData));
        }
    }

    /**
     * 리뷰 댓글 목록 조회 API (계층 구조)
     */
    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComments(@PathVariable Long reviewId) {
        
        try {
            // 리뷰 존재 여부 확인
            Optional<Review> reviewOpt = reviewService.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("success", false);
                errorData.put("message", "존재하지 않는 리뷰입니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, errorData));
            }

            // 댓글 목록 조회 (계층 구조)
            List<ReviewComment> allComments = reviewCommentService.findByReviewId(reviewId);
            
            // 계층 구조로 정리
            List<ReviewComment> topLevelComments = allComments.stream()
                .filter(comment -> comment.getParentId() == null)
                .toList();
            
            // 각 최상위 댓글에 대댓글 추가
            for (ReviewComment topComment : topLevelComments) {
                List<ReviewComment> replies = allComments.stream()
                    .filter(comment -> topComment.getCommentId().equals(comment.getParentId()))
                    .toList();
                // 대댓글 정보를 부모 댓글에 추가 (임시로 사용)
                // 실제로는 별도 DTO를 만들어야 하지만, 간단히 처리
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("comments", allComments);
            responseData.put("topLevelComments", topLevelComments);
            responseData.put("totalCount", allComments.size());
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (Exception e) {
            log.error("리뷰 댓글 목록 조회 실패: reviewId={}", reviewId, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "댓글 목록 조회에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, errorData));
        }
    }
}
