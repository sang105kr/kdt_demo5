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
    
    -- INSERT 시 code_id 생성
    IF INSERTING THEN
        SELECT seq_code_id.NEXTVAL INTO :NEW.code_id FROM DUAL;
    END IF;
    
    -- 경로 및 레벨 계산
    IF :NEW.pcode IS NULL THEN
        -- 루트 코드
        :NEW.code_path := '/' || :NEW.code_id;
        :NEW.code_level := 1;
    ELSE
        -- 부모 코드 정보 조회
        SELECT code_path, code_level
        INTO v_parent_path, v_parent_level
        FROM code
        WHERE code_id = :NEW.pcode;
        
        -- 하위 코드 경로 및 레벨 설정
        :NEW.code_path := v_parent_path || '/' || :NEW.code_id;
        :NEW.code_level := v_parent_level + 1;
    END IF;
    
    -- 기본값 설정
    IF :NEW.sort_order IS NULL THEN
        :NEW.sort_order := 1;
    END IF;
    
    IF :NEW.use_yn IS NULL THEN
        :NEW.use_yn := 'Y';
    END IF;
END;