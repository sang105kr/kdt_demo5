package com.kh.demo.web.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 채팅 페이지 뷰 Controller
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatViewController {

    /**
     * 고객용 채팅 페이지
     */
    @GetMapping("/chat/customer")
    public String customerChat() {
        return "chat/customer";
    }

    /**
     * 관리자용 채팅 대시보드 (새 URL)
     */
    @GetMapping("/admin/chat/dashboard")
    public String adminChatDashboard() {
        return "admin/chat/dashboard";
    }

    /**
     * 관리자용 채팅 대시보드 (이전 URL - 리다이렉트)
     */
    @GetMapping("/chat/admin")
    public String adminChatDashboardRedirect() {
        return "redirect:/admin/chat/dashboard";
    }

    /**
     * 관리자용 채팅 히스토리
     */
    @GetMapping("/admin/chat/history")
    public String adminChatHistory() {
        return "admin/chat/history";
    }

    /**
     * 특정 채팅 세션 페이지 (관리자용)
     */
    @GetMapping("/admin/chat/session/{sessionId}")
    public String adminChatSession(@PathVariable String sessionId, Model model) {
        log.info("관리자 채팅 세션 페이지 요청: sessionId={}", sessionId);
        model.addAttribute("sessionId", sessionId);
        return "admin/chat/session";
    }

    /**
     * 상담 히스토리 상세보기 페이지 (관리자용)
     */
    @GetMapping("/admin/chat/history/{sessionId}")
    public String adminChatHistoryDetail(@PathVariable String sessionId, Model model) {
        log.info("관리자 채팅 히스토리 상세보기 페이지 요청: sessionId={}", sessionId);
        model.addAttribute("sessionId", sessionId);
        return "admin/chat/historyDetail";
    }
}
