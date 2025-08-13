package com.kh.demo.web.common.controller.page;

import com.kh.demo.domain.common.entity.UploadFile;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.common.svc.UploadFileSVC;
import com.kh.demo.domain.common.util.FileUtils;
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
    private final CodeSVC codeSVC;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 파일 다운로드/조회
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        log.info("파일 요청: {}", filename);
        
        try {
            // 1. DB에서 파일 정보 조회
            Optional<UploadFile> fileOpt = uploadFileSVC.findByStoreFilename(filename);
            Resource resource = null;
            String folderName = "root";
            
            if (fileOpt.isPresent()) {
                // DB에 파일 정보가 있는 경우
                UploadFile uploadFile = fileOpt.get();
                Long codeId = uploadFile.getCode();
                
                log.info("DB에서 파일 정보 찾음: codeId={}, uploadFilename={}", codeId, uploadFile.getUploadFilename());
                
                // 2. 코드 ID를 기반으로 폴더명 생성
                folderName = FileUtils.generateFolderNameFromCodeId(codeId, codeSVC);
                Path filePath = Paths.get(uploadPath, folderName, filename);
                resource = new UrlResource(filePath.toUri());
                
                log.info("코드 기반 폴더에서 파일 찾기 시도: {}/{}", folderName, filename);
                
                // 3. 파일이 존재하지 않으면 기본 경로에서도 확인
                if (!resource.exists()) {
                    log.warn("코드 기반 폴더에서 파일을 찾을 수 없습니다: {}/{}, 기본 경로에서 확인", folderName, filename);
                    Path defaultPath = Paths.get(uploadPath, filename);
                    resource = new UrlResource(defaultPath.toUri());
                    folderName = "root";
                }
            } else {
                // DB에 파일 정보가 없는 경우 (기존 파일 호환성)
                log.warn("DB에서 파일 정보를 찾을 수 없습니다: {}, 기본 경로에서 확인", filename);
                Path defaultPath = Paths.get(uploadPath, filename);
                resource = new UrlResource(defaultPath.toUri());
            }
            
            // 4. 파일이 존재하는지 최종 확인
            if (!resource.exists()) {
                log.warn("파일을 찾을 수 없습니다: {} (폴더: {})", filename, folderName);
                return ResponseEntity.notFound().build();
            }
            
            // 5. 파일 확장자로 Content-Type 결정
            String contentType = determineContentTypeFromFilename(filename);
            
            // 6. 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            
            // 7. 파일명 결정 (DB 정보가 있으면 원본 파일명, 없으면 저장 파일명)
            String displayFilename = fileOpt.map(UploadFile::getUploadFilename).orElse(filename);
            
            // 이미지 파일은 인라인 표시, 나머지는 다운로드
            if (isImageFile(filename)) {
                headers.setContentDispositionFormData("inline", displayFilename);
                log.info("이미지 파일 전송: {} (폴더: {})", filename, folderName);
            } else {
                headers.setContentDispositionFormData("attachment", displayFilename);
                log.info("다운로드 파일 전송: {} (폴더: {})", filename, folderName);
            }
            
            log.info("파일 전송 완료: {} (폴더: {})", filename, folderName);
            
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
     * 이미지 파일인지 확인
     */
    private boolean isImageFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return extension.matches("(jpg|jpeg|png|gif|webp)");
    }
    
    /**
     * 다운로드 요청인지 확인 (간단한 구현)
     */
    private boolean isDownloadRequest() {
        // 실제로는 요청 헤더나 파라미터를 확인해야 함
        // 현재는 모든 요청을 다운로드로 처리
        return true;
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