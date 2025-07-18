package com.kh.demo.web.page;

import com.kh.demo.domain.common.svc.UploadFileSVC;
import com.kh.demo.domain.common.entity.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadFileSVC uploadFileSVC;

    @Value("${file.upload.path}")
    private String uploadPath;
    
    // 파일 타입 코드 상수 (AdminProductController와 동일)
    private static final Long PRODUCT_IMAGE_CODE = 7L; // PRODUCT_IMAGE
    private static final Long PRODUCT_MANUAL_CODE = 8L; // PRODUCT_MANUAL

    /**
     * 파일 다운로드/조회
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        log.info("파일 요청: {}", filename);
        
        try {
            // 파일 경로 생성
            Path filePath = Paths.get(uploadPath, filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                log.warn("물리적 파일이 존재하지 않습니다: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 파일 확장자로 Content-Type 결정
            String contentType = determineContentTypeFromFilename(filename);
            
            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            
            log.info("파일 전송 완료: {}", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            log.error("파일 URL 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 파일명에서 Content-Type 결정
     */
    private String determineContentTypeFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "application/octet-stream";
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            case "ppt", "pptx" -> "application/vnd.ms-powerpoint";
            default -> "application/octet-stream";
        };
    }
} 