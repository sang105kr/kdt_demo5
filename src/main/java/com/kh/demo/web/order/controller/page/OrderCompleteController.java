package com.kh.demo.web.order.controller.page;

import com.kh.demo.domain.order.svc.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class OrderCompleteController {
    private final OrderService orderService;

    @GetMapping("/order/complete")
    public String orderComplete(@RequestParam Long orderId, Model model) {
        var orderOpt = orderService.findDTOByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", "주문 정보를 찾을 수 없습니다.");
            return "redirect:/member/mypage/orders";
        }
        model.addAttribute("order", orderOpt.get());
        return "order/complete";
    }
} 