-- 테스트용 데이터

-- 코드 데이터
-- 검색 타입 그룹
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('SEARCH_TYPE', 'SEARCH_TYPE', '검색타입', NULL, 1, 'Y');

-- 검색 타입 상세
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('SEARCH_TYPE', 'PRODUCT', '상품검색', 1, 1, 'Y'),
('SEARCH_TYPE', 'BOARD', '게시판검색', 1, 2, 'Y'),
('SEARCH_TYPE', 'MEMBER', '회원검색', 1, 3, 'Y'),
('SEARCH_TYPE', 'ALL', '통합검색', 1, 4, 'Y');

-- 게시판 카테고리 그룹
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('BOARD_CATEGORY', 'BOARD_CATEGORY', '게시판카테고리', NULL, 1, 'Y');

-- 게시판 카테고리 상세  
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('BOARD_CATEGORY', 'NOTICE', '공지사항', 6, 1, 'Y'),
('BOARD_CATEGORY', 'FREE', '자유게시판', 6, 2, 'Y'),
('BOARD_CATEGORY', 'QNA', '질문과답변', 6, 3, 'Y');

-- 취미 코드 그룹
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('HOBBY', 'HOBBY', '취미', NULL, 1, 'Y');

-- 취미 코드 상세
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('HOBBY', 'READING', '독서', 9, 1, 'Y'),
('HOBBY', 'GAMING', '게임', 9, 2, 'Y'),
('HOBBY', 'SPORTS', '운동', 9, 3, 'Y'),
('HOBBY', 'MUSIC', '음악감상', 9, 4, 'Y'),
('HOBBY', 'COOKING', '요리', 9, 5, 'Y'),
('HOBBY', 'TRAVEL', '여행', 9, 6, 'Y'),
('HOBBY', 'PHOTOGRAPHY', '사진촬영', 9, 7, 'Y'),
('HOBBY', 'PAINTING', '그림그리기', 9, 8, 'Y');

-- 회원 구분 코드
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('MEMBER_GUBUN', 'MEMBER_GUBUN', '회원구분', NULL, 1, 'Y');

-- 회원 구분 상세
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('MEMBER_GUBUN', 'NORMAL', '일반회원', 17, 1, 'Y'),
('MEMBER_GUBUN', 'VIP', 'VIP회원', 17, 2, 'Y'),
('MEMBER_GUBUN', 'ADMIN1', '관리자1', 17, 3, 'Y'),
('MEMBER_GUBUN', 'ADMIN2', '관리자2', 17, 4, 'Y');

-- 회원 상태 코드
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('MEMBER_STATUS', 'MEMBER_STATUS', '회원상태', NULL, 1, 'Y');

-- 회원 상태 상세
INSERT INTO code (gcode, code, decode, pcode, sort_order, use_yn) VALUES
('MEMBER_STATUS', 'ACTIVE', '활성', 22, 1, 'Y'),
('MEMBER_STATUS', 'SUSPENDED', '정지', 22, 2, 'Y'),
('MEMBER_STATUS', 'WITHDRAWN', '탈퇴', 22, 3, 'Y'),
('MEMBER_STATUS', 'PENDING', '대기', 22, 4, 'Y');

-- 테스트용 회원 데이터 (hobby는 콤마로 구분된 code_id 값들)
INSERT INTO member (member_id, email, password, nickname, name, phone, birth_date, gender, region, hobby, member_gubun, member_status, cdate, udate) VALUES
(1, 'test1@example.com', 'password123', '테스트유저1', '김테스트', '010-1234-5678', '1990-01-15', 'M', 1, '10,12,14', 18, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'test2@example.com', 'password123', '테스트유저2', '이테스트', '010-2345-6789', '1992-05-20', 'F', 2, '11,13,15', 18, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'test3@example.com', 'password123', '테스트유저3', '박테스트', '010-3456-7890', '1988-12-10', 'M', 3, '10,11,16', 18, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'admin@example.com', 'admin123', '관리자', '관리자', '010-9999-9999', '1985-03-25', 'M', 1, '10,12,13,14', 20, 23, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 테스트용 게시글 데이터
INSERT INTO boards (bcategory, title, nickname, email, bcontent, status) VALUES
(7, '테스트 게시글 1', '테스트작성자1', 'test1@example.com', '테스트 게시글 내용 1입니다.', 'A'),
(7, '테스트 게시글 2', '테스트작성자2', 'test2@example.com', '테스트 게시글 내용 2입니다.', 'A'),
(8, '자유게시판 테스트', '자유작성자', 'free@example.com', '자유게시판 테스트 내용입니다.', 'A');

-- 테스트용 댓글 데이터
INSERT INTO replies (board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status) VALUES
(1, 'reply1@example.com', '댓글작성자1', '첫 번째 댓글입니다.', NULL, 1, 0, 0, 'A'),
(1, 'reply2@example.com', '댓글작성자2', '두 번째 댓글입니다.', NULL, 2, 0, 0, 'A'),
(1, 'reply3@example.com', '답글작성자', '첫 번째 댓글의 답글입니다.', 1, 1, 1, 1, 'A'); 