package com.kh.demo.web.chat.controller.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 고객용 채팅 페이지 뷰 Controller
 */
@Slf4j
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatViewController {

    /**
     * 고객용 채팅 페이지 (카테고리 선택)
     */
    @GetMapping("/customer")
    public String customerChat() {
        return "chat/customer";
    }

    /**
     * 팝업 채팅 페이지
     */
    @GetMapping("/popup")
    public String popupChat() {
        return "chat/popup";
    }
}
