package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional  // 테스트후 rollback수행 하므로 실제 DB에 반영되지 않음.
class MemberDAOImplTest {

  @Autowired   // 스프링 컨테이너 내에서 MemberDAO 타입의 객체를 참조할수 있게 해준다.
  private MemberDAO memberDAO;

  @Test
  @DisplayName("회원가입")
  void insertMember() {
    Member member = new Member();
    member.setEmail("test3@kh.com");
    member.setPasswd("1234");
    member.setTel("010-9999-9999");
    member.setNickname("별칭3");
    member.setGender("남자");
    member.setHobby("영화감상");
    member.setRegion("A0204");

    Member insertedMember = memberDAO.insertMember(member);
    log.info("insertedMember={}",insertedMember);
  }

  @Test
  @DisplayName("회원존재유무By이메일")
  void isExist() {
    boolean exist = memberDAO.isExist("test1@kh.com");
    Assertions.assertThat(exist).isEqualTo(true);

    exist = memberDAO.isExist("test1@google.com");
    Assertions.assertThat(exist).isEqualTo(false);
  }

  @Test
  @DisplayName("회원검색By회원아이디")
  void findByMemberId() {
    Optional<Member> optionalMember = memberDAO.findByMemberId(1L);
    if(optionalMember.isPresent()){
      Member member = optionalMember.get();
      log.info("member={}", member);
    }
    optionalMember = memberDAO.findByMemberId(0L);
    if(optionalMember.isEmpty()){
      log.info("회원없음");
    }
  }

  @Test
  @DisplayName("회원검색By이메일")
  void findByEmail() {
    Optional<Member> optionalMember = memberDAO.findByEmail("test1@kh.com");
    if(optionalMember.isPresent()){
      Member member = optionalMember.get();
      log.info("member={}", member);
    }
    optionalMember = memberDAO.findByEmail("test1@google.com");
    if(!optionalMember.isPresent()){
      log.info("회원없음");
    }
  }
}