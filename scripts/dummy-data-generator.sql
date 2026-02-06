-- ============================================
-- 대용량 더미 데이터 생성 스크립트
-- 목표: Member 50만, Product 1만, Order 500만, OrderItem 1250만, Review 600만
-- ============================================

-- 설정
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET autocommit = 0;

-- ============================================
-- 1. Category 생성 (10개)
-- ============================================
TRUNCATE TABLE category;

INSERT INTO category (name) VALUES
('의류'), ('전자기기'), ('식품'), ('가구'), ('스포츠'),
('뷰티'), ('도서'), ('완구'), ('주방용품'), ('생활용품');

COMMIT;

-- ============================================
-- 2. Member 생성 (50만명)
-- 성별: MALE/FEMALE 50:50
-- 나이대: 10대~50대 균등 분포
-- ============================================
DROP PROCEDURE IF EXISTS generate_members;

DELIMITER //
CREATE PROCEDURE generate_members()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE batch_size INT DEFAULT 10000;
    DECLARE total_count INT DEFAULT 500000;
    DECLARE gender_val VARCHAR(10);
    DECLARE birth_year INT;
    DECLARE birth_date DATETIME;

    TRUNCATE TABLE member_coupon;
    TRUNCATE TABLE delivery;
    TRUNCATE TABLE member;

    WHILE i < total_count DO
        -- 성별 설정 (50:50)
        IF i % 2 = 0 THEN
            SET gender_val = 'MALE';
        ELSE
            SET gender_val = 'FEMALE';
        END IF;

        -- 나이대 설정 (10대~50대 균등 분포)
        -- 10대: 2005-2010, 20대: 1995-2004, 30대: 1985-1994, 40대: 1975-1984, 50대: 1965-1974
        CASE (i % 5)
            WHEN 0 THEN SET birth_year = 2005 + FLOOR(RAND() * 6);  -- 10대
            WHEN 1 THEN SET birth_year = 1995 + FLOOR(RAND() * 10); -- 20대
            WHEN 2 THEN SET birth_year = 1985 + FLOOR(RAND() * 10); -- 30대
            WHEN 3 THEN SET birth_year = 1975 + FLOOR(RAND() * 10); -- 40대
            WHEN 4 THEN SET birth_year = 1965 + FLOOR(RAND() * 10); -- 50대
        END CASE;

        SET birth_date = DATE_ADD(
            CONCAT(birth_year, '-01-01'),
            INTERVAL FLOOR(RAND() * 365) DAY
        );

        INSERT INTO member (email, name, password, reg_date, gender, birth_date, role)
        VALUES (
            CONCAT('user', i, '@test.com'),
            CONCAT('사용자', i),
            '$2a$10$abcdefghijklmnopqrstuvwxyz123456', -- BCrypt hash placeholder
            NOW(),
            gender_val,
            birth_date,
            'ROLE_USER'
        );

        SET i = i + 1;

        -- 배치 커밋 (10000건마다)
        IF i % batch_size = 0 THEN
            COMMIT;
            SELECT CONCAT('Members created: ', i, ' / ', total_count) AS progress;
        END IF;
    END WHILE;

    COMMIT;
    SELECT 'Member generation completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 3. Product 생성 (1만개)
-- ============================================
DROP PROCEDURE IF EXISTS generate_products;

DELIMITER //
CREATE PROCEDURE generate_products()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE batch_size INT DEFAULT 1000;
    DECLARE total_count INT DEFAULT 10000;
    DECLARE category_id INT;
    DECLARE product_names VARCHAR(100);

    -- 기존 데이터 정리
    DELETE FROM review;
    DELETE FROM product_detail;
    DELETE FROM product_tag;
    DELETE FROM product;

    WHILE i < total_count DO
        SET category_id = (i % 10) + 1;

        -- 카테고리별 상품명 prefix
        CASE category_id
            WHEN 1 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '니트', '셔츠', '바지', '자켓', '코트'), ' ', i);
            WHEN 2 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '스마트폰', '노트북', '태블릿', '이어폰', '스피커'), ' ', i);
            WHEN 3 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '과자', '음료', '라면', '커피', '차'), ' ', i);
            WHEN 4 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '책상', '의자', '소파', '침대', '서랍'), ' ', i);
            WHEN 5 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '운동화', '요가매트', '덤벨', '자전거', '테니스'), ' ', i);
            WHEN 6 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '스킨케어', '메이크업', '향수', '헤어케어', '바디케어'), ' ', i);
            WHEN 7 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '소설', '에세이', '자기계발', '경제', '역사'), ' ', i);
            WHEN 8 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '레고', '인형', '보드게임', '퍼즐', 'RC카'), ' ', i);
            WHEN 9 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '냄비', '프라이팬', '칼', '도마', '그릇'), ' ', i);
            WHEN 10 THEN SET product_names = CONCAT(ELT(FLOOR(RAND()*5)+1, '청소기', '세제', '휴지', '수건', '바구니'), ' ', i);
        END CASE;

        INSERT INTO product (title, price, quantity, sale_quantity, category_id)
        VALUES (
            product_names,
            FLOOR(10000 + RAND() * 990000), -- 10,000 ~ 1,000,000원
            FLOOR(100 + RAND() * 9900),     -- 재고 100 ~ 10,000
            0,                               -- 판매량 초기값
            category_id
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
            SELECT CONCAT('Products created: ', i, ' / ', total_count) AS progress;
        END IF;
    END WHILE;

    COMMIT;
    SELECT 'Product generation completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 4. Delivery 생성 (회원당 1개)
-- ============================================
DROP PROCEDURE IF EXISTS generate_deliveries;

DELIMITER //
CREATE PROCEDURE generate_deliveries()
BEGIN
    DECLARE batch_size INT DEFAULT 10000;

    TRUNCATE TABLE delivery;

    INSERT INTO delivery (member_id, recipient, address, phone_number, request, delivery_checked)
    SELECT
        member_id,
        CONCAT('수령인', member_id),
        CONCAT('서울시 강남구 테헤란로 ', member_id, '번지'),
        CONCAT('010-', LPAD(FLOOR(RAND() * 10000), 4, '0'), '-', LPAD(FLOOR(RAND() * 10000), 4, '0')),
        '문 앞에 놓아주세요',
        'UNCHECKED'
    FROM member;

    COMMIT;
    SELECT 'Delivery generation completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 5. Order 생성 (500만건)
-- 회원당 평균 10건
-- ============================================
DROP PROCEDURE IF EXISTS generate_orders;

DELIMITER //
CREATE PROCEDURE generate_orders()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE batch_size INT DEFAULT 50000;
    DECLARE total_count INT DEFAULT 5000000;
    DECLARE member_count INT DEFAULT 500000;
    DECLARE member_id_val BIGINT;
    DECLARE delivery_id_val BIGINT;
    DECLARE order_date VARCHAR(20);
    DECLARE status_val VARCHAR(20);

    -- 기존 주문 데이터 정리
    TRUNCATE TABLE order_item;
    TRUNCATE TABLE orders;

    WHILE i < total_count DO
        -- 회원 ID (1 ~ 500000)
        SET member_id_val = (i % member_count) + 1;

        -- 배송지 ID (회원과 동일하게 매핑)
        SET delivery_id_val = member_id_val;

        -- 주문 날짜 (최근 2년)
        SET order_date = DATE_FORMAT(
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
            '%Y%m%d'
        );

        -- 주문 상태 (80% CONFIRMED, 15% PENDING, 5% CANCELLED)
        SET status_val = ELT(
            FLOOR(1 + RAND() * 100),
            CASE
                WHEN RAND() < 0.80 THEN 'CONFIRMED'
                WHEN RAND() < 0.95 THEN 'PENDING'
                ELSE 'CANCELLED'
            END
        );
        SET status_val = CASE
            WHEN RAND() < 0.80 THEN 'CONFIRMED'
            WHEN RAND() < 0.9375 THEN 'PENDING'
            ELSE 'CANCELLED'
        END;

        INSERT INTO orders (member_id, delivery_id, date, total_price, status)
        VALUES (
            member_id_val,
            delivery_id_val,
            order_date,
            0, -- OrderItem 생성 후 업데이트
            status_val
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
            SELECT CONCAT('Orders created: ', i, ' / ', total_count) AS progress;
        END IF;
    END WHILE;

    COMMIT;
    SELECT 'Order generation completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 6. OrderItem 생성 (1250만건)
-- 주문당 평균 2.5개
-- ============================================
DROP PROCEDURE IF EXISTS generate_order_items;

DELIMITER //
CREATE PROCEDURE generate_order_items()
BEGIN
    DECLARE i BIGINT DEFAULT 0;
    DECLARE batch_size INT DEFAULT 50000;
    DECLARE total_count BIGINT DEFAULT 12500000;
    DECLARE product_count INT DEFAULT 10000;
    DECLARE order_count BIGINT DEFAULT 5000000;
    DECLARE order_id_val BIGINT;
    DECLARE product_id_val BIGINT;
    DECLARE quantity_val INT;

    WHILE i < total_count DO
        -- 주문 ID (순환)
        SET order_id_val = (i % order_count) + 1;

        -- 상품 ID (1 ~ 10000)
        SET product_id_val = FLOOR(1 + RAND() * product_count);

        -- 수량 (1 ~ 5)
        SET quantity_val = FLOOR(1 + RAND() * 5);

        INSERT INTO order_item (order_id, product_id, quantity)
        VALUES (order_id_val, product_id_val, quantity_val);

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
            SELECT CONCAT('OrderItems created: ', i, ' / ', total_count) AS progress;
        END IF;
    END WHILE;

    COMMIT;
    SELECT 'OrderItem generation completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 7. Product sale_quantity 업데이트
-- ============================================
DROP PROCEDURE IF EXISTS update_sale_quantities;

DELIMITER //
CREATE PROCEDURE update_sale_quantities()
BEGIN
    UPDATE product p
    SET sale_quantity = (
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM order_item oi
        WHERE oi.product_id = p.id
    );

    COMMIT;
    SELECT 'Sale quantity update completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 8. Order total_price 업데이트
-- ============================================
DROP PROCEDURE IF EXISTS update_order_prices;

DELIMITER //
CREATE PROCEDURE update_order_prices()
BEGIN
    DECLARE batch_start BIGINT DEFAULT 0;
    DECLARE batch_size INT DEFAULT 100000;
    DECLARE total_orders BIGINT;

    SELECT COUNT(*) INTO total_orders FROM orders;

    WHILE batch_start < total_orders DO
        UPDATE orders o
        SET total_price = (
            SELECT COALESCE(SUM(p.price * oi.quantity), 0)
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            WHERE oi.order_id = o.order_id
        )
        WHERE o.order_id > batch_start AND o.order_id <= batch_start + batch_size;

        COMMIT;
        SET batch_start = batch_start + batch_size;
        SELECT CONCAT('Order prices updated: ', batch_start, ' / ', total_orders) AS progress;
    END WHILE;

    SELECT 'Order price update completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 9. Review 생성 (600만건)
-- 상품당 평균 600개
-- ============================================
DROP PROCEDURE IF EXISTS generate_reviews;

DELIMITER //
CREATE PROCEDURE generate_reviews()
BEGIN
    DECLARE i BIGINT DEFAULT 0;
    DECLARE batch_size INT DEFAULT 50000;
    DECLARE total_count BIGINT DEFAULT 6000000;
    DECLARE product_count INT DEFAULT 10000;
    DECLARE member_count INT DEFAULT 500000;
    DECLARE product_id_val BIGINT;
    DECLARE member_id_val BIGINT;
    DECLARE rating_val INT;
    DECLARE review_titles VARCHAR(100);

    DELETE FROM review;

    WHILE i < total_count DO
        -- 상품 ID (순환하여 균등 분포)
        SET product_id_val = (i % product_count) + 1;

        -- 회원 ID
        SET member_id_val = FLOOR(1 + RAND() * member_count);

        -- 별점 (1~5, 평균 4점으로 분포)
        SET rating_val = CASE
            WHEN RAND() < 0.05 THEN 1
            WHEN RAND() < 0.15 THEN 2
            WHEN RAND() < 0.30 THEN 3
            WHEN RAND() < 0.60 THEN 4
            ELSE 5
        END;

        -- 리뷰 제목
        SET review_titles = ELT(
            FLOOR(1 + RAND() * 10),
            '좋아요!', '만족합니다', '추천해요', '괜찮아요', '보통이에요',
            '아쉬워요', '별로예요', '최고입니다', '가성비 좋음', '재구매 의사 있음'
        );

        INSERT INTO review (product_id, member_id, rating, title, content, create_time)
        VALUES (
            product_id_val,
            member_id_val,
            rating_val,
            review_titles,
            CONCAT('상품 ', product_id_val, '에 대한 리뷰입니다. 별점: ', rating_val, '점'),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
            SELECT CONCAT('Reviews created: ', i, ' / ', total_count) AS progress;
        END IF;
    END WHILE;

    COMMIT;
    SELECT 'Review generation completed!' AS result;
END //
DELIMITER ;

-- ============================================
-- 실행 순서 (순차 실행 필요)
-- ============================================
-- 1. CALL generate_members();      -- 약 5-10분
-- 2. CALL generate_products();     -- 약 1분
-- 3. CALL generate_deliveries();   -- 약 1분
-- 4. CALL generate_orders();       -- 약 30-60분
-- 5. CALL generate_order_items();  -- 약 60-90분
-- 6. CALL update_sale_quantities(); -- 약 5-10분
-- 7. CALL update_order_prices();   -- 약 30-60분
-- 8. CALL generate_reviews();      -- 약 30-60분

-- ============================================
-- 전체 실행 프로시저
-- ============================================
DROP PROCEDURE IF EXISTS generate_all_data;

DELIMITER //
CREATE PROCEDURE generate_all_data()
BEGIN
    SELECT '=== 데이터 생성 시작 ===' AS status, NOW() AS start_time;

    SELECT '1. Member 생성 시작...' AS status;
    CALL generate_members();

    SELECT '2. Product 생성 시작...' AS status;
    CALL generate_products();

    SELECT '3. Delivery 생성 시작...' AS status;
    CALL generate_deliveries();

    SELECT '4. Order 생성 시작...' AS status;
    CALL generate_orders();

    SELECT '5. OrderItem 생성 시작...' AS status;
    CALL generate_order_items();

    SELECT '6. Sale Quantity 업데이트 시작...' AS status;
    CALL update_sale_quantities();

    SELECT '7. Order Price 업데이트 시작...' AS status;
    CALL update_order_prices();

    SELECT '8. Review 생성 시작...' AS status;
    CALL generate_reviews();

    SELECT '=== 데이터 생성 완료 ===' AS status, NOW() AS end_time;
END //
DELIMITER ;

-- 설정 복원
SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;
SET autocommit = 1;

-- ============================================
-- 데이터 확인 쿼리
-- ============================================
-- SELECT 'member' AS table_name, COUNT(*) AS count FROM member
-- UNION ALL SELECT 'product', COUNT(*) FROM product
-- UNION ALL SELECT 'orders', COUNT(*) FROM orders
-- UNION ALL SELECT 'order_item', COUNT(*) FROM order_item
-- UNION ALL SELECT 'review', COUNT(*) FROM review;
