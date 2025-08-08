package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.common.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final CodeSVC codeSVC;
    
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
            
            // fileType을 코드값으로 변환하여 폴더명 생성
            String codeValue = convertFileTypeToCodeValue(fileType);
            String folderName = FileUtils.generateFolderNameFromCode(codeValue);
            String typeSpecificPath = Paths.get(uploadPath, folderName).toString();
            FileUtils.createDirectoryIfNotExists(typeSpecificPath);
            
            // 물리적 파일 저장 (코드값 기반 폴더에 저장)
            Path filePath = Paths.get(typeSpecificPath, storeFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // UploadFile 엔티티 생성
            UploadFile uploadFile = new UploadFile();
            uploadFile.setUploadFilename(originalFilename);
            uploadFile.setStoreFilename(storeFilename);
            uploadFile.setFsize(String.valueOf(file.getSize()));
            uploadFile.setFtype(file.getContentType());
            
            log.info("파일 업로드 완료: {} -> {}/{}", originalFilename, folderName, storeFilename);
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
                log.warn("파일 정보를 찾을 수 없습니다: {}", uploadfileId);
                return false;
            }
            
            UploadFile uploadFile = fileOpt.get();
            String storeFilename = uploadFile.getStoreFilename();
            Long codeId = uploadFile.getCode();
            
            // 코드 ID를 기반으로 폴더명 생성
            String folderName = FileUtils.generateFolderNameFromCodeId(codeId, codeSVC);
            String typeSpecificPath = Paths.get(uploadPath, folderName).toString();
            
            // 물리적 파일 삭제
            Path filePath = Paths.get(typeSpecificPath, storeFilename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("물리적 파일 삭제 완료: {}/{}", folderName, storeFilename);
            } else {
                log.warn("물리적 파일이 존재하지 않습니다: {}", filePath);
            }
            
            // DB에서 파일 정보 삭제
            uploadFileSVC.delete(uploadfileId);
            log.info("DB 파일 정보 삭제 완료: {}", uploadfileId);
            
            return true;
            
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", uploadfileId, e);
            return false;
        }
    }

    @Override
    public int deleteMultipleFiles(List<Long> uploadfileIds, String uploadPath) {
        int deletedCount = 0;
        
        if (uploadfileIds == null || uploadfileIds.isEmpty()) {
            return deletedCount;
        }
        
        for (Long uploadfileId : uploadfileIds) {
            if (deleteFile(uploadfileId, uploadPath)) {
                deletedCount++;
            }
        }
        
        log.info("다중 파일 삭제 완료: {} / {}", deletedCount, uploadfileIds.size());
        return deletedCount;
    }

    @Override
    public boolean validateFile(MultipartFile file, String[] allowedExtensions, long maxSize) {
        if (file == null || file.isEmpty()) {
            log.warn("파일이 null이거나 비어있습니다.");
            return false;
        }
        
        // 파일 크기 검증
        if (file.getSize() > maxSize) {
            log.warn("파일 크기 초과: {} > {} bytes (파일명: {})", file.getSize(), maxSize, file.getOriginalFilename());
            return false;
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            log.warn("파일명이 null이거나 비어있습니다.");
            return false;
        }
        
        // 파일명에 확장자가 없는 경우 처리
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
            log.warn("파일명에 확장자가 없습니다: {}", originalFilename);
            return false;
        }
        
        String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        
        // 허용된 확장자 검사
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        
        log.warn("허용되지 않은 파일 확장자: {} (파일명: {}, 허용 확장자: {})", 
                extension, originalFilename, String.join(", ", allowedExtensions));
        return false;
    }
    
    /**
     * 파일 유효성 검사 결과를 상세 정보와 함께 반환
     */
    @Override
    public ValidationResult validateFileWithDetails(MultipartFile file, String[] allowedExtensions, long maxSize) {
        if (file == null || file.isEmpty()) {
            return new ValidationResult(false, "파일이 선택되지 않았습니다.");
        }
        
        // 파일 크기 검증
        if (file.getSize() > maxSize) {
            return new ValidationResult(false, 
                String.format("파일 크기가 %dMB를 초과합니다: %s", maxSize / (1024 * 1024), file.getOriginalFilename()));
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return new ValidationResult(false, "파일명이 유효하지 않습니다.");
        }
        
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
            return new ValidationResult(false, "파일명에 확장자가 없습니다: " + originalFilename);
        }
        
        String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        
        // 허용된 확장자 검사
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return new ValidationResult(true, "파일 검증 성공");
            }
        }
        
        return new ValidationResult(false, 
            String.format("지원하지 않는 파일 형식입니다: %s (지원 형식: %s)", 
                extension, String.join(", ", allowedExtensions)));
    }
    
    @Override
    public String[] getAllowedExtensions(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "image" -> new String[]{"jpg", "jpeg", "png", "gif", "webp"};
            case "manual" -> new String[]{"pdf", "doc", "docx", "txt"};
            case "document" -> new String[]{"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"};
            default -> new String[]{"*"};
        };
    }
    
    @Override
    public long getMaxFileSize(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "image" -> 10 * 1024 * 1024; // 10MB
            case "manual", "document" -> 50 * 1024 * 1024; // 50MB
            default -> 100 * 1024 * 1024; // 100MB
        };
    }
    
    private String generateStoreFilename(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
    
    /**
     * 파일 타입을 코드값으로 변환
     * @param fileType 파일 타입 (예: "image", "manual", "PRODUCT_IMAGE", "PRODUCT_MANUAL")
     * @return 코드값 (예: "PRODUCT_IMAGE", "PRODUCT_MANUAL")
     */
    private String convertFileTypeToCodeValue(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "image", "product_image" -> "PRODUCT_IMAGE";
            case "manual", "product_manual" -> "PRODUCT_MANUAL";
            case "board_attach" -> "BOARD_ATTACH";
            case "member_profile" -> "MEMBER_PROFILE";
            case "review_image" -> "REVIEW_IMAGE";
            case "qna_attach" -> "QNA_ATTACH";
            case "report_attach" -> "REPORT_ATTACH";
            case "system_doc" -> "SYSTEM_DOC";
            default -> "OTHERS"; // 기본값
        };
    }
} 