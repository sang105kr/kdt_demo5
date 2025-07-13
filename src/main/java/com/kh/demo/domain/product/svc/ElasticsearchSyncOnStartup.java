package com.kh.demo.domain.product.svc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
/**
 * 서버 구동 시 Oracle → Elasticsearch 전체 동기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchSyncOnStartup {
    private final ProductService productService;

    // 서버가 완전히 구동된 후(ApplicationReadyEvent 발생 시) 아래 메서드 실행
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        log.info("[ElasticsearchSyncOnStartup] 서버 구동 시 Oracle → Elasticsearch 전체 동기화 시작");
        productService.syncAllToElasticsearch();
        log.info("[ElasticsearchSyncOnStartup] 서버 구동 시 Oracle → Elasticsearch 전체 동기화 완료");
    }
} 