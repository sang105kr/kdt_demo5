SELECT code_id, gcode, code, decode, pcode, code_path, code_level, sort_order, use_yn 
FROM code 
WHERE gcode = 'REGION' 
ORDER BY sort_order, code_id; 