package com.kh.demo.domain.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 처리 공통 유틸리티
 */
@Slf4j
@Component
public class FileUtils {
    
    /**
     * 물리적 파일 삭제
     * @param uploadPath 업로드 경로
     * @param storeFilename 저장된 파일명
     * @return 삭제 성공 여부
     */
    public static boolean deletePhysicalFile(String uploadPath, String storeFilename) {
        if (storeFilename == null || storeFilename.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path filePath = Paths.get(uploadPath, storeFilename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("물리적 파일 삭제 완료: {}", storeFilename);
                return true;
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", storeFilename);
                return false;
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", storeFilename, e);
            return false;
        }
    }
    
    /**
     * 디렉토리 생성
     * @param uploadPath 업로드 경로
     * @return 생성 성공 여부
     */
    public static boolean createDirectoryIfNotExists(String uploadPath) {
        try {
            Path path = Paths.get(uploadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("업로드 디렉토리 생성: {}", uploadPath);
            }
            return true;
        } catch (IOException e) {
            log.error("디렉토리 생성 실패: {}", uploadPath, e);
            return false;
        }
    }
    
    /**
     * 파일 확장자 검증
     * @param filename 파일명
     * @param allowedExtensions 허용된 확장자 목록
     * @return 허용된 확장자인지 여부
     */
    public static boolean isValidFileExtension(String filename, String... allowedExtensions) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // "*"가 포함되어 있으면 모든 파일 허용
        for (String allowedExt : allowedExtensions) {
            if ("*".equals(allowedExt)) {
                return true;
            }
        }
        
        String extension = getFileExtension(filename);
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 파일 확장자 추출
     * @param filename 파일명
     * @return 확장자 (점 제외)
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 파일 크기 검증
     * @param fileSize 파일 크기 (bytes)
     * @param maxSize 최대 허용 크기 (bytes)
     * @return 허용된 크기인지 여부
     */
    public static boolean isValidFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    /**
     * 파일 경로 생성
     * @param uploadPath 업로드 경로
     * @param storeFilename 저장된 파일명
     * @return 전체 파일 경로
     */
    public static String generateFilePath(String uploadPath, String storeFilename) {
        if (uploadPath == null || storeFilename == null) {
            return null;
        }
        
        return Paths.get(uploadPath, storeFilename).toString();
    }
} 