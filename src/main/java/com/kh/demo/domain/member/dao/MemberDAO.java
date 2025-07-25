package com.kh.demo.domain.member.dao;

import com.kh.demo.domain.member.entity.Member;
import com.kh.demo.domain.common.base.BaseDAO;
import java.util.List;
import java.util.Optional;

public interface MemberDAO extends BaseDAO<Member, Long> {
    
    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);
    
    // 이메일과 비밀번호로 회원 조회 (로그인용)
    Optional<Member> findByEmailAndPasswd(String email, String passwd);
    
    // 페이징 회원 목록 조회
    List<Member> findAllWithPaging(int pageNo, int pageSize);
    
    // 지역별 회원 목록 조회
    List<Member> findByRegion(Long region);
    
    // 회원구분별 회원 목록 조회
    List<Member> findByGubun(Long gubun);
    
    // 회원 존재 여부 확인
    boolean existsByEmail(String email);
    
    // 지역별 회원 수 조회
    int countByRegion(Long region);
    
    // 회원구분별 회원 수 조회
    int countByGubun(Long gubun);
    
    // 아이디 찾기 (전화번호, 생년월일로 이메일 조회)
    Optional<String> findEmailByPhoneAndBirth(String phone, String birth);
    
    // 키워드(이메일/닉네임) 검색 회원 수
    int countByKeyword(String keyword);
    // 키워드(이메일/닉네임) 검색 + 페이징
    List<Member> findByKeywordWithPaging(String keyword, int pageNo, int pageSize);
}

