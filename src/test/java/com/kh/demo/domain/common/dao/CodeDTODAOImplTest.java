package com.kh.demo.domain.common.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class CodeDTODAOImplTest {

  @Autowired
  private CodeDAO codeDAO;

  @Test
  @DisplayName("코드조회")
  void loadCodes() {
    List<CodeDTO> codeDTOS = codeDAO.loadCodes(CodeId.A02);
//    for (Code code : codes) {
//      log.info("{}", code);
//    }
    codeDTOS.stream().forEach(ele->log.info("{}",ele));
  }
}