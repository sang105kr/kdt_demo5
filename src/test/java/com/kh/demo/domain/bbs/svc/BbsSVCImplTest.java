package com.kh.demo.domain.bbs.svc;

import com.kh.demo.domain.bbs.dao.BbsDAO;
import com.kh.demo.domain.entity.Bbs;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class BbsSVCImplTest {

  @Autowired
  private BbsSVC bbsSVC;

  @Test
  void save() {
    //given
    Bbs bbs = new Bbs();
    bbs.setTitle("테스트 제목");
    bbs.setContent("테스트 내용");
    bbs.setWriter("테스트 작성자");

    //when
    Long bbsId = bbsSVC.save(bbs);

    //then
    Assertions.assertThat(bbsId).isNotNull();
    Assertions.assertThat(bbsId).isGreaterThan(0);
  }

  @Test
  void findAll() {
  }

  @Test
  void findById() {
  }

  @Test
  void deleteById() {
  }

  @Test
  void deleteByIds() {
  }

  @Test
  void updateById() {
  }
}