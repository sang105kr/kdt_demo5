package com.kh.demo.web.page;

import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.web.page.form.order.OrderForm;
import com.kh.demo.web.page.form.login.LoginMember;
import com.kh.demo.web.session.SessionConst;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController extends BaseController {

    private final OrderService orderService;
    private final ProductService productService;
    private final MessageSource messageSource;

    /**
     * 주문 목록 조회 (회원용)
     */
    @GetMapping
    public String orderList(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.findOrdersByMemberId(loginMember.getMemberId());
        model.addAttribute("orders", orders);

        return "order/list";
    }

    /**
     * 주문 상세 조회 (회원용)
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", getMessage("order.not.found"));
            return "order/list";
        }

        Order order = orderOpt.get();
        
        // 본인 주문인지 확인
        if (!order.getMemberId().equals(loginMember.getMemberId())) {
            model.addAttribute("errorMessage", getMessage("order.access.denied"));
            return "order/list";
        }

        model.addAttribute("order", order);
        return "order/detail";
    }

    /**
     * 단일 상품 바로 주문 폼
     */
    @GetMapping("/direct/{productId}")
    public String directOrderForm(@PathVariable Long productId, 
                                @RequestParam(defaultValue = "1") Integer quantity,
                                HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Optional<Products> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            model.addAttribute("errorMessage", getMessage("product.not.found"));
            return "redirect:/products";
        }

        Products product = productOpt.get();
        
        // 재고 확인
        if (product.getStockQuantity() < quantity) {
            model.addAttribute("errorMessage", getMessage("product.stock.insufficient"));
            return "redirect:/products/" + productId;
        }

        model.addAttribute("product", product);
        model.addAttribute("quantity", quantity);
        model.addAttribute("orderForm", new OrderForm());
        
        return "order/direct-order";
    }

    /**
     * 단일 상품 바로 주문 처리
     */
    @PostMapping("/direct/{productId}")
    public String directOrder(@PathVariable Long productId,
                            @RequestParam Integer quantity,
                            @Valid @ModelAttribute OrderForm orderForm,
                            BindingResult bindingResult,
                            HttpServletRequest request,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            Optional<Products> productOpt = productService.findById(productId);
            if (productOpt.isPresent()) {
                model.addAttribute("product", productOpt.get());
                model.addAttribute("quantity", quantity);
                return "order/direct-order";
            }
            return "redirect:/products";
        }

        try {
            Order order = orderService.createDirectOrder(
                loginMember.getMemberId(),
                productId,
                quantity,
                orderForm.getPaymentMethod(),
                orderForm.getRecipientName(),
                orderForm.getRecipientPhone(),
                orderForm.getShippingAddress(),
                orderForm.getShippingMemo()
            );

            redirectAttributes.addFlashAttribute("successMessage", 
                getMessage("order.create.success", new Object[]{order.getOrderNumber()}));
            return "redirect:/orders/" + order.getOrderId();

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            Optional<Products> productOpt = productService.findById(productId);
            if (productOpt.isPresent()) {
                model.addAttribute("product", productOpt.get());
                model.addAttribute("quantity", quantity);
            }
            return "order/direct-order";
        }
    }

    /**
     * 장바구니에서 주문 폼
     */
    @GetMapping("/cart")
    public String cartOrderForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 장바구니 상품 조회는 CartController에서 처리
        model.addAttribute("orderForm", new OrderForm());
        return "order/cart-order";
    }

    /**
     * 장바구니에서 주문 처리
     */
    @PostMapping("/cart")
    public String cartOrder(@Valid @ModelAttribute OrderForm orderForm,
                           BindingResult bindingResult,
                           HttpServletRequest request,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "order/cart-order";
        }

        try {
            Order order = orderService.createOrderFromCart(
                loginMember.getMemberId(),
                orderForm.getPaymentMethod(),
                orderForm.getRecipientName(),
                orderForm.getRecipientPhone(),
                orderForm.getShippingAddress(),
                orderForm.getShippingMemo()
            );

            redirectAttributes.addFlashAttribute("successMessage", 
                getMessage("order.create.success", new Object[]{order.getOrderNumber()}));
            return "redirect:/orders/" + order.getOrderId();

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "order/cart-order";
        }
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("order.cancel.success", null, null));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/orders";
    }
} 