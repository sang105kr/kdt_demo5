package com.kh.demo.admin.chat.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자용 채팅 페이지 뷰 Controller
 */
@Slf4j
@Controller
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
public class AdminChatViewController {

    /**
     * 관리자용 채팅 대시보드 (새 URL)
     */
    @GetMapping("/dashboard")
    public String adminChatDashboard() {
        return "admin/chat/dashboard";
    }

    /**
     * 관리자용 채팅 히스토리
     */
    @GetMapping("/history")
    public String adminChatHistory() {
        return "admin/chat/history";
    }

    /**
     * 새로운 상담 시작 페이지 (관리자용)
     */
    @GetMapping("/session/new")
    public String adminNewChatSession() {
        log.info("관리자 새 상담 시작 페이지 요청");
        return "admin/chat/session";
    }

    /**
     * 특정 채팅 세션 페이지 (관리자용)
     */
    @GetMapping("/session/{sessionId}")
    public String adminChatSession(@PathVariable String sessionId, Model model) {
        log.info("관리자 채팅 세션 페이지 요청: sessionId={}", sessionId);
        model.addAttribute("sessionId", sessionId);
        return "admin/chat/session";
    }

    /**
     * 상담 히스토리 상세보기 페이지 (관리자용)
     */
    @GetMapping("/history/{sessionId}")
    public String adminChatHistoryDetail(@PathVariable String sessionId, Model model) {
        log.info("관리자 채팅 히스토리 상세보기 페이지 요청: sessionId={}", sessionId);
        model.addAttribute("sessionId", sessionId);
        return "admin/chat/historyDetail";
    }
}
