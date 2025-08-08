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
    int countByStatus(String status);
    List<Member> findByStatusWithPaging(String status, int pageNo, int pageSize);
    int countByStatusAndKeyword(String status, String keyword);
    List<Member> findByStatusAndKeywordWithPaging(String status, String keyword, int pageNo, int pageSize);

    // === statusId 기반 검색 메서드들 (새로 추가) ===
    int countByStatusId(Long statusId);
    List<Member> findByStatusIdWithPaging(Long statusId, int pageNo, int pageSize);
    int countByStatusIdAndKeyword(Long statusId, String keyword);
    List<Member> findByStatusIdAndKeywordWithPaging(Long statusId, String keyword, int pageNo, int pageSize);

    // 별칭으로 회원 조회
    Optional<Member> findByNickname(String nickname);
    
    // === 새로 추가된 메서드들 (코드 참조 및 취미 관련) ===
    
    // 회원 상세 정보 조회 (코드 decode 값 포함)
    Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findDetailById(Long memberId);
    
    // 회원 상세 정보 조회 (이메일로, 코드 decode 값 포함)
    Optional<com.kh.demo.domain.member.dto.MemberDetailDTO> findDetailByEmail(String email);
    
    // 회원 취미 목록 조회
    List<com.kh.demo.domain.member.dto.MemberHobbyDTO> findHobbiesByMemberId(Long memberId);
    
    // 회원 취미 추가
    Long addMemberHobby(Long memberId, Long hobbyCodeId);
    
    // 회원 취미 삭제
    int removeMemberHobby(Long memberId, Long hobbyCodeId);
    
    // 회원의 모든 취미 삭제
    int removeAllMemberHobbies(Long memberId);

    // === 신규 회원, VIP 회원, 휴면 회원 관련 메서드들 ===
    
    // 신규 회원 목록 조회 (최근 30일 내 가입)
    List<Member> findNewMembersWithPaging(int pageNo, int pageSize);
    
    // 신규 회원 수 조회
    int countNewMembers();
    
    // VIP 회원 목록 조회
    List<Member> findVipMembersWithPaging(int pageNo, int pageSize);
    
    // VIP 회원 수 조회
    int countVipMembers();
    
    // 휴면 회원 목록 조회
    List<Member> findInactiveMembersWithPaging(int pageNo, int pageSize);
    
    // 휴면 회원 수 조회
    int countInactiveMembers();
}

