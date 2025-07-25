package com.kh.demo.domain.common.exception;

/**
 * 엔티티를 찾을 수 없는 예외
 */
public class EntityNotFoundException extends DomainException {
    
    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s with id %s not found", entityName, id));
    }
    
    public EntityNotFoundException(String message) {
        super(message);
    }
} 