
## debear에서 별도로 실행 시켜야함

-- 코드 경로 및 레벨 자동 관리 트리거
CREATE OR REPLACE TRIGGER trg_code_path_level
    BEFORE INSERT OR UPDATE ON code
    FOR EACH ROW
DECLARE
    v_parent_path VARCHAR2(1000);
    v_parent_level NUMBER;
BEGIN
    -- 수정일시 업데이트
    :NEW.udate := SYSTIMESTAMP;
    
    -- 경로 및 레벨 계산
    IF :NEW.pcode IS NULL THEN
        -- 루트 코드
        :NEW.code_path := '/' || :NEW.code_id;
        :NEW.code_level := 1;
    ELSE
        -- 하위 코드
        SELECT code_path, code_level
        INTO v_parent_path, v_parent_level
        FROM code
        WHERE code_id = :NEW.pcode;
        
        :NEW.code_path := v_parent_path || '/' || :NEW.code_id;
        :NEW.code_level := v_parent_level + 1;
        
        -- 순환 참조 방지
        IF :NEW.code_level > 10 THEN
            RAISE_APPLICATION_ERROR(-20001, '코드 레벨이 10을 초과할 수 없습니다.');
        END IF;
    END IF;
END;