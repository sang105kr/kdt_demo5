package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.dao.MemberDAO;
import com.kh.demo.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSVCImpl implements MemberSVC {
  private final MemberDAO memberDAO;

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
    
    // 기본값 설정
    if (member.getCdate() == null) {
      member.setCdate(LocalDateTime.now());
    }
    if (member.getUdate() == null) {
      member.setUdate(LocalDateTime.now());
    }
    
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