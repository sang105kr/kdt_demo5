package com.kh.demo.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Bbs {
  private Long bbsId;                 //  bbs_id			number(10),  		--게시글 식별자
  private String title;               //  title				varchar2(150), 	--제목(한글50자)
  private String content;             //  content			clob, 					--내용,
  private String writer;              //  writer			varchar2(30),		--작성자(한글10자)
  private LocalDateTime createdAt;   //  created_at	timestamp ,			--작성 날짜 : date, timestamp
  private LocalDateTime updatedAt;   //  updated_at	timestamp				--수정 날짜
}
