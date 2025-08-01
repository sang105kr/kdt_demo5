package com.kh.demo.web.cart.controller.page;

import com.kh.demo.domain.cart.dto.CartItemDTO;
import com.kh.demo.domain.cart.svc.CartService;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.product.dao.ProductDAO;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.cart.controller.page.form.CartOrderForm;
import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController extends BaseController {
    
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductDAO productDAO;
    private final MessageSource messageSource;
    private final CodeSVC codeSVC;

    @ModelAttribute("paymentMethodCodes")
    public List<Code> paymentMethodCodes() {
        return codeSVC.getCodeList("PAYMENT_METHOD");
    }
    
    /**
     * 장바구니 목록 페이지
     */
    @GetMapping("")
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
     * 장바구니 주문 폼
     */
    @GetMapping("/order")
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
    @PostMapping("/order")
    public String createOrderFromCart(@Valid @ModelAttribute CartOrderForm cartOrderForm,
                                    BindingResult bindingResult,
                                    HttpServletRequest request,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
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
            // 주문상태/결제상태 code_id 동적 조회
            Long orderStatusId = codeSVC.getCodeId("ORDER_STATUS", "PENDING");
            Long paymentStatusId = codeSVC.getCodeId("PAYMENT_STATUS", "PENDING");
            Order order = orderService.createOrderFromCart(
                memberId,
                cartOrderForm.getPaymentMethodId(),
                orderStatusId,
                paymentStatusId,
                cartOrderForm.getRecipientName(),
                cartOrderForm.getRecipientPhone(),
                cartOrderForm.getShippingAddress(),
                cartOrderForm.getShippingMemo()
            );
            Long orderId = order.getOrderId();
            redirectAttributes.addFlashAttribute("successMessage", 
                getMessage("order.create.success", new Object[]{orderId}));
            return "redirect:/order/complete?orderId=" + orderId;
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