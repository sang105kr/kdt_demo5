package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.entity.UploadFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 파일 업로드 공통 서비스 인터페이스
 * - 물리적 파일 저장
 * - UploadFile 엔티티 생성
 * - 파일명 생성
 * - 검증 로직
 */
public interface FileUploadService {
    
    /**
     * 단일 파일 업로드 처리
     * @param file 업로드할 파일
     * @param fileType 파일 타입 (image, manual 등)
     * @param uploadPath 업로드 경로
     * @return UploadFile 엔티티
     */
    UploadFile uploadSingleFile(MultipartFile file, String fileType, String uploadPath);
    
    /**
     * 다중 파일 업로드 처리
     * @param files 업로드할 파일 목록
     * @param fileType 파일 타입 (image, manual 등)
     * @param uploadPath 업로드 경로
     * @return UploadFile 엔티티 목록
     */
    List<UploadFile> uploadMultipleFiles(List<MultipartFile> files, String fileType, String uploadPath);
    
    /**
     * 파일 삭제 처리 (물리적 파일 + DB)
     * @param uploadfileId 파일 ID
     * @param uploadPath 업로드 경로
     * @return 삭제 성공 여부
     */
    boolean deleteFile(Long uploadfileId, String uploadPath);
    
    /**
     * 다중 파일 삭제 처리
     * @param uploadfileIds 파일 ID 목록
     * @param uploadPath 업로드 경로
     * @return 삭제된 파일 개수
     */
    int deleteMultipleFiles(List<Long> uploadfileIds, String uploadPath);
    
    /**
     * 파일 검증
     * @param file 검증할 파일
     * @param allowedExtensions 허용된 확장자
     * @param maxSize 최대 파일 크기
     * @return 검증 성공 여부
     */
    boolean validateFile(MultipartFile file, String[] allowedExtensions, long maxSize);
} 