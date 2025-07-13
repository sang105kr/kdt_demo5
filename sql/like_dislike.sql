-- 호감/비호감 테이블 생성
CREATE TABLE like_dislike (
    like_dislike_id   NUMBER(10) PRIMARY KEY,
    target_type       VARCHAR2(20) NOT NULL,  -- 'BOARD' 또는 'REPLY'
    target_id         NUMBER(10) NOT NULL,    -- 게시글 ID 또는 댓글 ID
    member_id         NUMBER(10) NOT NULL,    -- 평가한 회원 ID
    like_type         VARCHAR2(10) NOT NULL,  -- 'LIKE' 또는 'DISLIKE'
    cdate             TIMESTAMP DEFAULT SYSTIMESTAMP,
    udate             TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- 시퀀스 생성
CREATE SEQUENCE seq_like_dislike_id;

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_like_dislike_target ON like_dislike(target_type, target_id);
CREATE INDEX idx_like_dislike_member ON like_dislike(member_id);
CREATE INDEX idx_like_dislike_unique ON like_dislike(target_type, target_id, member_id);

-- 유니크 제약조건 (한 회원이 같은 대상에 중복 평가 방지)
ALTER TABLE like_dislike ADD CONSTRAINT uk_like_dislike_unique 
    UNIQUE (target_type, target_id, member_id);

-- 외래키 제약조건
ALTER TABLE like_dislike ADD CONSTRAINT fk_like_dislike_member 
    FOREIGN KEY (member_id) REFERENCES member(member_id);

-- 체크 제약조건
ALTER TABLE like_dislike ADD CONSTRAINT ck_like_dislike_target_type 
    CHECK (target_type IN ('BOARD', 'REPLY'));
ALTER TABLE like_dislike ADD CONSTRAINT ck_like_dislike_type 
    CHECK (like_type IN ('LIKE', 'DISLIKE'));

-- 샘플 데이터
INSERT INTO like_dislike (like_dislike_id, target_type, target_id, member_id, like_type)
VALUES (seq_like_dislike_id.nextval, 'BOARD', 1, 1, 'LIKE');

INSERT INTO like_dislike (like_dislike_id, target_type, target_id, member_id, like_type)
VALUES (seq_like_dislike_id.nextval, 'BOARD', 1, 2, 'DISLIKE');

INSERT INTO like_dislike (like_dislike_id, target_type, target_id, member_id, like_type)
VALUES (seq_like_dislike_id.nextval, 'REPLY', 1, 1, 'LIKE');

COMMIT; 