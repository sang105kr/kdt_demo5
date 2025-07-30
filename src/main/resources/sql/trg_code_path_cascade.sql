-- 상위 코드 경로 변경 시 하위 코드 경로 자동 업데이트 트리거
CREATE OR REPLACE TRIGGER trg_code_path_cascade
    AFTER UPDATE OF code_path ON code
    FOR EACH ROW
BEGIN
    -- 하위 코드들의 경로 업데이트
    UPDATE code 
    SET code_path = :NEW.code_path || SUBSTR(code_path, LENGTH(:OLD.code_path) + 1),
        code_level = :NEW.code_level + (code_level - :OLD.code_level),
        udate = SYSTIMESTAMP
    WHERE code_path LIKE :OLD.code_path || '/%';
END;