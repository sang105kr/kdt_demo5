package com.kh.demo.domain.member.dto;

import lombok.Data;

/**
 * 회원 검색 조건 DTO
 */
@Data
public class MemberSearchDTO {
    private String email;
    private String nickname;
    private String gender;
    private Long region;
    private Long gubun;
    private String searchType; // email, nickname, all
    private String searchKeyword;
} 