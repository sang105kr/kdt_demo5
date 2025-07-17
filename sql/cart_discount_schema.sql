-- 할인 기능을 포함한 장바구니 스키마 설계

-- 기존 cart_items 테이블 수정 (할인 정보 추가)
ALTER TABLE cart_items ADD (
    sale_price      NUMBER(10),     -- 실제 판매가 (할인 적용된 가격)
    original_price  NUMBER(10),     -- 원가 (참고용)
    discount_rate   NUMBER(3,2)     -- 할인율 (예: 0.20 = 20% 할인)
);

-- 할인 정보 테이블 (선택사항)
CREATE TABLE product_discounts(
    discount_id     NUMBER(10)     NOT NULL,
    product_id      NUMBER(10)     NOT NULL,
    discount_type   VARCHAR2(20)   NOT NULL,  -- PERCENTAGE, FIXED_AMOUNT
    discount_value  NUMBER(10)     NOT NULL,  -- 할인율 또는 할인금액
    start_date      TIMESTAMP      NOT NULL,
    end_date        TIMESTAMP      NOT NULL,
    is_active       CHAR(1)        DEFAULT 'Y',
    cdate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    udate           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_product_discounts PRIMARY KEY (discount_id),
    CONSTRAINT fk_product_discounts_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT ck_product_discounts_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT ck_product_discounts_active CHECK (is_active IN ('Y', 'N'))
);

-- 시퀀스 생성
CREATE SEQUENCE seq_product_discount_id START WITH 1 INCREMENT BY 1;

-- 인덱스 생성
CREATE INDEX idx_product_discounts_product_id ON product_discounts(product_id);
CREATE INDEX idx_product_discounts_active ON product_discounts(is_active);
CREATE INDEX idx_product_discounts_date_range ON product_discounts(start_date, end_date);

-- 장바구니 아이템에 할인 정보 참조 추가 (선택사항)
ALTER TABLE cart_items ADD (
    discount_id     NUMBER(10)     -- 적용된 할인 정보
);

ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_discount 
    FOREIGN KEY (discount_id) REFERENCES product_discounts(discount_id);

-- 기존 데이터 마이그레이션 (sale_price = products.price로 설정)
UPDATE cart_items ci 
SET ci.sale_price = (
    SELECT p.price 
    FROM products p 
    WHERE p.product_id = ci.product_id
),
ci.original_price = (
    SELECT p.price 
    FROM products p 
    WHERE p.product_id = ci.product_id
),
ci.discount_rate = 0
WHERE ci.sale_price IS NULL;

-- NOT NULL 제약조건 추가
ALTER TABLE cart_items MODIFY sale_price NOT NULL;
ALTER TABLE cart_items MODIFY original_price NOT NULL;
ALTER TABLE cart_items MODIFY discount_rate NOT NULL; 