package com.kh.demo.domain.member.svc;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.member.dao.MemberDAO;
import com.kh.demo.domain.shared.base.BaseSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberSVCImpl implements MemberSVC {
  private final MemberDAO memberDAO;

  @Override
  public Long save(Member member) {
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
  public int updateById(Long id, Member member) {
    return memberDAO.updateById(id, member);
  }

  @Override
  public int deleteById(Long id) {
    return memberDAO.deleteById(id);
  }

  @Override
  public int getTotalCount() {
    return memberDAO.getTotalCount();
  }

  @Override
  public Member join(Member member) {
    Long memberId = memberDAO.save(member);
    return memberDAO.findById(memberId).orElseThrow();
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
}