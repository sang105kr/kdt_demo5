package com.kh.demo.common.batch;

import com.kh.demo.domain.common.dao.SearchLogDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 검색 로그 관련 배치 처리 서비스
 * 오래된 데이터 정리, 중복 기록 정리 등을 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchBatchService {
    
    private final SearchLogDAO searchLogDAO;
    
    /**
     * 매일 새벽 2시 - 오래된 검색 로그 정리
     * 30일 이상 된 검색 기록을 삭제하여 저장공간 확보
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldSearchLogs() {
        log.info("=== 검색 로그 정리 배치 시작 ===");
        
        try {
            int deletedCount = searchLogDAO.deleteOldSearchLogs(30);
            log.info("오래된 검색 로그 {}건 삭제 완료 (30일 이상)", deletedCount);
            
            if (deletedCount > 1000) {
                log.warn("대량 삭제 발생: {}건 - 성능 모니터링 필요", deletedCount);
            }
            
        } catch (Exception e) {
            log.error("검색 로그 정리 실패", e);
            // 알림 발송 등 후처리 가능
        }
        
        log.info("=== 검색 로그 정리 배치 종료 ===");
    }
    
    /**
     * 매 시간 정각 - 중복 검색 기록 정리
     * 같은 사용자의 같은 키워드 중복 기록을 정리하여 성능 향상
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void deduplicateSearchLogs() {
        log.debug("=== 중복 검색 기록 정리 시작 ===");
        
        try {
            int mergedCount = searchLogDAO.mergeDuplicateSearchLogs();
            
            if (mergedCount > 0) {
                log.info("중복 검색 기록 {}건 정리 완료", mergedCount);
            } else {
                log.debug("정리할 중복 검색 기록 없음");
            }
            
        } catch (Exception e) {
            log.error("중복 기록 정리 실패", e);
        }
        
        log.debug("=== 중복 검색 기록 정리 종료 ===");
    }
    
    /**
     * 매주 일요일 새벽 3시 - 검색 통계 분석 및 리포트
     * 주간 검색 트렌드 분석하여 인사이트 제공
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void generateWeeklySearchReport() {
        log.info("=== 주간 검색 통계 생성 시작 ===");
        
        try {
            // 전체 검색 건수
            int totalCount = searchLogDAO.getTotalCount();
            
            // 최근 7일간 인기 검색어
            var popularKeywords = searchLogDAO.getPopularKeywordsFromOracle(7, 10);
            
            // 주간 리포트 생성
            log.info("📊 주간 검색 통계 리포트");
            log.info("총 검색 기록: {}건", totalCount);
            log.info("인기 검색어 TOP 10: {}", popularKeywords);
            
            // 향후: 이메일 발송, 대시보드 업데이트 등
            
        } catch (Exception e) {
            log.error("주간 검색 통계 생성 실패", e);
        }
        
        log.info("=== 주간 검색 통계 생성 종료 ===");
    }
    
    /**
     * 매월 1일 새벽 4시 - 월간 검색 데이터 아카이빙
     * 이전 달 데이터를 별도 저장소로 이동하여 성능 유지
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void archiveMonthlySearchData() {
        log.info("=== 월간 검색 데이터 아카이빙 시작 ===");
        
        try {
            // 90일 이상 된 데이터를 더 강력하게 정리
            int archivedCount = searchLogDAO.deleteOldSearchLogs(90);
            log.info("월간 아카이빙: {}건 정리 완료 (90일 이상)", archivedCount);
            
            // 향후: 아카이브 스토리지로 이동, 압축 등
            
        } catch (Exception e) {
            log.error("월간 아카이빙 실패", e);
        }
        
        log.info("=== 월간 검색 데이터 아카이빙 종료 ===");
    }
    
    /**
     * 수동 실행용 - 즉시 검색 로그 정리
     * 관리자가 필요 시 호출할 수 있는 메서드
     */
    public void manualCleanup(int daysToKeep) {
        log.info("=== 수동 검색 로그 정리 시작 ({}일 이상) ===", daysToKeep);
        
        try {
            int deletedCount = searchLogDAO.deleteOldSearchLogs(daysToKeep);
            log.info("수동 정리 완료: {}건 삭제", deletedCount);
            
        } catch (Exception e) {
            log.error("수동 정리 실패", e);
            throw e; // 수동 실행은 예외를 다시 던져서 호출자가 알 수 있도록
        }
        
        log.info("=== 수동 검색 로그 정리 종료 ===");
    }
    
    /**
     * 검색 로그 상태 체크
     * 현재 저장된 검색 로그의 상태를 확인
     */
    public void checkSearchLogStatus() {
        try {
            int totalCount = searchLogDAO.getTotalCount();
            int recentCount = searchLogDAO.getTotalSearchCountByDays(7); // 최근 7일
            
            log.info("📈 검색 로그 현황");
            log.info("전체 검색 기록: {}건", totalCount);
            log.info("최근 7일 검색: {}건", recentCount);
            
            if (totalCount > 1_000_000) {
                log.warn("⚠️ 검색 로그가 100만건 초과: 성능 최적화 필요");
            }
            
            if (recentCount == 0) {
                log.warn("⚠️ 최근 7일간 검색 기록이 없음: 서비스 상태 확인 필요");
            }
            
        } catch (Exception e) {
            log.error("검색 로그 상태 체크 실패", e);
        }
    }
} 