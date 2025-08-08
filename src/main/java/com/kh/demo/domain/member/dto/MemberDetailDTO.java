package com.kh.demo.domain.member.dto;

import com.kh.demo.domain.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원 상세 정보 DTO
 * 조인으로 코드 정보와 취미 정보를 포함하여 조회
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberDetailDTO extends BaseDTO {
    
    // Member 기본 정보
    private Long memberId;
    private String email;
    private String tel;
    private String nickname;
    private LocalDate birthDate;
    private byte[] pic;
    private String statusReason;
    private LocalDateTime statusChangedAt;
    
    // 코드 참조 필드들 (ID)
    private Long gender;
    private Long region;
    private Long gubun;
    private Long status;
    
    // 코드 decode 값들 (조인으로 조회)
    private String genderName;      // 성별명 (남자/여자)
    private String regionName;      // 지역명 (서울/부산/대구/울산)
    private String gubunName;       // 회원구분명 (일반/우수/관리자1/관리자2)
    private String statusName;      // 상태명 (활성/정지/탈퇴/대기)
    
    // 주소 정보
    private String address;
    private String addressDetail;
    private String zipcode;
    
    // 취미 정보 (1:N 관계)
    private List<com.kh.demo.domain.member.dto.MemberHobbyDTO> hobbies;
    
    /**
     * 프로필 이미지 존재 여부 확인
     */
    public boolean hasProfileImage() {
        return pic != null && pic.length > 0;
    }
    
    /**
     * 취미 이름들을 콤마로 구분된 문자열로 반환
     */
    public String getHobbyNames() {
        if (hobbies == null || hobbies.isEmpty()) {
            return "";
        }
        return hobbies.stream()
                .map(com.kh.demo.domain.member.dto.MemberHobbyDTO::getHobbyName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}