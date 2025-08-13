package com.kh.demo.domain.faq.dao;

import com.kh.demo.domain.faq.entity.Faq;
import com.kh.demo.domain.faq.dto.FaqDTO;
import com.kh.demo.domain.common.base.BaseDAO;

import java.util.List;
import java.util.Optional;

public interface FaqDAO extends BaseDAO<Faq, Long> {
    
    // 카테고리별 FAQ 목록 조회 (페이징)
    List<Faq> findByCategoryId(Long categoryId, int offset, int limit);
    
    // 카테고리별 FAQ 개수 조회
    int countByCategoryId(Long categoryId);
    
    // 키워드 검색 FAQ 목록 조회 (페이징)
    List<Faq> findByKeyword(String keyword, int offset, int limit);
    
    // 키워드 검색 FAQ 개수 조회
    int countByKeyword(String keyword);
    
    // 활성화된 FAQ 목록 조회 (페이징)
    List<Faq> findActiveWithPaging(int offset, int limit);
    
    // 활성화된 FAQ 개수 조회
    int countActive();
    
    // 정렬순서별 FAQ 목록 조회
    List<Faq> findAllBySortOrder(int offset, int limit);
    
    // 조회수별 FAQ 목록 조회 (인기순)
    List<Faq> findAllByViewCount(int offset, int limit);
    
    // 조회수 증가
    int incrementViewCount(Long faqId);
    
    // 도움됨 수 증가
    int incrementHelpfulCount(Long faqId);
    
    // 도움안됨 수 증가
    int incrementUnhelpfulCount(Long faqId);
    
    // FAQ 활성화/비활성화
    int updateActiveStatus(Long faqId, String isActive);
    
    // 정렬순서 업데이트
    int updateSortOrder(Long faqId, Integer sortOrder);
    
    // DTO 조회 메서드들 (조인 데이터 포함)
    Optional<FaqDTO> findByIdWithJoin(Long faqId);
    List<FaqDTO> findActiveWithJoin(int offset, int limit);
}
