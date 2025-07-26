package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dao.CodeDAO;
import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.common.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        
        // 수정일시 자동 설정
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
        
        // 비즈니스 로직: 하위 코드 존재 여부 확인
        List<Code> subCodes = codeDAO.findByPcode(codeId);
        if (!subCodes.isEmpty()) {
            throw new BusinessValidationException("하위 코드가 존재하므로 삭제할 수 없습니다.");
        }
        
        return codeDAO.deleteById(codeId);
    }

    @Override
    public Optional<Code> findById(Long codeId) {
        return codeDAO.findById(codeId);
    }

    @Override
    public Code findById(Long codeId, boolean throwIfNotFound) {
        Optional<Code> code = codeDAO.findById(codeId);
        if (throwIfNotFound && !code.isPresent()) {
            throw new BusinessValidationException("코드번호: " + codeId + "를 찾을 수 없습니다.");
        }
        return code.orElse(null);
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
    public Optional<Code> findByGcodeAndCode(String gcode, String code) {
        return codeDAO.findByGcodeAndCode(gcode, code);
    }

    // 관리자 기능용 새 메서드들
    @Override
    public Page<Code> findCodesWithPaging(String gcode, String searchText, Pageable pageable) {
        List<Code> allCodes = codeDAO.findAll();
        
        // 필터링
        List<Code> filteredCodes = allCodes.stream()
            .filter(code -> {
                boolean matchGcode = gcode == null || gcode.isEmpty() || code.getGcode().contains(gcode);
                boolean matchSearch = searchText == null || searchText.isEmpty() || 
                    code.getCode().contains(searchText) || code.getDecode().contains(searchText);
                return matchGcode && matchSearch;
            })
            .collect(Collectors.toList());
        
        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCodes.size());
        
        if (start > filteredCodes.size()) {
            return new PageImpl<>(List.of(), pageable, filteredCodes.size());
        }
        
        List<Code> pagedCodes = filteredCodes.subList(start, end);
        return new PageImpl<>(pagedCodes, pageable, filteredCodes.size());
    }

    @Override
    public List<String> getAllGcodes() {
        List<Code> allCodes = codeDAO.findAll();
        return allCodes.stream()
            .map(Code::getGcode)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public List<Code> findRootCodes() {
        List<Code> allCodes = codeDAO.findAll();
        return allCodes.stream()
            .filter(code -> code.getPcode() == null || code.getPcode() == 0)
            .collect(Collectors.toList());
    }

    @Override
    public List<Code> findRootCodesExcluding(Long excludeCodeId) {
        List<Code> allCodes = codeDAO.findAll();
        return allCodes.stream()
            .filter(code -> (code.getPcode() == null || code.getPcode() == 0) && 
                          !code.getCodeId().equals(excludeCodeId))
            .collect(Collectors.toList());
    }

    // 비즈니스 로직: 코드 경로 생성
    private String generateCodePath(Code code) {
        if (code.getPcode() == null || code.getPcode() == 0) {
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