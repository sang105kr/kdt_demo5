-- 현재 사용자의 지역 정보 확인
SELECT m.member_id, m.email, m.region, c.gcode, c.code, c.decode, c.pcode
FROM member m
LEFT JOIN code c ON m.region = c.code_id
WHERE m.email = 'test1@kh.com';

-- 지역 코드 구조 확인
SELECT code_id, gcode, code, decode, pcode, code_level
FROM code 
WHERE gcode = 'REGION' 
ORDER BY code_level, sort_order, code_id; 