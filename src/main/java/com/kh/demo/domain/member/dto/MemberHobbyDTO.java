package com.kh.demo.domain.member.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 회원 취미 DTO
 * MemberHobby 엔티티와 Code 테이블을 조인한 결과
 */
@Data
public class MemberHobbyDTO {
    private Long hobbyId;           // 취미 매핑 ID
    private Long memberId;          // 회원 ID
    private Long hobbyCodeId;       // 취미 코드 ID
    private String hobbyCode;       // 취미 코드 (HIKING, SWIMMING, etc.)
    private String hobbyName;       // 취미명 (등산, 수영, etc.)
    private LocalDateTime cdate;    // 생성일시
}