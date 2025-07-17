--테이블 삭제(테이블 관련 index는 자동 drop된다)
drop table review_reports;
drop table review_comments;
drop table reviews;
drop table payments;
drop table order_items;
drop table orders;
drop table cart_items;
drop table cart;
drop table replies;
drop table boards;
drop table member;
drop table uploadfile;
drop table products;
drop table code;

--시퀀스삭제
drop sequence seq_review_report_id;
drop sequence seq_review_comment_id;
drop sequence seq_review_id;
drop sequence seq_payment_id;
drop sequence seq_order_item_id;
drop sequence seq_order_id;
drop sequence seq_cart_item_id;
drop sequence seq_cart_id;
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
    birth_date  DATE,                                   -- 생년월일
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
    stock_quantity  NUMBER(10)     DEFAULT 0,        -- 재고수량
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    -- 제약조건
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT ck_products_stock_quantity CHECK (stock_quantity >= 0)
);
-- 시퀀스 생성
CREATE SEQUENCE seq_product_id START WITH 1 INCREMENT BY 1;

-- products 인덱스
CREATE INDEX idx_products_pname ON products(pname);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_rating ON products(rating);

---------
--장바구니
---------
CREATE TABLE cart(
    cart_id         NUMBER(10)     NOT NULL,         -- 장바구니 식별자
    member_id       NUMBER(10)     NOT NULL,         -- 회원 식별자
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    
    -- 제약조건
    CONSTRAINT pk_cart PRIMARY KEY (cart_id),
    CONSTRAINT fk_cart_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT uk_cart_member UNIQUE (member_id)     -- 회원당 장바구니 1개
);

-- 시퀀스 생성
CREATE SEQUENCE seq_cart_id START WITH 1 INCREMENT BY 1;

-- cart 인덱스
CREATE INDEX idx_cart_cdate ON cart(cdate);

---------
--장바구니 상품
---------
CREATE TABLE cart_items(
    cart_item_id    NUMBER(10)     NOT NULL,         -- 장바구니 상품 식별자
    cart_id         NUMBER(10)     NOT NULL,         -- 장바구니 식별자
    product_id      NUMBER(10)     NOT NULL,         -- 상품 식별자
    quantity        NUMBER(5)      NOT NULL,         -- 수량
    sale_price      NUMBER(10)     NOT NULL,         -- 장바구니 추가 시점 판매가격
    original_price  NUMBER(10)     NOT NULL,         -- 장바구니 추가 시점 원가격
    discount_rate   NUMBER(3,2)    DEFAULT 0.00,     -- 할인율 (0.00 ~ 1.00)
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    
    -- 제약조건
    CONSTRAINT pk_cart_items PRIMARY KEY (cart_item_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES cart(cart_id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uk_cart_items_product UNIQUE (cart_id, product_id), -- 장바구니 내 동일 상품 중복 방지
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_cart_items_sale_price CHECK (sale_price > 0),
    CONSTRAINT ck_cart_items_original_price CHECK (original_price > 0),
    CONSTRAINT ck_cart_items_discount_rate CHECK (discount_rate >= 0.00 AND discount_rate <= 1.00)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_cart_item_id START WITH 1 INCREMENT BY 1;

-- cart_items 인덱스
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX idx_cart_items_cdate ON cart_items(cdate);

---------
--주문
---------
CREATE TABLE orders(
    order_id         NUMBER(10)     NOT NULL,         -- 주문 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    order_number     VARCHAR2(20)   NOT NULL,         -- 주문번호 (YYYYMMDD-XXXXX)
    order_status     VARCHAR2(20)   DEFAULT 'PENDING', -- 주문상태 (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
    total_amount     NUMBER(10)     NOT NULL,         -- 총 주문금액
    payment_method   VARCHAR2(20)   NOT NULL,         -- 결제방법 (CARD, BANK_TRANSFER, CASH)
    payment_status   VARCHAR2(20)   DEFAULT 'PENDING', -- 결제상태 (PENDING, COMPLETED, FAILED, REFUNDED)
    recipient_name   VARCHAR2(50)   NOT NULL,         -- 수령인명
    recipient_phone  VARCHAR2(20)   NOT NULL,         -- 수령인 연락처
    shipping_address VARCHAR2(200)  NOT NULL,         -- 배송주소
    shipping_memo    VARCHAR2(200),                   -- 배송메모
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    
    -- 제약조건
    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT uk_orders_number UNIQUE (order_number),
    CONSTRAINT ck_orders_status CHECK (order_status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT ck_orders_payment_method CHECK (payment_method IN ('CARD', 'BANK_TRANSFER', 'CASH')),
    CONSTRAINT ck_orders_payment_status CHECK (payment_status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_order_id START WITH 1 INCREMENT BY 1;

-- orders 인덱스
CREATE INDEX idx_orders_member_id ON orders(member_id);
CREATE INDEX idx_orders_order_status ON orders(order_status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_cdate ON orders(cdate);

---------
--주문 상품
---------
CREATE TABLE order_items(
    order_item_id    NUMBER(10)     NOT NULL,         -- 주문 상품 식별자
    order_id         NUMBER(10)     NOT NULL,         -- 주문 식별자
    product_id       NUMBER(10)     NOT NULL,         -- 상품 식별자
    product_name     VARCHAR2(100)  NOT NULL,         -- 주문 시점 상품명 (변경 방지)
    product_price    NUMBER(10)     NOT NULL,         -- 주문 시점 상품가격 (변경 방지)
    quantity         NUMBER(5)      NOT NULL,         -- 주문 수량
    subtotal         NUMBER(10)     NOT NULL,         -- 상품별 총액
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    
    -- 제약조건
    CONSTRAINT pk_order_items PRIMARY KEY (order_item_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_items_subtotal CHECK (subtotal > 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_order_item_id START WITH 1 INCREMENT BY 1;

-- order_items 인덱스
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_cdate ON order_items(cdate);

---------
--결 제
---------
CREATE TABLE payments (
    payment_id      NUMBER(19)      PRIMARY KEY,
    order_id        NUMBER(19)      NOT NULL,
    payment_number  VARCHAR2(50)    NOT NULL,
    payment_method  VARCHAR2(20)    NOT NULL,
    amount          NUMBER(15,2)    NOT NULL,
    status          VARCHAR2(20)    NOT NULL,
    card_number     VARCHAR2(32),
    card_company    VARCHAR2(32),
    approval_number VARCHAR2(32),
    approved_at     TIMESTAMP,
    failure_reason  VARCHAR2(255),
    refund_reason   VARCHAR2(255),
    refunded_at     TIMESTAMP,
    cdate           TIMESTAMP       DEFAULT SYSTIMESTAMP,
    udate           TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 결제 시퀀스
CREATE SEQUENCE seq_payment_id START WITH 1 INCREMENT BY 1 NOCACHE;

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

---------
--리뷰
---------
CREATE TABLE reviews(
    review_id        NUMBER(10)     NOT NULL,         -- 리뷰 식별자
    product_id       NUMBER(10)     NOT NULL,         -- 상품 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    order_id         NUMBER(10)     NOT NULL,         -- 주문 식별자 (구매 인증용)
    rating           NUMBER(2,1)    NOT NULL,         -- 평점 (1.0 ~ 5.0, 0.5 단위)
    title            VARCHAR2(200)  NOT NULL,         -- 리뷰 제목
    content          CLOB           NOT NULL,         -- 리뷰 내용
    helpful_count    NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    report_count     NUMBER(10)     DEFAULT 0,        -- 신고 수
    status           VARCHAR2(20)   DEFAULT 'ACTIVE', -- 상태 (ACTIVE, HIDDEN, DELETED)
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    
    -- 제약조건
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_reviews_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT uk_reviews_order UNIQUE (order_id), -- 주문당 리뷰 1개
    CONSTRAINT ck_reviews_rating CHECK (rating >= 1.0 AND rating <= 5.0),
    CONSTRAINT ck_reviews_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED')),
    CONSTRAINT ck_reviews_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_reviews_report_count CHECK (report_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_review_id START WITH 1 INCREMENT BY 1;

-- reviews 인덱스
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_member_id ON reviews(member_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE INDEX idx_reviews_helpful_count ON reviews(helpful_count);
CREATE INDEX idx_reviews_cdate ON reviews(cdate);

---------
--리뷰 댓글
---------
CREATE TABLE review_comments(
    comment_id       NUMBER(10)     NOT NULL,         -- 댓글 식별자
    review_id        NUMBER(10)     NOT NULL,         -- 리뷰 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    parent_id        NUMBER(10),                      -- 부모 댓글 식별자 (대댓글용)
    content          VARCHAR2(1000) NOT NULL,         -- 댓글 내용
    helpful_count    NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    report_count     NUMBER(10)     DEFAULT 0,        -- 신고 수
    status           VARCHAR2(20)   DEFAULT 'ACTIVE', -- 상태 (ACTIVE, HIDDEN, DELETED)
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    
    -- 제약조건
    CONSTRAINT pk_review_comments PRIMARY KEY (comment_id),
    CONSTRAINT fk_review_comments_review FOREIGN KEY (review_id) REFERENCES reviews(review_id),
    CONSTRAINT fk_review_comments_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_review_comments_parent FOREIGN KEY (parent_id) REFERENCES review_comments(comment_id),
    CONSTRAINT ck_review_comments_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED')),
    CONSTRAINT ck_review_comments_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_review_comments_report_count CHECK (report_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_review_comment_id START WITH 1 INCREMENT BY 1;

-- review_comments 인덱스
CREATE INDEX idx_review_comments_review_id ON review_comments(review_id);
CREATE INDEX idx_review_comments_member_id ON review_comments(member_id);
CREATE INDEX idx_review_comments_parent_id ON review_comments(parent_id);
CREATE INDEX idx_review_comments_status ON review_comments(status);
CREATE INDEX idx_review_comments_cdate ON review_comments(cdate);

---------
--리뷰 신고
---------
CREATE TABLE review_reports(
    report_id        NUMBER(10)     NOT NULL,         -- 신고 식별자
    review_id        NUMBER(10),                      -- 리뷰 식별자 (NULL이면 댓글 신고)
    comment_id       NUMBER(10),                      -- 댓글 식별자 (NULL이면 리뷰 신고)
    reporter_id      NUMBER(10)     NOT NULL,         -- 신고자 식별자
    report_type      VARCHAR2(20)   NOT NULL,         -- 신고 유형 (SPAM, INAPPROPRIATE, COPYRIGHT, OTHER)
    report_reason    VARCHAR2(500)  NOT NULL,         -- 신고 사유
    status           VARCHAR2(20)   DEFAULT 'PENDING', -- 처리 상태 (PENDING, PROCESSED, REJECTED)
    admin_memo       VARCHAR2(500),                   -- 관리자 메모
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시
    
    -- 제약조건
    CONSTRAINT pk_review_reports PRIMARY KEY (report_id),
    CONSTRAINT fk_review_reports_review FOREIGN KEY (review_id) REFERENCES reviews(review_id),
    CONSTRAINT fk_review_reports_comment FOREIGN KEY (comment_id) REFERENCES review_comments(comment_id),
    CONSTRAINT fk_review_reports_reporter FOREIGN KEY (reporter_id) REFERENCES member(member_id),
    CONSTRAINT ck_review_reports_type CHECK (report_type IN ('SPAM', 'INAPPROPRIATE', 'COPYRIGHT', 'OTHER')),
    CONSTRAINT ck_review_reports_status CHECK (status IN ('PENDING', 'PROCESSED', 'REJECTED')),
    CONSTRAINT ck_review_reports_target CHECK (
        (review_id IS NOT NULL AND comment_id IS NULL) OR 
        (review_id IS NULL AND comment_id IS NOT NULL)
    ) -- 리뷰 또는 댓글 중 하나만 신고 가능
);

-- 시퀀스 생성
CREATE SEQUENCE seq_review_report_id START WITH 1 INCREMENT BY 1;

-- review_reports 인덱스
CREATE INDEX idx_review_reports_review_id ON review_reports(review_id);
CREATE INDEX idx_review_reports_comment_id ON review_reports(comment_id);
CREATE INDEX idx_review_reports_reporter_id ON review_reports(reporter_id);
CREATE INDEX idx_review_reports_type ON review_reports(report_type);
CREATE INDEX idx_review_reports_status ON review_reports(status);
CREATE INDEX idx_review_reports_cdate ON review_reports(cdate);