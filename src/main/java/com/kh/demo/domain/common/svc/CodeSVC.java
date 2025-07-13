package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.entity.Code;
import java.util.List;
import java.util.Optional;

/**
 * 코드 서비스 인터페이스
 * 코드 관련 비즈니스 로직을 처리합니다.
 */
public interface CodeSVC {
    Long save(Code code);
    int update(Code code);
    int delete(Long codeId);
    Optional<Code> findById(Long codeId);
    List<Code> findByGcode(String gcode);
    List<Code> findActiveByGcode(String gcode);
    List<Code> findByPcode(Long pcode);
    List<Code> findByCodePath(String codePath);
    List<Code> findAll();
    boolean existsByGcodeAndCode(String gcode, String code);
    int countByGcode(String gcode);
} 