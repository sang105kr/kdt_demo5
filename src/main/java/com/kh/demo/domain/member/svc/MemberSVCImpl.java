package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.entity.PasswordResetToken;
import com.kh.demo.domain.member.entity.EmailVerificationToken;
import com.kh.demo.domain.member.dao.MemberDAO;
import com.kh.demo.domain.member.dao.PasswordResetTokenDAO;
import com.kh.demo.domain.member.dao.EmailVerificationTokenDAO;
import com.kh.demo.web.exception.BusinessValidationException;
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
import com.kh.demo.web.exception.LoginFailException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSVCImpl implements MemberSVC {
  private final MemberDAO memberDAO;
  private final PasswordResetTokenDAO passwordResetTokenDAO;
  private final EmailVerificationTokenDAO emailVerificationTokenDAO;
  private final EmailService emailService;

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
    int offset = (pageNo - 1) * numOfRows;
    return memberDAO.findAllWithPaging(offset, numOfRows);
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
  public Member loginOrThrow(String email, String passwd) {
    return memberDAO.findByEmailAndPasswd(email, encryptPassword(passwd))
      .orElseThrow(() -> new com.kh.demo.web.exception.LoginFailException("아이디 또는 비밀번호가 올바르지 않습니다."));
  }

  @Override
  @Transactional
  public void updateMember(Member member) {
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!memberDAO.findById(member.getMemberId()).isPresent()) {
      throw new BusinessValidationException("회원번호: " + member.getMemberId() + "를 찾을 수 없습니다.");
    }
    
    // 비즈니스 로직: 이메일 변경 시 중복 검증
    Optional<Member> existingMember = memberDAO.findByEmail(member.getEmail());
    if (existingMember.isPresent() && !existingMember.get().getMemberId().equals(member.getMemberId())) {
      throw new BusinessValidationException("이미 사용 중인 이메일입니다.");
    }
    
    member.setUdate(LocalDateTime.now());
    memberDAO.updateById(member.getMemberId(), member);
  }

  @Override
  @Transactional
  public void changePassword(Long memberId, String currentPassword, String newPassword) {
    // 비즈니스 로직: 회원 존재 여부 확인
    Optional<Member> memberOpt = memberDAO.findById(memberId);
    if (memberOpt.isEmpty()) {
      throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
    }
    
    Member member = memberOpt.get();
    
    // 비즈니스 로직: 현재 비밀번호 확인
    if (!member.getPasswd().equals(encryptPassword(currentPassword))) {
      throw new BusinessValidationException("현재 비밀번호가 올바르지 않습니다.");
    }
    
    // 비즈니스 로직: 새 비밀번호 암호화
    member.setPasswd(encryptPassword(newPassword));
    member.setUdate(LocalDateTime.now());
    
    memberDAO.updateById(memberId, member);
  }

  @Override
  @Transactional
  public void withdraw(Long memberId) {
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!memberDAO.findById(memberId).isPresent()) {
      throw new BusinessValidationException("회원번호: " + memberId + "를 찾을 수 없습니다.");
    }
    
    memberDAO.deleteById(memberId);
  }
  
  @Override
  @Transactional
  public void sendVerificationCode(String email) {
    // 비즈니스 로직: 이메일 형식 검증
    if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new BusinessValidationException("올바른 이메일 형식이 아닙니다.");
    }
    
    // 기존 활성 토큰 비활성화
    emailVerificationTokenDAO.deactivateByEmail(email);
    
    // 새로운 인증 코드 생성
    String verificationCode = generateVerificationCode();
    
    // 토큰 저장
    EmailVerificationToken token = new EmailVerificationToken();
    token.setEmail(email);
    token.setVerificationCode(verificationCode);
    token.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // 10분 유효
    token.setStatus("ACTIVE");
    
    emailVerificationTokenDAO.save(token);
    
    // 이메일 발송
    emailService.sendVerificationCode(email, verificationCode);
  }
  
  @Override
  public boolean verifyEmailCode(String email, String verificationCode) {
    Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenDAO.findByEmailAndActive(email);
    
    if (tokenOpt.isEmpty()) {
      return false;
    }
    
    EmailVerificationToken token = tokenOpt.get();
    
    // 인증 코드 확인
    if (!token.getVerificationCode().equals(verificationCode)) {
      return false;
    }
    
    // 토큰 상태를 VERIFIED로 변경
    emailVerificationTokenDAO.updateStatus(token.getTokenId(), "VERIFIED");
    
    return true;
  }
  
  @Override
  @Transactional
  public void sendPasswordResetToken(String email) {
    // 비즈니스 로직: 회원 존재 여부 확인
    if (!isMember(email)) {
      throw new BusinessValidationException("등록되지 않은 이메일입니다.");
    }
    
    // 기존 활성 토큰 비활성화
    passwordResetTokenDAO.deactivateByEmail(email);
    
    // 새로운 토큰 생성
    String resetToken = generateResetToken();
    
    // 토큰 저장
    PasswordResetToken token = new PasswordResetToken();
    token.setEmail(email);
    token.setToken(resetToken);
    token.setExpiryDate(LocalDateTime.now().plusMinutes(30)); // 30분 유효
    token.setStatus("ACTIVE");
    
    passwordResetTokenDAO.save(token);
    
    // 이메일 발송
    emailService.sendPasswordResetEmail(email, resetToken);
  }
  
  @Override
  @Transactional
  public void resetPassword(String token, String newPassword) {
    // 비즈니스 로직: 토큰 유효성 확인
    Optional<PasswordResetToken> tokenOpt = passwordResetTokenDAO.findByToken(token);
    
    if (tokenOpt.isEmpty()) {
      throw new BusinessValidationException("유효하지 않은 토큰입니다.");
    }
    
    PasswordResetToken resetToken = tokenOpt.get();
    
    if (!resetToken.isActive()) {
      throw new BusinessValidationException("만료되었거나 이미 사용된 토큰입니다.");
    }
    
    // 회원 조회
    Optional<Member> memberOpt = memberDAO.findByEmail(resetToken.getEmail());
    if (memberOpt.isEmpty()) {
      throw new BusinessValidationException("회원 정보를 찾을 수 없습니다.");
    }
    
    Member member = memberOpt.get();
    
    // 비밀번호 변경
    member.setPasswd(encryptPassword(newPassword));
    member.setUdate(LocalDateTime.now());
    
    memberDAO.updateById(member.getMemberId(), member);
    
    // 토큰 상태를 USED로 변경
    passwordResetTokenDAO.updateStatus(resetToken.getTokenId(), "USED");
  }
  
  @Override
  public boolean isEmailVerified(String email) {
    Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenDAO.findByEmailAndActive(email);
    return tokenOpt.isPresent() && "VERIFIED".equals(tokenOpt.get().getStatus());
  }
  
  @Override
  public Optional<String> findEmailByPhoneAndBirth(String phone, String birth) {
    return memberDAO.findEmailByPhoneAndBirth(phone, birth);
  }
  
  @Override
  public Optional<Member> findMemberByEmail(String email) {
    return memberDAO.findByEmail(email);
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
}