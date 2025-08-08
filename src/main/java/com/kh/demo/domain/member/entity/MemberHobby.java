package com.kh.demo.domain.member.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 회원 취미 매핑 엔티티
 * Member와 Code(HOBBY) 간의 다대다 관계를 위한 중간 테이블
 */
@Data
public class MemberHobby {
    private Long hobbyId;           // 취미 매핑 ID (PK)
    private Long memberId;          // 회원 ID (FK)
    private Long hobbyCodeId;       // 취미 코드 ID (FK, gcode='HOBBY')
    private LocalDateTime cdate;    // 생성일시
}