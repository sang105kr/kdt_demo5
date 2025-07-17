-- 상품 테이블 데이터 삭제
DELETE FROM products;
COMMIT;

-- 주문 테이블 데이터 삭제
DELETE FROM order_items;
DELETE FROM orders;

-- 장바구니 테이블 데이터 삭제
DELETE FROM cart_items;
DELETE FROM cart;

-- 댓글 테이블 데이터 삭제
DELETE FROM replies;
COMMIT;

-- 게시판 테이블 데이터 삭제
DELETE FROM boards;
COMMIT;

-- 회원 테이블 데이터 삭제
DELETE FROM member;
COMMIT;

-- 코드 테이블 데이터 삭제
DELETE FROM code;
COMMIT;


-- 코드 테이블
-- [회원구분]
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'MEMBER', 'MEMBER', '회원구분', NULL, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'MEMBER', 'NORMAL', '일반', seq_code_id.currval-1, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'MEMBER', 'VIP', '우수', seq_code_id.currval-2, 2, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'MEMBER', 'ADMIN1', '관리자1', seq_code_id.currval-3, 3, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'MEMBER', 'ADMIN2', '관리자2', seq_code_id.currval-4, 4, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);

-- [지역]
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'REGION', 'REGION', '지역', NULL, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'REGION', 'SEOUL', '서울', seq_code_id.currval-1, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'REGION', 'BUSAN', '부산', seq_code_id.currval-2, 2, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'REGION', 'DAEGU', '대구', seq_code_id.currval-3, 3, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'REGION', 'ULSAN', '울산', seq_code_id.currval-4, 4, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);

-- [취미]
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'HOBBY', 'HOBBY', '취미', NULL, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'HOBBY', 'HIKING', '등산', seq_code_id.currval-1, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'HOBBY', 'SWIMMING', '수영', seq_code_id.currval-2, 2, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'HOBBY', 'GOLF', '골프', seq_code_id.currval-3, 3, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'HOBBY', 'READING', '독서', seq_code_id.currval-4, 4, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);

-- [게시판 카테고리]
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'BOARD', 'BOARD', '게시판', NULL, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'BOARD', 'SPRING', 'Spring', seq_code_id.currval-1, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'BOARD', 'DATABASE', 'Database', seq_code_id.currval-2, 2, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'BOARD', 'QNA', 'Q&A', seq_code_id.currval-3, 3, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'BOARD', 'PROJECT', '프로젝트', seq_code_id.currval-4, 4, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'BOARD', 'FREE', '자유게시판', seq_code_id.currval-5, 5, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);

-- [성별]
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'GENDER', 'M', '남자', NULL, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'GENDER', 'F', '여자', NULL, 2, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);

-- [파일]
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'FILE', 'FILE', '파일', NULL, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'FILE', 'PRODUCT_IMAGE', '상품이미지', seq_code_id.currval-1, 1, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO code (code_id, gcode, code, decode, pcode, sort_order, use_yn, cdate, udate) VALUES (seq_code_id.nextval, 'FILE', 'PRODUCT_MANUAL', '상품설명서', seq_code_id.currval-2, 2, 'Y', SYSTIMESTAMP, SYSTIMESTAMP);

commit;

--샘플데이터 of member
-- region: 서울(7), 부산(8), 대구(9), 울산(10)
-- gubun: 일반(2), 우수(3), 관리자1(4), 관리자2(5)
-- hobby: 등산(12), 수영(13), 골프(14), 독서(15)
insert into member (member_id,email,passwd,tel,nickname,gender,birth_date,hobby,region,gubun)
    values(seq_member_id.nextval, 'test1@kh.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', '010-1111-1111','테스터1','M',TO_DATE('1990-03-15', 'YYYY-MM-DD'),'12,13',7, 2);
insert into member (member_id,email,passwd,tel,nickname,gender,birth_date,hobby,region,gubun)
    values(seq_member_id.nextval, 'test2@kh.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', '010-1111-1112','테스터2','F',TO_DATE('1992-07-22', 'YYYY-MM-DD'),'13,14',8, 2);
insert into member (member_id,email,passwd,tel,nickname,gender,birth_date,hobby,region,gubun)
    values(seq_member_id.nextval, 'admin1@kh.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','010-1111-1113','관리자1', 'M',TO_DATE('1985-11-08', 'YYYY-MM-DD'),'14,15',9,4);
insert into member (member_id,email,passwd,tel,nickname,gender,birth_date,hobby,region,gubun)
    values(seq_member_id.nextval, 'admin2@kh.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','010-1111-1114','관리자2', 'F',TO_DATE('1988-05-30', 'YYYY-MM-DD'),'12,14',10,5);
select * from member;
commit;

-- 상품 등록
-- 테스트 데이터 삽입 (product-settings.json 동의어 기반, HTML 포함)
INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '삼성 갤럭시 S24', '<h3>삼성 최신 스마트폰</h3><p>AI 기능이 탑재되어 있습니다. <strong>갤럭시 AI</strong>로 더욱 스마트한 사용이 가능합니다.</p><ul><li>6.2인치 디스플레이</li><li>5000mAh 배터리</li></ul>', 1200000, 4.8, '전자제품', 50, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, 'LG 그램 노트북', '<h3>LG 초경량 노트북</h3><p>휴대성이 뛰어납니다. <em>1kg 미만</em>의 가벼운 무게로 어디든 휴대하기 편합니다.</p><div><span style="color: blue;">인텔 i7 프로세서</span> 탑재</div>', 1800000, 4.6, '전자제품', 30, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '아이폰 15 Pro', '<h3>애플 최신 아이폰</h3><p>티타늄 소재로 제작되었습니다. <b>프로 카메라 시스템</b>으로 전문가급 사진 촬영이 가능합니다.</p><table><tr><td>화면</td><td>6.1인치</td></tr></table>', 1500000, 4.9, '전자제품', 25, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '맥북 프로 16인치', '<h3>애플 맥북 프로</h3><p>M3 칩이 탑재되어 있습니다. <i>최고 성능</i>을 제공하는 노트북입니다.</p><ol><li>16인치 Liquid Retina XDR 디스플레이</li><li>최대 128GB 통합 메모리</li></ol>', 3500000, 4.7, '전자제품', 15, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '삼성 냉장고', '<h3>삼성 스마트 냉장고</h3><p>AI 기능으로 식품을 관리합니다. <strong>스마트 센서</strong>가 내장되어 있습니다.</p><div style="background-color: #f0f0f0;">스마트 냉장고 기능</div>', 2500000, 4.5, '가전제품', 20, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, 'LG OLED TV', '<h3>LG OLED TV</h3><p>최고의 화질을 제공합니다. <em>OLED 기술</em>로 완벽한 블랙을 구현합니다.</p><span>4K 해상도 지원</span>', 3000000, 4.8, '가전제품', 10, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '컴퓨터 책상', '<h3>편안한 컴퓨터 책상</h3><p>높이 조절이 가능합니다. <b>인체공학적 설계</b>로 장시간 사용해도 편안합니다.</p><ul><li>전동 높이 조절</li><li>메모리 기능</li></ul>', 300000, 4.3, '가구', 40, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '휴대폰 케이스', '<h3>아이폰용 휴대폰 케이스</h3><p>보호 기능이 뛰어납니다. <strong>군용 등급</strong> 보호 기능을 제공합니다.</p><div><i>투명한 디자인</i>으로 아이폰의 아름다움을 그대로 유지</div>', 50000, 4.4, '액세서리', 100, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '랩탑 가방', '<h3>노트북용 가방</h3><p>충격 방지 기능이 있습니다. <em>방수 기능</em>도 함께 제공됩니다.</p><table><tr><td>크기</td><td>15.6인치</td></tr><tr><td>재질</td><td>네오프렌</td></tr></table>', 80000, 4.2, '액세서리', 80, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO products(product_id, pname, description, price, rating, category, stock_quantity, cdate, udate)
VALUES (seq_product_id.nextval, '전화기 충전기', '<h3>고속 충전기</h3><p>고속 충전기가 가능한 충전기입니다. <b>USB-C PD</b> 기술을 지원합니다.</p><div style="color: green;">최대 65W 출력</div>', 30000, 4.1, '액세서리', 150, SYSTIMESTAMP, SYSTIMESTAMP);

commit;

-- 장바구니 샘플 데이터
-- test1@kh.com (member_id: 1)의 장바구니
INSERT INTO cart (cart_id, member_id, cdate, udate)
VALUES (seq_cart_id.nextval, 1, SYSTIMESTAMP, SYSTIMESTAMP);

-- test1@kh.com의 장바구니 상품들
-- 삼성 갤럭시 S24: 원가 1200000, 할인가 1080000 (10% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 1, 2, 1080000, 1200000, 0.10, SYSTIMESTAMP, SYSTIMESTAMP);

-- 아이폰 15 Pro: 원가 1500000, 할인가 1350000 (10% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 3, 1, 1350000, 1500000, 0.10, SYSTIMESTAMP, SYSTIMESTAMP);

-- 휴대폰 케이스: 원가 50000, 할인가 40000 (20% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 8, 3, 40000, 50000, 0.20, SYSTIMESTAMP, SYSTIMESTAMP);

-- test2@kh.com (member_id: 2)의 장바구니
INSERT INTO cart (cart_id, member_id, cdate, udate)
VALUES (seq_cart_id.nextval, 2, SYSTIMESTAMP, SYSTIMESTAMP);

-- test2@kh.com의 장바구니 상품들
-- LG 그램 노트북: 원가 1800000, 할인가 1620000 (10% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 2, 1, 1620000, 1800000, 0.10, SYSTIMESTAMP, SYSTIMESTAMP);

-- 랩탑 가방: 원가 80000, 할인가 64000 (20% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 9, 2, 64000, 80000, 0.20, SYSTIMESTAMP, SYSTIMESTAMP);

-- 전화기 충전기: 원가 30000, 할인가 24000 (20% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 10, 5, 24000, 30000, 0.20, SYSTIMESTAMP, SYSTIMESTAMP);

-- admin1@kh.com (member_id: 3)의 장바구니
INSERT INTO cart (cart_id, member_id, cdate, udate)
VALUES (seq_cart_id.nextval, 3, SYSTIMESTAMP, SYSTIMESTAMP);

-- admin1@kh.com의 장바구니 상품들
-- 맥북 프로 16인치: 원가 3500000, 할인가 3150000 (10% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 4, 1, 3150000, 3500000, 0.10, SYSTIMESTAMP, SYSTIMESTAMP);

-- 컴퓨터 책상: 원가 300000, 할인가 270000 (10% 할인)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, sale_price, original_price, discount_rate, cdate, udate)
VALUES (seq_cart_item_id.nextval, seq_cart_id.currval, 7, 1, 270000, 300000, 0.10, SYSTIMESTAMP, SYSTIMESTAMP);

commit;

-- 주문 샘플 데이터
-- test1@kh.com (member_id: 1)의 주문들

-- 주문 1: 삼성 갤럭시 S24 + 휴대폰 케이스 (주문대기)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 1, '20241201-00001', 'PENDING', 2450000, 'CARD', 'PENDING', '김테스터', '010-1111-1111', '서울시 강남구 테헤란로 123', '문 앞에 놓아주세요', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 1의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 1, '삼성 갤럭시 S24', 1200000, 2, 2400000, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 8, '휴대폰 케이스', 50000, 1, 50000, SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 2: 아이폰 15 Pro (주문확정)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 1, '20241201-00002', 'CONFIRMED', 1500000, 'BANK_TRANSFER', 'COMPLETED', '김테스터', '010-1111-1111', '서울시 강남구 테헤란로 123', '경비실에 맡겨주세요', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 2의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 3, '아이폰 15 Pro', 1500000, 1, 1500000, SYSTIMESTAMP, SYSTIMESTAMP);

-- test2@kh.com (member_id: 2)의 주문들

-- 주문 3: LG 그램 노트북 + 랩탑 가방 + 충전기 (배송중)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 2, '20241201-00003', 'SHIPPED', 1960000, 'CARD', 'COMPLETED', '이테스터', '010-1111-1112', '부산시 해운대구 해운대로 456', '부재시 연락주세요', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 3의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 2, 'LG 그램 노트북', 1800000, 1, 1800000, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 9, '랩탑 가방', 80000, 2, 160000, SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 4: 충전기 여러개 (배송완료)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 2, '20241201-00004', 'DELIVERED', 150000, 'CASH', 'COMPLETED', '이테스터', '010-1111-1112', '부산시 해운대구 해운대로 456', '', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 4의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 10, '전화기 충전기', 30000, 5, 150000, SYSTIMESTAMP, SYSTIMESTAMP);

-- admin1@kh.com (member_id: 3)의 주문들

-- 주문 5: 맥북 프로 + 컴퓨터 책상 (주문확정)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 3, '20241201-00005', 'CONFIRMED', 3800000, 'CARD', 'COMPLETED', '관리자1', '010-1111-1113', '대구시 수성구 동대구로 789', '설치 서비스 요청', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 5의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 4, '맥북 프로 16인치', 3500000, 1, 3500000, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 7, '컴퓨터 책상', 300000, 1, 300000, SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 6: 삼성 냉장고 (주문취소)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 3, '20241201-00006', 'CANCELLED', 2500000, 'BANK_TRANSFER', 'REFUNDED', '관리자1', '010-1111-1113', '대구시 수성구 동대구로 789', '', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 6의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 5, '삼성 냉장고', 2500000, 1, 2500000, SYSTIMESTAMP, SYSTIMESTAMP);

-- admin2@kh.com (member_id: 4)의 주문들

-- 주문 7: LG OLED TV (결제실패)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 4, '20241201-00007', 'PENDING', 3000000, 'CARD', 'FAILED', '관리자2', '010-1111-1114', '울산시 남구 삼산로 321', '배송 전 연락주세요', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 7의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 6, 'LG OLED TV', 3000000, 1, 3000000, SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 8: 휴대폰 케이스 + 충전기 (배송완료)
INSERT INTO orders (order_id, member_id, order_number, order_status, total_amount, payment_method, payment_status, recipient_name, recipient_phone, shipping_address, shipping_memo, cdate, udate)
VALUES (seq_order_id.nextval, 4, '20241201-00008', 'DELIVERED', 80000, 'CASH', 'COMPLETED', '관리자2', '010-1111-1114', '울산시 남구 삼산로 321', '', SYSTIMESTAMP, SYSTIMESTAMP);

-- 주문 8의 상품들
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 8, '휴대폰 케이스', 50000, 1, 50000, SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_price, quantity, subtotal, cdate, udate)
VALUES (seq_order_item_id.nextval, seq_order_id.currval, 10, '전화기 충전기', 30000, 1, 30000, SYSTIMESTAMP, SYSTIMESTAMP);

commit;

--게시판 샘플 데이터
-- bcategory: Spring(17), Database(18), Q&A(19), 프로젝트(20), 자유게시판(21), 공지사항(22)

-- Spring 게시판 원글들 (bgroup = board_id, step = 0, bindent = 0)
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 시작하기', 'test1@kh.com', '테스터1', 15,
        'Spring Boot를 처음 시작하는 분들을 위한 가이드입니다. 기본 설정부터 시작해서 간단한 웹 애플리케이션을 만들어보겠습니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Security 설정 가이드', 'admin1@kh.com', '관리자1', 23,
        'Spring Security를 사용한 인증 및 권한 관리 설정 방법을 설명합니다. JWT 토큰 기반 인증도 포함됩니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Data JPA 활용법', 'test1@kh.com', '테스터1', 31,
        'Spring Data JPA의 다양한 기능들을 활용하는 방법을 정리했습니다. QueryDSL과 함께 사용하는 방법도 포함됩니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot Actuator 모니터링', 'admin2@kh.com', '관리자2', 19,
        'Spring Boot Actuator를 사용한 애플리케이션 모니터링 설정과 활용 방법을 설명합니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 테스트 작성법', 'test2@kh.com', '테스터2', 27,
        'Spring Boot 애플리케이션의 단위 테스트와 통합 테스트 작성 방법을 정리했습니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 배포 가이드', 'admin1@kh.com', '관리자1', 35,
        'Spring Boot 애플리케이션을 다양한 환경에 배포하는 방법을 설명합니다. Docker, AWS, Azure 등 포함.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 성능 최적화', 'test1@kh.com', '테스터1', 42,
        'Spring Boot 애플리케이션의 성능을 최적화하는 다양한 방법들을 정리했습니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 마이크로서비스', 'admin2@kh.com', '관리자2', 29,
        'Spring Boot를 사용한 마이크로서비스 아키텍처 구축 방법을 설명합니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot REST API 설계', 'test2@kh.com', '테스터2', 38,
        'Spring Boot를 사용한 RESTful API 설계 원칙과 구현 방법을 정리했습니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 캐싱 전략', 'admin1@kh.com', '관리자1', 33,
        'Spring Boot에서 다양한 캐싱 전략을 구현하는 방법을 설명합니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 비동기 처리', 'test1@kh.com', '테스터1', 26,
        'Spring Boot에서 비동기 처리를 구현하는 방법을 정리했습니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Spring Boot 로깅 설정', 'admin2@kh.com', '관리자2', 21,
        'Spring Boot 애플리케이션의 로깅 설정과 활용 방법을 설명합니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

-- Database 게시판 원글
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 18, 'Oracle vs MySQL 비교', 'admin1@kh.com', '관리자1', 25,
        'Oracle과 MySQL의 주요 차이점을 정리해보았습니다. 성능, 비용, 라이선스 등 여러 측면에서 비교해보겠습니다.',
        NULL, seq_board_id.currval, 0, 0, 'A');

-- Q&A 게시판 원글
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 19, 'JPA N+1 문제 해결 방법', 'test1@kh.com', '테스터1', 32,
        'JPA를 사용하면서 N+1 문제가 발생했습니다. 어떤 방법으로 해결할 수 있을까요?',
        NULL, seq_board_id.currval, 0, 0, 'A');

-- 프로젝트 게시판 원글
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 20, '팀 프로젝트 모집합니다', 'test1@kh.com', '테스터1', 45,
        'Spring Boot와 React를 사용한 웹 애플리케이션 개발 프로젝트 팀원을 모집합니다. 관심 있으신 분들 연락주세요!',
        NULL, seq_board_id.currval, 0, 0, 'A');

-- 자유게시판 원글
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 21, '개발자 커리어 조언 부탁드립니다', 'test2@kh.com', '테스터2', 67,
        '신입 개발자로서 앞으로의 커리어 방향에 대해 조언을 구하고 싶습니다. 어떤 기술 스택을 공부하면 좋을까요?',
        NULL, seq_board_id.currval, 0, 0, 'A');

-- Spring Boot 시작하기 게시글(1번)에 대한 답글
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 17, 'Re: Spring Boot 시작하기', 'test2@kh.com', '테스터2', 8,
        '정말 도움이 되는 글이네요! 추가로 궁금한 점이 있습니다. JPA 설정은 어떻게 하시나요?',
        1, 1, 1, 1, 'A');

-- JPA N+1 문제 해결 방법 게시글(14번)에 대한 답글
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 19, 'Re: JPA N+1 문제 해결 방법', 'admin2@kh.com', '관리자2', 18,
        'N+1 문제는 주로 fetch join이나 @EntityGraph를 사용해서 해결할 수 있습니다. 구체적인 예시를 보여드릴게요.',
        14, 14, 1, 1, 'A');

-- JPA N+1 문제 해결 방법 게시글(14번)에 대한 답글의 답글
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status)
VALUES (seq_board_id.nextval, 19, 'Re: Re: JPA N+1 문제 해결 방법', 'test2@kh.com', '테스터2', 5,
        '정말 감사합니다! @EntityGraph를 사용해보니 문제가 해결되었어요.',
        16, 14, 2, 2, 'A');

commit;

--댓글 샘플 데이터
-- Spring Boot 시작하기 게시글(1번)에 대한 댓글들 (rgroup = reply_id, rstep = 0, rindent = 0)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test2@kh.com', '테스터2',
        '정말 유용한 글이네요! Spring Boot 처음 시작하는데 도움이 많이 되었습니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin1@kh.com', '관리자1',
        '감사합니다! 추가로 궁금한 점이 있으시면 언제든 질문해주세요.',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test1@kh.com', '테스터1',
        '네, 정말 도움이 되었어요! 다음 글도 기대하겠습니다.',
        seq_reply_id.currval-2, seq_reply_id.currval-2, 2, 2, 'A');

-- JPA N+1 문제 해결 방법 게시글(14번)에 대한 댓글들
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 14, 'admin2@kh.com', '관리자2',
        'N+1 문제는 정말 까다로운 문제죠. fetch join을 사용하는 것이 가장 효과적입니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 14, 'test2@kh.com', '테스터2',
        '@EntityGraph도 좋은 방법이에요!',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 14, 'test1@kh.com', '테스터1',
        '정말 감사합니다! 두 방법 모두 시도해보겠습니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 팀 프로젝트 모집 게시글(15번)에 대한 댓글들
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 15, 'test2@kh.com', '테스터2',
        '관심이 많습니다! 어떤 기술 스택을 사용하실 예정인가요?',
        NULL, seq_reply_id.currval, 0, 0, 'A');

INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 15, 'admin1@kh.com', '관리자1',
        '저도 참여하고 싶습니다! 경력은 얼마나 되시나요?',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- Spring Boot 시작하기 게시글(1번)에 무한스크롤 테스트용 댓글들 추가
-- 5번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test1@kh.com', '테스터1',
        'Spring Boot 설정 파일에 대해 더 자세히 설명해주실 수 있나요?',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 6번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin2@kh.com', '관리자2',
        'application.yml과 application.properties의 차이점이 궁금합니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 7번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test2@kh.com', '테스터2',
        '개발 환경과 운영 환경 설정을 분리하는 방법도 알려주세요!',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 8번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin1@kh.com', '관리자1',
        'Spring Boot의 자동 설정(Auto Configuration)에 대해 궁금합니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 9번째 댓글 (8번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test1@kh.com', '테스터1',
        '외부 라이브러리를 추가할 때 자동으로 설정되는 것들이 신기해요!',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 10번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin2@kh.com', '관리자2',
        'Spring Boot의 내장 톰캣 사용법에 대해 설명해주세요.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 11번째 댓글 (10번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test2@kh.com', '테스터2',
        'JAR 파일로 실행하는 방법도 궁금합니다!',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 12번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin1@kh.com', '관리자1',
        'Spring Boot의 로깅 설정에 대해 알려주세요.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 13번째 댓글 (12번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test1@kh.com', '테스터1',
        'logback 설정 파일을 커스터마이징하는 방법도 있나요?',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 14번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin2@kh.com', '관리자2',
        'Spring Boot의 프로파일 기능에 대해 설명해주세요.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 15번째 댓글 (14번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test2@kh.com', '테스터2',
        '개발/테스트/운영 환경별로 다른 설정을 사용하는 방법이 궁금합니다.',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 16번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin1@kh.com', '관리자1',
        'Spring Boot의 의존성 관리에 대해 궁금합니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 17번째 댓글 (16번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test1@kh.com', '테스터1',
        'Spring Boot Starter의 종류와 사용법을 알려주세요!',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 18번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin2@kh.com', '관리자2',
        'Spring Boot의 메트릭스와 모니터링에 대해 설명해주세요.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 19번째 댓글 (18번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test2@kh.com', '테스터2',
        'Actuator 엔드포인트를 사용한 모니터링 방법이 궁금합니다.',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 20번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin1@kh.com', '관리자1',
        'Spring Boot의 보안 설정에 대해 궁금합니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 21번째 댓글 (20번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test1@kh.com', '테스터1',
        'Spring Security를 사용한 인증/인가 설정 방법을 알려주세요!',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 22번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin2@kh.com', '관리자2',
        'Spring Boot의 데이터베이스 연결 설정에 대해 설명해주세요.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 23번째 댓글 (22번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test2@kh.com', '테스터2',
        'H2 데이터베이스와 MySQL 설정의 차이점이 궁금합니다.',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

-- 24번째 댓글
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'admin1@kh.com', '관리자1',
        'Spring Boot의 캐싱 기능에 대해 궁금합니다.',
        NULL, seq_reply_id.currval, 0, 0, 'A');

-- 25번째 댓글 (24번째 댓글의 대댓글)
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status)
VALUES (seq_reply_id.nextval, 1, 'test1@kh.com', '테스터1',
        'Redis를 사용한 캐싱 설정 방법을 알려주세요!',
        seq_reply_id.currval-1, seq_reply_id.currval-1, 1, 1, 'A');

commit;


-- 좋아요/비호감 샘플 데이터 (메인 테이블에 직접 업데이트)
-- Spring Boot 시작하기 게시글(1번)에 대한 좋아요/비호감
UPDATE boards SET like_count = 2, dislike_count = 1 WHERE board_id = 1;

-- Spring Security 설정 가이드 게시글(2번)에 대한 좋아요/비호감
UPDATE boards SET like_count = 2, dislike_count = 0 WHERE board_id = 2;

-- JPA N+1 문제 해결 방법 게시글(14번)에 대한 좋아요/비호감
UPDATE boards SET like_count = 2, dislike_count = 1 WHERE board_id = 14;

-- 댓글에 대한 좋아요/비호감 (Spring Boot 시작하기 게시글의 첫 번째 댓글)
UPDATE replies SET like_count = 2, dislike_count = 0 WHERE reply_id = 1;

-- 댓글에 대한 좋아요/비호감 (JPA N+1 문제 해결 방법 게시글의 첫 번째 댓글)
UPDATE replies SET like_count = 1, dislike_count = 1 WHERE reply_id = 4;

commit;


