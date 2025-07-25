package com.kh.demo.common.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class LoginMember {
  private Long memberId;   //내부 관리용 멤버아이디
  private String email;    //회원 로그인 아이디
  private String nickname; //별칭
  private long gubun;    //일반(1), vip(2), 관리자1(4), 관리자2(5)
  private Boolean hasProfileImage; //프로필 사진 존재 여부
}
