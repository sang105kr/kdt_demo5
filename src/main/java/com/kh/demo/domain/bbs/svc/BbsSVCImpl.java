package com.kh.demo.domain.bbs.svc;

import com.kh.demo.domain.bbs.dao.BbsDAO;
import com.kh.demo.domain.entity.Bbs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BbsSVCImpl implements BbsSVC{

  private final BbsDAO bbsDAO;

//  public BbsSVCImpl(BbsDAO bbsDAO) {
//    this.bbsDAO = bbsDAO;
//  }

  @Override
  public Long save(Bbs bbs) {

    return bbsDAO.save(bbs);
  }

  @Override
  public List<Bbs> findAll() {
    return bbsDAO.findAll();
  }

  @Override
  public Optional<Bbs> findById(Long id) {
    return bbsDAO.findById(id);
  }

  @Override
  public int deleteById(Long id) {
    return bbsDAO.deleteById(id);
  }

  @Override
  public int deleteByIds(List<Long> ids) {
    return bbsDAO.deleteByIds(ids);
  }

  @Override
  public int updateById(Long id, Bbs bbs) {
    return bbsDAO.updateById(id,bbs);
  }
}
