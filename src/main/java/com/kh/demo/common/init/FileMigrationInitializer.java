package com.kh.demo.common.init;

import com.kh.demo.domain.common.svc.UploadFileSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 파일 마이그레이션 초기화 클래스
 * 기존 파일들을 새로운 코드 기반 폴더 구조로 이동시킵니다.
 */
@Slf4j
@RequiredArgsConstructor
public class FileMigrationInitializer implements CommandLineRunner {

    private final UploadFileSVC uploadFileSVC;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void run(String... args) throws Exception {
        log.info("파일 마이그레이션 시작");
        
        try {
            // 기존 폴더들 확인 및 마이그레이션
            migrateFolder("image", "product_image");
            migrateFolder("manual", "product_manual");
            
            log.info("파일 마이그레이션 완료");
        } catch (Exception e) {
            log.error("파일 마이그레이션 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 폴더 마이그레이션
     */
    private void migrateFolder(String oldFolderName, String newFolderName) throws IOException {
        Path oldPath = Paths.get(uploadPath, oldFolderName);
        Path newPath = Paths.get(uploadPath, newFolderName);
        
        if (!Files.exists(oldPath)) {
            log.info("기존 폴더가 존재하지 않습니다: {}", oldPath);
            return;
        }
        
        // 새 폴더 생성
        if (!Files.exists(newPath)) {
            Files.createDirectories(newPath);
            log.info("새 폴더 생성: {}", newPath);
        }
        
        // 파일 이동
        List<Path> files = Files.list(oldPath).filter(Files::isRegularFile).toList();
        for (Path file : files) {
            Path targetFile = newPath.resolve(file.getFileName());
            Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 이동: {} -> {}", file, targetFile);
        }
        
        // 빈 폴더 삭제
        if (Files.list(oldPath).findAny().isEmpty()) {
            Files.delete(oldPath);
            log.info("빈 폴더 삭제: {}", oldPath);
        }
    }
}
