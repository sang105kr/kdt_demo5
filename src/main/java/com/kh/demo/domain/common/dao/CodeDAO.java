package com.kh.demo.domain.common.dao;

import com.kh.demo.domain.common.CodeId;
import com.kh.demo.domain.entity.Code;

import java.util.List;

public interface CodeDAO {
  List<Code> loadCodes(CodeId pocdId);;
}
