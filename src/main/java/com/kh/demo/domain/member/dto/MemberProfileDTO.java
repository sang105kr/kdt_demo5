package com.kh.demo.domain.member.dto;

import lombok.Data;

/**
 * 회원 프로필 DTO
 */
@Data
public class MemberProfileDTO {
    private Long memberId;
    private String email;
    private String nickname;
    private String tel;
    private String gender;
    private String hobby;
    private String regionName;
    private String gubunName;
    private String profileImageUrl;
} 