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
    Long getCodeId(String gcode, String code);
    String getDecodeById(Long codeId);
    
    // 그룹코드별 하위코드만 조회 (상위코드 제외)
    List<Code> findSubCodesByGcode(String gcode);
    
    // 그룹코드별 활성 하위코드만 조회 (상위코드 제외)
    List<Code> findActiveSubCodesByGcode(String gcode);
} 