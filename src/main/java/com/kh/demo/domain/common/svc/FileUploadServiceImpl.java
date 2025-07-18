package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.shared.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 파일 업로드 공통 서비스 구현체
 * - 물리적 파일 저장
 * - UploadFile 엔티티 생성
 * - 파일명 생성
 * - 검증 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final UploadFileSVC uploadFileSVC;
    
    @Value("${file.upload.path}")
    private String defaultUploadPath;

    @Override
    public UploadFile uploadSingleFile(MultipartFile file, String fileType, String uploadPath) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // 파일 검증
            if (!validateFile(file, getAllowedExtensions(fileType), getMaxFileSize(fileType))) {
                throw new IllegalArgumentException("파일 검증 실패: " + file.getOriginalFilename());
            }
            
            // 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String storeFilename = generateStoreFilename(originalFilename);
            
            // 업로드 디렉토리 생성
            FileUtils.createDirectoryIfNotExists(uploadPath);
            
            // 물리적 파일 저장
            Path filePath = Paths.get(uploadPath, storeFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // UploadFile 엔티티 생성
            UploadFile uploadFile = new UploadFile();
            uploadFile.setUploadFilename(originalFilename);
            uploadFile.setStoreFilename(storeFilename);
            uploadFile.setFsize(String.valueOf(file.getSize()));
            uploadFile.setFtype(file.getContentType());
            
            log.info("파일 업로드 완료: {} -> {}", originalFilename, storeFilename);
            return uploadFile;
            
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public List<UploadFile> uploadMultipleFiles(List<MultipartFile> files, String fileType, String uploadPath) {
        List<UploadFile> uploadFiles = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return uploadFiles;
        }
        
        for (MultipartFile file : files) {
            UploadFile uploadFile = uploadSingleFile(file, fileType, uploadPath);
            if (uploadFile != null) {
                uploadFiles.add(uploadFile);
            }
        }
        
        return uploadFiles;
    }

    @Override
    public boolean deleteFile(Long uploadfileId, String uploadPath) {
        try {
            // DB에서 파일 정보 조회
            var fileOpt = uploadFileSVC.findById(uploadfileId);
            if (fileOpt.isEmpty()) {
                log.warn("삭제할 파일 정보가 DB에 없음: {}", uploadfileId);
                return false;
            }
            
            UploadFile uploadFile = fileOpt.get();
            String storeFilename = uploadFile.getStoreFilename();
            
            // 물리적 파일 삭제
            boolean physicalDeleted = FileUtils.deletePhysicalFile(uploadPath, storeFilename);
            
            // DB에서 파일 정보 삭제
            int dbDeleted = uploadFileSVC.delete(uploadfileId);
            
            if (physicalDeleted && dbDeleted > 0) {
                log.info("파일 삭제 완료: {}", storeFilename);
                return true;
            } else {
                log.warn("파일 삭제 실패: physicalDeleted={}, dbDeleted={}", physicalDeleted, dbDeleted);
                return false;
            }
            
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생: {}", uploadfileId, e);
            return false;
        }
    }

    @Override
    public int deleteMultipleFiles(List<Long> uploadfileIds, String uploadPath) {
        if (uploadfileIds == null || uploadfileIds.isEmpty()) {
            return 0;
        }
        
        int deletedCount = 0;
        for (Long uploadfileId : uploadfileIds) {
            if (deleteFile(uploadfileId, uploadPath)) {
                deletedCount++;
            }
        }
        
        log.info("다중 파일 삭제 완료: {}/{}", deletedCount, uploadfileIds.size());
        return deletedCount;
    }

    @Override
    public boolean validateFile(MultipartFile file, String[] allowedExtensions, long maxSize) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // 파일 크기 검증
        if (!FileUtils.isValidFileSize(file.getSize(), maxSize)) {
            log.warn("파일 크기 초과: {} (최대: {} bytes)", file.getSize(), maxSize);
            return false;
        }
        
        // 파일 확장자 검증
        if (!FileUtils.isValidFileExtension(file.getOriginalFilename(), allowedExtensions)) {
            log.warn("허용되지 않은 파일 확장자: {}", file.getOriginalFilename());
            return false;
        }
        
        return true;
    }
    
    /**
     * 파일 타입별 허용 확장자 반환
     */
    private String[] getAllowedExtensions(String fileType) {
        return switch (fileType) {
            case "image" -> new String[]{"jpg", "jpeg", "png", "gif", "webp"};
            case "manual" -> new String[]{"pdf", "doc", "docx", "txt"};
            case "document" -> new String[]{"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"};
            default -> new String[]{"*"};
        };
    }
    
    /**
     * 파일 타입별 최대 파일 크기 반환 (bytes)
     */
    private long getMaxFileSize(String fileType) {
        return switch (fileType) {
            case "image" -> 10 * 1024 * 1024; // 10MB
            case "manual", "document" -> 50 * 1024 * 1024; // 50MB
            default -> 10 * 1024 * 1024; // 기본 10MB
        };
    }
    
    /**
     * 저장용 파일명 생성
     */
    private String generateStoreFilename(String originalFilename) {
        String extension = FileUtils.getFileExtension(originalFilename);
        if (!extension.isEmpty()) {
            extension = "." + extension;
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return timestamp + "_" + uuid + extension;
    }
} 