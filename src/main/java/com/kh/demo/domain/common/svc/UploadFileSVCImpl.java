package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dao.UploadFileDAO;
import com.kh.demo.domain.entity.UploadFile;
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
        return uploadFileDAO.save(uploadFile);
    }

    @Override
    @Transactional
    public int update(UploadFile uploadFile) {
        return uploadFileDAO.update(uploadFile);
    }

    @Override
    @Transactional
    public int delete(Long uploadfileId) {
        return uploadFileDAO.delete(uploadfileId);
    }

    @Override
    public Optional<UploadFile> findById(Long uploadfileId) {
        return uploadFileDAO.findById(uploadfileId);
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
} 