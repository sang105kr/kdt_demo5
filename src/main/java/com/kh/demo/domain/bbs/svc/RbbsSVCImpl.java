package com.kh.demo.domain.bbs.svc;

import com.kh.demo.domain.bbs.dao.BbsDAO;
import com.kh.demo.domain.bbs.dao.RbbsDAO;
import com.kh.demo.domain.entity.Bbs;
import com.kh.demo.domain.entity.Rbbs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbbsSVCImpl implements RbbsSVC{

  private final RbbsDAO rbbsDAO;

  @Override
  public Rbbs save(Rbbs rbbs) {

    return rbbsDAO.save(rbbs);
  }

  @Override
  public List<Rbbs> findAll() {
    return rbbsDAO.findAll();
  }

  @Override
  public List<Rbbs> findAll(int pageNo, int numOfRows) {
    return rbbsDAO.findAll(pageNo,numOfRows);
  }

  @Override
  public int getTotalCount() {
    return rbbsDAO.getTotalCount();
  }

  @Override
  public Optional<Rbbs> findById(Long id) {
    return rbbsDAO.findById(id);
  }

  @Override
  public int deleteById(Long id) {
    return rbbsDAO.deleteById(id);
  }


  @Override
  public int updateById(Long id, Rbbs rbbs) {
    return rbbsDAO.updateById(id,rbbs);
  }
}
