package com.kh.demo.domain.bbs.svc;

import com.kh.demo.domain.entity.Bbs;
import com.kh.demo.domain.entity.Rbbs;

import java.util.List;
import java.util.Optional;

public interface RbbsSVC {

  /**
   * 댓글등록
   * @param rbbs
   * @return 댓글 번호
   */
  Rbbs save(Rbbs rbbs);

  /**
   * 댓글목록 
   * @return 댓글 목록
   */
  List<Rbbs> findAll();

  /**
   * 댓글목록 페이징
   * @param pageNo 요청 페이지
   * @param numOfRows 요청 페이지 레코드 수
   * @return 상품목록
   */
  List<Rbbs> findAll(int pageNo, int numOfRows);

  /**
   *  댓글 총 건수
   * @return 총 건수
   */
  int getTotalCount();

  /**
   * 댓글조회
   * @param id 댓글 번혼
   * @return 댓글
   */
  Optional<Rbbs> findById(Long id);
  
  /**
   * 댓글삭제(단건)
   * @param id 댓글
   * @return  삭제건수
   */
  int deleteById(Long id);

  /**
   * 댓글수정
   * @param id 댓글번호
   * @param rbbs 수정글
   * @return 수정건수
   */
  int updateById(Long id, Rbbs rbbs);

}
