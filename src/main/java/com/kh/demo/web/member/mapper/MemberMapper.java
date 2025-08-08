package com.kh.demo.web.member.mapper;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.web.member.controller.page.form.JoinForm;
import org.springframework.stereotype.Component;

/**
 * 회원 관련 매퍼
 * Form과 Entity 간의 변환을 담당합니다.
 */
@Component
public class MemberMapper {
    
    private final CodeSVC codeSVC;
    
    public MemberMapper(CodeSVC codeSVC) {
        this.codeSVC = codeSVC;
    }
    
    /**
     * JoinForm을 Member 엔티티로 변환
     */
    public Member toMember(JoinForm joinForm) {
        if (joinForm == null) {
            return null;
        }
        
        Member member = new Member();
        
        // 기본 정보 복사
        member.setEmail(joinForm.getEmail());
        member.setPasswd(joinForm.getPasswd());
        member.setTel(joinForm.getTel());
        member.setNickname(joinForm.getNickname());
        
        // 성별 정보 (이미 code_id)
        member.setGender(joinForm.getGender());
        
        member.setBirthDate(joinForm.getBirthDate());
        
        // 지역 정보 (이미 code_id)
        member.setRegion(joinForm.getRegion());
        
        // 기본 회원 구분 (일반 회원) - 동적으로 code_id 조회
        Long normalMemberCodeId = codeSVC.getCodeId("MEMBER_TYPE", "NORMAL");
        member.setGubun(normalMemberCodeId != null ? normalMemberCodeId : 2L);
        
        return member;
    }
} 