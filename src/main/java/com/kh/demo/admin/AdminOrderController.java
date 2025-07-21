package com.kh.demo.admin;

import com.kh.demo.domain.order.dto.OrderDTO;
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
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final MessageSource messageSource;

    /**
     * 관리자 권한 체크
     */
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember != null && (loginMember.getGubun() == 4 || loginMember.getGubun() == 5);
    }

    /**
     * 주문 목록 조회 (관리자용)
     */
    @GetMapping
    public String orderList(@RequestParam(required = false) String orderStatus,
                           Model model, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return "redirect:/login";
        }

        List<OrderDTO> orders;
        if (orderStatus != null && !orderStatus.isEmpty()) {
            orders = orderService.findAllOrderDTOs()
                .stream()
                .filter(order -> orderStatus.equals(order.getOrderStatus()))
                .toList();
            model.addAttribute("selectedStatus", orderStatus);
        } else {
            orders = orderService.findAllOrderDTOs();
        }

        log.info("관리자 주문 목록 조회 - 조회된 주문 개수: {}, 필터: {}", orders.size(), orderStatus);
        model.addAttribute("orders", orders);
        return "admin/order/list";
    }

    /**
     * 주문 상세 조회 (관리자용)
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, 
                            Model model, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return "redirect:/login";
        }

        Optional<OrderDTO> orderOpt = orderService.findDTOByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", messageSource.getMessage("order.not.found", null, null));
            return "admin/order/list";
        }

        log.info("관리자 주문 상세 조회 - orderId: {}, orderNumber: {}", 
                orderId, orderOpt.get().getOrderNumber());
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
        if (!isAdmin(request)) {
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
        if (!isAdmin(request)) {
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
        if (!isAdmin(request)) {
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