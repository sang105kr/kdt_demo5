package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dao.CodeDAO;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.common.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

/**
 * 코드 서비스 구현체
 * 코드 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeSVCImpl implements CodeSVC {
    private final CodeDAO codeDAO;
    private final Map<String, Long> codeCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        List<Code> allCodes = codeDAO.findAll();
        for (Code code : allCodes) {
            String key = code.getGcode() + ":" + code.getCode();
            codeCache.put(key, code.getCodeId());
        }
    }

    @Override
    @Transactional
    public Long save(Code code) {
        // 비즈니스 로직: 코드 중복 검증
        if (existsByGcodeAndCode(code.getGcode(), code.getCode())) {
            throw new BusinessValidationException("이미 존재하는 코드입니다.");
        }
        
        // 비즈니스 로직: 코드 경로 자동 생성
        if (code.getCodePath() == null) {
            code.setCodePath(generateCodePath(code));
        }
        
        // 기본값 설정
        if (code.getCdate() == null) {
            code.setCdate(LocalDateTime.now());
        }
        if (code.getUdate() == null) {
            code.setUdate(LocalDateTime.now());
        }
        
        return codeDAO.save(code);
    }

    @Override
    @Transactional
    public int update(Code code) {
        // 비즈니스 로직: 코드 존재 여부 확인
        if (!codeDAO.findById(code.getCodeId()).isPresent()) {
            throw new BusinessValidationException("코드번호: " + code.getCodeId() + "를 찾을 수 없습니다.");
        }
        
        // 비즈니스 로직: 코드 중복 검증 (자신 제외)
        Optional<Code> existingCode = codeDAO.findByGcode(code.getGcode()).stream()
            .filter(c -> c.getCode().equals(code.getCode()) && !c.getCodeId().equals(code.getCodeId()))
            .findFirst();
        if (existingCode.isPresent()) {
            throw new BusinessValidationException("이미 존재하는 코드입니다.");
        }
        
        // 비즈니스 로직: 코드 경로 자동 생성
        if (code.getCodePath() == null) {
            code.setCodePath(generateCodePath(code));
        }
        
        code.setUdate(LocalDateTime.now());
        return codeDAO.updateById(code.getCodeId(), code);
    }

    @Override
    @Transactional
    public int delete(Long codeId) {
        // 비즈니스 로직: 코드 존재 여부 확인
        if (!codeDAO.findById(codeId).isPresent()) {
            throw new BusinessValidationException("코드번호: " + codeId + "를 찾을 수 없습니다.");
        }
        
        // 비즈니스 로직: 하위 코드가 있는지 확인
        Optional<Code> code = codeDAO.findById(codeId);
        if (code.isPresent() && countByGcode(code.get().getGcode()) > 1) {
            throw new BusinessValidationException("하위 코드가 존재하여 삭제할 수 없습니다.");
        }
        
        return codeDAO.deleteById(codeId);
    }

    @Override
    public Optional<Code> findById(Long codeId) {
        return codeDAO.findById(codeId);
    }

    @Override
    public List<Code> findByGcode(String gcode) {
        return codeDAO.findByGcode(gcode);
    }

    @Override
    public List<Code> findActiveByGcode(String gcode) {
        return codeDAO.findActiveByGcode(gcode);
    }

    @Override
    public List<Code> findByPcode(Long pcode) {
        return codeDAO.findByPcode(pcode);
    }

    @Override
    public List<Code> findByCodePath(String codePath) {
        return codeDAO.findByCodePath(codePath);
    }

    @Override
    public List<Code> findAll() {
        return codeDAO.findAll();
    }

    @Override
    public boolean existsByGcodeAndCode(String gcode, String code) {
        return codeDAO.existsByGcodeAndCode(gcode, code);
    }

    @Override
    public int countByGcode(String gcode) {
        return codeDAO.countByGcode(gcode);
    }

    @Override
    public Long getCodeId(String gcode, String code) {
        String key = gcode + ":" + code;
        return codeCache.get(key);
    }

    @Override
    public String getDecodeById(Long codeId) {
        if (codeId == null) return null;
        return codeDAO.findById(codeId)
                .map(Code::getDecode)
                .orElse("");
    }

    @Override
    public List<Code> findSubCodesByGcode(String gcode) {
        return codeDAO.findSubCodesByGcode(gcode);
    }

    @Override
    public List<Code> findActiveSubCodesByGcode(String gcode) {
        return codeDAO.findActiveSubCodesByGcode(gcode);
    }

    /**
     * 코드 경로 자동 생성
     * 상위 코드의 경로 + 현재 코드로 구성
     */
    private String generateCodePath(Code code) {
        if (code.getPcode() == null) {
            return "/" + code.getCode();
        }
        
        Optional<Code> parentCode = codeDAO.findById(code.getPcode());
        if (parentCode.isPresent()) {
            String parentPath = parentCode.get().getCodePath();
            return parentPath + "/" + code.getCode();
        }
        
        return "/" + code.getCode();
    }
} 