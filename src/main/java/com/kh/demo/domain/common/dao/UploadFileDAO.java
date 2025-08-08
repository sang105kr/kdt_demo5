package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.common.base.BaseDAO;
import java.util.List;
import java.util.Optional;

public interface UploadFileDAO extends BaseDAO<UploadFile, Long> {
    
    // 코드별 파일 목록 조회
    List<UploadFile> findByCode(Long code);
    
    // 참조ID별 파일 목록 조회
    List<UploadFile> findByRid(String rid);
    
    // 코드와 참조ID로 파일 목록 조회
    List<UploadFile> findByCodeAndRid(Long code, String rid);
    
    // 저장 파일명으로 파일 조회
    Optional<UploadFile> findByStoreFilename(String storeFilename);
    
    // 파일 존재 여부 확인
    boolean existsByStoreFilename(String storeFilename);
    
    // 코드별 파일 개수 조회
    int countByCode(Long code);
    
    // 참조ID별 파일 개수 조회
    int countByRid(String rid);
} 