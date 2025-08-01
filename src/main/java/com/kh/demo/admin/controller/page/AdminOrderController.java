package com.kh.demo.admin.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.dto.Pagination;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.order.dto.OrderDTO;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.web.common.controller.page.BaseController;
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
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController extends BaseController {

    private final OrderService orderService;
    private final CodeSVC codeSVC;
    private final MessageSource messageSource;
    
    private static final int PAGE_SIZE = 10; // 페이지당 주문 수

    @ModelAttribute("orderStatusCodes")
    public List<Code> orderStatusCodes() {
        return codeSVC.getCodeList("ORDER_STATUS");
    }
    @ModelAttribute("paymentStatusCodes")
    public List<Code> paymentStatusCodes() {
        return codeSVC.getCodeList("PAYMENT_STATUS");
    }
    @ModelAttribute("paymentMethodCodes")
    public List<Code> paymentMethodCodes() {
        return codeSVC.getCodeList("PAYMENT_METHOD");
    }
    @ModelAttribute("statusMap")
    public Map<Long, String> statusMap() {
        return codeSVC.getCodeDecodeMap("ORDER_STATUS");
    }
    @ModelAttribute("paymentStatusMap")
    public Map<Long, String> paymentStatusMap() {
        return codeSVC.getCodeDecodeMap("PAYMENT_STATUS");
    }
    @ModelAttribute("paymentMethodMap")
    public Map<Long, String> paymentMethodMap() {
        return codeSVC.getCodeDecodeMap("PAYMENT_METHOD");
    }

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
    public String orderList(@RequestParam(required = false) Long orderStatusId,
                           @RequestParam(defaultValue = "1", name = "pageNo") int pageNo,
                           Model model, HttpServletRequest request, HttpSession session) {
        if (!isAdmin(request)) {
            return "redirect:/login";
        }

        List<OrderDTO> orders;
        int totalCount;
        Pagination pagination;
        
        if (orderStatusId != null) {
            // 주문 상태별 조회
            totalCount = orderService.countOrdersByStatus(orderStatusId);
            pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
            orders = orderService.findDTOByOrderStatusWithPaging(orderStatusId, pageNo, PAGE_SIZE);
            model.addAttribute("selectedStatus", orderStatusId);
        } else {
            // 전체 조회
            totalCount = orderService.countAllOrders();
            pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
            orders = orderService.findAllOrderDTOsWithPaging(pageNo, PAGE_SIZE);
        }

        log.info("관리자 주문 목록 조회 - 조회된 주문 개수: {}, 필터: {}, 페이지: {}/{}", 
                orders.size(), orderStatusId, pageNo, pagination.getTotalPages());
        
        model.addAttribute("orders", orders);
        model.addAttribute("pagination", pagination);
        addAuthInfoToModel(model, session);
        return "admin/order/list";
    }

    /**
     * 처리 대기 주문 목록 조회 (관리자용)
     */
    @GetMapping("/pending")
    public String pendingOrders(@RequestParam(defaultValue = "1", name = "pageNo") int pageNo,
                              Model model, HttpServletRequest request, HttpSession session) {
        if (!isAdmin(request)) {
            return "redirect:/login";
        }

        // PENDING 상태의 주문만 조회
        Long pendingStatusId = codeSVC.getCodeId("ORDER_STATUS", "PENDING");
        int totalCount = orderService.countOrdersByStatus(pendingStatusId);
        Pagination pagination = new Pagination(pageNo, PAGE_SIZE, totalCount);
        List<OrderDTO> orders = orderService.findDTOByOrderStatusWithPaging(pendingStatusId, pageNo, PAGE_SIZE);

        log.info("관리자 처리 대기 주문 조회 - 조회된 주문 개수: {}, 페이지: {}/{}", 
                orders.size(), pageNo, pagination.getTotalPages());
        
        model.addAttribute("orders", orders);
        model.addAttribute("pagination", pagination);
        model.addAttribute("selectedStatus", pendingStatusId);
        model.addAttribute("title", "처리 대기 주문");
        addAuthInfoToModel(model, session);
        return "admin/order/list";
    }

    /**
     * 주문 상세 조회 (관리자용)
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, 
                            Model model, HttpServletRequest request, HttpSession session) {
        if (!isAdmin(request)) {
            return "redirect:/login";
        }

        Optional<OrderDTO> orderOpt = orderService.findDTOByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", messageSource.getMessage("order.not.found", null, null));
            addAuthInfoToModel(model, session);
            return "admin/order/list";
        }

        log.info("관리자 주문 상세 조회 - orderId: {}, orderNumber: {}", 
                orderId, orderOpt.get().getOrderNumber());
        model.addAttribute("order", orderOpt.get());
        addAuthInfoToModel(model, session);
        return "admin/order/detail";
    }

    /**
     * 주문 상태 업데이트
     */
    @PostMapping("/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                  @RequestParam("orderStatusId") Long orderStatusId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        if (!isAdmin(request)) {
            return "redirect:/login";
        }

        try {
            orderService.updateOrderStatus(orderId, orderStatusId);
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
                                    @RequestParam("paymentStatusId") Long paymentStatusId,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        if (!isAdmin(request)) {
            return "redirect:/login";
        }

        try {
            orderService.updatePaymentStatus(orderId, paymentStatusId);
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