package com.kh.demo.web.qna.api;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.qna.entity.Qna;
import com.kh.demo.domain.qna.svc.QnaService;
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
 * Q&A API 컨트롤러
 * - REST API 엔드포인트
 * - JSON 응답
 * - 비동기 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaApiController {

    private final QnaService qnaService;

    /**
     * Q&A 목록 조회 API (상품별)
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQnaListByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            int offset = (page - 1) * size;
            List<Qna> qnaList = qnaService.findByProductId(productId, offset, size);
            int totalCount = qnaService.countByProductId(productId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("qnaList", qnaList);
            responseData.put("totalCount", totalCount);
            responseData.put("currentPage", page);
            responseData.put("pageSize", size);
            responseData.put("totalPages", (int) Math.ceil((double) totalCount / size));

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (Exception e) {
            log.error("Q&A 목록 조회 실패: productId={}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "Q&A 목록 조회에 실패했습니다.")));
        }
    }

    /**
     * Q&A 목록 조회 API (카테고리별)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQnaListByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            int offset = (page - 1) * size;
            List<Qna> qnaList = qnaService.findByCategoryId(categoryId, offset, size);
            int totalCount = qnaService.countByCategoryId(categoryId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("qnaList", qnaList);
            responseData.put("totalCount", totalCount);
            responseData.put("currentPage", page);
            responseData.put("pageSize", size);
            responseData.put("totalPages", (int) Math.ceil((double) totalCount / size));

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (Exception e) {
            log.error("Q&A 목록 조회 실패: categoryId={}", categoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "Q&A 목록 조회에 실패했습니다.")));
        }
    }

    /**
     * Q&A 검색 API
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchQna(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            int offset = (page - 1) * size;
            List<Qna> qnaList = qnaService.findByKeyword(keyword, offset, size);
            int totalCount = qnaService.countByKeyword(keyword);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("qnaList", qnaList);
            responseData.put("totalCount", totalCount);
            responseData.put("currentPage", page);
            responseData.put("pageSize", size);
            responseData.put("totalPages", (int) Math.ceil((double) totalCount / size));
            responseData.put("keyword", keyword);

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (Exception e) {
            log.error("Q&A 검색 실패: keyword={}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "Q&A 검색에 실패했습니다.")));
        }
    }

    /**
     * Q&A 상세 조회 API
     */
    @GetMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<Qna>> getQnaDetail(@PathVariable Long qnaId) {
        try {
            Optional<Qna> qnaOpt = qnaService.findByIdWithViewCount(qnaId);
            
            if (qnaOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, qnaOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null));
            }

        } catch (Exception e) {
            log.error("Q&A 상세 조회 실패: qnaId={}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * Q&A 작성 API
     */
    @PostMapping("/write")
    public ResponseEntity<ApiResponse<Map<String, Object>>> writeQna(
            @RequestBody Map<String, Object> request, HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("error", "로그인이 필요합니다.")));
            }

            // 필수 파라미터 검증
            if (!request.containsKey("title") || !request.containsKey("content") || !request.containsKey("categoryId")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", "필수 파라미터가 누락되었습니다.")));
            }

            Qna qna = new Qna();
            qna.setTitle((String) request.get("title"));
            qna.setContent((String) request.get("content"));
            qna.setCategoryId(Long.valueOf(request.get("categoryId").toString()));
            
            // 상품 ID는 선택사항
            if (request.containsKey("productId") && request.get("productId") != null) {
                qna.setProductId(Long.valueOf(request.get("productId").toString()));
            }

            Qna createdQna = qnaService.createQna(qna, loginMember.getMemberId());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("qnaId", createdQna.getQnaId());
            responseData.put("message", "Q&A가 성공적으로 작성되었습니다.");

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));

        } catch (BusinessValidationException e) {
            log.warn("Q&A 작성 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.error("Q&A 작성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "Q&A 작성에 실패했습니다.")));
        }
    }

    /**
     * Q&A 수정 API
     */
    @PutMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateQna(
            @PathVariable Long qnaId,
            @RequestBody Map<String, Object> request, HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("error", "로그인이 필요합니다.")));
            }

            // 필수 파라미터 검증
            if (!request.containsKey("title") || !request.containsKey("content") || !request.containsKey("categoryId")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", "필수 파라미터가 누락되었습니다.")));
            }

            Qna qna = new Qna();
            qna.setTitle((String) request.get("title"));
            qna.setContent((String) request.get("content"));
            qna.setCategoryId(Long.valueOf(request.get("categoryId").toString()));
            
            // 상품 ID는 선택사항
            if (request.containsKey("productId") && request.get("productId") != null) {
                qna.setProductId(Long.valueOf(request.get("productId").toString()));
            }

            int result = qnaService.updateQna(qnaId, qna, loginMember.getMemberId());
            
            if (result > 0) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "Q&A가 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, Map.of("error", "존재하지 않는 Q&A입니다.")));
            }

        } catch (BusinessValidationException e) {
            log.warn("Q&A 수정 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.error("Q&A 수정 실패: qnaId={}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "Q&A 수정에 실패했습니다.")));
        }
    }

    /**
     * Q&A 삭제 API
     */
    @DeleteMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteQna(
            @PathVariable Long qnaId, HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("error", "로그인이 필요합니다.")));
            }

            // TODO: 관리자 권한 체크 로직 추가
            boolean isAdmin = false; // 임시로 false 설정

            int result = qnaService.deleteQna(qnaId, loginMember.getMemberId(), isAdmin);
            
            if (result > 0) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "Q&A가 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, responseData));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, Map.of("error", "존재하지 않는 Q&A입니다.")));
            }

        } catch (BusinessValidationException e) {
            log.warn("Q&A 삭제 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.error("Q&A 삭제 실패: qnaId={}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("error", "Q&A 삭제에 실패했습니다.")));
        }
    }

    /**
     * Q&A 도움됨 표시 API
     */
    @PostMapping("/{qnaId}/helpful")
    public ResponseEntity<ApiResponse<String>> markHelpful(@PathVariable Long qnaId, HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));
            }

            // Q&A 존재 여부 확인
            if (qnaId == null || qnaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 Q&A ID입니다."));
            }

            // 도움됨 카운트 증가
            int result = qnaService.incrementHelpfulCount(qnaId);
            
            if (result > 0) {
                log.info("Q&A 도움됨 표시 성공: qnaId={}, memberId={}", qnaId, loginMember.getMemberId());
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "도움됨으로 표시되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "존재하지 않는 Q&A입니다."));
            }

        } catch (Exception e) {
            log.error("Q&A 도움됨 표시 실패: qnaId={}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * Q&A 도움안됨 표시 API
     */
    @PostMapping("/{qnaId}/unhelpful")
    public ResponseEntity<ApiResponse<String>> markUnhelpful(@PathVariable Long qnaId, HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));
            }

            // Q&A 존재 여부 확인
            if (qnaId == null || qnaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 Q&A ID입니다."));
            }

            // 도움안됨 카운트 증가
            int result = qnaService.incrementUnhelpfulCount(qnaId);
            
            if (result > 0) {
                log.info("Q&A 도움안됨 표시 성공: qnaId={}, memberId={}", qnaId, loginMember.getMemberId());
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "도움안됨으로 표시되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "존재하지 않는 Q&A입니다."));
            }

        } catch (Exception e) {
            log.error("Q&A 도움안됨 표시 실패: qnaId={}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."));
        }
    }
}
