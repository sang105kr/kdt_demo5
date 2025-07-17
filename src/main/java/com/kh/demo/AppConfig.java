package com.kh.demo;

import com.kh.demo.web.interceptor.ExecutionTimeInterceptor;
import com.kh.demo.web.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {

  private final LoginCheckInterceptor loginCheckInterceptor;
  private final ExecutionTimeInterceptor executionTimeInterceptor;

  @Bean
  public ChatClient openAIChatClient(OpenAiChatModel chatModel) {
    return ChatClient.create(chatModel);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {

//  case1) 블랙리스트 전략
//    registry.addInterceptor(loginCheckInterceptor)
//        .order(2)
//        .addPathPatterns("/**")   //루트부터 하위경로 모두 인터셉터 대상으로 포함.
//        .excludePathPatterns(
//            "/",                  //초기화면
//            "/login",             //로그인화면
//            "/logout",            //로그아웃
//            "/members/join",      //회원가입화면
//            "/bbs/**",             //게시판
//            "/test/**"            //테스트
//        );
// case2) 화이트리스트 전략
    registry.addInterceptor(loginCheckInterceptor)
        .order(2)
        .excludePathPatterns("/**")   //루트부터 하위경로 모두 인터셉터 대상으로 미포함.
        .addPathPatterns(
            "/member/**",           //회원 관련 페이지
            "/cart/**",             //장바구니
            "/order/**",            //주문
            "/payment/**",          //결제
            "/admin/**"             //관리자 페이지
        );
//    registry.addInterceptor(executionTimeInterceptor)
//        .order(1);
  }
}
