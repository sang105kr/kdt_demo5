-- 지역 코드 전체 조회
SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn 
FROM code 
WHERE gcode = 'REGION' 
ORDER BY sort_order, code_id;

-- 지역 하위 코드만 조회 (findActiveSubCodesByGcode 결과)
SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn 
FROM code 
WHERE gcode = 'REGION' 
AND use_yn = 'Y'
AND pcode IS NOT NULL 
AND code != gcode
ORDER BY sort_order, code_id; 