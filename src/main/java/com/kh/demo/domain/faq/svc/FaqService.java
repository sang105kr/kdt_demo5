package com.kh.demo.domain.faq.svc;

import com.kh.demo.domain.faq.entity.Faq;
import com.kh.demo.domain.faq.dto.FaqDTO;

import java.util.List;
import java.util.Optional;

public interface FaqService {
    
    // 기본 CRUD
    Faq createFaq(Faq faq, Long adminId);
    Optional<Faq> findById(Long faqId);
    Optional<FaqDTO> findByIdWithJoin(Long faqId);
    Optional<Faq> findByIdWithViewCount(Long faqId);  // 조회수 증가와 함께 조회
    int updateFaq(Long faqId, Faq faq, Long adminId);
    int deleteFaq(Long faqId, Long adminId);
    
    // 목록 조회
    List<Faq> findAll();
    List<Faq> findActiveWithPaging(int page, int size);
    List<FaqDTO> findActiveWithJoin(int page, int size);
    int countActive();
    
    // 카테고리별 조회
    List<Faq> findByCategoryId(Long categoryId, int page, int size);
    int countByCategoryId(Long categoryId);
    
    // 검색
    List<Faq> findByKeyword(String keyword, int page, int size);
    int countByKeyword(String keyword);
    
    // 정렬별 조회
    List<Faq> findAllBySortOrder(int page, int size);
    List<Faq> findAllByViewCount(int page, int size);
    
    // 카운트 증가
    int incrementViewCount(Long faqId);
    int incrementHelpfulCount(Long faqId);
    int incrementUnhelpfulCount(Long faqId);
    
    // 상태 관리
    int updateActiveStatus(Long faqId, String isActive, Long adminId);
    int updateSortOrder(Long faqId, Integer sortOrder, Long adminId);
}
