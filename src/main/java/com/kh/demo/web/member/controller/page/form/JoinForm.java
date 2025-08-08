package com.kh.demo.web.member.controller.page.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JoinForm {
  @NotBlank
  @Email(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")
  @Size(min=7,max=50)
  private String email;           //  EMAIL VARCHAR2(50 BYTE)
  @NotBlank
  @Size(min=2, max=12)
  private String passwd;          //  PASSWD VARCHAR2(12 BYTE)
  @NotBlank
  private String passwdChk;          //  PASSWD VARCHAR2(12 BYTE)
  private String tel;             //  TEL VARCHAR2(13 BYTE)
  @NotBlank(message = "별칭을 입력해주세요")
  @Size(min = 2, max = 10, message = "별칭은 2~10자 사이로 입력해주세요")
  private String nickname;        //  NICKNAME VARCHAR2(30 BYTE)
  @NotNull(message = "성별을 선택해주세요")
  private Long gender;            //  GENDER - code_id (NUMBER)
  @NotNull(message = "생년월일을 입력해주세요")
  private LocalDate birthDate;    //  BIRTH_DATE DATE
  private List<Long> hobby;       //  HOBBY VARCHAR2(300 BYTE) - code_id 리스트
  private Long region;            //  REGION - code_id
}
