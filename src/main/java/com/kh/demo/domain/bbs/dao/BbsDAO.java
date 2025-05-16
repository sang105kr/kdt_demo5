package com.kh.demo.domain.bbs.dao;

import com.kh.demo.domain.entity.Bbs;

import java.util.List;
import java.util.Optional;

public interface BbsDAO {

  /**
   * 게시글등록
   * @param bbs
   * @return 게시글 번호
   */
  Long save(Bbs bbs);

  /**
   * 게시글목록 
   * @return 게시글 목록
   */
  List<Bbs> findAll();
  
  /**
   * 게시글조회
   * @param id 게시글 번혼
   * @return 게시글
   */
  Optional<Bbs> findById(Long id);
  
  /**
   * 게시글삭제(단건)
   * @param id 게시글
   * @return  삭제건수
   */
  int deleteById(Long id);

  /**
   * 게시글삭제(여러건) 
   * @param ids 게시글번호(여러건)
   * @return 삭제건수
   */
  int deleteByIds(List<Long> ids);

  /**
   * 게시글수정
   * @param id 게시글번호
   * @param bbs 수정글
   * @return 수정건수
   */
  int updateById(Long id, Bbs bbs);

}
