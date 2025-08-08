package com.kh.demo.domain.product.svc;

import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.common.dao.UploadFileDAO;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * 상품 파일 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductFileService {

    private final UploadFileDAO uploadFileDAO;
    private final CodeSVC codeSVC;

    @Value("${file.upload.path}")
    private String uploadPath;

    // 파일 타입 그룹 코드
    private static final String FILE_TYPE_GCODE = "FILE_TYPE";

    /**
     * 상품 이미지 조회
     */
    public List<UploadFile> findProductImages(Long productId) {
        Long imageCodeId = getFileTypeCodeId("PRODUCT_IMAGE");
        return uploadFileDAO.findByCodeAndRid(imageCodeId, productId.toString());
    }

    /**
     * 상품 매뉴얼 조회
     */
    public List<UploadFile> findProductManuals(Long productId) {
        Long manualCodeId = getFileTypeCodeId("PRODUCT_MANUAL");
        return uploadFileDAO.findByCodeAndRid(manualCodeId, productId.toString());
    }

    /**
     * 상품 파일 저장
     */
    @Transactional
    public void saveProductFiles(Long productId, List<UploadFile> imageFiles, List<UploadFile> manualFiles) {
        if (imageFiles != null) {
            Long imageCodeId = getFileTypeCodeId("PRODUCT_IMAGE");
            for (UploadFile file : imageFiles) {
                file.setCode(imageCodeId);
                file.setRid(productId.toString());
                uploadFileDAO.save(file);
            }
        }
        
        if (manualFiles != null) {
            Long manualCodeId = getFileTypeCodeId("PRODUCT_MANUAL");
            for (UploadFile file : manualFiles) {
                file.setCode(manualCodeId);
                file.setRid(productId.toString());
                uploadFileDAO.save(file);
            }
        }
    }

    /**
     * 상품 파일 삭제
     */
    @Transactional
    public void deleteProductFiles(Long productId) {
        List<UploadFile> imageFiles = findProductImages(productId);
        List<UploadFile> manualFiles = findProductManuals(productId);
        
        for (UploadFile file : imageFiles) {
            deleteFile(file);
        }
        
        for (UploadFile file : manualFiles) {
            deleteFile(file);
        }
    }

    /**
     * 개별 파일 삭제
     */
    @Transactional
    public void deleteFile(UploadFile file) {
        // 물리적 파일 삭제
        deletePhysicalFile(file.getStoreFilename());
        
        // 데이터베이스에서 파일 정보 삭제
        uploadFileDAO.deleteById(file.getUploadfileId());
    }

    /**
     * 파일 ID 목록으로 파일 삭제
     */
    @Transactional
    public int deleteFiles(List<Long> fileIds) {
        int deletedCount = 0;
        
        for (Long fileId : fileIds) {
            try {
                UploadFile file = uploadFileDAO.findById(fileId).orElse(null);
                if (file != null) {
                    deleteFile(file);
                    deletedCount++;
                }
            } catch (Exception e) {
                log.error("파일 삭제 실패 - fileId: {}", fileId, e);
            }
        }
        
        return deletedCount;
    }

    /**
     * 물리적 파일 삭제
     */
    private boolean deletePhysicalFile(String storeFilename) {
        try {
            if (storeFilename == null || storeFilename.trim().isEmpty()) {
                return false;
            }

            // 파일 정보를 DB에서 조회하여 코드 ID를 가져와야 함
            // 임시로 기본 경로에서 삭제
            File file = new File(uploadPath, storeFilename);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("물리적 파일 삭제 완료: {}", storeFilename);
                } else {
                    log.warn("물리적 파일 삭제 실패: {}", storeFilename);
                }
                return deleted;
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", storeFilename);
                return false;
            }
        } catch (Exception e) {
            log.error("물리적 파일 삭제 중 오류 발생: {}", storeFilename, e);
            return false;
        }
    }
    
    /**
     * 코드 ID를 기반으로 물리적 파일 삭제
     */
    private boolean deletePhysicalFileByCodeId(Long codeId, String storeFilename) {
        try {
            if (storeFilename == null || storeFilename.trim().isEmpty()) {
                return false;
            }

            String folderName = FileUtils.generateFolderNameFromCodeId(codeId, codeSVC);
            String typeSpecificPath = Paths.get(uploadPath, folderName).toString();
            File file = new File(typeSpecificPath, storeFilename);
            
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("물리적 파일 삭제 완료: {}/{}", folderName, storeFilename);
                } else {
                    log.warn("물리적 파일 삭제 실패: {}/{}", folderName, storeFilename);
                }
                return deleted;
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}/{}", folderName, storeFilename);
                return false;
            }
        } catch (Exception e) {
            log.error("물리적 파일 삭제 중 오류 발생: {}", storeFilename, e);
            return false;
        }
    }

    /**
     * 파일 유효성 검사
     */
    public void validateFiles(List<UploadFile> imageFiles, List<UploadFile> manualFiles) {
        if (imageFiles != null) {
            for (UploadFile file : imageFiles) {
                validateFile(file);
            }
        }
        
        if (manualFiles != null) {
            for (UploadFile file : manualFiles) {
                validateFile(file);
            }
        }
    }

    /**
     * 개별 파일 유효성 검사
     */
    private void validateFile(UploadFile file) {
        if (file == null) {
            throw new IllegalArgumentException("파일 정보가 null입니다.");
        }
        
        if (file.getUploadFilename() == null || file.getUploadFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("업로드 파일명이 비어있습니다.");
        }
        
        if (file.getStoreFilename() == null || file.getStoreFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("저장 파일명이 비어있습니다.");
        }
        
        // 파일 크기 검사 (예: 10MB 제한)
        if (file.getFsize() != null) {
            try {
                long fileSize = Long.parseLong(file.getFsize());
                if (fileSize > 10 * 1024 * 1024) { // 10MB
                    throw new IllegalArgumentException("파일 크기가 10MB를 초과합니다: " + file.getUploadFilename());
                }
            } catch (NumberFormatException e) {
                log.warn("파일 크기 파싱 실패: {}", file.getFsize());
            }
        }
    }

    /**
     * 파일 경로 생성
     */
    public String generateFilePath(String storeFilename) {
        // 파일 정보를 DB에서 조회하여 코드 ID를 가져와야 함
        // 임시로 기본 경로 반환
        return FileUtils.generateFilePath(uploadPath, storeFilename);
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String storeFilename) {
        if (storeFilename == null || storeFilename.trim().isEmpty()) {
            return false;
        }
        
        // 파일 정보를 DB에서 조회하여 코드 ID를 가져와야 함
        // 임시로 기본 경로에서 확인
        File file = new File(uploadPath, storeFilename);
        return file.exists() && file.isFile();
    }
    
    /**
     * 코드 ID를 기반으로 파일 경로 생성
     */
    public String generateFilePathByCodeId(Long codeId, String storeFilename) {
        String folderName = FileUtils.generateFolderNameFromCodeId(codeId, codeSVC);
        String typeSpecificPath = Paths.get(uploadPath, folderName).toString();
        return FileUtils.generateFilePath(typeSpecificPath, storeFilename);
    }
    
    /**
     * 코드 ID를 기반으로 파일 존재 여부 확인
     */
    public boolean fileExistsByCodeId(Long codeId, String storeFilename) {
        if (storeFilename == null || storeFilename.trim().isEmpty()) {
            return false;
        }
        
        String folderName = FileUtils.generateFolderNameFromCodeId(codeId, codeSVC);
        String typeSpecificPath = Paths.get(uploadPath, folderName).toString();
        File file = new File(typeSpecificPath, storeFilename);
        return file.exists() && file.isFile();
    }
    
    /**
     * 파일 타입별 하위 디렉토리 경로 생성
     */
    private String createTypeSpecificPath(String basePath, String fileType) {
        String typeFolder = switch (fileType.toLowerCase()) {
            case "image" -> "images";
            case "manual" -> "manuals";
            case "document" -> "documents";
            case "profile" -> "profiles";
            case "board" -> "boards";
            case "product" -> "products";
            default -> "others";
        };
        
        return Paths.get(basePath, typeFolder).toString();
    }
    
    /**
     * 파일명으로부터 파일 타입을 추정
     */
    private String inferFileTypeFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "others";
        }
        
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> "image";
            case "pdf", "doc", "docx", "txt" -> "manual";
            case "xls", "xlsx", "ppt", "pptx" -> "document";
            default -> "others";
        };
    }

    /**
     * 파일 타입 코드 ID 조회
     */
    private Long getFileTypeCodeId(String code) {
        Long codeId = codeSVC.getCodeId(FILE_TYPE_GCODE, code);
        if (codeId != null) {
            return codeId;
        } else {
            log.error("파일 타입 코드를 찾을 수 없습니다: {}", code);
            throw new IllegalArgumentException("파일 타입 코드를 찾을 수 없습니다: " + code);
        }
    }
} 