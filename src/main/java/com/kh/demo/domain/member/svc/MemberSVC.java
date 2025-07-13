package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.shared.base.BaseSVC;
import java.util.Optional;

public interface MemberSVC extends BaseSVC<Member, Long> {
  // 가입
  Member join(Member member);

  // 회원 존재 유무
  boolean isMember(String email);

  // 회원 조회
  Optional<Member> findByMemberId(Long memberId);
  Optional<Member> findByEmail(String email);

  // 로그인 (이메일+비밀번호)
  Member loginOrThrow(String email, String passwd);
}
