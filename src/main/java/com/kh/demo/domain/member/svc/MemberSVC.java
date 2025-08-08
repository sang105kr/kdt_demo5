package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.common.base.BaseSVC;
import java.util.Optional;
import java.util.List;

public interface MemberSVC extends BaseSVC<Member, Long> {
  // 가입
  Member join(Member member);
  
  // 가입 (취미 포함)
  @org.springframework.transaction.annotation.Transactional
  Member joinWithHobbies(Member member, List<Long> hobbyCodeIds);

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
  
  // === 새로 추가된 메서드들 (코드 캐시 활용 및 취미 관리) ===
  
  /**
   * 회원 상세 정보 조회 (코드 decode 값 포함)
   */
  Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findMemberDetailById(Long memberId);
  
  /**
   * 회원 상세 정보 조회 (이메일로, 코드 decode 값 포함)
   */
  Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findMemberDetailByEmail(String email);
  
  /**
   * 회원 취미 목록 조회
   */
  List<com.kh.demo.domain.member.dto.MemberHobbyDTO> getMemberHobbies(Long memberId);
  
  /**
   * 회원 취미 추가
   */
  @org.springframework.transaction.annotation.Transactional
  Long addMemberHobby(Long memberId, Long hobbyCodeId);
  
  /**
   * 회원 취미 삭제
   */
  @org.springframework.transaction.annotation.Transactional
  int removeMemberHobby(Long memberId, String hobbyCode);
  
  /**
   * 회원 취미 전체 업데이트 (기존 취미 삭제 후 새로 추가)
   */
  @org.springframework.transaction.annotation.Transactional
  int updateMemberHobbies(Long memberId, List<Long> hobbyCodeIds);
  
  /**
   * 코드 decode 값 조회 헬퍼 메서드들
   */
  String getGenderName(Long genderId);
  String getRegionName(Long regionId);
  String getMemberTypeName(Long gubunId);
  String getMemberStatusName(Long statusId);
  List<Member> findByKeywordWithPaging(String keyword, int pageNo, int pageSize);
  List<Member> findAllWithPaging(int pageNo, int pageSize);
  int countByStatus(String status);
  List<Member> findByStatusWithPaging(String status, int pageNo, int pageSize);
  int countByStatusAndKeyword(String status, String keyword);
  List<Member> findByStatusAndKeywordWithPaging(String status, String keyword, int pageNo, int pageSize);

  // 별칭 중복 확인
  boolean isNicknameExists(String nickname);

  // === 신규 회원, VIP 회원, 휴면 회원 관련 메서드들 ===
  
  /**
   * 신규 회원 목록 조회 (최근 30일 내 가입)
   */
  List<Member> findNewMembersWithPaging(int pageNo, int pageSize);
  
  /**
   * 신규 회원 수 조회
   */
  int countNewMembers();
  
  /**
   * VIP 회원 목록 조회
   */
  List<Member> findVipMembersWithPaging(int pageNo, int pageSize);
  
  /**
   * VIP 회원 수 조회
   */
  int countVipMembers();
  
  /**
   * 휴면 회원 목록 조회
   */
  List<Member> findInactiveMembersWithPaging(int pageNo, int pageSize);
  
  /**
   * 휴면 회원 수 조회
   */
  int countInactiveMembers();
}
