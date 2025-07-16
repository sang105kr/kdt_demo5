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

  // 회원정보 수정
  void updateMember(Member member);

  // 비밀번호 변경
  void changePassword(Long memberId, String currentPassword, String newPassword);

  // 회원 탈퇴
  void withdraw(Long memberId);
  
  // 이메일 인증 코드 발송
  void sendVerificationCode(String email);
  
  // 이메일 인증 코드 확인
  boolean verifyEmailCode(String email, String verificationCode);
  
  // 비밀번호 재설정 토큰 발송
  void sendPasswordResetToken(String email);
  
  // 비밀번호 재설정
  void resetPassword(String token, String newPassword);
  
  // 이메일 인증 상태 확인
  boolean isEmailVerified(String email);
  
  // 아이디 찾기 (이메일로 회원 조회)
  Optional<Member> findMemberByEmail(String email);
  
  // 아이디 찾기 (전화번호, 생년월일로 이메일 조회)
  Optional<String> findEmailByPhoneAndBirth(String phone, String birth);
}
