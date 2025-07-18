package com.kh.demo.web.page;

import com.kh.demo.domain.cart.svc.CartService;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.web.exception.BusinessException;
import com.kh.demo.web.page.form.cart.CartOrderForm;
import com.kh.demo.web.page.form.login.LoginMember;
import com.kh.demo.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import com.kh.demo.domain.cart.entity.CartItem;
import com.kh.demo.domain.cart.dto.CartItemDTO;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CartController extends BaseController {
    
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductDAO productDAO;
    private final MessageSource messageSource;
    
    /**
     * 장바구니 목록 페이지
     */
    @GetMapping("/cart")
    public String cartList(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        // 장바구니 아이템 조회 (DTO 포함)
        var cartItems = cartService.getCartItemsWithProduct(memberId);
        int itemCount = cartService.getCartItemCount(memberId);
        Long totalAmount = cartService.getCartTotalAmount(memberId);
        
        // 디버깅을 위한 로그 추가
        log.info("Cart items: {}", cartItems);
        log.info("Cart items type: {}", cartItems != null ? cartItems.getClass().getSimpleName() : "null");
        if (cartItems != null && !cartItems.isEmpty()) {
            log.info("First item type: {}", cartItems.get(0).getClass().getSimpleName());
            log.info("First item: {}", cartItems.get(0));
            log.info("First item cartItemId: {}", cartItems.get(0).getCartItemId());
            log.info("First item productId: {}", cartItems.get(0).getProductId());
        }
        
        // 안전한 처리를 위한 수정
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        log.info("cartItems={},{}", cartItems.size(),cartItems);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("totalAmount", totalAmount);
        
        return "cart/list";
    }
    
    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/cart/add/{productId}")
    @ResponseBody
    public String addToCart(@PathVariable Long productId, 
                           @RequestParam Integer quantity,
                           HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "로그인이 필요합니다.";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            // 장바구니에 추가 (상품 존재 여부 및 재고 확인은 서비스에서 처리)
            Long cartItemId = cartService.addToCart(memberId, productId, quantity);
            log.info("장바구니 추가 성공: memberId={}, productId={}, quantity={}, cartItemId={}", 
                    memberId, productId, quantity, cartItemId);
            
            return "success";
        } catch (BusinessException e) {
            log.warn("장바구니 추가 실패 (BusinessException): memberId={}, productId={}, errorCode={}, message={}", 
                    memberId, productId, e.getErrorCode(), e.getMessage());
            return e.getMessage();
        } catch (Exception e) {
            log.error("장바구니 추가 실패 (Exception): memberId={}, productId={}", memberId, productId, e);
            return getMessage("cart.add.failed");
        }
    }
    
    /**
     * 장바구니 아이템 수량 업데이트
     */
    @PostMapping("/cart/update/{cartItemId}")
    @ResponseBody
    public Map<String, Object> updateQuantity(@PathVariable Long cartItemId,
                                             @RequestBody Map<String, Object> requestBody,
                                             HttpServletRequest request) {
        Integer quantity = Integer.valueOf(requestBody.get("quantity").toString());
        log.info("수량 업데이트 요청: cartItemId={}, quantity={}", cartItemId, quantity);
        
        Map<String, Object> response = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            log.warn("로그인이 필요합니다.");
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        log.info("로그인된 회원 ID: {}", memberId);
        
        try {
            boolean success = cartService.updateQuantity(memberId, cartItemId, quantity);
            log.info("수량 업데이트 결과: {}", success);
            
            if (success) {
                // 업데이트된 장바구니 아이템 정보 조회
                Optional<CartItem> updatedItem = cartService.getCartItemById(cartItemId);
                if (updatedItem.isPresent()) {
                    CartItem item = updatedItem.get();
                    response.put("success", true);
                    response.put("updatedQuantity", item.getQuantity());
                    response.put("updatedTotalPrice", item.getTotalPrice());
                    response.put("cartTotalAmount", cartService.getCartTotalAmount(memberId));
                    response.put("itemCount", cartService.getCartItemCount(memberId));
                } else {
                    response.put("success", false);
                    response.put("message", "업데이트된 아이템을 찾을 수 없습니다.");
                }
            } else {
                log.warn("수량 업데이트 실패: cartItemId={}, memberId={}", cartItemId, memberId);
                response.put("success", false);
                response.put("message", getMessage("cart.update.failed"));
            }
        } catch (IllegalArgumentException e) {
            log.error("수량 업데이트 중 IllegalArgumentException:", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("장바구니 수량 업데이트 실패", e);
            response.put("success", false);
            response.put("message", getMessage("cart.update.failed"));
        }
        
        return response;
    }
    
    /**
     * 장바구니 아이템 삭제
     */
    @PostMapping("/cart/remove/{cartItemId}")
    @ResponseBody
    public String removeFromCart(@PathVariable Long cartItemId,
                                HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "로그인이 필요합니다.";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            boolean success = cartService.removeFromCart(memberId, cartItemId);
            if (success) {
                return "success";
            } else {
                return getMessage("cart.remove.failed");
            }
        } catch (Exception e) {
            log.error("장바구니 아이템 삭제 실패", e);
            return getMessage("cart.remove.failed");
        }
    }
    
    /**
     * 장바구니 전체 비우기
     */
    @PostMapping("/cart/clear")
    @ResponseBody
    public String clearCart(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "로그인이 필요합니다.";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            boolean success = cartService.clearCart(memberId);
            if (success) {
                return "success";
            } else {
                return getMessage("cart.clear.failed");
            }
        } catch (Exception e) {
            log.error("장바구니 비우기 실패", e);
            return getMessage("cart.clear.failed");
        }
    }
    
    /**
     * 장바구니 아이템에 할인 적용
     */
    @PostMapping("/cart/discount/{cartItemId}")
    @ResponseBody
    public String applyDiscount(@PathVariable Long cartItemId,
                               @RequestParam Double discountRate,
                               HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "로그인이 필요합니다.";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            boolean success = cartService.applyDiscount(memberId, cartItemId, discountRate);
            if (success) {
                return "success";
            } else {
                return getMessage("cart.discount.failed");
            }
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("할인 적용 실패", e);
            return getMessage("cart.discount.failed");
        }
    }
    
    /**
     * 장바구니 아이템 개수 조회 (AJAX용)
     */
    @GetMapping("/cart/count")
    @ResponseBody
    public Map<String, Object> getCartCount(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            response.put("count", 0);
            response.put("loggedIn", false);
            return response;
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            int count = cartService.getCartItemCount(memberId);
            response.put("count", count);
            response.put("loggedIn", true);
        } catch (Exception e) {
            log.error("장바구니 개수 조회 실패", e);
            response.put("count", 0);
            response.put("loggedIn", true);
        }
        
        return response;
    }
    
    /**
     * 장바구니 주문 폼
     */
    @GetMapping("/cart/order")
    public String cartOrderForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        // 장바구니 아이템 조회 (DTO 포함)
        List<CartItemDTO> cartItems = cartService.getCartItemsWithProduct(memberId);
        if (cartItems == null || cartItems.isEmpty()) {
            log.warn("장바구니가 비어있음: memberId={}", memberId);
            return "redirect:/cart";
        }
        
        // 디버깅을 위한 로그
        log.info("장바구니 아이템 수: {}", cartItems.size());
        for (int i = 0; i < cartItems.size(); i++) {
            CartItemDTO item = cartItems.get(i);
            log.info("아이템 {}: cartItemId={}, productId={}, cart",
                    i, item.getCartItemId(), item.getProductId());
        }
        
        Long totalAmount = cartService.getCartTotalAmount(memberId);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartOrderForm", new CartOrderForm());
        
        return "cart/order";
    }
    
    /**
     * 장바구니에서 주문 생성
     */
    @PostMapping("/cart/order")
    public String createOrderFromCart(@Valid @ModelAttribute CartOrderForm cartOrderForm,
                                    BindingResult bindingResult,
                                    HttpServletRequest request,
                                    Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            Long memberId = loginMember.getMemberId();
            
            var cartItems = cartService.getCartItemsWithProduct(memberId);
            Long totalAmount = cartService.getCartTotalAmount(memberId);
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            return "cart/order";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            Order order = orderService.createOrderFromCart(
                memberId,
                cartOrderForm.getPaymentMethod(),
                cartOrderForm.getRecipientName(),
                cartOrderForm.getRecipientPhone(),
                cartOrderForm.getShippingAddress(),
                cartOrderForm.getShippingMemo()
            );
            
            Long orderId = order.getOrderId();
            
            return "redirect:/orders/" + orderId + "?success=" + 
                   getMessage("order.create.success", new Object[]{orderId});
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            
            var cartItems = cartService.getCartItemsWithProduct(memberId);
            Long totalAmount = cartService.getCartTotalAmount(memberId);
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            return "cart/order";
        } catch (Exception e) {
            log.error("장바구니 주문 생성 실패", e);
            model.addAttribute("errorMessage", getMessage("order.create.failed"));
            
            var cartItems = cartService.getCartItemsWithProduct(memberId);
            Long totalAmount = cartService.getCartTotalAmount(memberId);
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            return "cart/order";
        }
    }
} 