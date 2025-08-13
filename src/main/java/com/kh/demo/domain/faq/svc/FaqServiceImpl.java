package com.kh.demo.domain.faq.svc;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.domain.faq.dao.FaqDAO;
import com.kh.demo.domain.faq.entity.Faq;
import com.kh.demo.domain.faq.dto.FaqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqDAO faqDAO;

    @Override
    @Transactional
    public Faq createFaq(Faq faq, Long adminId) {
        // 권한 검증: 관리자만 FAQ 작성 가능
        if (adminId == null) {
            throw new BusinessValidationException("관리자 권한이 필요한 서비스입니다.");
        }

        // 기본값 설정
        faq.setDefaultValues();
        faq.setAdminId(adminId);

        // FAQ 저장
        Long faqId = faqDAO.save(faq);
        faq.setFaqId(faqId);

        log.info("FAQ 생성 성공: faqId={}, adminId={}", faqId, adminId);
        return faq;
    }

    @Override
    public Optional<Faq> findById(Long faqId) {
        return faqDAO.findById(faqId);
    }

    @Override
    public Optional<FaqDTO> findByIdWithJoin(Long faqId) {
        return faqDAO.findByIdWithJoin(faqId);
    }

    @Override
    public Optional<Faq> findByIdWithViewCount(Long faqId) {
        // 조회수 증가
        faqDAO.incrementViewCount(faqId);
        // FAQ 조회
        return faqDAO.findById(faqId);
    }

    @Override
    @Transactional
    public int updateFaq(Long faqId, Faq faq, Long adminId) {
        // 권한 검증: 관리자만 FAQ 수정 가능
        if (adminId == null) {
            throw new BusinessValidationException("관리자 권한이 필요한 서비스입니다.");
        }

        // 기존 FAQ 존재 여부 확인
        Optional<Faq> existingFaq = faqDAO.findById(faqId);
        if (existingFaq.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 FAQ입니다.");
        }

        // FAQ 업데이트
        int result = faqDAO.updateById(faqId, faq);
        
        if (result > 0) {
            log.info("FAQ 수정 성공: faqId={}, adminId={}", faqId, adminId);
        }
        
        return result;
    }

    @Override
    @Transactional
    public int deleteFaq(Long faqId, Long adminId) {
        // 권한 검증: 관리자만 FAQ 삭제 가능
        if (adminId == null) {
            throw new BusinessValidationException("관리자 권한이 필요한 서비스입니다.");
        }

        // 기존 FAQ 존재 여부 확인
        Optional<Faq> existingFaq = faqDAO.findById(faqId);
        if (existingFaq.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 FAQ입니다.");
        }

        // FAQ 삭제
        int result = faqDAO.deleteById(faqId);
        
        if (result > 0) {
            log.info("FAQ 삭제 성공: faqId={}, adminId={}", faqId, adminId);
        }
        
        return result;
    }

    @Override
    public List<Faq> findAll() {
        return faqDAO.findAll();
    }

    @Override
    public List<Faq> findActiveWithPaging(int page, int size) {
        int offset = (page - 1) * size;
        return faqDAO.findActiveWithPaging(offset, size);
    }

    @Override
    public List<FaqDTO> findActiveWithJoin(int page, int size) {
        int offset = (page - 1) * size;
        return faqDAO.findActiveWithJoin(offset, size);
    }

    @Override
    public int countActive() {
        return faqDAO.countActive();
    }

    @Override
    public List<Faq> findByCategoryId(Long categoryId, int page, int size) {
        int offset = (page - 1) * size;
        return faqDAO.findByCategoryId(categoryId, offset, size);
    }

    @Override
    public int countByCategoryId(Long categoryId) {
        return faqDAO.countByCategoryId(categoryId);
    }

    @Override
    public List<Faq> findByKeyword(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        return faqDAO.findByKeyword(keyword, offset, size);
    }

    @Override
    public int countByKeyword(String keyword) {
        return faqDAO.countByKeyword(keyword);
    }

    @Override
    public List<Faq> findAllBySortOrder(int page, int size) {
        int offset = (page - 1) * size;
        return faqDAO.findAllBySortOrder(offset, size);
    }

    @Override
    public List<Faq> findAllByViewCount(int page, int size) {
        int offset = (page - 1) * size;
        return faqDAO.findAllByViewCount(offset, size);
    }

    @Override
    @Transactional
    public int incrementViewCount(Long faqId) {
        return faqDAO.incrementViewCount(faqId);
    }

    @Override
    @Transactional
    public int incrementHelpfulCount(Long faqId) {
        return faqDAO.incrementHelpfulCount(faqId);
    }

    @Override
    @Transactional
    public int incrementUnhelpfulCount(Long faqId) {
        return faqDAO.incrementUnhelpfulCount(faqId);
    }

    @Override
    @Transactional
    public int updateActiveStatus(Long faqId, String isActive, Long adminId) {
        // 권한 검증: 관리자만 상태 변경 가능
        if (adminId == null) {
            throw new BusinessValidationException("관리자 권한이 필요한 서비스입니다.");
        }

        // 기존 FAQ 존재 여부 확인
        Optional<Faq> existingFaq = faqDAO.findById(faqId);
        if (existingFaq.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 FAQ입니다.");
        }

        // 상태 업데이트
        int result = faqDAO.updateActiveStatus(faqId, isActive);
        
        if (result > 0) {
            log.info("FAQ 상태 변경 성공: faqId={}, isActive={}, adminId={}", faqId, isActive, adminId);
        }
        
        return result;
    }

    @Override
    @Transactional
    public int updateSortOrder(Long faqId, Integer sortOrder, Long adminId) {
        // 권한 검증: 관리자만 정렬순서 변경 가능
        if (adminId == null) {
            throw new BusinessValidationException("관리자 권한이 필요한 서비스입니다.");
        }

        // 기존 FAQ 존재 여부 확인
        Optional<Faq> existingFaq = faqDAO.findById(faqId);
        if (existingFaq.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 FAQ입니다.");
        }

        // 정렬순서 업데이트
        int result = faqDAO.updateSortOrder(faqId, sortOrder);
        
        if (result > 0) {
            log.info("FAQ 정렬순서 변경 성공: faqId={}, sortOrder={}, adminId={}", faqId, sortOrder, adminId);
        }
        
        return result;
    }
}
