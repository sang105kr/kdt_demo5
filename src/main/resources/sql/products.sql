-- 테이블 삭제
DROP TABLE products;

-- 테이블 생성
CREATE TABLE products(
	product_id		number(10),  		-- 상품 식별자
	pname			varchar2(100), 	    -- 상품명(한글50자)
	description		varchar2(500),		-- 상품설명(한글250자)
	price			number(10),			-- 상품가격
	rating			number(3,2),		-- 상품평점(0.00~5.00)
	category		varchar2(50)		-- 상품카테고리(한글25자)
);

-- 기본키
ALTER TABLE products ADD CONSTRAINT products_product_id_pk PRIMARY key(product_id);

-- 시퀀스 삭제
DROP SEQUENCE products_product_id_seq;

-- 시퀀스 생성
CREATE SEQUENCE products_product_id_seq;

SELECT * FROM products;

-- 상품 등록
INSERT INTO products(product_id, pname, description, price, rating, category)
		 VALUES (products_product_id_seq.nextval, '노트북', '고성능 노트북입니다', 1500000, 4.5, '전자제품');

-- 상품 조회
SELECT product_id, pname, description, price, rating, category
  FROM products
 WHERE product_id = 1;

-- 상품 수정
UPDATE products
   SET pname = '수정된 노트북', description = '수정된 상품설명', price = 1600000,
       rating = 4.8, category = '전자제품'
 WHERE product_id = 1;

-- 상품 삭제(단건)
DELETE FROM products
 WHERE product_id = 1;

-- 상품 삭제(여러건)
DELETE FROM products
 WHERE product_id in(3,4);

-- 상품 목록
SELECT product_id, pname, description, price, rating, category
  FROM products
 ORDER BY product_id DESC;

-- 페이징
-- SELECT product_id, pname, description, price, rating, category
--   FROM products
--  ORDER BY product_id DESC
--  OFFSET (:pageNo - 1) * :numOfRows ROWS
--  FETCH NEXT :numOfRows ROWS ONLY;

-- 총건수
SELECT count(product_id)
  FROM products;

-- 테스트 데이터 삽입 (product-settings.json 동의어 기반, HTML 포함)
INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '삼성 갤럭시 S24', '<h3>삼성 최신 스마트폰</h3><p>AI 기능이 탑재되어 있습니다. <strong>갤럭시 AI</strong>로 더욱 스마트한 사용이 가능합니다.</p><ul><li>6.2인치 디스플레이</li><li>5000mAh 배터리</li></ul>', 1200000, 4.8, '전자제품');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, 'LG 그램 노트북', '<h3>LG 초경량 노트북</h3><p>휴대성이 뛰어납니다. <em>1kg 미만</em>의 가벼운 무게로 어디든 휴대하기 편합니다.</p><div><span style="color: blue;">인텔 i7 프로세서</span> 탑재</div>', 1800000, 4.6, '전자제품');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '아이폰 15 Pro', '<h3>애플 최신 아이폰</h3><p>티타늄 소재로 제작되었습니다. <b>프로 카메라 시스템</b>으로 전문가급 사진 촬영이 가능합니다.</p><table><tr><td>화면</td><td>6.1인치</td></tr></table>', 1500000, 4.9, '전자제품');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '맥북 프로 16인치', '<h3>애플 맥북 프로</h3><p>M3 칩이 탑재되어 있습니다. <i>최고 성능</i>을 제공하는 노트북입니다.</p><ol><li>16인치 Liquid Retina XDR 디스플레이</li><li>최대 128GB 통합 메모리</li></ol>', 3500000, 4.7, '전자제품');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '삼성 냉장고', '<h3>삼성 스마트 냉장고</h3><p>AI 기능으로 식품을 관리합니다. <strong>스마트 센서</strong>가 내장되어 있습니다.</p><div style="background-color: #f0f0f0;">스마트 냉장고 기능</div>', 2500000, 4.5, '가전제품');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, 'LG OLED TV', '<h3>LG OLED TV</h3><p>최고의 화질을 제공합니다. <em>OLED 기술</em>로 완벽한 블랙을 구현합니다.</p><span>4K 해상도 지원</span>', 3000000, 4.8, '가전제품');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '컴퓨터 책상', '<h3>편안한 컴퓨터 책상</h3><p>높이 조절이 가능합니다. <b>인체공학적 설계</b>로 장시간 사용해도 편안합니다.</p><ul><li>전동 높이 조절</li><li>메모리 기능</li></ul>', 300000, 4.3, '가구');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '휴대폰 케이스', '<h3>아이폰용 휴대폰 케이스</h3><p>보호 기능이 뛰어납니다. <strong>군용 등급</strong> 보호 기능을 제공합니다.</p><div><i>투명한 디자인</i>으로 아이폰의 아름다움을 그대로 유지</div>', 50000, 4.4, '액세서리');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '랩탑 가방', '<h3>노트북용 가방</h3><p>충격 방지 기능이 있습니다. <em>방수 기능</em>도 함께 제공됩니다.</p><table><tr><td>크기</td><td>15.6인치</td></tr><tr><td>재질</td><td>네오프렌</td></tr></table>', 80000, 4.2, '액세서리');

INSERT INTO products(product_id, pname, description, price, rating, category)
VALUES (products_product_id_seq.nextval, '전화기 충전기', '<h3>고속 충전기</h3><p>고속 충전기가 가능한 충전기입니다. <b>USB-C PD</b> 기술을 지원합니다.</p><div style="color: green;">최대 65W 출력</div>', 30000, 4.1, '액세서리');

commit;

ROLLBACK; 