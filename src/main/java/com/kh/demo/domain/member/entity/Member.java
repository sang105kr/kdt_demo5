package com.kh.demo.domain.member.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {
    private Long memberId;         // 내부 관리 아이디 (PK)
    private String email;          // 로그인 아이디 (UK)
    private String passwd;         // 로그인 비밀번호
    private String tel;            // 연락처
    private String nickname;       // 별칭
    private String gender;         // 성별 (M/F)
    private LocalDate birthDate;   // 생년월일
    private String hobby;          // 취미 (콤마로 구분된 code_id들)
    private Long region;           // 지역 (code_id 참조)
    private Long gubun;            // 회원구분 (code_id 참조)
    private Long status;         // 회원상태 (ACTIVE, SUSPENDED, WITHDRAWN, PENDING)
    private String statusReason;   // 상태 변경 사유
    private LocalDateTime statusChangedAt; // 상태 변경일시
    private byte[] pic;            // 사진 (바이트 배열)
    
    /**
     * 프로필 이미지 존재 여부 확인
     */
    public boolean hasProfileImage() {
        return pic != null && pic.length > 0;
    }
    
    /**
     * hobby code_id들을 decode 값으로 변환
     * @param codeDecodeMap HOBBY 그룹의 code_id -> decode 매핑
     * @return 콤마로 구분된 decode 값들
     */
    public String getHobbyDecodes(java.util.Map<Long, String> codeDecodeMap) {
        if (hobby == null || hobby.trim().isEmpty()) {
            return "";
        }
        
        return Arrays.stream(hobby.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .map(codeId -> codeDecodeMap.getOrDefault(codeId, "알 수 없음"))
                .collect(Collectors.joining(", "));
    }
    
    /**
     * hobby code_id들을 List로 반환
     * @return code_id 리스트
     */
    public List<Long> getHobbyCodeIds() {
        if (hobby == null || hobby.trim().isEmpty()) {
            return List.of();
        }
        
        return Arrays.stream(hobby.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
} 