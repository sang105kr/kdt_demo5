package com.kh.demo.domain.common.base;

import java.util.List;
import java.util.Optional;

/**
 * 모든 DAO의 기본 인터페이스
 * 공통 CRUD 메서드들을 정의
 */
public interface BaseDAO<T, ID> {
    
    /**
     * 엔티티 저장
     */
    ID save(T entity);
    
    /**
     * ID로 엔티티 조회
     */
    Optional<T> findById(ID id);
    
    /**
     * 모든 엔티티 조회
     */
    List<T> findAll();
    
    /**
     * 엔티티 수정
     */
    int updateById(ID id, T entity);
    
    /**
     * 엔티티 삭제
     */
    int deleteById(ID id);
    
    /**
     * 총 개수 조회
     */
    int getTotalCount();
    
    /**
     * 페이징 조회 공통 메서드 (기본 구현)
     * @param pageNo 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 행 수
     * @return 엔티티 목록
     */
    default List<T> findAllWithPaging(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return findAllWithOffset(offset, pageSize);
    }
    
    /**
     * 오프셋 기반 페이징 조회 (내부 구현용)
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 엔티티 목록
     */
    List<T> findAllWithOffset(int offset, int limit);
} 