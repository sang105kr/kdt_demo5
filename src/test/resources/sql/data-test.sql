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