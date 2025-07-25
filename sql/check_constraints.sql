-- reviews 테이블의 모든 제약 조건 확인
SELECT constraint_name, constraint_type, search_condition 
FROM user_constraints 
WHERE table_name = 'REVIEWS'
ORDER BY constraint_type, constraint_name;

-- 유니크 제약 조건만 확인
SELECT constraint_name, constraint_type, search_condition 
FROM user_constraints 
WHERE table_name = 'REVIEWS' 
AND constraint_type = 'U'; 