package com.kh.demo.web.member.controller.page;

import com.kh.demo.common.session.LoginMember;
import com.kh.demo.common.session.SessionConst;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.order.dto.OrderDTO;
import com.kh.demo.domain.order.dto.OrderItemDTO;
import com.kh.demo.domain.order.svc.OrderService;
import com.kh.demo.web.common.controller.page.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 회원 주문 관리 컨트롤러
 * - 주문 내역 조회
 * - 주문 상세 조회
 * - 주문 취소
 */
@Slf4j
@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberOrderController extends BaseController {

    private final OrderService orderService;
    private final CodeSVC codeSVC;

    @ModelAttribute("orderStatusCodes")
    public List<Code> orderStatusCodes() {
        return codeSVC.getCodeList("ORDER_STATUS");
    }

    @ModelAttribute("statusMap")
    public Map<Long, String> statusMap() {
        return codeSVC.getCodeDecodeMap("ORDER_STATUS");
    }

    @ModelAttribute("paymentMethodMap")
    public Map<Long, String> paymentMethodMap() {
        return codeSVC.getCodeDecodeMap("PAYMENT_METHOD");
    }

    @ModelAttribute("paymentStatusMap")
    public Map<Long, String> paymentStatusMap() {
        return codeSVC.getCodeDecodeMap("PAYMENT_STATUS");
    }

    /**
     * 주문 내역 조회
     */
    @GetMapping("/mypage/orders")
    public String orderHistory(@RequestParam(required = false) Long orderStatusId,
                             HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<OrderDTO> orders;
        if (orderStatusId != null) {
            orders = orderService.findDTOByMemberIdAndStatus(loginMember.getMemberId(), orderStatusId);
        } else {
            orders = orderService.findDTOByMemberId(loginMember.getMemberId());
        }

        Long deliveredCodeId = codeSVC.getCodeId("ORDER_STATUS", "DELIVERED");
        Long pendingCodeId = codeSVC.getCodeId("ORDER_STATUS", "PENDING");
        Long confirmedCodeId = codeSVC.getCodeId("ORDER_STATUS", "CONFIRMED");
        Long shippedCodeId = codeSVC.getCodeId("ORDER_STATUS", "SHIPPED");
        Long cancelledCodeId = codeSVC.getCodeId("ORDER_STATUS", "CANCELLED");
        log.info("DELIVERED_CODE_ID: {} / PENDING: {} / CONFIRMED: {} / SHIPPED: {} / CANCELLED: {}", deliveredCodeId, pendingCodeId, confirmedCodeId, shippedCodeId, cancelledCodeId);
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", orderStatusId);
        model.addAttribute("orderStatusCodes", codeSVC.getCodeList("ORDER_STATUS"));
        model.addAttribute("DELIVERED_CODE_ID", deliveredCodeId);
        model.addAttribute("PENDING_CODE_ID", pendingCodeId);
        model.addAttribute("CONFIRMED_CODE_ID", confirmedCodeId);
        model.addAttribute("SHIPPED_CODE_ID", shippedCodeId);
        model.addAttribute("CANCELLED_CODE_ID", cancelledCodeId);
        
        return "member/order/orderHistory";
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/mypage/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 주문 조회 및 권한 확인
        Optional<OrderDTO> orderOpt = orderService.findDTOByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/member/mypage/orders";
        }
        
        OrderDTO order = orderOpt.get();
        if (!order.getMemberId().equals(loginMember.getMemberId())) {
            return "redirect:/member/mypage/orders";
        }

        // 주문 아이템 조회
        List<OrderItemDTO> orderItems = orderService.getOrderItemDTOs(orderId);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        
        return "member/order/orderDetail";
    }

    /**
     * 주문 취소
     */
    @PostMapping("/mypage/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId, HttpSession session, 
                            RedirectAttributes redirectAttributes) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            // 주문 조회 및 권한 확인
            Optional<OrderDTO> orderOpt = orderService.findDTOByOrderId(orderId);
            if (orderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "주문을 찾을 수 없습니다.");
                return "redirect:/member/mypage/orders";
            }
            
            OrderDTO order = orderOpt.get();
            if (!order.getMemberId().equals(loginMember.getMemberId())) {
                redirectAttributes.addFlashAttribute("error", "주문을 찾을 수 없습니다.");
                return "redirect:/member/mypage/orders";
            }

            // 주문 취소 처리
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("message", "주문이 성공적으로 취소되었습니다.");
        } catch (Exception e) {
            log.error("주문 취소 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "주문 취소 중 오류가 발생했습니다.");
        }

        return "redirect:/member/mypage/orders";
    }
} 