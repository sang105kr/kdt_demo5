package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.entity.Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * 코드 서비스 인터페이스
 * 코드 관련 비즈니스 로직을 처리합니다.
 * 모든 코드 조회는 캐시 기반으로 동작합니다.
 */
public interface CodeSVC {
    // 기본 CRUD 작업
    Long save(Code code);
    int update(Code code);
    int delete(Long codeId);
    
    // 캐시 기반 코드 조회
    List<Code> getCodeList(String gcode);
    String getCodeValue(String gcode, Long codeId);
    String getCodeDecode(String gcode, Long codeId);
    Long getCodeId(String gcode, String code);
    Map<Long, String> getCodeDecodeMap(String gcode);
    
    // 캐시 관리
    void refreshCache();
    
    // 관리자 기능용 추가 메서드들
    Page<Code> findCodesWithPaging(String gcode, String searchText, Pageable pageable);
    List<String> getAllGcodes();
    List<Code> findRootCodes();
    List<Code> findRootCodesExcluding(Long excludeCodeId);
} 