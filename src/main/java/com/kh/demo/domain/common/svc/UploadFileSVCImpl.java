package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dao.UploadFileDAO;
import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.common.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 파일 업로드 서비스 구현체
 * 파일 업로드 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadFileSVCImpl implements UploadFileSVC {
    private final UploadFileDAO uploadFileDAO;

    @Override
    @Transactional
    public Long save(UploadFile uploadFile) {
        // 비즈니스 로직: 파일 정보 검증
        validateUploadFile(uploadFile);
        
        return uploadFileDAO.save(uploadFile);
    }

    @Override
    @Transactional
    public int update(UploadFile uploadFile) {
        return uploadFileDAO.updateById(uploadFile.getUploadfileId(), uploadFile);
    }

    @Override
    @Transactional
    public int delete(Long uploadfileId) {
        return uploadFileDAO.deleteById(uploadfileId);
    }

    @Override
    public Optional<UploadFile> findById(Long uploadfileId) {
        return uploadFileDAO.findById(uploadfileId);
    }

    @Override
    public Optional<UploadFile> findByStoreFilename(String storeFilename) {
        return uploadFileDAO.findByStoreFilename(storeFilename);
    }

    @Override
    public List<UploadFile> findByCode(Long code) {
        return uploadFileDAO.findByCode(code);
    }

    @Override
    public List<UploadFile> findByRid(String rid) {
        return uploadFileDAO.findByRid(rid);
    }

    @Override
    public List<UploadFile> findByCodeAndRid(Long code, String rid) {
        return uploadFileDAO.findByCodeAndRid(code, rid);
    }

    @Override
    public List<UploadFile> findAll() {
        return uploadFileDAO.findAll();
    }

    @Override
    public boolean existsByStoreFilename(String storeFilename) {
        return uploadFileDAO.existsByStoreFilename(storeFilename);
    }

    @Override
    public int countByCode(Long code) {
        return uploadFileDAO.countByCode(code);
    }

    @Override
    public int countByRid(String rid) {
        return uploadFileDAO.countByRid(rid);
    }
    
    /**
     * 비즈니스 로직: 업로드 파일 검증
     */
    private void validateUploadFile(UploadFile uploadFile) {
        if (uploadFile == null) {
            throw new BusinessValidationException("업로드 파일 정보는 필수입니다.");
        }
        if (uploadFile.getStoreFilename() == null || uploadFile.getStoreFilename().trim().isEmpty()) {
            throw new BusinessValidationException("저장 파일명은 필수입니다.");
        }
        if (uploadFile.getUploadFilename() == null || uploadFile.getUploadFilename().trim().isEmpty()) {
            throw new BusinessValidationException("업로드 파일명은 필수입니다.");
        }
        if (uploadFile.getFsize() == null || uploadFile.getFsize().trim().isEmpty()) {
            throw new BusinessValidationException("파일 크기는 필수입니다.");
        }
        if (uploadFile.getFtype() == null || uploadFile.getFtype().trim().isEmpty()) {
            throw new BusinessValidationException("파일 타입은 필수입니다.");
        }
    }
} 