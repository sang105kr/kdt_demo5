package com.kh.demo.web.page;

import com.kh.demo.domain.cart.svc.CartService;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.domain.product.entity.Products;
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
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CartController {
    
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
        
        // 장바구니 아이템 조회
        var cartItems = cartService.getCartItems(memberId);
        int itemCount = cartService.getCartItemCount(memberId);
        Long totalAmount = cartService.getCartTotalAmount(memberId);
        
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
            // 상품 존재 여부 확인
            Optional<Products> productOpt = productDAO.findById(productId);
            if (productOpt.isEmpty()) {
                return messageSource.getMessage("cart.product.not.found", null, null);
            }
            
            // 장바구니에 추가
            cartService.addToCart(memberId, productId, quantity);
            
            return "success";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("장바구니 추가 실패", e);
            return messageSource.getMessage("cart.add.failed", null, null);
        }
    }
    
    /**
     * 장바구니 아이템 수량 업데이트
     */
    @PostMapping("/cart/update/{cartItemId}")
    @ResponseBody
    public String updateQuantity(@PathVariable Long cartItemId,
                                @RequestParam Integer quantity,
                                HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "로그인이 필요합니다.";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        try {
            boolean success = cartService.updateQuantity(memberId, cartItemId, quantity);
            if (success) {
                return "success";
            } else {
                return messageSource.getMessage("cart.update.failed", null, null);
            }
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("장바구니 수량 업데이트 실패", e);
            return messageSource.getMessage("cart.update.failed", null, null);
        }
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
                return messageSource.getMessage("cart.remove.failed", null, null);
            }
        } catch (Exception e) {
            log.error("장바구니 아이템 삭제 실패", e);
            return messageSource.getMessage("cart.remove.failed", null, null);
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
                return messageSource.getMessage("cart.clear.failed", null, null);
            }
        } catch (Exception e) {
            log.error("장바구니 비우기 실패", e);
            return messageSource.getMessage("cart.clear.failed", null, null);
        }
    }
    
    /**
     * 장바구니에서 주문하기 페이지
     */
    @GetMapping("/cart/order")
    public String cartOrderForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getMemberId();
        
        // 장바구니 아이템 조회
        var cartItems = cartService.getCartItems(memberId);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
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
            
            var cartItems = cartService.getCartItems(memberId);
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
                   messageSource.getMessage("order.create.success", new Object[]{orderId}, null);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            
            var cartItems = cartService.getCartItems(memberId);
            Long totalAmount = cartService.getCartTotalAmount(memberId);
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            return "cart/order";
        } catch (Exception e) {
            log.error("장바구니 주문 생성 실패", e);
            model.addAttribute("errorMessage", messageSource.getMessage("order.create.failed", null, null));
            
            var cartItems = cartService.getCartItems(memberId);
            Long totalAmount = cartService.getCartTotalAmount(memberId);
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            return "cart/order";
        }
    }
} 