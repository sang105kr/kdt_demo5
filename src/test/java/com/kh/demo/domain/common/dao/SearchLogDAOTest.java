package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.SearchLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SearchLogDAOTest {

    @Autowired
    private SearchLogDAO searchLogDAO;

    @Test
    @DisplayName("검색 기록 저장 및 조회 테스트")
    void saveAndFindSearchLog() {
        // Given
        Long memberId = 1L;
        String keyword = "테스트 키워드";
        String searchType = "PRODUCT";
        Integer resultCount = 10;
        String searchIp = "192.168.1.1";

        // When
        Long savedId = searchLogDAO.saveSearchLog(memberId, keyword, searchType, resultCount, searchIp);

        // Then
        assertThat(savedId).isNotNull();
        assertThat(savedId).isGreaterThan(0);

        Optional<SearchLog> found = searchLogDAO.findById(savedId);
        assertThat(found).isPresent();
        assertThat(found.get().getKeyword()).isEqualTo(keyword);
        assertThat(found.get().getMemberId()).isEqualTo(memberId);
        // 검색 타입은 code_id로 저장되므로 직접 비교 대신 존재 여부만 확인
        assertThat(found.get().getSearchTypeId()).isNotNull();
    }

    @Test
    @DisplayName("개인 검색 히스토리 조회 테스트")
    void getMemberSearchHistory() {
        // Given
        Long memberId = 1L;
        String[] keywords = {"노트북", "스마트폰", "태블릿"};
        
        // 검색 기록 저장
        for (String keyword : keywords) {
            searchLogDAO.saveSearchLog(memberId, keyword, "PRODUCT", null, null);
        }

        // When
        List<String> history = searchLogDAO.getMemberSearchHistory(memberId, 5);

        // Then
        assertThat(history).isNotEmpty();
        assertThat(history).hasSize(3);
        assertThat(history).containsExactlyInAnyOrder("노트북", "스마트폰", "태블릿");
    }

    @Test
    @DisplayName("인기 검색어 조회 테스트 (Oracle 백업)")
    void getPopularKeywordsFromOracle() {
        // Given
        String[] keywords = {"인기키워드1", "인기키워드2", "인기키워드1", "인기키워드3", "인기키워드1"};
        
        // 검색 기록 저장 (인기키워드1이 3번, 나머지는 1번씩)
        for (String keyword : keywords) {
            searchLogDAO.saveSearchLog(1L, keyword, "PRODUCT", null, null);
        }

        // When
        List<String> popularKeywords = searchLogDAO.getPopularKeywordsFromOracle(5);

        // Then
        assertThat(popularKeywords).isNotEmpty();
        assertThat(popularKeywords.get(0)).isEqualTo("인기키워드1"); // 가장 많이 검색된 키워드가 첫 번째
        assertThat(popularKeywords).contains("인기키워드2", "인기키워드3");
    }

    @Test
    @DisplayName("총 검색 건수 조회 테스트")
    void getTotalCount() {
        // Given
        int initialCount = searchLogDAO.getTotalCount();
        
        // 검색 기록 3건 추가
        searchLogDAO.saveSearchLog(1L, "테스트1", "PRODUCT", null, null);
        searchLogDAO.saveSearchLog(2L, "테스트2", "PRODUCT", null, null);
        searchLogDAO.saveSearchLog(3L, "테스트3", "PRODUCT", null, null);

        // When
        int finalCount = searchLogDAO.getTotalCount();

        // Then
        assertThat(finalCount).isEqualTo(initialCount + 3);
    }

    @Test
    @DisplayName("최근 기간 검색 건수 조회 테스트")
    void getTotalSearchCountByDays() {
        // Given
        searchLogDAO.saveSearchLog(1L, "최근검색", "PRODUCT", null, null);
        searchLogDAO.saveSearchLog(2L, "또다른검색", "PRODUCT", null, null);

        // When
        int recentCount = searchLogDAO.getTotalSearchCountByDays(7);

        // Then
        assertThat(recentCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("빈 키워드 처리 테스트")
    void handleEmptyKeyword() {
        // Given & When & Then
        assertThatCode(() -> {
            searchLogDAO.saveSearchLog(1L, "", "PRODUCT", null, null);
            searchLogDAO.saveSearchLog(1L, "   ", "PRODUCT", null, null);
            searchLogDAO.saveSearchLog(1L, null, "PRODUCT", null, null);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("검색 타입별 통계 테스트")
    void getSearchCountByType() {
        // Given
        searchLogDAO.saveSearchLog(1L, "상품검색", "PRODUCT", null, null);
        searchLogDAO.saveSearchLog(1L, "게시판검색", "BOARD", null, null);
        searchLogDAO.saveSearchLog(1L, "또다른상품검색", "PRODUCT", null, null);

        // When
        int productCount = searchLogDAO.getSearchCountByType("PRODUCT", 7);
        int boardCount = searchLogDAO.getSearchCountByType("BOARD", 7);

        // Then
        assertThat(productCount).isGreaterThanOrEqualTo(2);
        assertThat(boardCount).isGreaterThanOrEqualTo(1);
    }
} 