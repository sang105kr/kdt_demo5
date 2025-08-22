package com.kh.demo.config;

import com.kh.demo.common.init.ElasticsearchSyncOnStartup;
import com.kh.demo.common.init.FileMigrationInitializer;
import com.kh.demo.domain.common.svc.UploadFileSVC;
import com.kh.demo.domain.product.svc.ProductService;
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
  private final ProductService productService;
  private final UploadFileSVC uploadFileSVC;
  /**
   * OpenAI ChatClient Bean
   * @param chatModel
   * @return
   */
  @Bean
  public ChatClient openAIChatClient(OpenAiChatModel chatModel) {
    return ChatClient.create(chatModel);
  }

  /**
   * WebClient Bean
   * @return
   */
  @Bean
  public WebClient webClient() {
    return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
            .build();
  }

  /**
   * Elasticsearch 동기화 초기화 Bean
   * 서버 구동 시 Oracle → Elasticsearch 데이터 동기화
   */
  @Bean
  public ElasticsearchSyncOnStartup elasticsearchSyncOnStartup(ProductService productService) {
    return new ElasticsearchSyncOnStartup(productService);
  }

  /**
   * 파일 마이그레이션 초기화 Bean 등록
   * 서버 구동 시 기존 파일 구조를 새로운 폴더 구조로 마이그레이션
   */
  @Bean
  public FileMigrationInitializer fileMigrationInitializer(com.kh.demo.domain.common.svc.UploadFileSVC uploadFileSVC) {
    return new FileMigrationInitializer(uploadFileSVC);
  }
  /**
   * 정적 리소스 경로 설정
   * @param registry
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 정적 리소스 경로 설정
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");
    
    // .well-known 경로 설정 (Chrome DevTools 등에서 사용)
    registry.addResourceHandler("/.well-known/**")
            .addResourceLocations("classpath:/static/.well-known/");
  }

  /**
   * 인터셉터 설정
   * @param registry
   */ 
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
