package com.kh.demo.domain.member.svc;

import com.kh.demo.common.exception.BusinessValidationException;
import com.kh.demo.common.exception.LoginFailException;

import com.kh.demo.domain.common.svc.CodeSVC;
import com.kh.demo.domain.member.dao.MemberDAO;
import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.entity.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSVCImpl implements MemberSVC {
  private final MemberDAO memberDAO;
  private final TokenSVC tokenSVC;
  private final EmailService emailService;
  private final CodeSVC codeSVC;  // CodeCache 대신 CodeSVC 사용

  @Override
  @Transactional
  public Long save(Member member) {
    // 비즈니스 로직: 이메일 중복 검증
    if (isMember(member.getEmail())) {
      throw new BusinessValidationException("이미 가입된 이메일입니다.");
    }
    
    // 비즈니스 로직: 비밀번호 암호화
    member.setPasswd(encryptPassword(member.getPasswd()));
    
    // 기본값 설정
    if (member.getCdate() == null) {
      member.setCdate(LocalDateTime.now());
    }
    if (member.getUdate() == null) {
      member.setUdate(LocalDateTime.now());
    }
    
    return memberDAO.save(member);
  }

  @Override
  public Optional<Member> findById(Long id) {
    return memberDAO.findById(id);
  }

  @Override
  public List<Member> findAll() {
    return memberDAO.findAll();
  }

  @Override
  public List<Member> findAll(int pageNo, int numOfRows) {
    return memberDAO.findAllWithPaging(pageNo, numOfRows);
  }

  @Override
  @Transactional
  public int updateById(Long id, Member member) {
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!memberDAO.findById(id).isPresent()) {
      throw new BusinessValidationException("회원번호: " + id + "를 찾을 수 없습니다.");
    }
    
    // 비즈니스 로직: 이메일 변경 시 중복 검증
    Optional<Member> existingMember = memberDAO.findByEmail(member.getEmail());
    if (existingMember.isPresent() && !existingMember.get().getMemberId().equals(id)) {
      throw new BusinessValidationException("이미 사용 중인 이메일입니다.");
    }
    
    // 비즈니스 로직: 비밀번호가 변경된 경우 암호화
    if (member.getPasswd() != null && !member.getPasswd().isEmpty()) {
      member.setPasswd(encryptPassword(member.getPasswd()));
    }
    
    member.setUdate(LocalDateTime.now());
    return memberDAO.updateById(id, member);
  }

  @Override
  @Transactional
  public int deleteById(Long id) {
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!memberDAO.findById(id).isPresent()) {
      throw new BusinessValidationException("회원번호: " + id + "를 찾을 수 없습니다.");
    }
    
    return memberDAO.deleteById(id);
  }

  @Override
  public int getTotalCount() {
    return memberDAO.getTotalCount();
  }

  @Override
  @Transactional
  public Member join(Member member) {
    // 비즈니스 로직: 이메일 중복 검증
    if (isMember(member.getEmail())) {
      throw new BusinessValidationException("이미 가입된 이메일입니다.");
    }
    
    // 비즈니스 로직: 비밀번호 암호화
    member.setPasswd(encryptPassword(member.getPasswd()));
    
    Long memberId = memberDAO.save(member);
    return memberDAO.findById(memberId).orElseThrow(
      () -> new BusinessValidationException("회원가입 처리 중 오류가 발생했습니다.")
    );
  }
  
  @Override
  @Transactional
  public Member joinWithHobbies(Member member, List<Long> hobbyCodeIds) {
    log.debug("회원가입 (취미 포함): email={}, hobbyCodeIds={}", member.getEmail(), hobbyCodeIds);
    
    // 기본 회원 정보 저장
    Member savedMember = join(member);
    
    // 취미 정보 저장
    if (hobbyCodeIds != null && !hobbyCodeIds.isEmpty()) {
      try {
        int updatedCount = updateMemberHobbies(savedMember.getMemberId(), hobbyCodeIds);
        log.debug("취미 정보 저장 완료: memberId={}, updatedCount={}", savedMember.getMemberId(), updatedCount);
      } catch (Exception e) {
        log.error("취미 정보 저장 실패: memberId={}, hobbyCodeIds={}", savedMember.getMemberId(), hobbyCodeIds, e);
        // 취미 저장 실패해도 회원가입은 성공으로 처리
        // 예외를 다시 던지지 않고 로그만 남김
      }
    }
    
    return savedMember;
  }

  @Override
  public boolean isMember(String email) {
    return memberDAO.existsByEmail(email);
  }

  @Override
  public Optional<Member> findByMemberId(Long memberId) {
    return memberDAO.findById(memberId);
  }

  @Override
  public Optional<Member> findByEmail(String email) {
    return memberDAO.findByEmail(email);
  }

  @Override
  public Member login(String email, String passwd) {
    Member member = memberDAO.findByEmailAndPasswd(email, encryptPassword(passwd))
      .orElseThrow(() -> new LoginFailException("아이디 또는 비밀번호가 올바르지 않습니다."));
    
    // 회원 상태 체크 (code_id 사용)
    if (member.getStatus() == null) {
      // 기본값: ACTIVE 상태의 code_id
      Long defaultStatus = codeSVC.getCodeId("MEMBER_STATUS", "ACTIVE");
      member.setStatus(defaultStatus);
    }
    
    // 상태별 로그인 허용 여부 체크
    String statusCode = codeSVC.getCodeValue("MEMBER_STATUS", member.getStatus());
    switch (statusCode) {
      case "ACTIVE":
        // 정상 로그인 허용
        break;
      case "SUSPENDED":
        throw new LoginFailException("정지된 계정입니다. 관리자에게 문의하세요.");
      case "WITHDRAWN":
        throw new LoginFailException("탈퇴된 계정입니다.");
      case "PENDING":
        throw new LoginFailException("승인 대기 중인 계정입니다. 관리자 승인 후 로그인 가능합니다.");
      default:
        throw new LoginFailException("계정 상태가 올바르지 않습니다. 관리자에게 문의하세요.");
    }
    
    return member;
  }

  @Override
  @Transactional
  public int updateMember(Long memberId, Member member) {
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!memberDAO.findById(memberId).isPresent()) {
      throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
    }
    
    // 비즈니스 로직: 이메일 변경 시 중복 검증
    Optional<Member> existingMember = memberDAO.findByEmail(member.getEmail());
    if (existingMember.isPresent() && !existingMember.get().getMemberId().equals(memberId)) {
      throw new BusinessValidationException("이미 사용 중인 이메일입니다.");
    }
    
    member.setUdate(LocalDateTime.now());
    return memberDAO.updateById(memberId, member);
  }

  @Override
  @Transactional
  public int changePasswd(Long memberId, String oldPasswd, String newPasswd) {
    // 비즈니스 로직: 회원 존재 여부 확인
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
    }
    
    Member member = memberOpt.get();
    
    // 비즈니스 로직: 현재 비밀번호 확인
    if (!member.getPasswd().equals(encryptPassword(oldPasswd))) {
      throw new BusinessValidationException("현재 비밀번호가 올바르지 않습니다.");
    }
    
    // 비즈니스 로직: 새 비밀번호 암호화
    member.setPasswd(encryptPassword(newPasswd));
    member.setUdate(LocalDateTime.now());
    
    return memberDAO.updateById(memberId, member);
  }

  @Override
  @Transactional
  public int deleteMember(Long memberId) {
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!memberDAO.findById(memberId).isPresent()) {
      throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
    }
    
    return memberDAO.deleteById(memberId);
  }
  
  @Override
  @Transactional
  public void sendVerificationCode(String email) {
    // 비즈니스 로직: 이메일 형식 검증
    if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new BusinessValidationException("올바른 이메일 형식이 아닙니다.");
    }
    
    // 새로운 인증 코드 생성
    String verificationCode = generateVerificationCode();
    
    // 통합 토큰 서비스를 사용하여 토큰 생성
    tokenSVC.createEmailVerificationToken(email, verificationCode);
    
    // 이메일 발송
    emailService.sendVerificationCode(email, verificationCode);
  }
  
  @Override
  public boolean verifyEmailCode(String email, String verificationCode) {
    // 통합 토큰 서비스를 사용하여 토큰 검증
    return tokenSVC.verifyAndDeactivateToken(email, verificationCode, "EMAIL_VERIFICATION");
  }
  
  @Override
  @Transactional
  public void sendPasswordResetToken(String email) {
    log.info("비밀번호 재설정 토큰 발송 시작: email={}", email);
    
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!isMember(email)) {
      log.warn("등록되지 않은 이메일로 비밀번호 재설정 요청: email={}", email);
      throw new BusinessValidationException("등록되지 않은 이메일입니다.");
    }
    
    log.info("회원 존재 확인 완료: email={}", email);
    
    // 새로운 토큰 생성
    String resetToken = generateResetToken();
    log.info("비밀번호 재설정 토큰 생성 완료: token={}", resetToken);
    
    // 통합 토큰 서비스를 사용하여 토큰 생성
    Long tokenId = tokenSVC.createPasswordResetToken(email, resetToken);
    log.info("비밀번호 재설정 토큰 저장 완료: tokenId={}", tokenId);
    
    // 이메일 발송
    emailService.sendPasswordResetEmail(email, resetToken);
    log.info("비밀번호 재설정 이메일 발송 완료: email={}", email);
  }
  
  @Override
  @Transactional
  public boolean resetPassword(String token, String newPassword) {
    log.info("비밀번호 재설정 시작: token={}", token);
    
    // 통합 토큰 서비스를 사용하여 토큰 검증
    Optional<Token> tokenOpt = tokenSVC.findActiveByValue(token);
    
    if (tokenOpt.isEmpty()) {
      log.warn("유효하지 않은 토큰으로 비밀번호 재설정 시도: token={}", token);
      throw new BusinessValidationException("유효하지 않은 토큰입니다.");
    }
    
    Token resetToken = tokenOpt.get();
    log.info("토큰 검증 완료: tokenId={}, email={}", resetToken.getTokenId(), resetToken.getEmail());
    
    // 토큰 타입 확인 (코드 캐시 활용)
    String tokenTypeCode = codeSVC.getCodeValue("TOKEN_TYPE", resetToken.getTokenTypeId());
    if (!"PASSWORD_RESET".equals(tokenTypeCode)) {
      log.warn("잘못된 토큰 타입: expected=PASSWORD_RESET, actual={}", tokenTypeCode);
      throw new BusinessValidationException("잘못된 토큰 타입입니다.");
    }
    
    log.info("토큰 타입 확인 완료: type={}", tokenTypeCode);
    
    // 회원 조회
    Optional<Member> memberOpt = memberDAO.findByEmail(resetToken.getEmail());
    if (memberOpt.isEmpty()) {
      log.warn("토큰에 해당하는 회원 정보 없음: email={}", resetToken.getEmail());
      throw new BusinessValidationException("회원 정보를 찾을 수 없습니다.");
    }
    
    Member member = memberOpt.get();
    log.info("회원 정보 조회 완료: memberId={}, email={}", member.getMemberId(), member.getEmail());
    
    // 비밀번호 변경
    member.setPasswd(encryptPassword(newPassword));
    member.setUdate(LocalDateTime.now());
    
    int result = memberDAO.updateById(member.getMemberId(), member);
    log.info("비밀번호 업데이트 완료: result={}", result);
    
    // 토큰 비활성화
    tokenSVC.updateTokenStatus(resetToken.getTokenId(), "VERIFIED");
    log.info("토큰 상태 업데이트 완료: tokenId={}", resetToken.getTokenId());
    
    return result > 0;
  }
  
  @Override
  public boolean isEmailVerified(String email) {
    Optional<Token> tokenOpt = tokenSVC.findActiveByEmailAndType(email, "EMAIL_VERIFICATION");
    if (!tokenOpt.isPresent()) return false;
    
    // 토큰 상태 확인 (코드 캐시 활용)
    String statusCode = codeSVC.getCodeValue("TOKEN_STATUS", tokenOpt.get().getStatusId());
    return "VERIFIED".equals(statusCode);
  }
  
  @Override
  public Optional<String> findEmailByPhoneAndBirth(String phone, String birth) {
    return memberDAO.findEmailByPhoneAndBirth(phone, birth);
  }
  

  
  @Override
  public boolean isAdmin(Long memberId) {
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      return false;
    }
    
    Member member = memberOpt.get();
    try {
      // CodeSVC를 사용하여 관리자 구분 확인
      Long admin1CodeId = codeSVC.getCodeId("MEMBER_GUBUN", "ADMIN1");
      Long admin2CodeId = codeSVC.getCodeId("MEMBER_GUBUN", "ADMIN2");
      
      return member.getGubun().equals(admin1CodeId) || member.getGubun().equals(admin2CodeId);
    } catch (Exception e) {
      log.warn("관리자 권한 체크 중 오류 발생: memberId={}", memberId, e);
      return false;
    }
  }

  @Override
  public boolean isVip(Long memberId) {
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      return false;
    }
    
    Member member = memberOpt.get();
    try {
      // CodeSVC를 사용하여 VIP 구분 확인
      Long vipCodeId = codeSVC.getCodeId("MEMBER_GUBUN", "VIP");
      return member.getGubun().equals(vipCodeId);
    } catch (Exception e) {
      log.warn("VIP 권한 체크 중 오류 발생: memberId={}", memberId, e);
      return false;
    }
  }

  @Override
  public boolean hasPermission(Long memberId, String permission) {
    // 기본적으로 관리자는 모든 권한을 가짐
    if (isAdmin(memberId)) {
      return true;
    }
    
    // VIP 회원 특별 권한 체크
    if (isVip(memberId)) {
      return "VIP_ACCESS".equals(permission) || "PREMIUM_CONTENT".equals(permission);
    }
    
    // 일반 회원 기본 권한
    return "BASIC_ACCESS".equals(permission);
  }
  
  /**
   * 6자리 인증 코드 생성
   */
  private String generateVerificationCode() {
    SecureRandom random = new SecureRandom();
    StringBuilder code = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      code.append(random.nextInt(10));
    }
    return code.toString();
  }
  
  /**
   * 비밀번호 재설정 토큰 생성
   */
  private String generateResetToken() {
    SecureRandom random = new SecureRandom();
    StringBuilder token = new StringBuilder();
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (int i = 0; i < 32; i++) {
      token.append(chars.charAt(random.nextInt(chars.length())));
    }
    return token.toString();
  }

  /**
   * 비즈니스 로직: 비밀번호 암호화 (SHA-256)
   */
  private String encryptPassword(String password) {
    if (password == null || password.isEmpty()) {
      throw new BusinessValidationException("비밀번호는 필수입니다.");
    }
    
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(password.getBytes());
      StringBuilder hexString = new StringBuilder();
      
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new BusinessValidationException("비밀번호 암호화 중 오류가 발생했습니다.");
    }
  }
  
  // ========== 회원 상태 관리 메서드들 ==========
  
  @Override
  @Transactional
  public int updateMemberStatus(Long memberId, String status, String reason) {
    // 회원 존재 여부 확인
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
    }
    
    Member member = memberOpt.get();
    
    // 상태 변경 로직
    member.setStatus(codeSVC.getCodeId("MEMBER_STATUS", status));
    member.setStatusReason(reason);
    member.setStatusChangedAt(LocalDateTime.now());
    member.setUdate(LocalDateTime.now());
    
    
    
    return memberDAO.updateById(memberId, member);
  }
  
  @Override
  public boolean isActiveMember(Long memberId) {
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      return false;
    }
    Long activeStatusId = codeSVC.getCodeId("MEMBER_STATUS", "ACTIVE");
    return activeStatusId.equals(memberOpt.get().getStatus());
  }
  
  @Override
  public boolean isSuspendedMember(Long memberId) {
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      return false;
    }
    Long suspendedStatusId = codeSVC.getCodeId("MEMBER_STATUS", "SUSPENDED");
    return suspendedStatusId.equals(memberOpt.get().getStatus());
  }
  
  @Override
  public boolean isWithdrawnMember(Long memberId) {
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      return false;
    }
    Long withdrawnStatusId = codeSVC.getCodeId("MEMBER_STATUS", "WITHDRAWN");
    return withdrawnStatusId.equals(memberOpt.get().getStatus());
  }

  @Override
  public boolean checkPassword(Long memberId, String rawPassword) {
      Optional<Member> memberOpt = memberDAO.findById(memberId);
      if (memberOpt.isEmpty()) return false;
      Member member = memberOpt.get();
      String encrypted = encryptPassword(rawPassword);
      return member.getPasswd() != null && member.getPasswd().equals(encrypted);
  }

  @Override
  @Transactional
  public int updateProfileImage(Long memberId, byte[] imageData) {
      // 비즈니스 로직: 회원 존재 여부 확인
      Optional<Member> memberOpt = memberDAO.findById(memberId);
      if (memberOpt.isEmpty()) {
          throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
      }

      Member member = memberOpt.get();
      member.setPic(imageData);
      member.setUdate(LocalDateTime.now());

      return memberDAO.updateById(memberId, member);
  }

  @Override
  @Transactional
  public int deleteProfileImage(Long memberId) {
      // 비즈니스 로직: 회원 존재 여부 확인
      Optional<Member> memberOpt = memberDAO.findById(memberId);
      if (memberOpt.isEmpty()) {
          throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
      }

      Member member = memberOpt.get();
      member.setPic(null);
      member.setUdate(LocalDateTime.now());

      return memberDAO.updateById(memberId, member);
  }

  @Override
  public int countByKeyword(String keyword) {
      return memberDAO.countByKeyword(keyword);
  }
  @Override
  public List<Member> findByKeywordWithPaging(String keyword, int pageNo, int pageSize) {
      return memberDAO.findByKeywordWithPaging(keyword, pageNo, pageSize);
  }

  @Override
  public List<Member> findAllWithPaging(int pageNo, int pageSize) {
      return memberDAO.findAllWithPaging(pageNo, pageSize);
  }

  @Override
  public int countByStatus(String status) {
      // status는 code 값 (예: "ACTIVE", "SUSPENDED")
      Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
      return memberDAO.countByStatusId(statusId);
  }

  @Override
  public List<Member> findByStatusWithPaging(String status, int pageNo, int pageSize) {
      // status는 code 값 (예: "ACTIVE", "SUSPENDED")
      Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
      return memberDAO.findByStatusIdWithPaging(statusId, pageNo, pageSize);
  }

  @Override
  public int countByStatusAndKeyword(String status, String keyword) {
      // status는 code 값 (예: "ACTIVE", "SUSPENDED")
      Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
      return memberDAO.countByStatusIdAndKeyword(statusId, keyword);
  }

  @Override
  public List<Member> findByStatusAndKeywordWithPaging(String status, String keyword, int pageNo, int pageSize) {
      // status는 code 값 (예: "ACTIVE", "SUSPENDED")
      Long statusId = codeSVC.getCodeId("MEMBER_STATUS", status);
      return memberDAO.findByStatusIdAndKeywordWithPaging(statusId, keyword, pageNo, pageSize);
  }

  @Override
  public boolean isNicknameExists(String nickname) {
      return memberDAO.findByNickname(nickname).isPresent();
  }

  // === 새로 추가된 메서드들 구현 ===

  @Override
  public Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findMemberDetailById(Long memberId) {
      log.debug("회원 상세 정보 조회: memberId={}", memberId);
      return memberDAO.findDetailById(memberId);
  }

  @Override
  public Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findMemberDetailByEmail(String email) {
      log.debug("회원 상세 정보 조회: email={}", email);
      return memberDAO.findDetailByEmail(email);
  }

  @Override
  public List<com.kh.demo.domain.member.dto.MemberHobbyDTO> getMemberHobbies(Long memberId) {
      log.debug("회원 취미 목록 조회: memberId={}", memberId);
      return memberDAO.findHobbiesByMemberId(memberId);
  }

  @Override
  @Transactional
  public Long addMemberHobby(Long memberId, Long hobbyCodeId) {
      log.debug("회원 취미 추가: memberId={}, hobbyCodeId={}", memberId, hobbyCodeId);

      // 비즈니스 로직: 회원 존재 여부 확인
      if (!memberDAO.findById(memberId).isPresent()) {
          throw new BusinessValidationException("회원을 찾을 수 없습니다: " + memberId);
      }

      // 비즈니스 로직: 취미 코드 존재 여부 확인
      if (hobbyCodeId == null) {
          log.error("취미 코드 ID가 null입니다");
          throw new BusinessValidationException("유효하지 않은 취미 코드입니다");
      }
      log.debug("취미 코드 ID 확인 완료: hobbyCodeId={}", hobbyCodeId);

      // 비즈니스 로직: 중복 취미 확인
      List<com.kh.demo.domain.member.dto.MemberHobbyDTO> existingHobbies = memberDAO.findHobbiesByMemberId(memberId);
      boolean alreadyExists = existingHobbies.stream()
              .anyMatch(hobby -> hobbyCodeId.equals(hobby.getHobbyCodeId()));

      if (alreadyExists) {
          log.warn("중복 취미 감지: memberId={}, hobbyCodeId={}", memberId, hobbyCodeId);
          throw new BusinessValidationException("이미 등록된 취미입니다");
      }

      try {
          Long hobbyId = memberDAO.addMemberHobby(memberId, hobbyCodeId);
          log.debug("취미 추가 성공: memberId={}, hobbyCodeId={}, hobbyId={}", memberId, hobbyCodeId, hobbyId);
          return hobbyId;
      } catch (Exception e) {
          log.error("취미 추가 중 데이터베이스 오류: memberId={}, hobbyCodeId={}", memberId, hobbyCodeId, e);
          throw new BusinessValidationException("취미 추가 중 오류가 발생했습니다");
      }
  }

  @Override
  @Transactional
  public int removeMemberHobby(Long memberId, String hobbyCode) {
      log.debug("회원 취미 삭제: memberId={}, hobbyCode={}", memberId, hobbyCode);

      // 비즈니스 로직: 취미 코드 ID 조회
      Long hobbyCodeId = codeSVC.getCodeId("HOBBY", hobbyCode);
      if (hobbyCodeId == null) {
          throw new BusinessValidationException("유효하지 않은 취미 코드입니다: " + hobbyCode);
      }

      return memberDAO.removeMemberHobby(memberId, hobbyCodeId);
  }

  @Override
  @Transactional
  public int updateMemberHobbies(Long memberId, List<Long> hobbyCodeIds) {
      log.debug("회원 취미 전체 업데이트: memberId={}, hobbyCodeIds={}", memberId, hobbyCodeIds);

      // 비즈니스 로직: 회원 존재 여부 확인
      if (!memberDAO.findById(memberId).isPresent()) {
          throw new BusinessValidationException("회원을 찾을 수 없습니다: " + memberId);
      }

      // 기존 취미 모두 삭제
      int deletedCount = memberDAO.removeAllMemberHobbies(memberId);
      log.debug("기존 취미 삭제 완료: deletedCount={}", deletedCount);

      // 새로운 취미들 추가
      int addedCount = 0;
      if (hobbyCodeIds != null && !hobbyCodeIds.isEmpty()) {
          for (Long hobbyCodeId : hobbyCodeIds) {
              try {
                  log.debug("취미 추가 시도: memberId={}, hobbyCodeId={}", memberId, hobbyCodeId);
                  addMemberHobby(memberId, hobbyCodeId);
                  addedCount++;
                  log.debug("취미 추가 성공: memberId={}, hobbyCodeId={}", memberId, hobbyCodeId);
              } catch (BusinessValidationException e) {
                  log.warn("취미 추가 실패 (BusinessValidationException): hobbyCodeId={}, error={}", hobbyCodeId, e.getMessage());
              } catch (Exception e) {
                  log.error("취미 추가 실패 (Exception): hobbyCodeId={}, error={}", hobbyCodeId, e.getMessage(), e);
              }
          }
      }

      log.debug("새로운 취미 추가 완료: addedCount={}", addedCount);
      return addedCount;
  }

  @Override
  public String getGenderName(Long genderId) {
      if (genderId == null) return null;
      return codeSVC.getCodeDecode("GENDER", genderId);
  }

  @Override
  public String getRegionName(Long regionId) {
      if (regionId == null) return null;
      return codeSVC.getCodeDecode("REGION", regionId);
  }

  @Override
  public String getMemberTypeName(Long gubunId) {
      if (gubunId == null) return null;
      return codeSVC.getCodeDecode("MEMBER_TYPE", gubunId);
  }

  @Override
  public String getMemberStatusName(Long statusId) {
      if (statusId == null) return null;
      return codeSVC.getCodeDecode("MEMBER_STATUS", statusId);
  }

  // === 신규 회원, VIP 회원, 휴면 회원 관련 메서드들 ===

  @Override
  public List<Member> findNewMembersWithPaging(int pageNo, int pageSize) {
      return memberDAO.findNewMembersWithPaging(pageNo, pageSize);
  }

  @Override
  public int countNewMembers() {
      return memberDAO.countNewMembers();
  }

  @Override
  public List<Member> findVipMembersWithPaging(int pageNo, int pageSize) {
      return memberDAO.findVipMembersWithPaging(pageNo, pageSize);
  }

  @Override
  public int countVipMembers() {
      return memberDAO.countVipMembers();
  }

  @Override
  public List<Member> findInactiveMembersWithPaging(int pageNo, int pageSize) {
      return memberDAO.findInactiveMembersWithPaging(pageNo, pageSize);
  }

  @Override
  public int countInactiveMembers() {
      return memberDAO.countInactiveMembers();
  }
}