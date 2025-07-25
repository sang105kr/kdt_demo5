package com.kh.demo.web.common.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OpenAIChatController {
  private final ChatClient chatClient;

  @GetMapping("/openAIChat")
  public String chat() {
    return chatClient.prompt()
        .user("OpenAI에 대한 소개글을 작성해줘")
        .call()
        .content();
  }
}