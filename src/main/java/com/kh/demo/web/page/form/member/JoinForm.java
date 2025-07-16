package com.kh.demo.web.page.form.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
  private String nickname;        //  NICKNAME VARCHAR2(30 BYTE)
  private String gender;          //  GENDER VARCHAR2(6 BYTE)
  private List<String> hobby;     //  HOBBY VARCHAR2(300 BYTE)
  private String region;          //  REGION VARCHAR2(11 BYTE)
}
