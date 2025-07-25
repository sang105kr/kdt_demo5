package com.kh.demo.web.wishlist.controller.api;

import com.kh.demo.domain.wishlist.entity.Wishlist;
import com.kh.demo.domain.wishlist.svc.WishlistSVC;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 위시리스트 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {

    private final WishlistSVC wishlistSVC;

    /**
     * 위시리스트 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getWishlistCount(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, 0));
        }

        try {
            int count = wishlistSVC.getWishlistCountByMemberId(loginMember.getMemberId());
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, count));

        } catch (Exception e) {
            log.error("위시리스트 개수 조회 실패 - memberId: {}, error: {}", 
                    loginMember.getMemberId(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0));
        }
    }

    /**
     * 위시리스트 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Wishlist>>> getWishlist(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, List.of()));
        }

        try {
            Long memberId = loginMember.getMemberId();
            
            List<Wishlist> wishlistItems = wishlistSVC.getWishlistByMemberId(memberId, page, size);
            int totalCount = wishlistSVC.getWishlistCountByMemberId(memberId);
            
            ApiResponse.Paging paging = new ApiResponse.Paging(page, size, totalCount);
            
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, wishlistItems, paging));

        } catch (Exception e) {
            log.error("위시리스트 조회 실패 - memberId: {}, error: {}", 
                    loginMember.getMemberId(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.of()));
        }
    }

    /**
     * 위시리스트 존재 여부 확인
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlist(
            @PathVariable Long productId,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, false));
        }

        try {
            boolean isInWishlist = wishlistSVC.isInWishlist(loginMember.getMemberId(), productId);
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, isInWishlist));

        } catch (Exception e) {
            log.error("위시리스트 확인 실패 - memberId: {}, productId: {}, error: {}", 
                    loginMember.getMemberId(), productId, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false));
        }
    }

    /**
     * 위시리스트 추가
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> addToWishlist(
            @PathVariable Long productId,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, false));
        }

        try {
            Long memberId = loginMember.getMemberId();
            boolean success = wishlistSVC.addToWishlist(memberId, productId);
            
            if (success) {
                int newCount = wishlistSVC.getWishlistCountByMemberId(memberId);
                
                Map<String, Object> details = new HashMap<>();
                details.put("count", newCount);
                details.put("message", "위시리스트에 추가되었습니다.");
                
                return ResponseEntity.ok(ApiResponse.withDetails(ApiResponseCode.SUCCESS, details, true));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, false));
            }

        } catch (Exception e) {
            log.error("위시리스트 추가 실패 - memberId: {}, productId: {}, error: {}", 
                    loginMember.getMemberId(), productId, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false));
        }
    }

    /**
     * 위시리스트 제거
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> removeFromWishlist(
            @PathVariable Long productId,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, false));
        }

        try {
            Long memberId = loginMember.getMemberId();
            boolean success = wishlistSVC.removeFromWishlist(memberId, productId);
            
            if (success) {
                int newCount = wishlistSVC.getWishlistCountByMemberId(memberId);
                
                Map<String, Object> details = new HashMap<>();
                details.put("count", newCount);
                details.put("message", "위시리스트에서 제거되었습니다.");
                
                return ResponseEntity.ok(ApiResponse.withDetails(ApiResponseCode.SUCCESS, details, true));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, false));
            }

        } catch (Exception e) {
            log.error("위시리스트 제거 실패 - memberId: {}, productId: {}, error: {}", 
                    loginMember.getMemberId(), productId, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false));
        }
    }

    /**
     * 위시리스트 토글 (추가/제거)
     */
    @PostMapping("/toggle/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> toggleWishlist(
            @PathVariable Long productId,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, false));
        }

        try {
            Long memberId = loginMember.getMemberId();
            boolean isInWishlist = wishlistSVC.toggleWishlist(memberId, productId);
            int newCount = wishlistSVC.getWishlistCountByMemberId(memberId);
            
            Map<String, Object> details = new HashMap<>();
            details.put("count", newCount);
            details.put("isInWishlist", isInWishlist);
            details.put("message", isInWishlist ? "위시리스트에 추가되었습니다." : "위시리스트에서 제거되었습니다.");
            
            return ResponseEntity.ok(ApiResponse.withDetails(ApiResponseCode.SUCCESS, details, isInWishlist));

        } catch (Exception e) {
            log.error("위시리스트 토글 실패 - memberId: {}, productId: {}, error: {}", 
                    loginMember.getMemberId(), productId, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, false));
        }
    }

    /**
     * 위시리스트 전체 제거
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Integer>> clearWishlist(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, 0));
        }

        try {
            int removedCount = wishlistSVC.clearWishlist(loginMember.getMemberId());
            
            Map<String, Object> details = new HashMap<>();
            details.put("count", 0);
            details.put("message", removedCount + "개의 상품이 위시리스트에서 제거되었습니다.");
            
            return ResponseEntity.ok(ApiResponse.withDetails(ApiResponseCode.SUCCESS, details, removedCount));

        } catch (Exception e) {
            log.error("위시리스트 전체 제거 실패 - memberId: {}, error: {}", 
                    loginMember.getMemberId(), e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0));
        }
    }

    /**
     * 여러 상품의 위시리스트 상태 일괄 확인
     */
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<List<Long>>> checkWishlistStatus(
            @RequestBody Map<String, List<Long>> request,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) LoginMember loginMember) {

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(ApiResponseCode.UNAUTHORIZED, List.of()));
        }

        List<Long> productIds = request.get("productIds");
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, List.of()));
        }

        try {
            Long memberId = loginMember.getMemberId();
            
            // 위시리스트에 있는 상품 ID 목록 반환
            List<Long> wishlistProductIds = productIds.stream()
                    .filter(productId -> {
                        try {
                            return wishlistSVC.isInWishlist(memberId, productId);
                        } catch (Exception e) {
                            log.warn("상품 {} 위시리스트 상태 확인 중 오류: {}", productId, e.getMessage());
                            return false;
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, wishlistProductIds));

        } catch (Exception e) {
            log.error("위시리스트 상태 일괄 확인 실패 - memberId: {}, productIds: {}, error: {}", 
                    loginMember.getMemberId(), productIds, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, List.of()));
        }
    }
} 