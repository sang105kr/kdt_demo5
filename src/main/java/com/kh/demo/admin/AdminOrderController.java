package com.kh.demo.admin;

import com.kh.demo.domain.order.entity.Order;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.web.page.form.login.LoginMember;
import com.kh.demo.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final MessageSource messageSource;

    /**
     * 주문 목록 조회 (관리자용)
     */
    @GetMapping
    public String orderList(@RequestParam(required = false) String orderStatus,
                           Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !"관리자1".equals(loginMember.getGubun())) {
            return "redirect:/login";
        }

        List<Order> orders;
        if (orderStatus != null && !orderStatus.isEmpty()) {
            orders = orderService.getOrdersByStatus(orderStatus);
            model.addAttribute("selectedStatus", orderStatus);
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        return "admin/order/list";
    }

    /**
     * 주문 상세 조회 (관리자용)
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, 
                            Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !"관리자1".equals(loginMember.getGubun())) {
            return "redirect:/login";
        }

        var orderOpt = orderService.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", messageSource.getMessage("order.not.found", null, null));
            return "admin/order/list";
        }

        model.addAttribute("order", orderOpt.get());
        return "admin/order/detail";
    }

    /**
     * 주문 상태 업데이트
     */
    @PostMapping("/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                  @RequestParam String orderStatus,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !"관리자1".equals(loginMember.getGubun())) {
            return "redirect:/login";
        }

        try {
            orderService.updateOrderStatus(orderId, orderStatus);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("order.status.update.success", null, null));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/orders/" + orderId;
    }

    /**
     * 결제 상태 업데이트
     */
    @PostMapping("/{orderId}/payment")
    public String updatePaymentStatus(@PathVariable Long orderId,
                                    @RequestParam String paymentStatus,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null || !"관리자1".equals(loginMember.getGubun())) {
            return "redirect:/login";
        }

        try {
            orderService.updatePaymentStatus(orderId, paymentStatus);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("order.payment.update.success", null, null));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/orders/" + orderId;
    }

    /**
     * 주문 취소 (관리자용)
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
        if (loginMember == null || !"관리자1".equals(loginMember.getGubun())) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("order.cancel.success", null, null));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/orders";
    }
} 