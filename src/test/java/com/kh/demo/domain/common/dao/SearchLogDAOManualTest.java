package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.SearchLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SearchLogDAO 수동 테스트 클래스
 * 애플리케이션 시작 후 자동으로 기본 기능을 테스트합니다.
 * 프로덕션 환경에서는 실행되지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경이 아닐 때만 실행
public class SearchLogDAOManualTest {

    private final SearchLogDAO searchLogDAO;

    /**
     * 애플리케이션 시작 후 SearchLogDAO 기능 테스트
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runSearchLogTests() {
        log.info("🧪 SearchLogDAO 기능 테스트 시작");
        
        try {
            testBasicCRUD();
            testPopularKeywords();
            testSearchHistory();
            testStatistics();
            
            log.info("✅ SearchLogDAO 모든 테스트 완료!");
            
        } catch (Exception e) {
            log.error("❌ SearchLogDAO 테스트 실패", e);
        }
    }

    private void testBasicCRUD() {
        log.info("📝 기본 CRUD 테스트");
        
        // 검색 기록 저장
        Long savedId = searchLogDAO.saveSearchLog(999L, "테스트 키워드", "PRODUCT", 10, "127.0.0.1");
        log.info("검색 기록 저장 완료: ID = {}", savedId);
        
        // 저장된 기록 조회
        var searchLog = searchLogDAO.findById(savedId);
        if (searchLog.isPresent()) {
            log.info("검색 기록 조회 성공: {}", searchLog.get().getKeyword());
        } else {
            log.warn("검색 기록 조회 실패");
        }
        
        // 전체 개수 확인
        int totalCount = searchLogDAO.getTotalCount();
        log.info("전체 검색 기록 수: {}건", totalCount);
    }

    private void testPopularKeywords() {
        log.info("🔥 인기 검색어 테스트");
        
        // 샘플 검색 기록 생성
        String[] keywords = {"노트북", "스마트폰", "노트북", "태블릿", "노트북"};
        for (String keyword : keywords) {
            searchLogDAO.saveSearchLog(1L, keyword, "PRODUCT", null, null);
        }
        
        // 인기 검색어 조회
        List<String> popularKeywords = searchLogDAO.getPopularKeywordsFromOracle(5);
        log.info("인기 검색어: {}", popularKeywords);
        
        if (!popularKeywords.isEmpty() && "노트북".equals(popularKeywords.get(0))) {
            log.info("✅ 인기 검색어 정렬 정상");
        } else {
            log.warn("⚠️ 인기 검색어 정렬 확인 필요");
        }
    }

    private void testSearchHistory() {
        log.info("📚 검색 히스토리 테스트");
        
        Long testMemberId = 123L;
        String[] userKeywords = {"개인검색1", "개인검색2", "개인검색3"};
        
        // 개인 검색 기록 생성
        for (String keyword : userKeywords) {
            searchLogDAO.saveSearchLog(testMemberId, keyword, "PRODUCT", null, null);
        }
        
        // 개인 검색 히스토리 조회
        List<String> history = searchLogDAO.getMemberSearchHistory(testMemberId, 5);
        log.info("회원 {}의 검색 히스토리: {}", testMemberId, history);
        
        if (history.size() == 3) {
            log.info("✅ 개인 검색 히스토리 정상");
        } else {
            log.warn("⚠️ 개인 검색 히스토리 확인 필요: 예상 3건, 실제 {}건", history.size());
        }
    }

    private void testStatistics() {
        log.info("📊 통계 기능 테스트");
        
        // 최근 7일 검색 통계
        int recentCount = searchLogDAO.getTotalSearchCountByDays(7);
        log.info("최근 7일 검색 건수: {}건", recentCount);
        
        // 타입별 통계
        int productCount = searchLogDAO.getSearchCountByType("PRODUCT", 7);
        int boardCount = searchLogDAO.getSearchCountByType("BOARD", 7);
        
        log.info("최근 7일 상품 검색: {}건", productCount);
        log.info("최근 7일 게시판 검색: {}건", boardCount);
        
        if (recentCount > 0) {
            log.info("✅ 검색 통계 기능 정상");
        } else {
            log.warn("⚠️ 검색 통계 확인 필요");
        }
    }

    /**
     * 수동으로 배치 기능 테스트
     */
    public void testBatchFunctions() {
        log.info("🔄 배치 기능 테스트");
        
        // 오래된 로그 정리 (테스트용으로 1일)
        int deletedCount = searchLogDAO.deleteOldSearchLogs(1);
        log.info("오래된 로그 정리: {}건 삭제", deletedCount);
        
        // 중복 기록 정리
        int mergedCount = searchLogDAO.mergeDuplicateSearchLogs();
        log.info("중복 기록 정리: {}건 처리", mergedCount);
    }
} 