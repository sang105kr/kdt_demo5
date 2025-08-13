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
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatViewController {

    /**
     * 고객용 채팅 페이지
     */
    @GetMapping("/customer")
    public String customerChat() {
        return "chat/customer";
    }

    /**
     * 관리자용 채팅 대시보드
     */
    @GetMapping("/admin")
    public String adminChatDashboard() {
        return "admin/chat/dashboard";
    }

    /**
     * 특정 채팅 세션 페이지 (관리자용)
     */
    @GetMapping("/admin/session/{sessionId}")
    public String adminChatSession(@PathVariable String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "admin/chat/session";
    }
}
