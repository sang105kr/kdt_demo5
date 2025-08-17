-- 테이블 삭제 (테이블 관련 인덱스는 자동 drop)
DROP TABLE notifications;
DROP TABLE search_logs;
DROP TABLE auto_action_rules;
DROP TABLE report_statistics;
DROP TABLE reports;
DROP TABLE tokens;
DROP TABLE evaluation;
DROP TABLE qna_comment;
DROP TABLE qna;
DROP TABLE chat_message;
DROP TABLE chat_session;
DROP TABLE faq;
DROP TABLE review_comments;
DROP TABLE reviews;
DROP TABLE payments;
DROP TABLE order_items;
DROP TABLE orders;
DROP TABLE cart_items;
DROP TABLE cart;
DROP TABLE wishlist;
DROP TABLE replies;
DROP TABLE boards;
DROP TABLE notices;
DROP TABLE member_hobbies;
DROP TABLE member;
DROP TABLE uploadfile;
DROP TABLE products;
DROP TABLE code;

-- 시퀀스 삭제
DROP SEQUENCE seq_notification_id;
DROP SEQUENCE seq_search_log_id;
DROP SEQUENCE seq_auto_action_rule_id;
DROP SEQUENCE seq_report_id;
DROP SEQUENCE seq_report_stat_id;
DROP SEQUENCE seq_token_id;
DROP SEQUENCE seq_evaluation_id;
DROP SEQUENCE seq_qna_comment_id;
DROP SEQUENCE seq_qna_id;
DROP SEQUENCE seq_chat_message_id;
DROP SEQUENCE seq_chat_session_id;
DROP SEQUENCE seq_faq_id;
DROP SEQUENCE seq_review_comment_id;
DROP SEQUENCE seq_review_id;
DROP SEQUENCE seq_payment_id;
DROP SEQUENCE seq_order_item_id;
DROP SEQUENCE seq_order_id;
DROP SEQUENCE seq_cart_item_id;
DROP SEQUENCE seq_cart_id;
DROP SEQUENCE seq_wishlist_id;
DROP SEQUENCE seq_reply_id;
DROP SEQUENCE seq_board_id;
DROP SEQUENCE seq_notice_id;
DROP SEQUENCE seq_member_hobby_id;
DROP SEQUENCE seq_member_id;
DROP SEQUENCE seq_uploadfile_id;
DROP SEQUENCE seq_product_id;
DROP SEQUENCE seq_code_id;

-------
-- 코드
-------
CREATE TABLE code (
    code_id     NUMBER(10)     NOT NULL,                    -- 코드 시퀀스 (기본키)
    gcode       VARCHAR2(30)   NOT NULL,                    -- 코드 그룹(분류)
    code        VARCHAR2(30)   NOT NULL,                    -- 코드값
    decode      VARCHAR2(100)  NOT NULL,                    -- 코드명(한글)
    pcode       NUMBER(10),                                 -- 상위코드 시퀀스
    code_path   VARCHAR2(200),                              -- 코드 경로 (성능 향상, 크기 축소)
    code_level  NUMBER(2)      DEFAULT 1,                   -- 코드 레벨
    sort_order  NUMBER         DEFAULT 1,                   -- 정렬순서
    use_yn      CHAR(1)        DEFAULT 'Y',                 -- 사용여부
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,   -- 생성일시
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,   -- 수정일시

    -- 제약 조건
    CONSTRAINT pk_code PRIMARY KEY (code_id),
    CONSTRAINT fk_code_parent FOREIGN KEY (pcode) REFERENCES code(code_id),
    CONSTRAINT uk_code_group_code UNIQUE (gcode, code),      -- 그룹 내 코드 중복 방지
    CONSTRAINT ck_code_use_yn CHECK (use_yn IN ('Y', 'N')),
    CONSTRAINT ck_code_level CHECK (code_level BETWEEN 1 AND 10)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_code_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 인덱스 생성 (필수 인덱스만)
CREATE INDEX idx_code_gcode_sort ON code(gcode, sort_order); -- 코드 그룹별 정렬용
CREATE INDEX idx_code_pcode ON code(pcode); -- 계층 구조 조회용

-------
-- 회원
-------
CREATE TABLE member (
    member_id   NUMBER(10)     NOT NULL,                -- 내부 관리 아이디 (PK)
    email       VARCHAR2(50)   NOT NULL,                -- 로그인 아이디 (UK)
    passwd      VARCHAR2(128)  NOT NULL,                -- 로그인 비밀번호 (해시 알고리즘 고려)
    tel         VARCHAR2(13),                           -- 연락처
    nickname    VARCHAR2(30),                           -- 별칭
    gender      NUMBER(10)     NOT NULL,                -- 성별 (code_id 참조, gcode='GENDER')
    birth_date  DATE,                                   -- 생년월일

    region      NUMBER(10),                             -- 지역 (code_id 참조)
    address     VARCHAR2(200),                          -- 기본주소
    address_detail VARCHAR2(100),                       -- 상세주소
    zipcode     VARCHAR2(10),                           -- 우편번호
    gubun       NUMBER(10)     DEFAULT 2 NOT NULL,      -- 회원구분 (code_id 참조)
    status      NUMBER(10)     DEFAULT 15 NOT NULL,     -- 회원상태 (code_id 참조)
    status_reason VARCHAR2(200),                        -- 상태 변경 사유
    status_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 상태 변경일시
    pic         BLOB,                                   -- 사진
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,    -- 생성일시
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,    -- 수정일시

    -- 제약조건
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uk_member_email UNIQUE (email),
    CONSTRAINT ck_member_email_nn CHECK (email IS NOT NULL),
    CONSTRAINT fk_member_gender FOREIGN KEY (gender) REFERENCES code(code_id),
    CONSTRAINT fk_member_region FOREIGN KEY (region) REFERENCES code(code_id),
    CONSTRAINT fk_member_gubun FOREIGN KEY (gubun) REFERENCES code(code_id),
    CONSTRAINT fk_member_status FOREIGN KEY (status) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_member_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- member 인덱스 (필수 인덱스만)
CREATE INDEX idx_member_status ON member(status); -- 회원 상태별 조회용

---------
-- 회원 취미 (1:N 관계)
---------
CREATE TABLE member_hobbies (
    hobby_id    NUMBER(10)     NOT NULL,                -- 취미 매핑 ID
    member_id   NUMBER(10)     NOT NULL,                -- 회원 ID
    hobby_code_id NUMBER(10)   NOT NULL,                -- 취미 코드 ID (code_id 참조, gcode='HOBBY')
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시

    -- 제약조건
    CONSTRAINT pk_member_hobbies PRIMARY KEY (hobby_id),
    CONSTRAINT fk_member_hobbies_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_member_hobbies_hobby FOREIGN KEY (hobby_code_id) REFERENCES code(code_id),
    CONSTRAINT uk_member_hobbies UNIQUE (member_id, hobby_code_id) -- 동일 회원의 동일 취미 중복 방지
);

-- 시퀀스 생성
CREATE SEQUENCE seq_member_hobby_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 인덱스 생성
CREATE INDEX idx_member_hobbies_member ON member_hobbies(member_id); -- 회원별 취미 조회용

---------
-- 알림 테이블
---------
CREATE TABLE notifications (
    notification_id    NUMBER(10)     NOT NULL,              -- 알림 ID
    member_id         NUMBER(10)     NOT NULL,              -- 회원 ID
    target_type       NUMBER(10)     NOT NULL,              -- 대상 타입 (code_id 참조, gcode='NOTIFICATION_TARGET_TYPE')
    notification_type_id NUMBER(10)   NOT NULL,              -- 알림 타입 (code_id 참조)
    title             VARCHAR2(200)  NOT NULL,              -- 알림 제목
    message           VARCHAR2(1000) NOT NULL,              -- 알림 메시지
    target_url        VARCHAR2(500),                        -- 관련 URL
    target_id         NUMBER(10),                           -- 관련 ID (주문ID, 상품ID 등)
    is_read           CHAR(1)        DEFAULT 'N',           -- 읽음 여부 (N: 안읽음, Y: 읽음)
    created_date      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    read_date         TIMESTAMP,                            -- 읽음 일시
    use_yn            CHAR(1)        DEFAULT 'Y',           -- 사용여부

    -- 제약조건
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_notifications_target_type FOREIGN KEY (target_type) REFERENCES code(code_id),
    CONSTRAINT fk_notifications_type FOREIGN KEY (notification_type_id) REFERENCES code(code_id),
    CONSTRAINT ck_notifications_is_read CHECK (is_read IN ('Y', 'N')),
    CONSTRAINT ck_notifications_use_yn CHECK (use_yn IN ('Y', 'N'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_notification_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_notifications_member_read ON notifications(member_id, is_read); -- 회원별 읽지 않은 알림 조회용
CREATE INDEX idx_notifications_type ON notifications(notification_type_id); -- 알림 타입별 조회용
CREATE INDEX idx_notifications_created_date ON notifications(created_date DESC); -- 최신순 정렬용
CREATE INDEX idx_notifications_target ON notifications(target_type, target_id); -- 대상별 조회용

---------
-- 상품
---------
CREATE TABLE products(
    product_id      NUMBER(10)     NOT NULL,         -- 상품 식별자
    pname           VARCHAR2(100),                   -- 상품명
    description     VARCHAR2(500),                   -- 상품설명
    price           NUMBER(15,2),                    -- 상품가격 (정밀도 통일)
    rating          NUMBER(3,1)    DEFAULT 0.0,      -- 상품평점 (소수점 1자리까지)
    review_count    NUMBER(10)     DEFAULT 0,        -- 리뷰 개수
    category_id     NUMBER(10),                      -- 상품카테고리 (code_id 참조)
    stock_quantity  NUMBER(10)     DEFAULT 0,        -- 재고수량
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES code(code_id),
    CONSTRAINT ck_products_stock_quantity CHECK (stock_quantity >= 0),
    CONSTRAINT ck_products_review_count CHECK (review_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_product_id START WITH 1 INCREMENT BY 1 NOCACHE;
-- products 인덱스 (개선된 인덱스)
CREATE INDEX idx_products_category_rating ON products(category_id, rating DESC); -- 카테고리별 평점 정렬용
CREATE INDEX idx_products_category_price ON products(category_id, price); -- 카테고리별 가격 검색/정렬용

---------
-- 장바구니
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
CREATE SEQUENCE seq_cart_id START WITH 1 INCREMENT BY 1 NOCACHE;

---------
-- 장바구니 상품
---------
CREATE TABLE cart_items(
    cart_item_id    NUMBER(10)     NOT NULL,         -- 장바구니 상품 식별자
    cart_id         NUMBER(10)     NOT NULL,         -- 장바구니 식별자
    product_id      NUMBER(10)     NOT NULL,         -- 상품 식별자
    quantity        NUMBER(5)      NOT NULL,         -- 수량
    sale_price      NUMBER(15,2)   NOT NULL,         -- 장바구니 추가 시점 판매가격 (정밀도 통일)
    original_price  NUMBER(15,2)   NOT NULL,         -- 장바구니 추가 시점 원가격 (정밀도 통일)
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
CREATE SEQUENCE seq_cart_item_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 인덱스 추가
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id); -- 장바구니별 조회용

---------
-- 위시리스트
---------
CREATE TABLE wishlist(
    wishlist_id     NUMBER(10)     NOT NULL,         -- 위시리스트 식별자
    member_id       NUMBER(10)     NOT NULL,         -- 회원 식별자
    product_id      NUMBER(10)     NOT NULL,         -- 상품 식별자
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_wishlist PRIMARY KEY (wishlist_id),
    CONSTRAINT fk_wishlist_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uk_wishlist_member_product UNIQUE (member_id, product_id) -- 회원당 상품별 위시리스트 1개
);

-- 시퀀스 생성
CREATE SEQUENCE seq_wishlist_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- wishlist 인덱스 (필수 인덱스만)
CREATE INDEX idx_wishlist_member ON wishlist(member_id); -- 회원별 위시리스트 조회용

---------
-- 주문
---------
CREATE TABLE orders(
    order_id         NUMBER(10)     NOT NULL,         -- 주문 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    order_number     VARCHAR2(20)   NOT NULL,         -- 주문번호 (YYYYMMDD-XXXXX)
    order_status_id  NUMBER(10)     NOT NULL,         -- 주문상태 (code_id, gcode='ORDER_STATUS')
    total_amount     NUMBER(15,2)   NOT NULL,         -- 총 주문금액 (정밀도 통일)
    payment_method_id NUMBER(10)    NOT NULL,         -- 결제방법 (code_id, gcode='PAYMENT_METHOD')
    payment_status_id NUMBER(10)    NOT NULL,         -- 결제상태 (code_id, gcode='PAYMENT_STATUS')
    recipient_name   VARCHAR2(50)   NOT NULL,         -- 수령인명
    recipient_phone  VARCHAR2(20)   NOT NULL,         -- 수령인 연락처
    zipcode          VARCHAR2(10)   NOT NULL,         -- 우편번호
    address          VARCHAR2(200)  NOT NULL,         -- 기본주소
    address_detail   VARCHAR2(100)  NOT NULL,         -- 상세주소
    shipping_memo    VARCHAR2(200),                   -- 배송메모
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT uk_orders_number UNIQUE (order_number),
    CONSTRAINT fk_orders_order_status FOREIGN KEY (order_status_id) REFERENCES code(code_id),
    CONSTRAINT fk_orders_payment_method FOREIGN KEY (payment_method_id) REFERENCES code(code_id),
    CONSTRAINT fk_orders_payment_status FOREIGN KEY (payment_status_id) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_order_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- orders 인덱스 (성능 향상 추가)
CREATE INDEX idx_orders_member_status ON orders(member_id, order_status_id); -- 회원별 주문 상태 조회용
CREATE INDEX idx_orders_payment_status ON orders(payment_status_id); -- 결제 상태별 조회용
CREATE INDEX idx_orders_created_date ON orders(cdate DESC); -- 최신 주문 조회용

---------
-- 주문 상품
---------
CREATE TABLE order_items(
    order_item_id    NUMBER(10)     NOT NULL,         -- 주문 상품 식별자
    order_id         NUMBER(10)     NOT NULL,         -- 주문 식별자
    product_id       NUMBER(10)     NOT NULL,         -- 상품 식별자
    product_name     VARCHAR2(100)  NOT NULL,         -- 주문 시점 상품명 (변경 방지)
    product_price    NUMBER(15,2)   NOT NULL,         -- 주문 시점 상품가격 (정밀도 통일)
    quantity         NUMBER(5)      NOT NULL,         -- 주문 수량
    subtotal         NUMBER(15,2)   NOT NULL,         -- 상품별 총액 (정밀도 통일)
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
CREATE SEQUENCE seq_order_item_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 인덱스 추가
CREATE INDEX idx_order_items_order ON order_items(order_id); -- 주문별 조회용

---------
-- 결제
---------
CREATE TABLE payments (
    payment_id      NUMBER(10)     NOT NULL,         -- ID 타입 통일
    order_id        NUMBER(10)     NOT NULL,         -- ID 타입 통일
    payment_number  VARCHAR2(50)   NOT NULL,
    payment_method  NUMBER(10)     NOT NULL,         -- 결제 방법 (code_id 참조, gcode='PAYMENT_METHOD')
    amount          NUMBER(15,2)   NOT NULL,         -- 정밀도 통일
    status          NUMBER(10)     NOT NULL,         -- 결제 상태 (code_id 참조, gcode='PAYMENT_STATUS')
    card_number     VARCHAR2(32),
    card_company    VARCHAR2(32),
    approval_number VARCHAR2(32),
    approved_at     TIMESTAMP,
    failure_reason  VARCHAR2(255),
    refund_reason   VARCHAR2(255),
    refunded_at     TIMESTAMP,
    cdate           TIMESTAMP      DEFAULT SYSTIMESTAMP,
    udate           TIMESTAMP      DEFAULT SYSTIMESTAMP,

    -- 제약조건
    CONSTRAINT pk_payments PRIMARY KEY (payment_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_payments_method FOREIGN KEY (payment_method) REFERENCES code(code_id),
    CONSTRAINT fk_payments_status FOREIGN KEY (status) REFERENCES code(code_id)
);

-- 결제 시퀀스
CREATE SEQUENCE seq_payment_id START WITH 1 INCREMENT BY 1 NOCACHE;

---------
-- 첨부파일
---------
CREATE TABLE uploadfile(
    uploadfile_id   NUMBER(10)     NOT NULL,         -- 파일아이디
    code            NUMBER(10)     NOT NULL,         -- 분류코드 (code_id 참조)
    rid             VARCHAR2(10)   NOT NULL,         -- 참조번호
    store_filename  VARCHAR2(100)  NOT NULL,         -- 서버보관파일명
    upload_filename VARCHAR2(100)  NOT NULL,         -- 업로드파일명
    fsize           NUMBER(10)     NOT NULL,         -- 업로드파일크기
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
CREATE SEQUENCE seq_uploadfile_id START WITH 1 INCREMENT BY 1 NOCACHE;

---------
-- 게시판
---------
CREATE TABLE boards(
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
    status_id   NUMBER(10)     NOT NULL,         -- 답글상태 (code_id 참조)
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_boards PRIMARY KEY (board_id),
    CONSTRAINT fk_boards_bcategory FOREIGN KEY (bcategory) REFERENCES code(code_id),
    CONSTRAINT fk_boards_pboard_id FOREIGN KEY (pboard_id) REFERENCES boards(board_id),
    CONSTRAINT fk_boards_email FOREIGN KEY (email) REFERENCES member(email),
    CONSTRAINT fk_boards_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_boards_bcategory_nn CHECK (bcategory IS NOT NULL),
    CONSTRAINT ck_boards_title_nn CHECK (title IS NOT NULL),
    CONSTRAINT ck_boards_email_nn CHECK (email IS NOT NULL),
    CONSTRAINT ck_boards_nickname_nn CHECK (nickname IS NOT NULL),
    CONSTRAINT ck_boards_bcontent_nn CHECK (bcontent IS NOT NULL),
    CONSTRAINT ck_boards_hit CHECK (hit >= 0),
    CONSTRAINT ck_boards_step CHECK (step >= 0),
    CONSTRAINT ck_boards_bindent CHECK (bindent >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_board_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- boards 인덱스 (필수 인덱스만)
CREATE INDEX idx_boards_category_group ON boards(bcategory, bgroup, step); -- 카테고리별 계층 조회용
CREATE INDEX idx_boards_email ON boards(email); -- 작성자별 조회용

---------
-- 댓글 테이블
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
    status_id      NUMBER(10)     NOT NULL,         -- 댓글 상태 (code_id 참조)
    cdate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_replies PRIMARY KEY (reply_id),
    CONSTRAINT fk_replies_board_id FOREIGN KEY (board_id) REFERENCES boards(board_id),
    CONSTRAINT fk_replies_parent_id FOREIGN KEY (parent_id) REFERENCES replies(reply_id),
    CONSTRAINT fk_replies_email FOREIGN KEY (email) REFERENCES member(email),
    CONSTRAINT fk_replies_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_replies_board_id_nn CHECK (board_id IS NOT NULL),
    CONSTRAINT ck_replies_email_nn CHECK (email IS NOT NULL),
    CONSTRAINT ck_replies_nickname_nn CHECK (nickname IS NOT NULL),
    CONSTRAINT ck_replies_rcontent_nn CHECK (rcontent IS NOT NULL),
    CONSTRAINT ck_replies_rstep CHECK (rstep >= 0),
    CONSTRAINT ck_replies_rindent CHECK (rindent >= 0),
    CONSTRAINT ck_replies_no_self_parent CHECK (parent_id != reply_id) -- 순환 참조 방지
);

-- 시퀀스
CREATE SEQUENCE seq_reply_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 인덱스 (필수 인덱스만)
CREATE INDEX idx_replies_board_group ON replies(board_id, rgroup, rstep); -- 게시글별 계층 조회용
CREATE INDEX idx_replies_email ON replies(email); -- 작성자별 조회용

---------
-- 리뷰
---------
CREATE TABLE reviews(
    review_id        NUMBER(10)     NOT NULL,         -- 리뷰 식별자
    product_id       NUMBER(10)     NOT NULL,         -- 상품 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    order_id         NUMBER(10)     NOT NULL,         -- 주문 식별자 (구매 인증용)
    rating           NUMBER(1)      NOT NULL,         -- 평점 (1 ~ 5)
    title            VARCHAR2(200)  NOT NULL,         -- 리뷰 제목
    content          CLOB           NOT NULL,         -- 리뷰 내용
    helpful_count    NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    unhelpful_count  NUMBER(10)     DEFAULT 0,        -- 도움안됨 수
    report_count     NUMBER(10)     DEFAULT 0,        -- 신고 수
    status_id        NUMBER(10)     NOT NULL,         -- 상태 (code_id, gcode='REVIEW_STATUS')
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_reviews_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_reviews_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT ck_reviews_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_reviews_unhelpful_count CHECK (unhelpful_count >= 0),
    CONSTRAINT ck_reviews_report_count CHECK (report_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_review_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- reviews 인덱스 (성능 향상 추가)
CREATE INDEX idx_reviews_product_status ON reviews(product_id, status_id); -- 상품별 활성 리뷰 조회용
CREATE INDEX idx_reviews_member ON reviews(member_id); -- 회원별 리뷰 조회용
CREATE INDEX idx_reviews_rating_created ON reviews(rating DESC, cdate DESC); -- 평점/최신순 정렬용

---------
-- 리뷰 댓글
---------
CREATE TABLE review_comments(
    comment_id       NUMBER(10)     NOT NULL,         -- 댓글 식별자
    review_id        NUMBER(10)     NOT NULL,         -- 리뷰 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    parent_id        NUMBER(10),                      -- 부모 댓글 식별자 (대댓글용)
    content          VARCHAR2(1000) NOT NULL,         -- 댓글 내용
    helpful_count    NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    unhelpful_count  NUMBER(10)     DEFAULT 0,        -- 도움안됨 수
    report_count     NUMBER(10)     DEFAULT 0,        -- 신고 수
    status_id        NUMBER(10)     NOT NULL,         -- 상태 (code_id, gcode='REVIEW_COMMENT_STATUS')
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_review_comments PRIMARY KEY (comment_id),
    CONSTRAINT fk_review_comments_review FOREIGN KEY (review_id) REFERENCES reviews(review_id),
    CONSTRAINT fk_review_comments_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_review_comments_parent FOREIGN KEY (parent_id) REFERENCES review_comments(comment_id),
    CONSTRAINT fk_review_comments_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_review_comments_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_review_comments_unhelpful_count CHECK (unhelpful_count >= 0),
    CONSTRAINT ck_review_comments_report_count CHECK (report_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_review_comment_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- review_comments 인덱스 (필수 인덱스만)
CREATE INDEX idx_review_comments_review_status ON review_comments(review_id, status_id); -- 리뷰별 활성 댓글 조회용

---------
-- FAQ 테이블
---------
CREATE TABLE faq (
    faq_id         NUMBER(10)     NOT NULL,         -- FAQ ID
    category_id    NUMBER(10)     NOT NULL,         -- FAQ 카테고리 (code_id 참조)
    question       VARCHAR2(500)  NOT NULL,         -- 질문
    answer         CLOB           NOT NULL,         -- 답변
    keywords       VARCHAR2(1000),                  -- 검색 키워드
    view_count     NUMBER(10)     DEFAULT 0,        -- 조회수
    helpful_count  NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    unhelpful_count NUMBER(10)    DEFAULT 0,        -- 도움안됨 수
    sort_order     NUMBER(5)      DEFAULT 0,        -- 정렬순서
    is_active      CHAR(1)        DEFAULT 'Y',      -- 활성화 여부
    admin_id       NUMBER(10),                      -- 작성자 관리자 ID
    cdate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_faq PRIMARY KEY (faq_id),
    CONSTRAINT fk_faq_category FOREIGN KEY (category_id) REFERENCES code(code_id),
    CONSTRAINT fk_faq_admin FOREIGN KEY (admin_id) REFERENCES member(member_id),
    CONSTRAINT ck_faq_view_count CHECK (view_count >= 0),
    CONSTRAINT ck_faq_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_faq_unhelpful_count CHECK (unhelpful_count >= 0),
    CONSTRAINT ck_faq_is_active CHECK (is_active IN ('Y', 'N'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_faq_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- FAQ 인덱스
CREATE INDEX idx_faq_category_active ON faq(category_id, is_active); -- 카테고리별 활성 FAQ 조회용
CREATE INDEX idx_faq_keywords ON faq(keywords); -- 키워드 검색용
CREATE INDEX idx_faq_sort_order ON faq(sort_order); -- 정렬순서용
CREATE INDEX idx_faq_view_count ON faq(view_count DESC); -- 인기순 정렬용

---------
-- Q&A 테이블
---------
CREATE TABLE qna (
    qna_id         NUMBER(10)     NOT NULL,         -- Q&A ID
    product_id     NUMBER(10),                      -- 상품 ID (선택적, 일반 Q&A는 NULL)
    member_id      NUMBER(10)     NOT NULL,         -- 질문자 회원 ID
    category_id    NUMBER(10)     NOT NULL,         -- Q&A 카테고리 (code_id 참조, gcode='QNA_CATEGORY')
    admin_id       NUMBER(10),                      -- 답변자 관리자 ID
    title          VARCHAR2(200)  NOT NULL,         -- 질문 제목
    content        VARCHAR2(2000) NOT NULL,         -- 질문 내용
    answer         VARCHAR2(2000),                  -- 답변 내용
    helpful_count  NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    unhelpful_count NUMBER(10)    DEFAULT 0,        -- 도움안됨 수
    view_count     NUMBER(10)     DEFAULT 0,        -- 조회수
    comment_count  NUMBER(10)     DEFAULT 0,        -- 댓글 수
    status_id      NUMBER(10)     DEFAULT 1 NOT NULL, -- 상태 (code_id 참조, gcode='QNA_STATUS')
    visibility     CHAR(1)        DEFAULT 'P',      -- 공개여부 (P: 공개, S: 비밀)
    answered_at    TIMESTAMP,                       -- 답변일시
    cdate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_qna PRIMARY KEY (qna_id),
    CONSTRAINT fk_qna_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_qna_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_qna_category FOREIGN KEY (category_id) REFERENCES code(code_id),
    CONSTRAINT fk_qna_admin FOREIGN KEY (admin_id) REFERENCES member(member_id),
    CONSTRAINT fk_qna_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_qna_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_qna_unhelpful_count CHECK (unhelpful_count >= 0),
    CONSTRAINT ck_qna_view_count CHECK (view_count >= 0),
    CONSTRAINT ck_qna_comment_count CHECK (comment_count >= 0),
    CONSTRAINT ck_qna_visibility CHECK (visibility IN ('P', 'S'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_qna_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- qna 인덱스 (필수 인덱스만)
CREATE INDEX idx_qna_product_status ON qna(product_id, status_id); -- 상품별 상태 조회용
CREATE INDEX idx_qna_category_status ON qna(category_id, status_id); -- 카테고리별 상태 조회용
CREATE INDEX idx_qna_member ON qna(member_id); -- 회원별 Q&A 조회용
CREATE INDEX idx_qna_admin ON qna(admin_id); -- 관리자별 답변 조회용
CREATE INDEX idx_qna_created_date ON qna(cdate DESC); -- 최신순 정렬용
CREATE INDEX idx_qna_helpful_count ON qna(helpful_count DESC); -- 인기순 정렬용
CREATE INDEX idx_qna_answered_date ON qna(answered_at DESC); -- 답변순 정렬용

---------
-- Q&A 댓글 테이블
---------
CREATE TABLE qna_comment (
    comment_id      NUMBER(10)     NOT NULL,         -- 댓글 ID
    qna_id          NUMBER(10)     NOT NULL,         -- Q&A ID
    member_id       NUMBER(10)     NOT NULL,         -- 작성자 ID
    admin_id        NUMBER(10),                      -- 관리자 ID (관리자 댓글인 경우)
    content         VARCHAR2(1000) NOT NULL,         -- 댓글 내용
    comment_type_id NUMBER(10)     NOT NULL,         -- 댓글 타입 (code_id 참조)
    helpful_count   NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    unhelpful_count NUMBER(10)     DEFAULT 0,        -- 도움안됨 수
    status_id       NUMBER(10)     DEFAULT 1 NOT NULL, -- 상태 (code_id 참조)
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_qna_comment PRIMARY KEY (comment_id),
    CONSTRAINT fk_qna_comment_qna FOREIGN KEY (qna_id) REFERENCES qna(qna_id),
    CONSTRAINT fk_qna_comment_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_qna_comment_admin FOREIGN KEY (admin_id) REFERENCES member(member_id),
    CONSTRAINT fk_qna_comment_type FOREIGN KEY (comment_type_id) REFERENCES code(code_id),
    CONSTRAINT fk_qna_comment_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_qna_comment_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_qna_comment_unhelpful_count CHECK (unhelpful_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_qna_comment_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- Q&A 댓글 인덱스
CREATE INDEX idx_qna_comment_qna ON qna_comment(qna_id); -- Q&A별 댓글 조회용
CREATE INDEX idx_qna_comment_member ON qna_comment(member_id); -- 회원별 댓글 조회용
CREATE INDEX idx_qna_comment_admin ON qna_comment(admin_id); -- 관리자별 댓글 조회용

---------
-- 공지사항 테이블
---------
CREATE TABLE notices (
    notice_id       NUMBER(10)     NOT NULL,         -- 공지사항 ID
    category_id     NUMBER(10)     NOT NULL,         -- 공지사항 카테고리 (code_id 참조)
    title           VARCHAR2(200)  NOT NULL,         -- 제목
    content         CLOB           NOT NULL,         -- 내용
    author_id       NUMBER(10)     NOT NULL,         -- 작성자 ID (관리자)
    view_count      NUMBER(10)     DEFAULT 0,        -- 조회수
    is_important    CHAR(1)        DEFAULT 'N',      -- 중요공지 여부 (Y: 중요, N: 일반)
    is_fixed        CHAR(1)        DEFAULT 'N',      -- 상단고정 여부 (Y: 고정, N: 일반)
    start_date      DATE,                            -- 공지 시작일
    end_date        DATE,                            -- 공지 종료일
    status_id       NUMBER(10)     DEFAULT 1 NOT NULL, -- 상태 (code_id 참조, gcode='NOTICE_STATUS')
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_notices PRIMARY KEY (notice_id),
    CONSTRAINT fk_notices_category FOREIGN KEY (category_id) REFERENCES code(code_id),
    CONSTRAINT fk_notices_author FOREIGN KEY (author_id) REFERENCES member(member_id),
    CONSTRAINT fk_notices_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_notices_view_count CHECK (view_count >= 0),
    CONSTRAINT ck_notices_is_important CHECK (is_important IN ('Y', 'N')),
    CONSTRAINT ck_notices_is_fixed CHECK (is_fixed IN ('Y', 'N')),
    CONSTRAINT ck_notices_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_notice_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 공지사항 인덱스
CREATE INDEX idx_notices_category_status ON notices(category_id, status_id); -- 카테고리별 활성 공지 조회용
CREATE INDEX idx_notices_important_fixed ON notices(is_important, is_fixed, cdate DESC); -- 중요/고정 공지 우선 정렬용
CREATE INDEX idx_notices_status_date ON notices(status_id, cdate DESC); -- 상태별 최신순 정렬용
CREATE INDEX idx_notices_author ON notices(author_id); -- 작성자별 공지 조회용
CREATE INDEX idx_notices_date_range ON notices(start_date, end_date); -- 기간별 공지 조회용

---------
-- 1:1 채팅 세션 테이블
---------
CREATE TABLE chat_session (
    session_id      VARCHAR2(50)   NOT NULL,         -- 채팅 세션 ID
    member_id       NUMBER(10)     NOT NULL,         -- 고객 ID
    admin_id        NUMBER(10),                      -- 상담원 ID
    category_id     NUMBER(10)     NOT NULL,         -- 문의 카테고리 (code_id 참조), FAQ_CATEGORY
    status_id       NUMBER(10)     DEFAULT 1 NOT NULL, -- chat_session_status (code_id 참조)
    title           VARCHAR2(200),                   -- 채팅 제목
    start_time      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 시작시간
    end_time        TIMESTAMP,                       -- 종료시간
    message_count   NUMBER(10)     DEFAULT 0,        -- 메시지 수
    member_last_seen TIMESTAMP,                      -- 고객 마지막 접속 시간
    admin_last_seen  TIMESTAMP,                      -- 상담원 마지막 접속 시간
    disconnect_reason VARCHAR2(100),                 -- 이탈 사유
    grace_until     TIMESTAMP,                       -- 유예 만료 시간(재접속 허용 기한)
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_chat_session PRIMARY KEY (session_id),
    CONSTRAINT fk_chat_session_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_chat_session_admin FOREIGN KEY (admin_id) REFERENCES member(member_id),
    CONSTRAINT fk_chat_session_category FOREIGN KEY (category_id) REFERENCES code(code_id),
    CONSTRAINT fk_chat_session_status FOREIGN KEY (status_id) REFERENCES code(code_id),
    CONSTRAINT ck_chat_session_message_count CHECK (message_count >= 0)
);
-- 시퀀스 생성
CREATE SEQUENCE seq_chat_session_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 채팅 세션 인덱스
CREATE INDEX idx_chat_session_member ON chat_session(member_id); -- 고객별 채팅 세션 조회용
CREATE INDEX idx_chat_session_admin ON chat_session(admin_id); -- 상담원별 채팅 세션 조회용
CREATE INDEX idx_chat_session_status ON chat_session(status_id); -- 상태별 채팅 세션 조회용
CREATE INDEX idx_chat_session_start_time ON chat_session(start_time DESC); -- 최신순 정렬용

---------
-- 1:1 채팅 메시지 테이블
---------
CREATE TABLE chat_message (
    message_id      NUMBER(10)     NOT NULL,         -- 메시지 ID
    session_id      VARCHAR2(50)   NOT NULL,         -- 채팅 세션 ID
    sender_id       NUMBER(10)     NOT NULL,         -- 발신자 ID
    sender_type     CHAR(1)        NOT NULL,         -- 발신자 타입 (M:고객, A:관리자, S:시스템)
    message_type_id NUMBER(10)     NOT NULL,         -- 메시지 타입 (code_id 참조)
    content         VARCHAR2(2000) NOT NULL,         -- 메시지 내용
    is_read         CHAR(1)        DEFAULT 'N',      -- 읽음 여부 (Y: 읽음, N: 안읽음)
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시

    -- 제약조건
    CONSTRAINT pk_chat_message PRIMARY KEY (message_id),
    CONSTRAINT fk_chat_message_session FOREIGN KEY (session_id) REFERENCES chat_session(session_id),
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES member(member_id),
    CONSTRAINT fk_chat_message_type FOREIGN KEY (message_type_id) REFERENCES code(code_id),
    CONSTRAINT ck_chat_message_sender_type CHECK (sender_type IN ('M', 'A', 'S')),
    CONSTRAINT ck_chat_message_is_read CHECK (is_read IN ('Y', 'N'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_chat_message_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 채팅 메시지 인덱스
CREATE INDEX idx_chat_message_session ON chat_message(session_id); -- 세션별 메시지 조회용
CREATE INDEX idx_chat_message_sender ON chat_message(sender_id); -- 발신자별 메시지 조회용
CREATE INDEX idx_chat_message_cdate ON chat_message(cdate); -- 시간순 정렬용

---------
-- 통합 평가 테이블
---------
CREATE TABLE evaluation (
    evaluation_id      NUMBER(10)     NOT NULL,         -- 평가 ID
    target_type        CHAR(1)        NOT NULL,         -- 평가 대상 타입 (Q:Q&A, C:댓글, F:FAQ, R:리뷰, RC:리뷰댓글)
    target_id          NUMBER(10)     NOT NULL,         -- 평가 대상 ID
    member_id          NUMBER(10)     NOT NULL,         -- 평가자 ID
    evaluation_type_id NUMBER(10)     NOT NULL,         -- 평가 타입 (code_id 참조)
    cdate              TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,

    -- 제약조건
    CONSTRAINT pk_evaluation PRIMARY KEY (evaluation_id),
    CONSTRAINT fk_evaluation_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_evaluation_type FOREIGN KEY (evaluation_type_id) REFERENCES code(code_id),
    CONSTRAINT uk_evaluation UNIQUE (target_type, target_id, member_id), -- 중복 평가 방지
    CONSTRAINT ck_evaluation_target_type CHECK (target_type IN ('Q', 'C', 'F', 'R', 'RC'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_evaluation_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 평가 인덱스
CREATE INDEX idx_evaluation_target ON evaluation(target_type, target_id); -- 대상별 평가 조회용
CREATE INDEX idx_evaluation_member ON evaluation(member_id); -- 평가자별 평가 조회용


---------
-- 토큰 테이블 (이메일 인증, 비밀번호 재설정 등)
---------
CREATE TABLE tokens (
    token_id      NUMBER(10)     NOT NULL,         -- 토큰 ID
    email         VARCHAR2(50)   NOT NULL,
    token_type_id NUMBER(10)     NOT NULL,         -- 토큰 타입 (code_id 참조)
    token_value   VARCHAR2(100)  NOT NULL,         -- 인증 코드 또는 토큰 값
    expiry_date   TIMESTAMP,                       -- 만료일시
    status_id     NUMBER(10)     NOT NULL,         -- 상태 (code_id 참조)
    cdate         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    udate         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,

    -- 제약조건
    CONSTRAINT pk_tokens PRIMARY KEY (token_id),
    CONSTRAINT fk_tokens_type FOREIGN KEY (token_type_id) REFERENCES code(code_id),
    CONSTRAINT fk_tokens_status FOREIGN KEY (status_id) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_token_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 인덱스 생성 (필수 인덱스만)
CREATE INDEX idx_tokens_email_type ON tokens(email, token_type_id); -- 이메일별 토큰 타입 조회용
CREATE INDEX idx_tokens_value ON tokens(token_value); -- 토큰 값 검증용

---------
-- 신고 테이블
---------
CREATE TABLE reports (
    report_id        NUMBER(10)     NOT NULL,         -- 신고 ID
    reporter_id      NUMBER(10)     NOT NULL,         -- 신고자 ID
    target_type_id   NUMBER(10)     NOT NULL,         -- 신고 대상 타입 (code_id 참조)
    target_id        NUMBER(10)     NOT NULL,         -- 신고 대상 ID
    category_id      NUMBER(10)     NOT NULL,         -- 신고 카테고리 ID
    reason           VARCHAR2(500)  NOT NULL,         -- 신고 사유
    evidence         VARCHAR2(1000),                  -- 증거 자료 (URL, 스크린샷 등)
    status_id        NUMBER(10)     NOT NULL,         -- 상태 (code_id 참조)
    admin_notes      VARCHAR2(1000),                  -- 관리자 메모
    resolved_by      NUMBER(10),                      -- 처리자 ID
    resolved_at      TIMESTAMP,                       -- 처리일시
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_reports PRIMARY KEY (report_id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES member(member_id),
    CONSTRAINT fk_reports_target_type FOREIGN KEY (target_type_id) REFERENCES code(code_id),
    CONSTRAINT fk_reports_category FOREIGN KEY (category_id) REFERENCES code(code_id),
    CONSTRAINT fk_reports_resolver FOREIGN KEY (resolved_by) REFERENCES member(member_id),
    CONSTRAINT fk_reports_status FOREIGN KEY (status_id) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_report_id START WITH 1 INCREMENT BY 1 NOCACHE;

---------
-- 신고 통계 테이블
---------
CREATE TABLE report_statistics (
    stat_id          NUMBER(10)     NOT NULL,         -- 통계 ID
    target_type_id   NUMBER(10)     NOT NULL,         -- 대상 타입 (code_id 참조)
    target_id        NUMBER(10)     NOT NULL,         -- 대상 ID
    total_reports    NUMBER(10)     DEFAULT 0 NOT NULL, -- 총 신고 수
    pending_count    NUMBER(10)     DEFAULT 0 NOT NULL, -- 대기 중 신고 수
    resolved_count   NUMBER(10)     DEFAULT 0 NOT NULL, -- 처리 완료 신고 수
    last_reported    TIMESTAMP,                        -- 마지막 신고일시
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_report_statistics PRIMARY KEY (stat_id),
    CONSTRAINT fk_report_statistics_target_type FOREIGN KEY (target_type_id) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_report_stat_id START WITH 1 INCREMENT BY 1 NOCACHE;

---------
-- 자동 조치 규칙 테이블
---------
CREATE TABLE auto_action_rules (
    rule_id          NUMBER(10)     NOT NULL,         -- 규칙 ID
    target_type_id   NUMBER(10)     NOT NULL,         -- 대상 타입 (code_id 참조)
    report_threshold NUMBER(5)      NOT NULL,         -- 신고 임계값
    action_type_id   NUMBER(10)     NOT NULL,         -- 조치 타입 (code_id 참조)
    duration_days    NUMBER(5),                       -- 조치 기간 (일)
    is_active        CHAR(1)        DEFAULT 'Y' NOT NULL, -- 활성화 여부
    description      VARCHAR2(200),                   -- 규칙 설명
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_auto_action_rules PRIMARY KEY (rule_id),
    CONSTRAINT fk_auto_action_target_type FOREIGN KEY (target_type_id) REFERENCES code(code_id),
    CONSTRAINT fk_auto_action_action_type FOREIGN KEY (action_type_id) REFERENCES code(code_id),
    CONSTRAINT ck_auto_action_is_active CHECK (is_active IN ('Y', 'N'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_auto_action_rule_id START WITH 1 INCREMENT BY 1 NOCACHE;

---------
-- 검색 로그 테이블
---------
CREATE TABLE search_logs (
    search_log_id   NUMBER(10)     NOT NULL,         -- 검색 로그 ID
    member_id       NUMBER(10),                      -- 회원 ID (로그인 사용자, NULL 허용)
    keyword         VARCHAR2(200)  NOT NULL,         -- 검색 키워드
    search_type_id  NUMBER(10)     NOT NULL,         -- 검색 타입 (code_id 참조)
    result_count    NUMBER(10)     DEFAULT 0,        -- 검색 결과 수
    search_ip       VARCHAR2(50),                    -- 검색 IP
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시

    -- 제약조건
    CONSTRAINT pk_search_logs PRIMARY KEY (search_log_id),
    CONSTRAINT fk_search_logs_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_search_logs_type FOREIGN KEY (search_type_id) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_search_log_id START WITH 1 INCREMENT BY 1 NOCACHE;

-- 검색 로그 인덱스 (성능 최적화)
CREATE INDEX idx_search_logs_keyword ON search_logs(keyword);                   -- 인기검색어 집계용
CREATE INDEX idx_search_logs_member_date ON search_logs(member_id, cdate DESC); -- 개인 히스토리용
CREATE INDEX idx_search_logs_date ON search_logs(cdate);                        -- 기간별 집계용
CREATE INDEX idx_search_logs_type_id ON search_logs(search_type_id);            -- 타입별 검색용

---------
-- 선택 사항: 주문 총액 무결성 체크 트리거 (필요 시 활성화)
---------
/*
CREATE OR REPLACE TRIGGER check_order_total
BEFORE INSERT OR UPDATE ON orders
FOR EACH ROW
DECLARE
    v_total NUMBER(15,2);
BEGIN
    SELECT SUM(subtotal) INTO v_total
    FROM order_items
    WHERE order_id = :NEW.order_id;
    IF v_total != :NEW.total_amount THEN
        RAISE_APPLICATION_ERROR(-20001, 'Total amount does not match order items subtotal');
    END IF;
END;
/
*/