-- 테이블 삭제
DROP TABLE bbs;

-- 테이블 생성
CREATE TABLE bbs(
	bbs_id			number(10),  		--게시글 식별자
	title				varchar2(150), 	--제목(한글50자)
	content			clob, 					--내용,
	writer			varchar2(30),		--작성자(한글10자)
	created_at	timestamp ,			--작성 날짜 : date, timestamp
	updated_at	timestamp				--수정 날짜
);

-- 기본키
ALTER TABLE bbs ADD CONSTRAINT bbs_bbs_id_pk PRIMARY key(bbs_id);

-- 기본값
ALTER TABLE bbs MODIFY created_at DEFAULT systimestamp;
ALTER TABLE bbs MODIFY updated_at DEFAULT systimestamp;

-- 시퀀스 삭제
DROP SEQUENCE bbs_bbs_id_seq;

-- 시퀀스 생성
CREATE SEQUENCE bbs_bbs_id_seq;

SELECT * FROM bbs;

-- 게시글 등록
INSERT INTO BBS(bbs_id, title, content, writer)
		 VALUES (bbs_bbs_id_seq.nextval, '제목1', '내용1', '작성자');

-- 게시글 조회
SELECT bbs_id, title, content, writer, created_at, updated_at
  FROM bbs
 WHERE bbs_id = 1;

-- 게시글 수정
UPDATE BBS
   SET title = '수정제목1', content = '수정내용1', writer = '수정작성자',
       updated_at = systimestamp
 WHERE bbs_id = 1;

-- 게시글 삭제(단건)
DELETE FROM BBS
 WHERE bbs_id = 1;

-- 게시글 삭제(여러건)
DELETE FROM BBS
 WHERE bbs_id in(3,4);

-- 게시글 목록
SELECT bbs_id, title, content, writer, created_at, updated_at
  FROM bbs;

ROLLBACK;
