package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.common.base.BaseSVC;
import java.util.Optional;
import java.util.List;

public interface MemberSVC extends BaseSVC<Member, Long> {
  // 가입
  Member join(Member member);

  // 회원 존재 유무
  boolean isMember(String email);

  // 회원 조회
  Optional<Member> findByMemberId(Long memberId);
  Optional<Member> findByEmail(String email);

  // 로그인
  Member login(String email, String passwd);

  // 회원정보 수정
  int updateMember(Long memberId, Member member);

  // 회원 탈퇴
  int deleteMember(Long memberId);

  // 비밀번호 변경
  int changePasswd(Long memberId, String oldPasswd, String newPasswd);

  // 이메일 인증 코드 발송
  void sendVerificationCode(String email);

  // 이메일 인증 코드 확인
  boolean verifyEmailCode(String email, String verificationCode);

  // 비밀번호 재설정 토큰 발송
  void sendPasswordResetToken(String email);

  // 비밀번호 재설정
  boolean resetPassword(String token, String newPassword);

  // 이메일 인증 상태 확인
  boolean isEmailVerified(String email);

  // 아이디 찾기 (전화번호, 생년월일로 이메일 조회)
  Optional<String> findEmailByPhoneAndBirth(String phone, String birth);

  // 권한 체크 메서드들
  boolean isAdmin(Long memberId);
  boolean isVip(Long memberId);
  boolean hasPermission(Long memberId, String permission);
  
  // 회원 상태 관리 메서드들
  int updateMemberStatus(Long memberId, String status, String reason);
  boolean isActiveMember(Long memberId);
  boolean isSuspendedMember(Long memberId);
  boolean isWithdrawnMember(Long memberId);

  /**
   * 비밀번호 일치 여부 확인
   */
  boolean checkPassword(Long memberId, String rawPassword);

  /**
   * 프로필 이미지 업데이트
   */
  int updateProfileImage(Long memberId, byte[] imageData);

  /**
   * 프로필 이미지 삭제
   */
  int deleteProfileImage(Long memberId);

  int countByKeyword(String keyword);
  List<Member> findByKeywordWithPaging(String keyword, int pageNo, int pageSize);
}
