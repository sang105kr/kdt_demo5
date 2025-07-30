package com.kh.demo.admin.controller.api;

import com.kh.demo.admin.form.code.CodeAddForm;
import com.kh.demo.admin.form.code.CodeEditForm;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.common.exception.BusinessValidationException;
import com.kh.demo.domain.common.exception.EntityNotFoundException;
import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.web.common.controller.api.response.ApiResponse;
import com.kh.demo.web.common.controller.api.response.ApiResponseCode;
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> addCode(
            @Valid @RequestBody CodeAddForm form,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("fieldErrors", bindingResult.getFieldErrors());
            ApiResponse<Map<String, Object>> response = ApiResponse.withDetails(
                ApiResponseCode.VALIDATION_ERROR, 
                errorDetails, 
                null
            );
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 비즈니스 검증
            validateCodeAddForm(form);
            
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
            
            Map<String, Object> data = new HashMap<>();
            data.put("codeId", codeId);
            data.put("gcode", form.getGcode());
            data.put("code", form.getCode());
            data.put("decode", form.getDecode());
            
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (BusinessValidationException e) {
            log.error("코드 등록 비즈니스 검증 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("코드 등록 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 코드 수정
     */
    @PutMapping("/{codeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> editCode(
            @PathVariable Long codeId,
            @Valid @RequestBody CodeEditForm form,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("fieldErrors", bindingResult.getFieldErrors());
            ApiResponse<Map<String, Object>> response = ApiResponse.withDetails(
                ApiResponseCode.VALIDATION_ERROR, 
                errorDetails, 
                null
            );
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 기존 코드 조회
            Code existingCode = codeSVC.findById(codeId, true);
            if (existingCode == null) {
                throw new EntityNotFoundException("Code", codeId);
            }
            
            // 비즈니스 검증
            validateCodeEditForm(form, codeId);
            
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
                Map<String, Object> data = new HashMap<>();
                data.put("codeId", codeId);
                data.put("gcode", form.getGcode());
                data.put("code", form.getCode());
                data.put("decode", form.getDecode());
                data.put("updatedAt", existingCode.getUdate());
                
                ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
                return ResponseEntity.ok(response);
            }
            
        } catch (EntityNotFoundException e) {
            log.error("코드 수정 - 엔티티 없음: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (BusinessValidationException e) {
            log.error("코드 수정 비즈니스 검증 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("코드 수정 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 코드 삭제
     */
    @DeleteMapping("/{codeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCode(@PathVariable Long codeId) {
        
        try {
            // 기존 코드 조회
            Code existingCode = codeSVC.findById(codeId, true);
            if (existingCode == null) {
                throw new EntityNotFoundException("Code", codeId);
            }
            
            // 비즈니스 검증 - 하위 코드가 있는지 확인
            List<Code> subCodes = codeSVC.findByPcode(codeId);
            if (!subCodes.isEmpty()) {
                throw new BusinessValidationException("하위 코드가 존재하여 삭제할 수 없습니다.");
            }
            
            int result = codeSVC.delete(codeId);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("codeId", codeId);
                data.put("deletedAt", LocalDateTime.now());
                
                ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
                return ResponseEntity.ok(response);
            }
            
        } catch (EntityNotFoundException e) {
            log.error("코드 삭제 - 엔티티 없음: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (BusinessValidationException e) {
            log.error("코드 삭제 비즈니스 검증 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("코드 삭제 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 그룹코드별 코드 목록 조회
     */
    @GetMapping("/gcode/{gcode}")
    public ResponseEntity<ApiResponse<List<Code>>> getCodesByGcode(@PathVariable String gcode) {
        
        try {
            List<Code> codes = codeSVC.findByGcode(gcode);
            
            ApiResponse<List<Code>> response = ApiResponse.of(ApiResponseCode.SUCCESS, codes);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("그룹코드별 코드 조회 실패", e);
            ApiResponse<List<Code>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 코드 중복 확인
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDuplicate(
            @RequestParam String gcode,
            @RequestParam String code,
            @RequestParam(required = false) Long excludeCodeId) {
        
        try {
            boolean exists = codeSVC.existsByGcodeAndCode(gcode, code);
            
            // 수정인 경우 자기 자신은 제외
            if (exists && excludeCodeId != null) {
                Code existingCode = codeSVC.findByGcodeAndCode(gcode, code).orElse(null);
                if (existingCode != null && existingCode.getCodeId().equals(excludeCodeId)) {
                    exists = false;
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("exists", exists);
            data.put("gcode", gcode);
            data.put("code", code);
            data.put("message", exists ? "이미 존재하는 코드입니다." : "사용 가능한 코드입니다.");
            
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.SUCCESS, data);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("코드 중복 확인 실패", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 코드 등록 폼 검증
     */
    private void validateCodeAddForm(CodeAddForm form) {
        // 그룹코드와 코드값 중복 확인
        if (codeSVC.existsByGcodeAndCode(form.getGcode(), form.getCode())) {
            throw new BusinessValidationException("이미 존재하는 코드입니다.");
        }
        
        // 상위코드가 있는 경우 해당 코드가 존재하는지 확인
        if (form.getPcode() != null) {
            Code parentCode = codeSVC.findById(form.getPcode(), true);
            if (parentCode == null) {
                throw new BusinessValidationException("상위코드가 존재하지 않습니다.");
            }
        }
    }

    /**
     * 코드 수정 폼 검증
     */
    private void validateCodeEditForm(CodeEditForm form, Long codeId) {
        // 그룹코드와 코드값 중복 확인 (자기 자신 제외)
        if (codeSVC.existsByGcodeAndCode(form.getGcode(), form.getCode())) {
            Code existingCode = codeSVC.findByGcodeAndCode(form.getGcode(), form.getCode()).orElse(null);
            if (existingCode != null && !existingCode.getCodeId().equals(codeId)) {
                throw new BusinessValidationException("이미 존재하는 코드입니다.");
            }
        }
        
        // 상위코드가 있는 경우 해당 코드가 존재하는지 확인
        if (form.getPcode() != null) {
            Code parentCode = codeSVC.findById(form.getPcode(), true);
            if (parentCode == null) {
                throw new BusinessValidationException("상위코드가 존재하지 않습니다.");
            }
            
            // 자기 자신을 상위코드로 설정할 수 없음
            if (form.getPcode().equals(codeId)) {
                throw new BusinessValidationException("자기 자신을 상위코드로 설정할 수 없습니다.");
            }
        }
    }
} 