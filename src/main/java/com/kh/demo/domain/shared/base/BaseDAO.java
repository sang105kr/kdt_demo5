package com.kh.demo.domain.shared.base;

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
} 