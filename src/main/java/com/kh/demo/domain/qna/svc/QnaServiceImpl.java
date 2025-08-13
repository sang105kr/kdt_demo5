package com.kh.demo.domain.qna.svc;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.qna.dao.QnaDAO;
import com.kh.demo.domain.qna.entity.Qna;
import com.kh.demo.domain.qna.entity.QnaComment;
import com.kh.demo.common.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaServiceImpl implements QnaService {

    private final QnaDAO qnaDAO;
    private final QnaCommentService qnaCommentService;
    private final CodeSVC codeSVC;

    @Override
    @Transactional
    public Long save(Qna qna) {
        return qnaDAO.save(qna);
    }

    @Override
    public Optional<Qna> findById(Long id) {
        Optional<Qna> qnaOpt = qnaDAO.findById(id);
        qnaOpt.ifPresent(this::populateCategoryName);
        return qnaOpt;
    }

    @Override
    public List<Qna> findAll() {
        List<Qna> qnaList = qnaDAO.findAll();
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public List<Qna> findAll(int pageNo, int numOfRows) {
        int offset = (pageNo - 1) * numOfRows;
        List<Qna> qnaList = qnaDAO.findAllWithPaging(offset, numOfRows);
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    @Transactional
    public int updateById(Long id, Qna qna) {
        return qnaDAO.updateById(id, qna);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        return qnaDAO.deleteById(id);
    }

    @Override
    public int getTotalCount() {
        return qnaDAO.getTotalCount();
    }

    @Override
    public List<Qna> findByProductId(Long productId, int offset, int limit) {
        List<Qna> qnaList = qnaDAO.findByProductId(productId, offset, limit);
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public int countByProductId(Long productId) {
        return qnaDAO.countByProductId(productId);
    }

    @Override
    public List<Qna> findByMemberId(Long memberId) {
        List<Qna> qnaList = qnaDAO.findByMemberId(memberId);
        populateCategoryNames(qnaList);
        return qnaList;
    }
    
    @Override
    public List<Qna> findByMemberIdAllStatus(Long memberId) {
        List<Qna> qnaList = qnaDAO.findByMemberIdAllStatus(memberId);
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public List<Qna> findByCategoryId(Long categoryId, int offset, int limit) {
        List<Qna> qnaList = qnaDAO.findByCategoryId(categoryId, offset, limit);
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public int countByCategoryId(Long categoryId) {
        return qnaDAO.countByCategoryId(categoryId);
    }

    @Override
    public List<Qna> findByStatusId(Long statusId, int offset, int limit) {
        List<Qna> qnaList = qnaDAO.findByStatusId(statusId, offset, limit);
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public int countByStatusId(Long statusId) {
        return qnaDAO.countByStatusId(statusId);
    }

    @Override
    public List<Qna> findByKeyword(String keyword, int offset, int limit) {
        List<Qna> qnaList = qnaDAO.findByKeyword(keyword, offset, limit);
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public int countByKeyword(String keyword) {
        return qnaDAO.countByKeyword(keyword);
    }

    @Override
    @Transactional
    public Optional<Qna> findByIdWithViewCount(Long qnaId) {
        // 조회수 증가
        qnaDAO.incrementViewCount(qnaId);
        Optional<Qna> qnaOpt = qnaDAO.findById(qnaId);
        qnaOpt.ifPresent(this::populateCategoryName);
        return qnaOpt;
    }

    @Override
    @Transactional
    public int incrementViewCount(Long qnaId) {
        return qnaDAO.incrementViewCount(qnaId);
    }

    @Override
    @Transactional
    public int incrementHelpfulCount(Long qnaId) {
        return qnaDAO.incrementHelpfulCount(qnaId);
    }

    @Override
    @Transactional
    public int incrementUnhelpfulCount(Long qnaId) {
        return qnaDAO.incrementUnhelpfulCount(qnaId);
    }

    @Override
    @Transactional
    public int updateStatus(Long qnaId, Long statusId) {
        return qnaDAO.updateStatus(qnaId, statusId);
    }

    @Override
    @Transactional
    public int updateAnswer(Long qnaId, String answer, Long adminId) {
        return qnaDAO.updateAnswer(qnaId, answer, adminId);
    }

    @Override
    @Transactional
    public Qna createQna(Qna qna, Long memberId) {
        // 권한 검증: 로그인한 사용자만 Q&A 작성 가능
        if (memberId == null) {
            throw new BusinessValidationException("로그인이 필요한 서비스입니다.");
        }

        qna.setMemberId(memberId);
        
        // statusId가 null이면 PENDING 상태로 설정
        if (qna.getStatusId() == null) {
            Long pendingStatusId = codeSVC.getCodeId("QNA_STATUS", "PENDING");
            qna.setStatusId(pendingStatusId);
        }

        // 초기값 설정
        if (qna.getHelpfulCount() == null) {
            qna.setHelpfulCount(0);
        }
        if (qna.getUnhelpfulCount() == null) {
            qna.setUnhelpfulCount(0);
        }
        if (qna.getViewCount() == null) {
            qna.setViewCount(0);
        }
        if (qna.getCommentCount() == null) {
            qna.setCommentCount(0);
        }

        Long qnaId = qnaDAO.save(qna);
        Qna createdQna = qnaDAO.findById(qnaId)
            .orElseThrow(() -> new BusinessValidationException("Q&A 저장 중 오류가 발생했습니다."));
        populateCategoryName(createdQna);
        return createdQna;
    }

    @Override
    @Transactional
    public int updateQna(Long qnaId, Qna qna, Long memberId) {
        // 권한 검증: 본인이 작성한 Q&A만 수정 가능
        Optional<Qna> existingQna = qnaDAO.findById(qnaId);
        if (existingQna.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 Q&A입니다.");
        }

        Qna foundQna = existingQna.get();
        if (!foundQna.getMemberId().equals(memberId)) {
            throw new BusinessValidationException("본인이 작성한 Q&A만 수정할 수 있습니다.");
        }

        // 수정 불가능한 상태인지 확인
        Long pendingStatusId = codeSVC.getCodeId("QNA_STATUS", "PENDING");
        if (!foundQna.getStatusId().equals(pendingStatusId)) {
            throw new BusinessValidationException("답변대기 상태의 Q&A만 수정할 수 있습니다.");
        }

        qna.setQnaId(qnaId);
        qna.setMemberId(memberId);
        qna.setStatusId(foundQna.getStatusId()); // 기존 상태 유지

        return qnaDAO.updateById(qnaId, qna);
    }

    @Override
    @Transactional
    public int deleteQna(Long qnaId, Long memberId, boolean isAdmin) {
        // 권한 검증: 본인이 작성한 Q&A 또는 관리자만 삭제 가능
        Optional<Qna> existingQna = qnaDAO.findById(qnaId);
        if (existingQna.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 Q&A입니다.");
        }

        Qna foundQna = existingQna.get();
        if (!isAdmin && !foundQna.getMemberId().equals(memberId)) {
            throw new BusinessValidationException("본인이 작성한 Q&A만 삭제할 수 있습니다.");
        }

        // 관리자가 아닌 경우 답변대기 상태인지 확인
        if (!isAdmin) {
            Long pendingStatusId = codeSVC.getCodeId("QNA_STATUS", "PENDING");
            if (!foundQna.getStatusId().equals(pendingStatusId)) {
                throw new BusinessValidationException("답변대기 상태의 Q&A만 삭제할 수 있습니다.");
            }
        }

        return qnaDAO.deleteById(qnaId);
    }

    @Override
    public List<QnaComment> findCommentsByQnaId(Long qnaId) {
        return qnaDAO.findCommentsByQnaId(qnaId);
    }

    @Override
    public int countCommentsByQnaId(Long qnaId) {
        return qnaDAO.countCommentsByQnaId(qnaId);
    }

    @Override
    public List<Qna> findAllWithPaging(int offset, int limit) {
        List<Qna> qnaList = qnaDAO.findAllWithPaging(offset, limit);
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public int countAll() {
        return qnaDAO.countAll();
    }

    @Override
    public List<Qna> findAll(String category, String keyword, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<Qna> qnaList;
        
        // 필터링 조건에 따른 조회
        if (category != null && !category.isEmpty()) {
            Long categoryId = codeSVC.getCodeId("QNA_CATEGORY", category);
            qnaList = qnaDAO.findByCategoryId(categoryId, offset, size);
        } else if (status != null && !status.isEmpty()) {
            Long statusId = codeSVC.getCodeId("QNA_STATUS", status);
            qnaList = qnaDAO.findByStatusId(statusId, offset, size);
        } else if (keyword != null && !keyword.isEmpty()) {
            qnaList = qnaDAO.findByKeyword(keyword, offset, size);
        } else {
            qnaList = qnaDAO.findAllWithPaging(offset, size);
        }
        
        populateCategoryNames(qnaList);
        return qnaList;
    }

    @Override
    public int countAll(String category, String keyword, String status) {
        // 필터링 조건에 따른 개수 조회
        if (category != null && !category.isEmpty()) {
            Long categoryId = codeSVC.getCodeId("QNA_CATEGORY", category);
            return qnaDAO.countByCategoryId(categoryId);
        } else if (status != null && !status.isEmpty()) {
            Long statusId = codeSVC.getCodeId("QNA_STATUS", status);
            return qnaDAO.countByStatusId(statusId);
        } else if (keyword != null && !keyword.isEmpty()) {
            return qnaDAO.countByKeyword(keyword);
        } else {
            return qnaDAO.countAll();
        }
    }
    
    /**
     * Qna 객체의 categoryName과 statusName을 설정하는 헬퍼 메서드
     */
    private void populateCategoryName(Qna qna) {
        if (qna.getCategoryId() != null) {
            qna.setCategoryName(codeSVC.getCodeDecode("QNA_CATEGORY", qna.getCategoryId()));
        }
        if (qna.getStatusId() != null) {
            qna.setStatusName(codeSVC.getCodeDecode("QNA_STATUS", qna.getStatusId()));
        }
    }
    
    /**
     * Qna 리스트의 모든 객체에 categoryName을 설정하는 헬퍼 메서드
     */
    private void populateCategoryNames(List<Qna> qnaList) {
        if (qnaList != null) {
            qnaList.forEach(this::populateCategoryName);
        }
    }
}
