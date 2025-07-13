package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.dao.CodeDAO;
import com.kh.demo.domain.entity.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Override
    @Transactional
    public Long save(Code code) {
        return codeDAO.save(code);
    }

    @Override
    @Transactional
    public int update(Code code) {
        return codeDAO.update(code);
    }

    @Override
    @Transactional
    public int delete(Long codeId) {
        return codeDAO.delete(codeId);
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
} 