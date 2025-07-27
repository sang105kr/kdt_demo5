package com.kh.demo.admin.controller.api;

import com.kh.demo.admin.form.code.CodeAddForm;
import com.kh.demo.admin.form.code.CodeEditForm;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.svc.CodeSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 코드 관리 API 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/codes")
@RestController
public class AdminCodeApiController {

    private final CodeSVC codeSVC;

    /**
     * 코드 등록
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCode(
            @Valid @RequestBody CodeAddForm form,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "입력값이 올바르지 않습니다.");
            response.put("errors", bindingResult.getFieldErrors());
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Form을 Entity로 변환
            Code code = new Code();
            code.setGcode(form.getGcode());
            code.setCode(form.getCode());
            code.setDecode(form.getDecode());
            code.setPcode(form.getPcode());
            code.setSortOrder(form.getSortOrder());
            code.setUseYn(form.getUseYn());
            code.setCodeLevel(form.getPcode() == null ? 1 : 2); // 임시로 레벨 설정
            
            Long codeId = codeSVC.save(code);
            
            response.put("success", true);
            response.put("message", "코드가 성공적으로 등록되었습니다.");
            response.put("codeId", codeId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("코드 등록 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 코드 수정
     */
    @PutMapping("/{codeId}")
    public ResponseEntity<Map<String, Object>> editCode(
            @PathVariable Long codeId,
            @Valid @RequestBody CodeEditForm form,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "입력값이 올바르지 않습니다.");
            response.put("errors", bindingResult.getFieldErrors());
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 기존 코드 조회
            Code existingCode = codeSVC.findById(codeId, true);
            
            // Form 데이터로 업데이트
            existingCode.setGcode(form.getGcode());
            existingCode.setCode(form.getCode());
            existingCode.setDecode(form.getDecode());
            existingCode.setPcode(form.getPcode());
            existingCode.setSortOrder(form.getSortOrder());
            existingCode.setUseYn(form.getUseYn());
            existingCode.setUdate(LocalDateTime.now());
            
            int result = codeSVC.update(existingCode);
            
            if (result > 0) {
                response.put("success", true);
                response.put("message", "코드가 성공적으로 수정되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "코드 수정에 실패했습니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("코드 수정 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 코드 삭제
     */
    @DeleteMapping("/{codeId}")
    public ResponseEntity<Map<String, Object>> deleteCode(@PathVariable Long codeId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = codeSVC.delete(codeId);
            
            if (result > 0) {
                response.put("success", true);
                response.put("message", "코드가 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "코드 삭제에 실패했습니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("코드 삭제 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 그룹코드별 코드 목록 조회
     */
    @GetMapping("/gcode/{gcode}")
    public ResponseEntity<Map<String, Object>> getCodesByGcode(@PathVariable String gcode) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Code> codes = codeSVC.findByGcode(gcode);
            
            response.put("success", true);
            response.put("codes", codes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("그룹코드별 코드 조회 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 코드 중복 확인
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Object>> checkDuplicate(
            @RequestParam String gcode,
            @RequestParam String code,
            @RequestParam(required = false) Long excludeCodeId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = codeSVC.existsByGcodeAndCode(gcode, code);
            
            // 수정인 경우 자기 자신은 제외
            if (exists && excludeCodeId != null) {
                Code existingCode = codeSVC.findByGcodeAndCode(gcode, code).orElse(null);
                if (existingCode != null && existingCode.getCodeId().equals(excludeCodeId)) {
                    exists = false;
                }
            }
            
            response.put("success", true);
            response.put("exists", exists);
            response.put("message", exists ? "이미 존재하는 코드입니다." : "사용 가능한 코드입니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("코드 중복 확인 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 