package com.kh.demo.domain.member.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 회원 데이터 전송 객체
 */
@Data
public class MemberDTO {
    private Long memberId;
    private String email;
    private String tel;
    private String nickname;
    private String gender;
    private String hobby;
    private Long region;
    private Long gubun;
    private LocalDateTime cdate;
    private LocalDateTime udate;
} 