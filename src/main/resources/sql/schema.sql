--테이블 삭제(테이블 관련 index는 자동 drop된다)
drop table replies;
drop table boards;
drop table member;
drop table uploadfile;
drop table products;
drop table code;

--시퀀스삭제
drop sequence seq_reply_id;
drop sequence seq_board_id;
drop sequence seq_member_id;
drop sequence seq_uploadfile_id;
drop sequence seq_product_id;
drop sequence seq_code_id;

-------
--코드
-------
CREATE TABLE code (
    code_id     NUMBER(10)     NOT NULL,                    -- 코드 시퀀스 (기본키)
    gcode       VARCHAR2(30)   NOT NULL,                    -- 코드 그룹(분류)
    code        VARCHAR2(30)   NOT NULL,                    -- 코드값
    decode      VARCHAR2(100)  NOT NULL,                    -- 코드명(한글)
    pcode       NUMBER(10),                                 -- 상위코드 시퀀스
    code_path   VARCHAR2(1000),                             -- 코드 경로 (성능 향상)
    code_level  NUMBER(2)      DEFAULT 1,                   -- 코드 레벨
    sort_order  NUMBER         DEFAULT 1,                   -- 정렬순서
    use_yn      CHAR(1)        DEFAULT 'Y',                 -- 사용여부
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,        -- 생성일시
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,        -- 수정일시

    -- 제약 조건
    CONSTRAINT pk_code PRIMARY KEY (code_id),
    CONSTRAINT fk_code_parent FOREIGN KEY (pcode) REFERENCES code(code_id),
    CONSTRAINT uk_code_group_code UNIQUE (gcode, code),      -- 그룹 내 코드 중복 방지
    CONSTRAINT ck_code_use_yn CHECK (use_yn IN ('Y', 'N')),
    CONSTRAINT ck_code_level CHECK (code_level BETWEEN 1 AND 10)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_code_id START WITH 1 INCREMENT BY 1;

-- 인덱스 생성
CREATE INDEX idx_code_gcode ON code(gcode);
CREATE INDEX idx_code_pcode ON code(pcode);
CREATE INDEX idx_code_path ON code(code_path);
CREATE INDEX idx_code_level ON code(code_level);
CREATE INDEX idx_code_use_yn ON code(use_yn);
CREATE INDEX idx_code_sort ON code(gcode, sort_order);

-------
--회원
-------
create table member (
    member_id   NUMBER(10)     NOT NULL,                -- 내부 관리 아이디 (PK)
    email       VARCHAR2(50)   NOT NULL,                -- 로그인 아이디 (UK)
    passwd      VARCHAR2(64)   NOT NULL,                -- 로그인 비밀번호
    tel         VARCHAR2(13),                           -- 연락처
    nickname    VARCHAR2(30),                           -- 별칭
    gender      VARCHAR2(6)    NOT NULL,                -- 성별 (M/F)
    hobby       VARCHAR2(300),                          -- 취미
    region      NUMBER(10),                             -- 지역 (code_id 참조)
    gubun       NUMBER(10)    DEFAULT 2,                -- 회원구분 (code_id 참조)
    pic         BLOB,                                   -- 사진
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,    -- 생성일시
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,    -- 수정일시

    -- 제약조건
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uk_member_email UNIQUE (email),
    CONSTRAINT ck_member_email_nn CHECK (email IS NOT NULL),
    CONSTRAINT ck_member_gender CHECK (gender IN ('M','F')),
    CONSTRAINT fk_member_region FOREIGN KEY (region) REFERENCES code(code_id),
    CONSTRAINT fk_member_gubun FOREIGN KEY (gubun) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_member_id START WITH 1 INCREMENT BY 1;

-- member 인덱스
CREATE INDEX idx_member_region ON member(region);
CREATE INDEX idx_member_gubun ON member(gubun);
CREATE INDEX idx_member_cdate ON member(cdate);

---------
--상품
---------
CREATE TABLE products(
    product_id      NUMBER(10)     NOT NULL,         -- 상품 식별자
    pname           VARCHAR2(100),                   -- 상품명
    description     VARCHAR2(500),                   -- 상품설명
    price           NUMBER(10),                      -- 상품가격
    rating          NUMBER(3,2),                     -- 상품평점
    category        VARCHAR2(50),                    -- 상품카테고리
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    -- 제약조건
    CONSTRAINT pk_products PRIMARY KEY (product_id)
);
-- 시퀀스 생성
CREATE SEQUENCE seq_product_id START WITH 1 INCREMENT BY 1;

-- products 인덱스
CREATE INDEX idx_products_pname ON products(pname);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_rating ON products(rating);

---------
--첨부파일
---------
create table uploadfile(
    uploadfile_id   NUMBER(10)     NOT NULL,         -- 파일아이디
    code            NUMBER(10)     NOT NULL,         -- 분류코드 (code_id 참조)
    rid             VARCHAR2(10)   NOT NULL,         -- 참조번호
    store_filename  VARCHAR2(100)  NOT NULL,         -- 서버보관파일명
    upload_filename VARCHAR2(100)  NOT NULL,         -- 업로드파일명
    fsize           VARCHAR2(45)   NOT NULL,         -- 업로드파일크기
    ftype           VARCHAR2(100)  NOT NULL,         -- 파일유형
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 등록일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_uploadfile PRIMARY KEY (uploadfile_id),
    CONSTRAINT fk_uploadfile_code FOREIGN KEY (code) REFERENCES code(code_id),
    CONSTRAINT ck_uploadfile_code_nn CHECK (code IS NOT NULL),
    CONSTRAINT ck_uploadfile_rid_nn CHECK (rid IS NOT NULL),
    CONSTRAINT ck_uploadfile_store_filename_nn CHECK (store_filename IS NOT NULL),
    CONSTRAINT ck_uploadfile_upload_filename_nn CHECK (upload_filename IS NOT NULL),
    CONSTRAINT ck_uploadfile_fsize_nn CHECK (fsize IS NOT NULL),
    CONSTRAINT ck_uploadfile_ftype_nn CHECK (ftype IS NOT NULL)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_uploadfile_id START WITH 1 INCREMENT BY 1;

-- uploadfile 인덱스
CREATE INDEX idx_uploadfile_code ON uploadfile(code);
CREATE INDEX idx_uploadfile_rid ON uploadfile(rid);
CREATE INDEX idx_uploadfile_cdate ON uploadfile(cdate);

---------
--게시판
---------
create table boards(
    board_id    NUMBER(10)     NOT NULL,         -- 게시글 번호
    bcategory   NUMBER(10)     NOT NULL,         -- 분류카테고리 (code_id 참조)
    title       VARCHAR2(150)  NOT NULL,         -- 제목
    email       VARCHAR2(50)   NOT NULL,         -- email
    nickname    VARCHAR2(30)   NOT NULL,         -- 별칭
    hit         NUMBER(5)      DEFAULT 0,        -- 조회수
    bcontent    CLOB           NOT NULL,         -- 본문
    pboard_id   NUMBER(10),                     -- 부모 게시글번호
    bgroup      NUMBER(10),                     -- 답글그룹
    step        NUMBER(3)      DEFAULT 0,        -- 답글단계
    bindent     NUMBER(3)      DEFAULT 0,        -- 답글들여쓰기
    like_count    NUMBER(5)      DEFAULT 0,        -- 좋아요 수
    dislike_count NUMBER(5)      DEFAULT 0,        -- 비호감 수
    status      CHAR(1)        DEFAULT 'A',      -- 답글상태
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_boards PRIMARY KEY (board_id),
    CONSTRAINT fk_boards_bcategory FOREIGN KEY (bcategory) REFERENCES code(code_id),
    CONSTRAINT fk_boards_pboard_id FOREIGN KEY (pboard_id) REFERENCES boards(board_id),
    CONSTRAINT fk_boards_email FOREIGN KEY (email) REFERENCES member(email),
    CONSTRAINT ck_boards_bcategory_nn CHECK (bcategory IS NOT NULL),
    CONSTRAINT ck_boards_title_nn CHECK (title IS NOT NULL),
    CONSTRAINT ck_boards_email_nn CHECK (email IS NOT NULL),
    CONSTRAINT ck_boards_nickname_nn CHECK (nickname IS NOT NULL),
    CONSTRAINT ck_boards_bcontent_nn CHECK (bcontent IS NOT NULL),
    CONSTRAINT ck_boards_hit CHECK (hit >= 0),
    CONSTRAINT ck_boards_step CHECK (step >= 0),
    CONSTRAINT ck_boards_bindent CHECK (bindent >= 0),
    CONSTRAINT ck_boards_status CHECK (status IN ('A','D','I'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_board_id START WITH 1 INCREMENT BY 1;

-- boards 인덱스
CREATE INDEX idx_boards_bcategory ON boards(bcategory);
CREATE INDEX idx_boards_email ON boards(email);
CREATE INDEX idx_boards_cdate ON boards(cdate);
CREATE INDEX idx_boards_bgroup_step ON boards(bgroup, step);
CREATE INDEX idx_boards_like_count ON boards(like_count);
CREATE INDEX idx_boards_dislike_count ON boards(dislike_count);

---------
--댓글 테이블
---------
CREATE TABLE replies(
    reply_id       NUMBER(10)     NOT NULL,         -- 댓글 번호
    board_id       NUMBER(10)     NOT NULL,         -- 원글 번호 (boards 테이블 참조)
    email          VARCHAR2(50)   NOT NULL,         -- 작성자 이메일
    nickname       VARCHAR2(30)   NOT NULL,         -- 작성자 별칭
    rcontent       VARCHAR2(1000) NOT NULL,         -- 댓글 내용
    parent_id      NUMBER(10),                     -- 부모 댓글 번호 (대댓글용, NULL이면 최상위 댓글)
    rgroup         NUMBER(10),                     -- 댓글 그룹 (같은 그룹 내에서 정렬)
    rstep          NUMBER(3)      DEFAULT 0,        -- 댓글 단계 (대댓글 깊이)
    rindent        NUMBER(3)      DEFAULT 0,        -- 들여쓰기 레벨
    like_count     NUMBER(5)      DEFAULT 0,        -- 좋아요 수
    dislike_count  NUMBER(5)      DEFAULT 0,        -- 비호감 수
    status         CHAR(1)        DEFAULT 'A',      -- 댓글 상태 (활성: 'A', 삭제: 'D', 숨김: 'H')
    cdate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_replies PRIMARY KEY (reply_id),
    CONSTRAINT fk_replies_board_id FOREIGN KEY (board_id) REFERENCES boards(board_id),
    CONSTRAINT fk_replies_parent_id FOREIGN KEY (parent_id) REFERENCES replies(reply_id),
    CONSTRAINT fk_replies_email FOREIGN KEY (email) REFERENCES member(email),
    CONSTRAINT ck_replies_board_id_nn CHECK (board_id IS NOT NULL),
    CONSTRAINT ck_replies_email_nn CHECK (email IS NOT NULL),
    CONSTRAINT ck_replies_nickname_nn CHECK (nickname IS NOT NULL),
    CONSTRAINT ck_replies_rcontent_nn CHECK (rcontent IS NOT NULL),
    CONSTRAINT ck_replies_rstep CHECK (rstep >= 0),
    CONSTRAINT ck_replies_rindent CHECK (rindent >= 0),
    CONSTRAINT ck_replies_status CHECK (status IN ('A','D','H'))
);

--시퀀스
CREATE SEQUENCE seq_reply_id START WITH 1 INCREMENT BY 1;

--인덱스 (성능 최적화)
CREATE INDEX idx_replies_board_id ON replies(board_id);
CREATE INDEX idx_replies_parent_id ON replies(parent_id);
CREATE INDEX idx_replies_rgroup_rstep ON replies(rgroup, rstep);
CREATE INDEX idx_replies_email ON replies(email);
CREATE INDEX idx_replies_cdate ON replies(cdate);
CREATE INDEX idx_replies_like_count ON replies(like_count);
CREATE INDEX idx_replies_dislike_count ON replies(dislike_count);



