package com.kh.demo.web.member.controller.page.form;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 회원 정보 표시용 DTO
 * 엔티티를 직접 노출하지 않고 필요한 정보만 전달
 */
@Data
public class MemberDTO {
    private Long memberId;
    private String email;
    private String tel;
    private String nickname;
    private String gender;
    private String genderName;  // 디코드된 성별명
    private LocalDate birthDate;
    private String hobby;       // 원본 취미 코드들
    private List<String> hobbyNames;  // 디코드된 취미명들
    private Long region;        // 원본 지역 코드
    private String regionName;  // 디코드된 지역명
    private Long gubun;
    private String gubunName;   // 디코드된 회원구분명
    private Long status;
    private String statusName;  // 디코드된 상태명
    private byte[] pic;
    private Boolean hasProfileImage;
    
    // 주소 정보
    private String zipcode;         // 우편번호
    private String address;         // 기본주소
    private String addressDetail;   // 상세주소
    
    /**
     * 프로필 이미지 존재 여부 확인
     */
    public boolean hasProfileImage() {
        return pic != null && pic.length > 0;
    }
} 