package com.kh.demo.web.review.controller.api;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.review.entity.Review;
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
 * 리뷰 API 컨트롤러
 * - REST API 엔드포인트
 * - JSON 응답
 * - 비동기 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;
    private final OrderService orderService;

    /**
     * 사용자의 구매 이력 조회 (리뷰 작성용)
     */
    @GetMapping("/purchase-history/{productId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPurchaseHistory(
            @PathVariable Long productId, HttpSession session) {
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, List.<Map<String, Object>>of()));
            }

            // 사용자의 배송완료된 주문 중 해당 상품이 포함된 주문 조회
            List<Order> deliveredOrders = orderService.findDeliveredOrdersByMemberAndProduct(
                loginMember.getMemberId(), productId);
            
            List<Map<String, Object>> purchaseHistory = deliveredOrders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getOrderId());
                    orderMap.put("orderDate", order.getCdate().toString());
                    orderMap.put("orderStatus", order.getOrderStatusId());
                    return orderMap;
                })
                .toList();

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, purchaseHistory));

        } catch (Exception e) {
            log.error("구매 이력 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.<Map<String, Object>>of()));
        }
    }

    /**
     * 리뷰 작성 API
     */
    @PostMapping("/write")
    public ResponseEntity<ApiResponse<String>> writeReview(@RequestBody Map<String, Object> request, HttpSession session) {
        log.info("리뷰 작성 API 호출됨: request={}", request);
        
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            log.info("로그인 멤버: {}", loginMember);
            
            if (loginMember == null) {
                log.warn("로그인되지 않은 사용자가 리뷰 작성 시도");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));
            }

            // 필수 파라미터 검증
            if (!request.containsKey("productId") || !request.containsKey("title") || 
                !request.containsKey("content") || !request.containsKey("rating") ||
                !request.containsKey("orderId")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "필수 파라미터가 누락되었습니다."));
            }

            // 파라미터 추출 및 타입 검증
            Long productId;
            String title;
            String content;
            Integer rating;
            Long orderId;
            
            try {
                productId = Long.valueOf(request.get("productId").toString());
                title = request.get("title").toString().trim();
                content = request.get("content").toString().trim();
                rating = Integer.valueOf(request.get("rating").toString());
                orderId = Long.valueOf(request.get("orderId").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 데이터 형식입니다."));
            }

            // 비즈니스 로직 검증
            if (title.isEmpty() || title.length() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "제목은 2자 이상 입력해주세요."));
            }
            
            if (title.length() > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "제목은 100자 이하로 입력해주세요."));
            }
            
            if (content.isEmpty() || content.length() < 10) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "내용은 10자 이상 입력해주세요."));
            }
            
            if (content.length() > 2000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "내용은 2000자 이하로 입력해주세요."));
            }
            
            if (rating < 1 || rating > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "평점은 1~5점 사이로 입력해주세요."));
            }

            // 주문 존재 여부 및 권한 확인
            Optional<Order> orderOpt = orderService.findByOrderId(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "존재하지 않는 주문입니다."));
            }
            
            Order order = orderOpt.get();
            if (!order.getMemberId().equals(loginMember.getMemberId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.of(ApiResponseCode.FORBIDDEN, "본인의 주문에만 리뷰를 작성할 수 있습니다."));
            }

            // 리뷰 생성
            Review review = new Review();
            review.setProductId(productId);
            review.setMemberId(loginMember.getMemberId());
            review.setOrderId(orderId);
            review.setTitle(title);
            review.setContent(content);
            review.setRating(rating);
            
            // 리뷰 저장 (비즈니스 로직 포함)
            log.info("리뷰 저장 시도: review={}", review);
            Review savedReview = reviewService.createReview(review);
            Long reviewId = savedReview.getReviewId();
            
            log.info("리뷰 등록 성공: reviewId={}, productId={}, memberId={}", 
                    reviewId, productId, loginMember.getMemberId());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ApiResponseCode.SUCCESS, "리뷰가 성공적으로 등록되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("리뷰 등록 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "리뷰 등록 중 오류가 발생했습니다."));
        }
    }

    /**
     * 리뷰 도움됨 표시 API
     */
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<ApiResponse<String>> markHelpful(@PathVariable Long reviewId, HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));
            }

            // 리뷰 존재 여부 확인
            if (reviewId == null || reviewId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 리뷰 ID입니다."));
            }

            // 도움됨 카운트 증가
            int result = reviewService.incrementHelpfulCount(reviewId);
            
            if (result > 0) {
                log.info("리뷰 도움됨 표시 성공: reviewId={}, memberId={}", reviewId, loginMember.getMemberId());
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "도움됨으로 표시되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "존재하지 않는 리뷰입니다."));
            }

        } catch (IllegalArgumentException e) {
            log.warn("리뷰 도움됨 표시 검증 실패: reviewId={}, error={}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 도움됨 표시 실패: reviewId={}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 리뷰 도움안됨 표시 API
     */
    @PostMapping("/{reviewId}/unhelpful")
    public ResponseEntity<ApiResponse<String>> markUnhelpful(@PathVariable Long reviewId, HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));
            }

            // 리뷰 존재 여부 확인
            if (reviewId == null || reviewId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 리뷰 ID입니다."));
            }

            // 도움안됨 카운트 증가
            int result = reviewService.incrementUnhelpfulCount(reviewId);
            
            if (result > 0) {
                log.info("리뷰 도움안됨 표시 성공: reviewId={}, memberId={}", reviewId, loginMember.getMemberId());
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "도움안됨으로 표시되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "존재하지 않는 리뷰입니다."));
            }

        } catch (IllegalArgumentException e) {
            log.warn("리뷰 도움안됨 표시 검증 실패: reviewId={}, error={}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 도움안됨 표시 실패: reviewId={}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 리뷰 신고 API
     */
    @PostMapping("/{reviewId}/report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reportReview(@PathVariable Long reviewId, 
                                                                        @RequestBody Map<String, Object> request, 
                                                                        HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("success", false, "message", "로그인이 필요합니다.")));
            }

            // 리뷰 존재 여부 확인
            if (reviewId == null || reviewId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("success", false, "message", "잘못된 리뷰 ID입니다.")));
            }

            // 신고 사유 확인
            String reason = (String) request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("success", false, "message", "신고 사유를 입력해주세요.")));
            }

            // 리뷰 신고 처리
            int result = reviewService.reportReview(reviewId, loginMember.getMemberId(), reason);
            
            if (result > 0) {
                log.info("리뷰 신고 성공: reviewId={}, memberId={}, reason={}", reviewId, loginMember.getMemberId(), reason);
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, Map.of("success", true, "message", "신고가 접수되었습니다.")));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, Map.of("success", false, "message", "존재하지 않는 리뷰입니다.")));
            }

        } catch (IllegalArgumentException e) {
            log.warn("리뷰 신고 검증 실패: reviewId={}, error={}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("success", false, "message", e.getMessage())));
        } catch (Exception e) {
            log.error("리뷰 신고 실패: reviewId={}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("success", false, "message", "신고 처리 중 오류가 발생했습니다.")));
        }
    }

    /**
     * 댓글 신고 API
     */
    @PostMapping("/comments/{commentId}/report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reportComment(@PathVariable Long commentId, 
                                                                         @RequestBody Map<String, Object> request, 
                                                                         HttpSession session) {
        try {
            // 로그인 체크
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, Map.of("success", false, "message", "로그인이 필요합니다.")));
            }

            // 댓글 존재 여부 확인
            if (commentId == null || commentId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("success", false, "message", "잘못된 댓글 ID입니다.")));
            }

            // 신고 사유 확인
            String reason = (String) request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("success", false, "message", "신고 사유를 입력해주세요.")));
            }

            // 댓글 신고 처리
            int result = reviewService.reportComment(commentId, loginMember.getMemberId(), reason);
            
            if (result > 0) {
                log.info("댓글 신고 성공: commentId={}, memberId={}, reason={}", commentId, loginMember.getMemberId(), reason);
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, Map.of("success", true, "message", "신고가 접수되었습니다.")));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, Map.of("success", false, "message", "존재하지 않는 댓글입니다.")));
            }

        } catch (IllegalArgumentException e) {
            log.warn("댓글 신고 검증 실패: commentId={}, error={}", commentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, Map.of("success", false, "message", e.getMessage())));
        } catch (Exception e) {
            log.error("댓글 신고 실패: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, Map.of("success", false, "message", "신고 처리 중 오류가 발생했습니다.")));
        }
    }
} 