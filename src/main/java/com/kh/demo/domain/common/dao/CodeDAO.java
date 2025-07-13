package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.entity.Code;
import com.kh.demo.domain.shared.base.BaseDAO;
import java.util.List;
import java.util.Optional;

public interface CodeDAO extends BaseDAO<Code, Long> {
    
    // 그룹코드별 코드 목록 조회
    List<Code> findByGcode(String gcode);
    
    // 그룹코드별 활성 코드 목록 조회
    List<Code> findActiveByGcode(String gcode);
    
    // 상위코드별 하위 코드 목록 조회
    List<Code> findByPcode(Long pcode);
    
    // 코드 경로별 코드 목록 조회
    List<Code> findByCodePath(String codePath);
    
    // 코드 존재 여부 확인
    boolean existsByGcodeAndCode(String gcode, String code);
    
    // 그룹코드별 코드 개수 조회
    int countByGcode(String gcode);
} 