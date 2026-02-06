-- ============================================
-- Covering Index 적용 스크립트
-- 목표: 상품 검색 쿼리 48s → 24ms (2000배 성능 향상)
-- ============================================

-- ============================================
-- 1. order_item 테이블 인덱스
-- ============================================

-- 기존 인덱스 확인
SHOW INDEX FROM order_item;

-- Covering Index: 주문순 정렬 쿼리 최적화
-- 이 인덱스로 product_id로 JOIN 후 quantity SUM 연산 시 테이블 액세스 없이 인덱스만으로 처리
CREATE INDEX idx_order_item_product_quantity ON order_item(product_id, quantity);

-- ============================================
-- 2. product 테이블 인덱스
-- ============================================

SHOW INDEX FROM product;

-- 상품명 검색 최적화
CREATE INDEX idx_product_title ON product(title);

-- 판매량 정렬 최적화
CREATE INDEX idx_product_sale_quantity ON product(sale_quantity DESC);

-- 카테고리별 판매량 정렬 최적화
CREATE INDEX idx_product_category_sale ON product(category_id, sale_quantity DESC);

-- ============================================
-- 3. review 테이블 인덱스
-- ============================================

SHOW INDEX FROM review;

-- Covering Index: 상품별 별점 집계 최적화
CREATE INDEX idx_review_product_rating ON review(product_id, rating);

-- 회원별 리뷰 조회 최적화
CREATE INDEX idx_review_member ON review(member_id);

-- ============================================
-- 4. orders 테이블 인덱스
-- ============================================

SHOW INDEX FROM orders;

-- 회원별 주문 조회 최적화
CREATE INDEX idx_orders_member ON orders(member_id);

-- 주문 상태별 조회 최적화
CREATE INDEX idx_orders_status ON orders(status);

-- 주문 날짜별 조회 최적화
CREATE INDEX idx_orders_date ON orders(date);

-- ============================================
-- 5. member 테이블 인덱스
-- ============================================

SHOW INDEX FROM member;

-- 성별/나이대별 필터링 최적화
CREATE INDEX idx_member_gender_birth ON member(gender, birth_date);

-- ============================================
-- 인덱스 적용 확인
-- ============================================

-- 인덱스 적용 전 쿼리 실행 계획 (Before)
-- EXPLAIN SELECT p.* FROM product p
-- LEFT JOIN order_item oi ON oi.product_id = p.id
-- WHERE p.title LIKE '%니트%'
-- GROUP BY p.id
-- ORDER BY SUM(oi.quantity) DESC;

-- 예상 결과 (Before - 인덱스 없음):
-- +--+-----------+-----+------+-------------+------+-------+------+-------+-------------------------------+
-- |id|select_type|table|type  |possible_keys|key   |key_len|ref   |rows   |Extra                          |
-- +--+-----------+-----+------+-------------+------+-------+------+-------+-------------------------------+
-- |1 |SIMPLE     |oi   |ALL   |null         |null  |null   |null  |2487090|Using temporary; Using filesort|
-- |1 |SIMPLE     |p    |eq_ref|PRIMARY      |PRIMARY|8     |...   |1      |Using where                    |
-- +--+-----------+-----+------+-------------+------+-------+------+-------+-------------------------------+
-- 실행 시간: 48s

-- 예상 결과 (After - Covering Index 적용):
-- +--+-----------+-----+------+--------------------------------+--------------------------------+-------+------+------+-----------------------------+
-- |id|select_type|table|type  |possible_keys                   |key                             |key_len|ref   |rows  |Extra                        |
-- +--+-----------+-----+------+--------------------------------+--------------------------------+-------+------+------+-----------------------------+
-- |1 |SIMPLE     |p    |range |idx_product_title               |idx_product_title               |767    |null  |100   |Using index condition        |
-- |1 |SIMPLE     |oi   |ref   |idx_order_item_product_quantity |idx_order_item_product_quantity |8      |p.id  |125   |Using index                  |
-- +--+-----------+-----+------+--------------------------------+--------------------------------+-------+------+------+-----------------------------+
-- 실행 시간: 24ms

-- ============================================
-- 성능 테스트 쿼리
-- ============================================

-- 테스트 1: 상품명 검색 + 주문순 정렬
SET @start_time = NOW(6);
SELECT p.* FROM product p
LEFT JOIN order_item oi ON oi.product_id = p.id
WHERE p.title LIKE '%니트%'
GROUP BY p.id
ORDER BY COALESCE(SUM(oi.quantity), 0) DESC;
SELECT TIMEDIFF(NOW(6), @start_time) AS execution_time;

-- 테스트 2: 상품명 검색 + 별점순 정렬
SET @start_time = NOW(6);
SELECT p.* FROM product p
LEFT JOIN review r ON r.product_id = p.id
WHERE p.title LIKE '%니트%'
GROUP BY p.id
ORDER BY COALESCE(AVG(r.rating), 0) DESC;
SELECT TIMEDIFF(NOW(6), @start_time) AS execution_time;

-- 테스트 3: 카테고리별 주문순 정렬
SET @start_time = NOW(6);
SELECT p.* FROM product p
LEFT JOIN order_item oi ON oi.product_id = p.id
WHERE p.category_id = 1
GROUP BY p.id
ORDER BY COALESCE(SUM(oi.quantity), 0) DESC;
SELECT TIMEDIFF(NOW(6), @start_time) AS execution_time;
