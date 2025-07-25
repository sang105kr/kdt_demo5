-- 기존 유니크 제약 조건 삭제
ALTER TABLE reviews DROP CONSTRAINT uk_reviews_order;

-- 새로운 유니크 제약 조건 추가 (주문 ID + 상품 ID)
ALTER TABLE reviews ADD CONSTRAINT uk_reviews_order_product UNIQUE (order_id, product_id);

-- 제약 조건 변경 확인
SELECT constraint_name, constraint_type, search_condition 
FROM user_constraints 
WHERE table_name = 'REVIEWS' 
AND constraint_type = 'U'; 