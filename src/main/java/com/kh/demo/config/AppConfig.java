package com.kh.demo.config;

import com.kh.demo.web.common.interceptor.AuthInterceptor;
import com.kh.demo.web.common.interceptor.ExecutionTimeInterceptor;
import com.kh.demo.web.common.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {

  private final LoginCheckInterceptor loginCheckInterceptor;
  private final ExecutionTimeInterceptor executionTimeInterceptor;
  private final AuthInterceptor authInterceptor;

  @Bean
  public ChatClient openAIChatClient(OpenAiChatModel chatModel) {
    return ChatClient.create(chatModel);
  }

  @Bean
  public WebClient webClient() {
    return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
            .build();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 정적 리소스 경로 설정
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");
    
    // .well-known 경로 설정 (Chrome DevTools 등에서 사용)
    registry.addResourceHandler("/.well-known/**")
            .addResourceLocations("classpath:/static/.well-known/");
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
        .order(3)
        .addPathPatterns(
            "/member/**",           //회원 관련 페이지
            "/cart/**",             //장바구니
            "/order/**",            //주문
            "/payment/**",          //결제
            "/admin/**",            //관리자 페이지
            "/api/chat/**"          //채팅 API
        )
        .excludePathPatterns(
            "/member/join",             //회원 가입
            "/member/email/verify",     //이메일 검증
            "/member/id/**",          //아이디찾기
            "/member/password/**"     //비밀번호찾기
        );
    registry.addInterceptor(authInterceptor)
        .order(1)
        .addPathPatterns("/**");
//    registry.addInterceptor(executionTimeInterceptor)
//        .order(2)
//        .addPathPatterns("/**");
  }
}
