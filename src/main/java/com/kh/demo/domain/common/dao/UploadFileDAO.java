package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.UploadFile;
import java.util.List;
import java.util.Optional;

public interface UploadFileDAO {
    
    // 파일 등록
    Long save(UploadFile uploadFile);
    
    // 파일 수정
    int update(UploadFile uploadFile);
    
    // 파일 삭제
    int delete(Long uploadfileId);
    
    // 파일 단건 조회
    Optional<UploadFile> findById(Long uploadfileId);
    
    // 코드별 파일 목록 조회
    List<UploadFile> findByCode(Long code);
    
    // 참조ID별 파일 목록 조회
    List<UploadFile> findByRid(String rid);
    
    // 코드와 참조ID로 파일 목록 조회
    List<UploadFile> findByCodeAndRid(Long code, String rid);
    
    // 전체 파일 목록 조회
    List<UploadFile> findAll();
    
    // 파일 존재 여부 확인
    boolean existsByStoreFilename(String storeFilename);
    
    // 코드별 파일 개수 조회
    int countByCode(Long code);
    
    // 참조ID별 파일 개수 조회
    int countByRid(String rid);
} 