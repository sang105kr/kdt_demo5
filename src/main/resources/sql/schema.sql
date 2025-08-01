--테이블 삭제(테이블 관련 index는 자동 drop된다)
drop table notifications;
drop table search_logs;
drop table auto_action_rules;
drop table report_statistics;
drop table reports;
drop table tokens;
drop table review_comments;
drop table reviews;
drop table payments;
drop table order_items;
drop table orders;
drop table cart_items;
drop table cart;
drop table wishlist;
drop table replies;
drop table boards;
drop table member;
drop table uploadfile;
drop table products;
drop table code;

--시퀀스삭제
drop sequence seq_notification_id;
drop sequence seq_search_log_id;
drop sequence seq_auto_action_rule_id;
drop sequence seq_report_id;
drop sequence seq_report_stat_id;
drop sequence seq_token_id;
drop sequence seq_review_comment_id;
drop sequence seq_review_id;
drop sequence seq_payment_id;
drop sequence seq_order_item_id;
drop sequence seq_order_id;
drop sequence seq_cart_item_id;
drop sequence seq_cart_id;
drop sequence seq_wishlist_id;
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

-- 인덱스 생성 (필수 인덱스만)
CREATE INDEX idx_code_gcode_sort ON code(gcode, sort_order); -- 코드 그룹별 정렬용
CREATE INDEX idx_code_pcode ON code(pcode); -- 계층 구조 조회용



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
    gubun       NUMBER(10)    DEFAULT 2 NOT NULL,                -- 회원구분 (code_id 참조)
    status      NUMBER(10)   DEFAULT 15 NOT NULL, -- 회원상태 (ACTIVE, SUSPENDED, WITHDRAWN, PENDING)
    status_reason VARCHAR2(200),                        -- 상태 변경 사유
    status_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 상태 변경일시
    pic         BLOB,                                   -- 사진
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,    -- 생성일시
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,    -- 수정일시

    -- 제약조건
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uk_member_email UNIQUE (email),
    CONSTRAINT ck_member_email_nn CHECK (email IS NOT NULL),
    CONSTRAINT ck_member_gender CHECK (gender IN ('M','F')),
    CONSTRAINT fk_member_region FOREIGN KEY (region) REFERENCES code(code_id),
    CONSTRAINT fk_member_gubun FOREIGN KEY (gubun) REFERENCES code(code_id),
    CONSTRAINT ck_member_status FOREIGN KEY (status) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_member_id START WITH 1 INCREMENT BY 1;

-- member 인덱스 (필수 인덱스만)
CREATE INDEX idx_member_status ON member(status); -- 회원 상태별 조회용

---------
-- 알림 테이블
---------
CREATE TABLE notifications (
    notification_id    NUMBER(10)     NOT NULL,              -- 알림 ID
    member_id         NUMBER(10)     NOT NULL,              -- 회원 ID
    target_type       VARCHAR2(20)   NOT NULL,              -- 대상 타입 (CUSTOMER, ADMIN)
    notification_type_id NUMBER(10)   NOT NULL,              -- 알림 타입 (code 테이블 참조)
    title             VARCHAR2(200)  NOT NULL,              -- 알림 제목
    message           VARCHAR2(1000) NOT NULL,              -- 알림 메시지
    target_url        VARCHAR2(500),                        -- 관련 URL
    target_id         NUMBER(10),                           -- 관련 ID (주문ID, 상품ID 등)
    is_read           char(1)      DEFAULT 'N',             -- 읽음 여부 (N: 안읽음, Y: 읽음)
    created_date      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    read_date         TIMESTAMP,                            -- 읽음 일시
    use_yn            char(1)    DEFAULT 'Y',           -- 사용여부

    -- 제약조건
    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_notifications_type FOREIGN KEY (notification_type_id) REFERENCES code(code_id),
    CONSTRAINT ck_notifications_target_type CHECK (target_type IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT ck_notifications_is_read CHECK (is_read IN ('Y', 'N')),
    CONSTRAINT ck_notifications_use_yn CHECK (use_yn IN ('Y', 'N'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_notification_id START WITH 1 INCREMENT BY 1;

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_notifications_member_read ON notifications(member_id, is_read); -- 회원별 읽지 않은 알림 조회용
CREATE INDEX idx_notifications_type ON notifications(notification_type_id); -- 알림 타입별 조회용
CREATE INDEX idx_notifications_created_date ON notifications(created_date DESC); -- 최신순 정렬용
CREATE INDEX idx_notifications_target ON notifications(target_type, target_id); -- 대상별 조회용

---------
--상품
---------
CREATE TABLE products(
    product_id      NUMBER(10)     NOT NULL,         -- 상품 식별자
    pname           VARCHAR2(100),                   -- 상품명
    description     VARCHAR2(500),                   -- 상품설명
    price           NUMBER(10),                      -- 상품가격
    rating          NUMBER(3,2)    DEFAULT 0.0,      -- 상품평점
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

-- products 인덱스 (필수 인덱스만)
CREATE INDEX idx_products_category_rating ON products(category, rating); -- 카테고리별 평점 정렬용
CREATE INDEX idx_products_price ON products(price); -- 가격 정렬/검색용

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

-- cart 인덱스 (외래키는 자동 생성됨)

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

-- cart_items 인덱스 (외래키는 자동 생성됨)

---------
--위시리스트
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
CREATE SEQUENCE seq_wishlist_id START WITH 1 INCREMENT BY 1;

-- wishlist 인덱스 (필수 인덱스만)
CREATE INDEX idx_wishlist_member ON wishlist(member_id); -- 회원별 위시리스트 조회용

---------
--주문
---------
--주문상태별 의미
--1. PENDING (주문대기)
--의미: 주문이 접수되었지만 아직 결제 확인/처리 중인 상태
--상황:
--결제 승인 대기
--재고 확인 중
--주문 검증 중
--2. CONFIRMED (주문확정)
--의미: 주문이 확정되어 배송 준비 중인 상태
--상황:
--결제 완료
--재고 확보
--상품 포장/배송 준비
--3. SHIPPED (배송중)
--의미: 상품이 배송업체에 인계되어 배송 진행 중인 상태
--상황:
--택배사 수령
--배송 중
--고객에게 전달 중
--4. DELIVERED (배송완료)
--의미: 상품이 고객에게 정상적으로 전달된 상태 <= 리뷰작성가능 상태
--상황:
--배송 완료
--고객 수령 확인
--구매 확정 대기
--5. CANCELLED (주문취소)
--의미: 주문이 취소된 상태
--상황:
--고객 요청 취소
--재고 부족으로 취소
--결제 실패로 취소

--결제상태별 의미
--1. PENDING (대기중)
--의미: 결제 요청이 접수되었지만 아직 처리 중인 상태
--상황:
--결제 요청이 결제사(카드사, 은행 등)에 전송됨
--결제사에서 승인/거절 처리 중
--네트워크 지연으로 응답 대기 중
--결제 성공/실패가 확정되지 않은 상태
--2. COMPLETED (완료)
--의미: 결제가 성공적으로 완료된 상태
--상황:
--결제사에서 승인 완료
--금액이 정상적으로 결제됨
--주문 처리를 진행할 수 있는 상태
--가장 안전한 결제 상태
--3. FAILED (실패)
--의미: 결제가 실패한 상태
--상황:
--카드 한도 초과
--잘못된 카드 정보
--결제사 시스템 오류
--고객이 결제 취소
--재결제가 필요한 상태
--4. REFUNDED (환불)
--의미: 결제된 금액이 환불된 상태
--상황:
--고객 요청으로 환불
--상품 하자/불량으로 환불
--주문 취소로 인한 환불
--결제 취소와는 다른 개념 (이미 결제된 후 환불)
CREATE TABLE orders(
    order_id         NUMBER(10)     NOT NULL,         -- 주문 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    order_number     VARCHAR2(20)   NOT NULL,         -- 주문번호 (YYYYMMDD-XXXXX)
    order_status_id  NUMBER(10)     NOT NULL,         -- 주문상태 (code_id, gcode='ORDER_STATUS')
    total_amount     NUMBER(10)     NOT NULL,         -- 총 주문금액
    payment_method_id NUMBER(10)    NOT NULL,         -- 결제방법 (code_id, gcode='PAYMENT_METHOD')
    payment_status_id NUMBER(10)    NOT NULL,         -- 결제상태 (code_id, gcode='PAYMENT_STATUS')
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
    CONSTRAINT fk_orders_order_status FOREIGN KEY (order_status_id) REFERENCES code(code_id),
    CONSTRAINT fk_orders_payment_method FOREIGN KEY (payment_method_id) REFERENCES code(code_id),
    CONSTRAINT fk_orders_payment_status FOREIGN KEY (payment_status_id) REFERENCES code(code_id)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_order_id START WITH 1 INCREMENT BY 1;

-- orders 인덱스 (필수 인덱스만)
CREATE INDEX idx_orders_member_status ON orders(member_id, order_status_id); -- 회원별 주문 상태 조회용
CREATE INDEX idx_orders_payment_status ON orders(payment_status_id); -- 결제 상태별 조회용

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

-- order_items 인덱스 (외래키는 자동 생성됨)

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

-- uploadfile 인덱스 (필수 인덱스만)
CREATE INDEX idx_uploadfile_code_rid ON uploadfile(code, rid); -- 분류별 참조 조회용

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

-- boards 인덱스 (필수 인덱스만)
CREATE INDEX idx_boards_category_group ON boards(bcategory, bgroup, step); -- 카테고리별 계층 조회용
CREATE INDEX idx_boards_email ON boards(email); -- 작성자별 조회용

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

--인덱스 (필수 인덱스만)
CREATE INDEX idx_replies_board_group ON replies(board_id, rgroup, rstep); -- 게시글별 계층 조회용
CREATE INDEX idx_replies_email ON replies(email); -- 작성자별 조회용

---------
--리뷰
---------
--리뷰 상태별 의미
--ACTIVE (활성)
--의미: 정상적으로 표시되는 리뷰
--상황:
--사용자가 작성한 리뷰가 정상적으로 노출됨
--상품 상세 페이지에서 볼 수 있음
--다른 사용자들이 읽고 평점에 반영됨
--작성자가 수정/삭제 가능
--2. HIDDEN (숨김)
--의미: 일시적으로 숨겨진 리뷰
--상황:
--관리자가 부적절한 내용으로 판단하여 임시 숨김
--신고가 접수되어 검토 중
--작성자가 임시로 숨김 처리
--삭제되지 않고 데이터는 보존됨
--필요시 다시 ACTIVE로 복구 가능
--3. DELETED (삭제)
--의미: 삭제된 리뷰
--상황:
--작성자가 리뷰를 삭제함
--관리자가 부적절한 리뷰를 영구 삭제함
--논리 삭제로 실제 데이터는 보존하지만 표시되지 않음
--복구가 어려운 상태
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
    status           NUMBER(10)     DEFAULT NULL,     -- 상태 (code_id, gcode='REVIEW_STATUS')
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_reviews_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_reviews_status FOREIGN KEY (status) REFERENCES code(code_id),
    CONSTRAINT uk_reviews_order_product UNIQUE (order_id, product_id), -- 주문별 상품당 리뷰 1개
    CONSTRAINT ck_reviews_rating CHECK (rating >= 1.0 AND rating <= 5.0),
    CONSTRAINT ck_reviews_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_reviews_report_count CHECK (report_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_review_id START WITH 1 INCREMENT BY 1;

-- reviews 인덱스 (필수 인덱스만)
CREATE INDEX idx_reviews_product_status ON reviews(product_id, status); -- 상품별 활성 리뷰 조회용
CREATE INDEX idx_reviews_member ON reviews(member_id); -- 회원별 리뷰 조회용

---------
--리뷰 댓글
---------
--리뷰 댓글 상태별 의미
--ACTIVE (활성)
--의미: 정상적으로 표시되는 리뷰 댓글
--상황:
--사용자가 작성한 리뷰 댓글이 정상적으로 노출됨
--리뷰 상세 페이지에서 볼 수 있음
--다른 사용자들이 읽고 평점에 반영됨
--작성자가 수정/삭제 가능
--2. HIDDEN (숨김)
--의미: 일시적으로 숨겨진 리뷰 댓글
--상황:
--관리자가 부적절한 내용으로 판단하여 임시 숨김
--신고가 접수되어 검토 중
--작성자가 임시로 숨김 처리
--삭제되지 않고 데이터는 보존됨
--필요시 다시 ACTIVE로 복구 가능
--3. DELETED (삭제)
--의미: 삭제된 리뷰 댓글
--상황:
--작성자가 리뷰 댓글을 삭제함
--관리자가 부적절한 리뷰 댓글을 영구 삭제함
--논리 삭제로 실제 데이터는 보존하지만 표시되지 않음
--복구가 어려운 상태
CREATE TABLE review_comments(
    comment_id       NUMBER(10)     NOT NULL,         -- 댓글 식별자
    review_id        NUMBER(10)     NOT NULL,         -- 리뷰 식별자
    member_id        NUMBER(10)     NOT NULL,         -- 회원 식별자
    parent_id        NUMBER(10),                      -- 부모 댓글 식별자 (대댓글용)
    content          VARCHAR2(1000) NOT NULL,         -- 댓글 내용
    helpful_count    NUMBER(10)     DEFAULT 0,        -- 도움됨 수
    report_count     NUMBER(10)     DEFAULT 0,        -- 신고 수
    status           NUMBER(10)     DEFAULT NULL,     -- 상태 (code_id, gcode='REVIEW_COMMENT_STATUS')
    cdate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    -- 제약조건
    CONSTRAINT pk_review_comments PRIMARY KEY (comment_id),
    CONSTRAINT fk_review_comments_review FOREIGN KEY (review_id) REFERENCES reviews(review_id),
    CONSTRAINT fk_review_comments_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_review_comments_parent FOREIGN KEY (parent_id) REFERENCES review_comments(comment_id),
    CONSTRAINT fk_review_comments_status FOREIGN KEY (status) REFERENCES code(code_id),
    CONSTRAINT ck_review_comments_helpful_count CHECK (helpful_count >= 0),
    CONSTRAINT ck_review_comments_report_count CHECK (report_count >= 0)
);

-- 시퀀스 생성
CREATE SEQUENCE seq_review_comment_id START WITH 1 INCREMENT BY 1;

-- review_comments 인덱스 (필수 인덱스만)
CREATE INDEX idx_review_comments_review_status ON review_comments(review_id, status); -- 리뷰별 활성 댓글 조회용

---------
--토큰 테이블 (이메일 인증, 비밀번호 재설정 등)
---------
CREATE TABLE tokens (
    token_id    NUMBER(10),
    email       VARCHAR2(50)   NOT NULL,
    token_type  VARCHAR2(30)   NOT NULL, -- EMAIL_VERIFICATION, PASSWORD_RESET 등
    token_value VARCHAR2(100)  NOT NULL, -- 인증 코드 또는 토큰 값
    expiry_date TIMESTAMP, -- 만료일시
    status      NUMBER(20)     NOT NULL, -- 기본값 활성화
    cdate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    udate       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tokens PRIMARY KEY (token_id),
    CONSTRAINT chk_tokens_type CHECK (token_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET'))
);

CREATE SEQUENCE seq_token_id START WITH 1 INCREMENT BY 1;

-- 인덱스 생성 (필수 인덱스만)
CREATE INDEX idx_tokens_email_type ON tokens(email, token_type); -- 이메일별 토큰 타입 조회용
CREATE INDEX idx_tokens_value ON tokens(token_value); -- 토큰 값 검증용
CREATE INDEX idx_tokens_expiry ON tokens(expiry_date); -- 만료 토큰 정리용

---------
-- 신고 테이블
---------
--용도:
--범용 신고 시스템 - 리뷰, 댓글, 회원 등 다양한 대상에 대한 신고 처리
--관리자 처리 시스템 - 신고 상태 관리, 처리자 기록, 관리자 메모 등
--통계 및 분석 - 신고 통계 테이블과 연동하여 신고 패턴 분석
CREATE TABLE reports (
    report_id      NUMBER(10)     , 						          -- 신고 ID
    reporter_id    NUMBER(10)     NOT NULL,              -- 신고자 ID
    target_type    VARCHAR2(20)   NOT NULL,              -- 신고 대상 타입 (REVIEW, COMMENT, MEMBER,SYSTEM)
    target_id      NUMBER(10)     NOT NULL,              -- 신고 대상 ID
    category_id    NUMBER(10)     NOT NULL,              -- 신고 카테고리 ID
    reason         VARCHAR2(500)  NOT NULL,              -- 신고 사유
    evidence       VARCHAR2(1000),                       -- 증거 자료 (URL, 스크린샷 등)
    status         VARCHAR2(20)   DEFAULT 'PENDING' NOT NULL, -- 상태 (PENDING, PROCESSING, RESOLVED, REJECTED)
    admin_notes    VARCHAR2(1000),                       -- 관리자 메모
    resolved_by    NUMBER(10),                           -- 처리자 ID
    resolved_at    TIMESTAMP,                            -- 처리일시
    cdate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    CONSTRAINT pk_reports PRIMARY KEY (report_id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES member(member_id),
    CONSTRAINT fk_reports_category FOREIGN KEY (category_id) REFERENCES code(code_id),
    CONSTRAINT fk_reports_resolver FOREIGN KEY (resolved_by) REFERENCES member(member_id),
    CONSTRAINT chk_reports_target_type CHECK (target_type IN ('REVIEW', 'COMMENT', 'MEMBER','SYSTEM')),
    CONSTRAINT chk_reports_status CHECK (status IN ('PENDING', 'PROCESSING', 'RESOLVED', 'REJECTED'))
);

CREATE SEQUENCE seq_report_id START WITH 1 INCREMENT BY 1;

---------
-- 신고 통계 테이블
---------
CREATE TABLE report_statistics (
    stat_id        NUMBER(10)     ,           -- 통계 ID
    target_type    VARCHAR2(20)   NOT NULL,              -- 대상 타입
    target_id      NUMBER(10)     NOT NULL,              -- 대상 ID
    total_reports  NUMBER(10)     DEFAULT 0 NOT NULL,    -- 총 신고 수
    pending_count  NUMBER(10)     DEFAULT 0 NOT NULL,    -- 대기 중 신고 수
    resolved_count NUMBER(10)     DEFAULT 0 NOT NULL,    -- 처리 완료 신고 수
    last_reported  TIMESTAMP,                            -- 마지막 신고일시
    cdate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 수정일시

    CONSTRAINT pk_report_statistics PRIMARY KEY (stat_id),
    CONSTRAINT chk_report_stats_target_type CHECK (target_type IN ('REVIEW', 'COMMENT', 'MEMBER'))
);

CREATE SEQUENCE seq_report_stat_id START WITH 1 INCREMENT BY 1;

---------
-- 자동 조치 규칙 테이블
---------
CREATE TABLE auto_action_rules (
    rule_id        NUMBER(10)     ,						           -- 규칙 ID
    target_type    VARCHAR2(20)   NOT NULL,              -- 대상 타입
    report_threshold NUMBER(5)    NOT NULL,              -- 신고 임계값
    action_type    VARCHAR2(20)   NOT NULL,              -- 조치 타입 (HIDE, SUSPEND, DELETE)
    duration_days  NUMBER(5),                            -- 조치 기간 (일)
    is_active      CHAR(1)        DEFAULT 'Y' NOT NULL,  -- 활성화 여부
    description    VARCHAR2(200),                        -- 규칙 설명
    cdate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    udate          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,  -- 수정일시

    CONSTRAINT pk_auto_action_rules PRIMARY KEY (rule_id),
    CONSTRAINT chk_auto_action_target_type CHECK (target_type IN ('REVIEW', 'COMMENT', 'MEMBER')),
    CONSTRAINT chk_auto_action_type CHECK (action_type IN ('HIDE', 'SUSPEND', 'DELETE'))
);

CREATE SEQUENCE seq_auto_action_rule_id START WITH 1 INCREMENT BY 1;

---------
-- 검색 로그 테이블
---------
CREATE TABLE search_logs (
    search_log_id   NUMBER(19)     NOT NULL,              -- 검색 로그 ID
    member_id       NUMBER(10),                           -- 회원 ID (로그인 사용자, NULL 허용)
    keyword         VARCHAR2(200)  NOT NULL,              -- 검색 키워드
    search_type_id  NUMBER(10)     NOT NULL,              -- 검색 타입 (code 테이블 참조)
    result_count    NUMBER(10)     DEFAULT 0,             -- 검색 결과 수
    search_ip       VARCHAR2(50),                         -- 검색 IP
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 생성일시

    CONSTRAINT pk_search_logs PRIMARY KEY (search_log_id),
    CONSTRAINT fk_search_logs_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_search_logs_type FOREIGN KEY (search_type_id) REFERENCES code(code_id)
);

CREATE SEQUENCE seq_search_log_id START WITH 1 INCREMENT BY 1;

-- 검색 로그 인덱스 (성능 최적화)
CREATE INDEX idx_search_logs_keyword ON search_logs(keyword);                   -- 인기검색어 집계용
CREATE INDEX idx_search_logs_member_date ON search_logs(member_id, cdate DESC); -- 개인 히스토리용
CREATE INDEX idx_search_logs_date ON search_logs(cdate);                        -- 기간별 집계용
CREATE INDEX idx_search_logs_type_id ON search_logs(search_type_id);            -- 타입별 검색용
