package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.CodeId;
import com.kh.demo.domain.dto.CodeDTO;

import java.util.List;

public interface CodeDAO {
  List<CodeDTO> loadCodes(CodeId pocdId);;
}
