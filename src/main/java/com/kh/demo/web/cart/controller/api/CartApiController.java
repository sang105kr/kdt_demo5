package com.kh.demo.web.cart.controller.api;

import com.kh.demo.common.exception.BusinessException;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.cart.svc.CartService;
import com.kh.demo.web.common.controller.api.BaseApiController;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController extends BaseApiController {
    
    private final CartService cartService;
    private final MessageSource messageSource;

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<ApiResponse<String>> addToCart(@PathVariable Long productId, 
                           @RequestParam Integer quantity,
                           HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            Long cartItemId = cartService.addToCart(memberId, productId, quantity);
            log.info("장바구니 추가 성공: memberId={}, productId={}, quantity={}, cartItemId={}", 
                    memberId, productId, quantity, cartItemId);
            
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "장바구니에 추가되었습니다.");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("장바구니 추가 실패 (BusinessException): memberId={}, productId={}, errorCode={}, message={}", 
                    memberId, productId, e.getErrorCode(), e.getMessage());
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("장바구니 추가 실패 (Exception): memberId={}, productId={}", memberId, productId, e);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "장바구니 추가에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 장바구니 아이템 수량 업데이트
     */
    @PostMapping("/update/{cartItemId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateQuantity(@PathVariable Long cartItemId,
                                             @RequestBody Map<String, Object> requestBody,
                                             HttpServletRequest request) {
        Integer quantity = Integer.valueOf(requestBody.get("quantity").toString());
        log.info("수량 업데이트 요청: cartItemId={}, quantity={}", cartItemId, quantity);
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            log.warn("로그인이 필요합니다.");
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.UNAUTHORIZED, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        log.info("로그인된 회원 ID: {}", memberId);
        
        try {
            boolean success = cartService.updateQuantity(memberId, cartItemId, quantity);
            log.info("수량 업데이트 결과: {}", success);
            
            if (success) {
                Optional<CartItem> updatedItem = cartService.getCartItemById(cartItemId);
                if (updatedItem.isPresent()) {
                    CartItem item = updatedItem.get();
                    Map<String, Object> data = new HashMap<>();
                    data.put("updatedQuantity", item.getQuantity());
                    data.put("updatedTotalPrice", item.getTotalPrice());
                    data.put("cartTotalAmount", cartService.getCartTotalAmount(memberId));
                    data.put("itemCount", cartService.getCartItemCount(memberId));
                    
                    ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
                    return ResponseEntity.ok(response);
                } else {
                    ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                log.warn("수량 업데이트 실패: cartItemId={}, memberId={}", cartItemId, memberId);
                ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("수량 업데이트 중 IllegalArgumentException:", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("장바구니 수량 업데이트 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 장바구니 아이템 삭제
     */
    @PostMapping("/remove/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(@PathVariable Long cartItemId,
                                HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            boolean success = cartService.removeFromCart(memberId, cartItemId);
            if (success) {
                ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "장바구니에서 제거되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "장바구니 아이템 제거에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("장바구니 아이템 삭제 실패", e);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "장바구니 아이템 제거에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 장바구니 전체 비우기
     */
    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            boolean success = cartService.clearCart(memberId);
            if (success) {
                ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "장바구니가 비워졌습니다.");
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "장바구니 비우기에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("장바구니 비우기 실패", e);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "장바구니 비우기에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 장바구니 아이템에 할인 적용
     */
    @PostMapping("/discount/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> applyDiscount(@PathVariable Long cartItemId,
                               @RequestParam Double discountRate,
                               HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.UNAUTHORIZED, "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            boolean success = cartService.applyDiscount(memberId, cartItemId, discountRate);
            if (success) {
                ApiResponse<String> response = ApiResponse.of(ApiResponseCode.SUCCESS, "할인이 적용되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "할인 적용에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("할인 적용 실패", e);
            ApiResponse<String> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "할인 적용에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 장바구니 아이템 개수 조회 (AJAX용)
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCartCount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        Map<String, Object> data = new HashMap<>();
        
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            data.put("count", 0);
            data.put("loggedIn", false);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
            return ResponseEntity.ok(response);
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            int count = cartService.getCartItemCount(memberId);
            data.put("count", count);
            data.put("loggedIn", true);
            
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("장바구니 개수 조회 실패", e);
            data.put("count", 0);
            data.put("loggedIn", true);
            
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
            return ResponseEntity.ok(response);
        }
    }
} 