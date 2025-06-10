-- 테이블 삭제
DROP TABLE rbbs;

-- 테이블 생성
CREATE TABLE rbbs(
	rbbs_id			number(10),  		--댓글 식별자
    bbs_id			number(10),  		--원글 식별자
	content			clob, 				--내용,
	writer			varchar2(30),		--작성자(한글10자)
	created_at	    timestamp ,			--작성 날짜 : date, timestamp
	updated_at	    timestamp			--수정 날짜
);

-- 기본키
ALTER TABLE rbbs ADD CONSTRAINT rbbs_rbbs_id_pk PRIMARY key(rbbs_id);

-- 기본값
ALTER TABLE rbbs MODIFY created_at DEFAULT systimestamp;
ALTER TABLE rbbs MODIFY updated_at DEFAULT systimestamp;

-- 제약조건
alter table rbbs modify bbs_id constraint rbbs_bbs_id_nn not null;
alter table rbbs modify content constraint rbbs_content_nn not null;
alter table rbbs modify writer constraint rbbs_writer_nn not null;

-- 시퀀스 삭제
DROP SEQUENCE rbbs_rbbs_id_seq;

-- 시퀀스 생성
CREATE SEQUENCE rbbs_rbbs_id_seq;

SELECT * FROM rbbs;

-- 댓글 등록
INSERT INTO RBBS(rbbs_id, bbs_id, content, writer)
	 VALUES (rbbs_rbbs_id_seq.nextval, 1, '내용1', '작성자');

-- 댓글 조회
SELECT rbbs_id, bbs_id, content, writer, created_at, updated_at
  FROM rbbs
 WHERE rbbs_id = 1;

-- 댓글 수정
UPDATE RBBS
   SET content = '수정내용1', writer = '수정작성자',
       updated_at = systimestamp
 WHERE rbbs_id = 1;

-- 댓글 삭제(단건)
DELETE FROM RBBS
 WHERE rbbs_id = 1;

-- 댓글 목록
SELECT rbbs_id, content, writer, created_at, updated_at
  FROM rbbs;

--페이징
--	SELECT rbbs_id,bbs_id,content,created_at,updated_at
--	  FROM rbbs
--ORDER BY rbbs_id asc
--offset (:pageNo -1) * :numOfRows ROWS
--FETCH NEXT :numOfRows ROWS only;

--총건수
SELECT count(rbbs_id)
  FROM rbbs;
commit;

ROLLBACK;
