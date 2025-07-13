-- 테스트용 코드 데이터 삽입
INSERT INTO code (code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate) VALUES
(1, 'REGION', 'SEOUL', '서울', NULL, '1', 1, 1, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'REGION', 'BUSAN', '부산', NULL, '2', 1, 2, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'REGION', 'DAEGU', '대구', NULL, '3', 1, 3, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'REGION', 'INCHEON', '인천', NULL, '4', 1, 4, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'REGION', 'GWANGJU', '광주', NULL, '5', 1, 5, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO code (code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate) VALUES
(10, 'MEMBER_GUBUN', 'GUEST', '게스트', NULL, '10', 1, 1, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'MEMBER_GUBUN', 'USER', '일반사용자', NULL, '11', 1, 2, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'MEMBER_GUBUN', 'VIP', 'VIP', NULL, '12', 1, 3, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'MEMBER_GUBUN', 'ADMIN1', '관리자1', NULL, '13', 1, 4, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'MEMBER_GUBUN', 'ADMIN2', '관리자2', NULL, '14', 1, 5, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO code (code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate) VALUES
(20, 'BOARD_CATEGORY', 'FREE', '자유게시판', NULL, '20', 1, 1, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'BOARD_CATEGORY', 'NOTICE', '공지사항', NULL, '21', 1, 2, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'BOARD_CATEGORY', 'QNA', '질문과답변', NULL, '22', 1, 3, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO code (code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn, cdate, udate) VALUES
(30, 'UPLOADFILE_CODE', 'MEMBER_PIC', '회원사진', NULL, '30', 1, 1, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 'UPLOADFILE_CODE', 'BOARD_ATTACH', '게시판첨부', NULL, '31', 1, 2, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 'UPLOADFILE_CODE', 'PRODUCT_IMAGE', '상품이미지', NULL, '32', 1, 3, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 테스트용 회원 데이터 삽입
INSERT INTO member (member_id, email, passwd, tel, nickname, gender, hobby, region, gubun, cdate, udate) VALUES
(1, 'test1@example.com', 'password123', '010-1111-1111', '테스트유저1', 'M', '독서,영화감상', 1, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'test2@example.com', 'password123', '010-2222-2222', '테스트유저2', 'F', '운동,음악감상', 2, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'admin@example.com', 'admin123', '010-3333-3333', '관리자', 'M', '관리', 1, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 테스트용 상품 데이터 삽입
INSERT INTO products (product_id, pname, description, price, rating, category, cdate, udate) VALUES
(1, '테스트 상품 1', '테스트 상품 1의 설명입니다.', 10000, 4.5, '전자제품', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '테스트 상품 2', '테스트 상품 2의 설명입니다.', 20000, 4.0, '의류', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '테스트 상품 3', '테스트 상품 3의 설명입니다.', 15000, 4.8, '도서', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 테스트용 게시글 데이터 삽입
INSERT INTO boards (board_id, bcategory, title, email, nickname, hit, bcontent, pboard_id, bgroup, step, bindent, status, cdate, udate) VALUES
(1, 20, '테스트 게시글 1', 'test1@example.com', '테스트유저1', 0, '테스트 게시글 1의 내용입니다.', NULL, 1, 0, 0, 'A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 20, '테스트 게시글 2', 'test2@example.com', '테스트유저2', 0, '테스트 게시글 2의 내용입니다.', NULL, 2, 0, 0, 'A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 21, '공지사항 테스트', 'admin@example.com', '관리자', 0, '공지사항 테스트 내용입니다.', NULL, 3, 0, 0, 'A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 테스트용 댓글 데이터 삽입
INSERT INTO replies (reply_id, board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status, cdate, udate) VALUES
(1, 1, 'test2@example.com', '테스트유저2', '테스트 댓글 1입니다.', NULL, 1, 0, 0, 'A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'test1@example.com', '테스트유저1', '테스트 댓글 2입니다.', NULL, 2, 0, 0, 'A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'admin@example.com', '관리자', '관리자 댓글입니다.', NULL, 3, 0, 0, 'A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 