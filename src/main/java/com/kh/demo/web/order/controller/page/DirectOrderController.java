package com.kh.demo.web.order.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.domain.product.entity.Products;
import com.kh.demo.domain.product.svc.ProductService;
import com.kh.demo.web.common.controller.page.BaseController;
import com.kh.demo.web.order.controller.page.form.OrderForm;
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

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class DirectOrderController extends BaseController {

    private final OrderService orderService;
    private final ProductService productService;
    private final MessageSource messageSource;
    private final CodeSVC codeSVC;

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
            Long orderStatusId = codeSVC.getCodeId("ORDER_STATUS", "PENDING");
            Long paymentStatusId = codeSVC.getCodeId("PAYMENT_STATUS", "PENDING");
            Order order = orderService.createDirectOrder(
                loginMember.getMemberId(),
                productId,
                quantity,
                orderForm.getPaymentMethodId(),
                orderStatusId,
                paymentStatusId,
                orderForm.getRecipientName(),
                orderForm.getRecipientPhone(),
                orderForm.getShippingAddress(),
                orderForm.getShippingMemo()
            );

            redirectAttributes.addFlashAttribute("successMessage", 
                getMessage("order.create.success", new Object[]{order.getOrderNumber()}));
            return "redirect:/order/complete?orderId=" + order.getOrderId();

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
} 