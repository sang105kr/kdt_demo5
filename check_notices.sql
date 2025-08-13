-- 공지사항 테이블 데이터 확인
SELECT COUNT(*) as total_notices FROM notices;

SELECT notice_id, category_id, title, status_id, cdate 
FROM notices 
WHERE ROWNUM <= 5;

-- status_id별 개수 확인
SELECT status_id, COUNT(*) as count 
FROM notices 
GROUP BY status_id;

-- code 테이블에서 NOTICE_STATUS 확인
SELECT code_id, gcode, code, decode 
FROM code 
WHERE gcode = 'NOTICE_STATUS';

-- 공지사항과 코드 조인 결과 확인
SELECT n.notice_id, n.status_id, c.decode as status_name
FROM notices n
LEFT JOIN code c ON n.status_id = c.code_id
WHERE ROWNUM <= 5;

EXIT;
