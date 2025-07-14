-- 테스트용 데이터

-- 코드 데이터
INSERT INTO codes (gcode, pcode, code_name, code_value, sort_order) VALUES
('BOARD', 0, '게시판', 'BOARD', 1),
('BOARD', 1, '공지사항', 'NOTICE', 1),
('BOARD', 1, '자유게시판', 'FREE', 2),
('BOARD', 1, '질문과답변', 'QNA', 3);

-- 테스트용 게시글 데이터
INSERT INTO boards (bcategory, title, nickname, email, bcontent, status) VALUES
(1, '테스트 게시글 1', '테스트작성자1', 'test1@example.com', '테스트 게시글 내용 1입니다.', 'A'),
(1, '테스트 게시글 2', '테스트작성자2', 'test2@example.com', '테스트 게시글 내용 2입니다.', 'A'),
(2, '자유게시판 테스트', '자유작성자', 'free@example.com', '자유게시판 테스트 내용입니다.', 'A');

-- 테스트용 댓글 데이터
INSERT INTO replies (board_id, email, nickname, rcontent, parent_id, rgroup, rstep, rindent, status) VALUES
(1, 'reply1@example.com', '댓글작성자1', '첫 번째 댓글입니다.', NULL, 1, 0, 0, 'A'),
(1, 'reply2@example.com', '댓글작성자2', '두 번째 댓글입니다.', NULL, 2, 0, 0, 'A'),
(1, 'reply3@example.com', '답글작성자', '첫 번째 댓글의 답글입니다.', 1, 1, 1, 1, 'A'); 