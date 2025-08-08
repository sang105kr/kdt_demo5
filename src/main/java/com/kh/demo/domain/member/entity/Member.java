package com.kh.demo.domain.member.entity;

import com.kh.demo.domain.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {
    private Long memberId;         // 내부 관리 아이디 (PK)
    private String email;          // 로그인 아이디 (UK)
    private String passwd;         // 로그인 비밀번호
    private String tel;            // 연락처
    private String nickname;       // 별칭
    private Long gender;           // 성별 (code_id 참조, gcode='GENDER')
    private LocalDate birthDate;   // 생년월일

    private Long region;           // 지역 (code_id 참조, gcode='REGION')
    private Long gubun;            // 회원구분 (code_id 참조, gcode='MEMBER_TYPE')
    private Long status;           // 회원상태 (code_id 참조, gcode='MEMBER_STATUS')
    private String statusReason;   // 상태 변경 사유
    private LocalDateTime statusChangedAt; // 상태 변경일시
    private byte[] pic;            // 사진 (바이트 배열)
    
    // 주소 정보
    private String address;
    private String addressDetail;
    private String zipcode;
    
    /**
     * 프로필 이미지 존재 여부 확인
     */
    public boolean hasProfileImage() {
        return pic != null && pic.length > 0;
    }
    

} 