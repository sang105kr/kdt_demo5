package com.kh.demo.web.qna.api;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.qna.entity.QnaComment;
import com.kh.demo.domain.qna.svc.QnaCommentService;
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
 * Q&A 댓글 API 컨트롤러
 * - REST API 엔드포인트
 * - JSON 응답
 * - 비동기 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/qna/comments")
@RequiredArgsConstructor
public class QnaCommentApiController {

    private final QnaCommentService qnaCommentService;

    /**
     * Q&A 댓글 목록 조회 API
     */
    @GetMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<List<QnaComment>>> getCommentList(@PathVariable Long qnaId) {
        try {
            List<QnaComment> commentList = qnaCommentService.findByQnaId(qnaId);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, commentList));

        } catch (Exception e) {
            log.error("Q&A 댓글 목록 조회 실패: qnaId={}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * Q&A 댓글 작성 API
     */
    @PostMapping("/write")
    public ResponseEntity<ApiResponse<Map<String, Object>>> writeComment(
            @RequestBody Map<String, Object> request, HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("error", "로그인이 필요합니다.")));
            }

            // 필수 파라미터 검증
            if (!request.containsKey("qnaId") || !request.containsKey("content")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", "필수 파라미터가 누락되었습니다.")));
            }

            QnaComment comment = new QnaComment();
            comment.setQnaId(Long.valueOf(request.get("qnaId").toString()));
            comment.setContent((String) request.get("content"));
            
            // 댓글 타입은 선택사항 (기본값: COMMENT)
            if (request.containsKey("commentTypeId") && request.get("commentTypeId") != null) {
                comment.setCommentTypeId(Long.valueOf(request.get("commentTypeId").toString()));
            }

            QnaComment createdComment = qnaCommentService.createComment(comment, loginMember.getMemberId(), null);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("commentId", createdComment.getCommentId());
            responseData.put("message", "댓글이 성공적으로 작성되었습니다.");

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (BusinessValidationException e) {
            log.warn("Q&A 댓글 작성 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.error("Q&A 댓글 작성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "댓글 작성에 실패했습니다.")));
        }
    }

    /**
     * Q&A 댓글 수정 API
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request, HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("error", "로그인이 필요합니다.")));
            }

            // 필수 파라미터 검증
            if (!request.containsKey("content")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", "댓글 내용이 필요합니다.")));
            }

            QnaComment comment = new QnaComment();
            comment.setContent((String) request.get("content"));

            int result = qnaCommentService.updateComment(commentId, comment, loginMember.getMemberId(), null);
            
            if (result > 0) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "댓글이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, Map.of("error", "존재하지 않는 댓글입니다.")));
            }

        } catch (BusinessValidationException e) {
            log.warn("Q&A 댓글 수정 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.error("Q&A 댓글 수정 실패: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "댓글 수정에 실패했습니다.")));
        }
    }

    /**
     * Q&A 댓글 삭제 API
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteComment(
            @PathVariable Long commentId, HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("error", "로그인이 필요합니다.")));
            }

            // 관리자 권한 체크 (gubun: 4=관리자1, 5=관리자2)
            boolean isAdmin = loginMember.getGubun() == 4L || loginMember.getGubun() == 5L;

            int result = qnaCommentService.deleteComment(commentId, loginMember.getMemberId(), null, isAdmin);
            
            if (result > 0) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "댓글이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, Map.of("error", "존재하지 않는 댓글입니다.")));
            }

        } catch (BusinessValidationException e) {
            log.warn("Q&A 댓글 삭제 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.error("Q&A 댓글 삭제 실패: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "댓글 삭제에 실패했습니다.")));
        }
    }

    /**
     * Q&A 댓글 도움됨 표시 API
     */
    @PostMapping("/{commentId}/helpful")
    public ResponseEntity<ApiResponse<String>> markCommentHelpful(@PathVariable Long commentId, HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));
            }

            // 댓글 존재 여부 확인
            if (commentId == null || commentId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 댓글 ID입니다."));
            }

            // 도움됨 카운트 증가
            int result = qnaCommentService.incrementHelpfulCount(commentId);
            
            if (result > 0) {
                log.info("Q&A 댓글 도움됨 표시 성공: commentId={}, memberId={}", commentId, loginMember.getMemberId());
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "도움됨으로 표시되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "존재하지 않는 댓글입니다."));
            }

        } catch (Exception e) {
            log.error("Q&A 댓글 도움됨 표시 실패: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * Q&A 댓글 도움안됨 표시 API
     */
    @PostMapping("/{commentId}/unhelpful")
    public ResponseEntity<ApiResponse<String>> markCommentUnhelpful(@PathVariable Long commentId, HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));
            }

            // 댓글 존재 여부 확인
            if (commentId == null || commentId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 댓글 ID입니다."));
            }

            // 도움안됨 카운트 증가
            int result = qnaCommentService.incrementUnhelpfulCount(commentId);
            
            if (result > 0) {
                log.info("Q&A 댓글 도움안됨 표시 성공: commentId={}, memberId={}", commentId, loginMember.getMemberId());
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "도움안됨으로 표시되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "존재하지 않는 댓글입니다."));
            }

        } catch (Exception e) {
            log.error("Q&A 댓글 도움안됨 표시 실패: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."));
        }
    }
}
