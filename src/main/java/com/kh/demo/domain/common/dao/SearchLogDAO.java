package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.SearchLog;
import com.kh.demo.domain.common.base.BaseDAO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 검색 로그 DAO 인터페이스
 * 인기검색어, 검색 히스토리 관련 데이터베이스 작업을 담당
 */
public interface SearchLogDAO extends BaseDAO<SearchLog, Long> {
    
    /**
     * 검색 기록 저장
     * @param memberId 회원 ID (비로그인 시 NULL)
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (PRODUCT, BOARD, MEMBER)
     * @param resultCount 검색 결과 수
     * @param searchIp 검색 IP
     * @return 생성된 검색 로그 ID
     */
    Long saveSearchLog(Long memberId, String keyword, String searchType, Integer resultCount, String searchIp);
    
    /**
     * 인기 검색어 조회 (Elasticsearch 장애 시 백업용)
     * @param days 최근 몇 일간의 데이터를 사용할지
     * @param limit 조회할 키워드 개수
     * @return 인기 검색어 목록 (검색 횟수 순)
     */
    List<String> getPopularKeywordsFromOracle(int days, int limit);
    
    /**
     * 인기 검색어 조회 (기본 7일, 백업용)
     * @param limit 조회할 키워드 개수
     * @return 인기 검색어 목록
     */
    List<String> getPopularKeywordsFromOracle(int limit);
    
    /**
     * 개인 검색 히스토리 조회
     * @param memberId 회원 ID
     * @param limit 조회할 검색어 개수
     * @return 개인 검색 히스토리 (최근 검색 순)
     */
    List<String> getMemberSearchHistory(Long memberId, int limit);
    
    /**
     * 오래된 검색 기록 삭제
     * @param daysToKeep 보관할 일수 (이보다 오래된 데이터 삭제)
     * @return 삭제된 건수
     */
    int deleteOldSearchLogs(int daysToKeep);
    
    /**
     * 중복 검색 기록 정리 (같은 사용자의 같은 키워드)
     * @return 정리된 건수
     */
    int mergeDuplicateSearchLogs();
    
    /**
     * 기간별 검색 통계 조회
     * @param from 시작 일시
     * @param to 종료 일시
     * @return 키워드별 검색 횟수 맵
     */
    Map<String, Integer> getSearchStatsByPeriod(LocalDateTime from, LocalDateTime to);
    
    /**
     * 특정 키워드의 검색 횟수 조회
     * @param keyword 검색 키워드
     * @param days 최근 몇 일간
     * @return 검색 횟수
     */
    int getKeywordSearchCount(String keyword, int days);
    
    /**
     * 회원별 검색 활동 통계
     * @param memberId 회원 ID
     * @param days 최근 몇 일간
     * @return 검색 횟수
     */
    int getMemberSearchCount(Long memberId, int days);
    
    /**
     * 검색 타입별 통계
     * @param searchType 검색 타입
     * @param days 최근 몇 일간
     * @return 검색 횟수
     */
    int getSearchCountByType(String searchType, int days);
    
    /**
     * 전체 사용자의 최근 검색 활동 통계
     * @param days 최근 몇 일간
     * @return 검색 횟수
     */
    int getTotalSearchCountByDays(int days);
    
    /**
     * 특정 회원의 모든 검색 히스토리 삭제
     * @param memberId 회원 ID
     * @return 삭제된 건수
     */
    int clearMemberSearchHistory(Long memberId);
    
    /**
     * 특정 회원의 특정 검색 히스토리 삭제
     * @param memberId 회원 ID
     * @param keyword 삭제할 검색어
     * @return 삭제된 건수
     */
    int deleteMemberSearchHistoryItem(Long memberId, String keyword);
} 