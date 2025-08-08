package com.kh.demo.domain.common.svc;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.domain.common.dao.CodeDAO;
import com.kh.demo.domain.common.entity.Code;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    
    // 캐시 저장소
    private final Map<String, Long> codeIdCache = new ConcurrentHashMap<>();
    private final Map<String, String> codeValueCache = new ConcurrentHashMap<>();
    private final Map<String, String> codeDecodeCache = new ConcurrentHashMap<>();
    private final Map<String, List<Code>> codeListCache = new ConcurrentHashMap<>();
    private final Map<String, Map<Long, String>> codeDecodeMapCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 캐시 새로고침
     */
    @Override
    public void refreshCache() {
        log.info("코드 캐시 새로고침 시작");
        
        // 기존 캐시 클리어
        codeIdCache.clear();
        codeValueCache.clear();
        codeDecodeCache.clear();
        codeListCache.clear();
        codeDecodeMapCache.clear();
        
        // 모든 코드 조회
        List<Code> allCodes = codeDAO.findAll();
        
        // 캐시에 데이터 저장
        for (Code code : allCodes) {
            String gcodeCodeKey = code.getGcode() + ":" + code.getCode();
            String gcodeCodeIdKey = code.getGcode() + ":" + code.getCodeId();
            
            // codeId 캐시
            codeIdCache.put(gcodeCodeKey, code.getCodeId());
            
            // codeValue 캐시
            codeValueCache.put(gcodeCodeIdKey, code.getCode());
            
            // codeDecode 캐시
            codeDecodeCache.put(gcodeCodeIdKey, code.getDecode());
        }
        
        // gcode별 코드 리스트 캐시
        Map<String, List<Code>> codesByGcode = allCodes.stream()
            .collect(Collectors.groupingBy(Code::getGcode));
        
        for (Map.Entry<String, List<Code>> entry : codesByGcode.entrySet()) {
            String gcode = entry.getKey();
            List<Code> codes = entry.getValue();
            
            // 코드 리스트 캐시
            codeListCache.put(gcode, codes);
            
            // 코드 디코드 맵 캐시
            Map<Long, String> decodeMap = codes.stream()
                .collect(Collectors.toMap(Code::getCodeId, Code::getDecode));
            codeDecodeMapCache.put(gcode, decodeMap);
        }
        
        log.info("코드 캐시 새로고침 완료: {} 개 코드", allCodes.size());
    }

    @Override
    @Transactional
    public Long save(Code code) {
        // 비즈니스 로직: 코드 중복 검증
        if (getCodeId(code.getGcode(), code.getCode()) != null) {
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
        
        Long savedCodeId = codeDAO.save(code);
        
        // 캐시 새로고침
        refreshCache();
        
        return savedCodeId;
    }

    @Override
    @Transactional
    public int update(Code code) {
        // 비즈니스 로직: 코드 존재 여부 확인 (캐시에서 확인)
        List<Code> allCodes = codeListCache.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        boolean codeExists = allCodes.stream()
            .anyMatch(c -> c.getCodeId().equals(code.getCodeId()));
        if (!codeExists) {
            throw new BusinessValidationException("코드번호: " + code.getCodeId() + "를 찾을 수 없습니다.");
        }
        
        // 비즈니스 로직: 코드 중복 검증 (자신 제외)
        Long existingCodeId = getCodeId(code.getGcode(), code.getCode());
        if (existingCodeId != null && !existingCodeId.equals(code.getCodeId())) {
            throw new BusinessValidationException("이미 존재하는 코드입니다.");
        }
        
        // 비즈니스 로직: 코드 경로 자동 생성
        if (code.getCodePath() == null) {
            code.setCodePath(generateCodePath(code));
        }
        
        // 수정일시 자동 설정
        code.setUdate(LocalDateTime.now());
        
        int result = codeDAO.updateById(code.getCodeId(), code);
        
        // 캐시 새로고침
        refreshCache();
        
        return result;
    }

    @Override
    @Transactional
    public int delete(Long codeId) {
        // 비즈니스 로직: 코드 존재 여부 확인 (캐시에서 확인)
        List<Code> allCodes = codeListCache.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        boolean codeExists = allCodes.stream()
            .anyMatch(c -> c.getCodeId().equals(codeId));
        if (!codeExists) {
            throw new BusinessValidationException("코드번호: " + codeId + "를 찾을 수 없습니다.");
        }
        
        int result = codeDAO.deleteById(codeId);
        
        // 캐시 새로고침
        refreshCache();
        
        return result;
    }



    @Override
    public Long getCodeId(String gcode, String code) {
        String key = gcode + ":" + code;
        Long codeId = codeIdCache.get(key);
        
        if (codeId == null) {
            log.warn("캐시에서 코드 ID를 찾을 수 없음: gcode={}, code={}", gcode, code);
            // 캐시가 비어있을 수 있으므로 데이터베이스에서 직접 조회
            try {
                Optional<Code> codeOpt = codeDAO.findByGcodeAndCode(gcode, code);
                if (codeOpt.isPresent()) {
                    codeId = codeOpt.get().getCodeId();
                    // 캐시에 추가
                    codeIdCache.put(key, codeId);
                    log.info("데이터베이스에서 코드 ID 조회 성공: gcode={}, code={}, codeId={}", gcode, code, codeId);
                } else {
                    log.error("데이터베이스에서도 코드를 찾을 수 없음: gcode={}, code={}", gcode, code);
                }
            } catch (Exception e) {
                log.error("코드 ID 조회 중 오류 발생: gcode={}, code={}", gcode, code, e);
            }
        } else {
            log.debug("캐시에서 코드 ID 조회 성공: gcode={}, code={}, codeId={}", gcode, code, codeId);
        }
        
        return codeId;
    }



    // 캐싱 기능 구현
    @Override
    public String getCodeValue(String gcode, Long codeId) {
        String key = gcode + ":" + codeId;
        return codeValueCache.get(key);
    }

    @Override
    public String getCodeDecode(String gcode, Long codeId) {
        String key = gcode + ":" + codeId;
        return codeDecodeCache.get(key);
    }

    @Override
    public Map<Long, String> getCodeDecodeMap(String gcode) {
        return codeDecodeMapCache.get(gcode);
    }

    @Override
    public List<Code> getCodeList(String gcode) {
        return codeListCache.get(gcode);
    }

    // 관리자 기능용 새 메서드들
    @Override
    public Page<Code> findCodesWithPaging(String gcode, String searchText, Pageable pageable) {
        // 캐시에서 모든 코드 조회
        List<Code> allCodes = codeListCache.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
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
        // 캐시에서 모든 gcode 조회
        return codeListCache.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public List<Code> findRootCodes() {
        // 캐시에서 모든 코드 조회하여 필터링
        return codeListCache.values().stream()
            .flatMap(List::stream)
            .filter(code -> code.getPcode() == null || code.getPcode() == 0)
            .collect(Collectors.toList());
    }

    @Override
    public List<Code> findRootCodesExcluding(Long excludeCodeId) {
        // 캐시에서 모든 코드 조회하여 필터링
        return codeListCache.values().stream()
            .flatMap(List::stream)
            .filter(code -> (code.getPcode() == null || code.getPcode() == 0) && 
                          !code.getCodeId().equals(excludeCodeId))
            .collect(Collectors.toList());
    }

    // 비즈니스 로직: 코드 경로 생성
    private String generateCodePath(Code code) {
        if (code.getPcode() == null || code.getPcode() == 0) {
            return "/" + code.getCode();
        }
        
        // 캐시에서 부모 코드 찾기
        Optional<Code> parentCode = codeListCache.values().stream()
            .flatMap(List::stream)
            .filter(c -> c.getCodeId().equals(code.getPcode()))
            .findFirst();
        if (parentCode.isPresent()) {
            String parentPath = parentCode.get().getCodePath();
            return parentPath + "/" + code.getCode();
        }
        
        return "/" + code.getCode();
    }
} 