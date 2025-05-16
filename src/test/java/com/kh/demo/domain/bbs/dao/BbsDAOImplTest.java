package com.kh.demo.domain.bbs.dao;

import com.kh.demo.domain.entity.Bbs;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional     //테스트환경에서 commit수행을 안하고 rollback수행함.
class BbsDAOImplTest {

  @Autowired
  private BbsDAO bbsDAO;

  @Test
  @DisplayName("게시글 등록")
  void save() {
    //given
    Bbs bbs = new Bbs();
    bbs.setTitle("테스트 제목");
    bbs.setContent("테스트 내용");
    bbs.setWriter("테스트 작성자");

    //when
    Long bbsId = bbsDAO.save(bbs);

    //then
    Assertions.assertThat(bbsId).isNotNull();
    Assertions.assertThat(bbsId).isGreaterThan(0);
  }

  @Test
  @DisplayName("게시글 목록")
  void findAll() {
    //given
    Bbs bbs1 = new Bbs();
    bbs1.setTitle("테스트 제목1");
    bbs1.setContent("테스트 내용1");
    bbs1.setWriter("테스트 작성자1");

    Bbs bbs2 = new Bbs();
    bbs2.setTitle("테스트 제목2");
    bbs2.setContent("테스트 내용2");
    bbs2.setWriter("테스트 작성자2");

    bbsDAO.save(bbs1);
    bbsDAO.save(bbs2);

    //when
    List<Bbs> bbsList = bbsDAO.findAll();

    //then
    Assertions.assertThat(bbsList).isNotNull();
    Assertions.assertThat(bbsList.size()).isEqualTo(2);
  }

  @Test
  @DisplayName("게시글 조회")
  void findById() {
    //given
    Bbs bbs = new Bbs();
    bbs.setTitle("테스트 제목");
    bbs.setContent("테스트 내용");
    bbs.setWriter("테스트 작성자");
    Long bbsId = bbsDAO.save(bbs);

    //when
    Optional<Bbs> foundBbs = bbsDAO.findById(bbsId);

    //then
    Assertions.assertThat(foundBbs).isPresent(); //optional객체에 아이템이 존재하는지 확인
    Assertions.assertThat(foundBbs.get().getTitle()).isEqualTo("테스트 제목");
    Assertions.assertThat(foundBbs.get().getContent()).isEqualTo("테스트 내용");
    Assertions.assertThat(foundBbs.get().getWriter()).isEqualTo("테스트 작성자");
  }

  @Test
  @DisplayName("게시글 삭제(단건)")
  void deleteById() {
    //given
    Bbs bbs = new Bbs();
    bbs.setTitle("테스트 제목");
    bbs.setContent("테스트 내용");
    bbs.setWriter("테스트 작성자");
    Long bbsId = bbsDAO.save(bbs);

    //when
    int deletedRow = bbsDAO.deleteById(bbsId);

    //then
    Assertions.assertThat(deletedRow).isEqualTo(1);
    Assertions.assertThat(bbsDAO.findById(bbsId)).isEmpty();
  }

  @Test
  @DisplayName("게시글 삭제(여러건)")
  void deleteByIds() {
    //given
    Bbs bbs1 = new Bbs();
    bbs1.setTitle("테스트 제목1");
    bbs1.setContent("테스트 내용1");
    bbs1.setWriter("테스트 작성자1");

    Bbs bbs2 = new Bbs();
    bbs2.setTitle("테스트 제목2");
    bbs2.setContent("테스트 내용2");
    bbs2.setWriter("테스트 작성자2");

    Long bbsId1 = bbsDAO.save(bbs1);
    Long bbsId2 = bbsDAO.save(bbs2);

    //when
    int deletedRow = bbsDAO.deleteByIds(List.of(bbsId1, bbsId2));

    //then
    Assertions.assertThat(deletedRow).isEqualTo(2);
    Assertions.assertThat(bbsDAO.findById(bbsId1)).isEmpty();
    Assertions.assertThat(bbsDAO.findById(bbsId2)).isEmpty();
  }

  @Test
  @DisplayName("게시글 수정")
  void updateById() {
  }
}