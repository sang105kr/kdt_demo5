package com.kh.demo.domain.common.svc;

import com.kh.demo.domain.common.CodeId;
import com.kh.demo.domain.dto.CodeDTO;

import java.util.List;

public interface CodeSVC {
  /**
   * 코드정보 가져오기
   * @param pcodeId  부모코드
   * @return 하위코드
   */
  List<CodeDTO> getCodes(CodeId pcodeId);

  /**
   * A02 코드 정보 가져오기
   * @return A02 코드 정보
   */
  List<CodeDTO> getA02();

}
