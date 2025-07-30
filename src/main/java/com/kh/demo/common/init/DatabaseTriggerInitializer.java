package com.kh.demo.common.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

/**
 * 데이터베이스 트리거 초기화 컴포넌트
 * 애플리케이션 시작 시 트리거를 자동으로 생성합니다.
 */
@Slf4j
//@Component
@RequiredArgsConstructor
public class DatabaseTriggerInitializer {
    
    private final JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void initializeTriggers() {
        log.info("데이터베이스 트리거 초기화 시작");
        
        try {
            // 코드 경로 및 레벨 자동 관리 트리거
            createCodePathLevelTrigger();
            
            // 상위 코드 경로 변경 시 하위 코드 경로 자동 업데이트 트리거
            createCodePathCascadeTrigger();
            
            log.info("데이터베이스 트리거 초기화 완료");
        } catch (Exception e) {
            log.error("트리거 초기화 중 오류 발생", e);
            throw new RuntimeException("트리거 초기화 실패", e);
        }
    }
    
    private void createCodePathLevelTrigger() throws Exception {
        String triggerSql = loadSqlFile("sql/trg_code_path_level.sql");
        log.info("코드 경로 레벨 트리거 생성");
        jdbcTemplate.execute(triggerSql);
    }
    
    private void createCodePathCascadeTrigger() throws Exception {
        String triggerSql = loadSqlFile("sql/trg_code_path_cascade.sql");
        log.info("코드 경로 캐스케이드 트리거 생성");
        jdbcTemplate.execute(triggerSql);
    }
    
    private String loadSqlFile(String filePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(filePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
} 