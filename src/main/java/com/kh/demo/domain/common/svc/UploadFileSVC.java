package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.entity.UploadFile;
import java.util.List;
import java.util.Optional;

/**
 * 파일 업로드 서비스 인터페이스
 * 파일 업로드 관련 비즈니스 로직을 처리합니다.
 */
public interface UploadFileSVC {
    Long save(UploadFile uploadFile);
    int update(UploadFile uploadFile);
    int delete(Long uploadfileId);
    Optional<UploadFile> findById(Long uploadfileId);
    Optional<UploadFile> findByStoreFilename(String storeFilename);
    List<UploadFile> findByCode(Long code);
    List<UploadFile> findByRid(String rid);
    List<UploadFile> findByCodeAndRid(Long code, String rid);
    List<UploadFile> findAll();
    boolean existsByStoreFilename(String storeFilename);
    int countByCode(Long code);
    int countByRid(String rid);
} 