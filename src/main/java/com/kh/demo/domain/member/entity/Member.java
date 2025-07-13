package com.kh.demo.domain.member.entity;

import com.kh.demo.domain.shared.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.sql.Blob;

@Data
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {
    private Long memberId;         // 내부 관리 아이디 (PK)
    private String email;          // 로그인 아이디 (UK)
    private String passwd;         // 로그인 비밀번호
    private String tel;            // 연락처
    private String nickname;       // 별칭
    private String gender;         // 성별 (M/F)
    private String hobby;          // 취미
    private Long region;           // 지역 (code_id 참조)
    private Long gubun;            // 회원구분 (code_id 참조)
    private Blob pic;              // 사진
} 