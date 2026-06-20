#!/usr/bin/env python3
"""
MSA Store 대용량 테스트 데이터 생성 스크립트
========================================
목표: 주문 3,000,000건 / 주문상품 7,500,000건

사전 준비:
  pip install pymysql

실행:
  python scripts/generate_test_data.py

예상 소요 시간: 10~30분 (DB 서버 성능에 따라 다름)
"""

import os
import pymysql
import random
import time
import sys
import hashlib
from datetime import datetime, timedelta

# ──────────────────────────────────────────────────────────
# 설정 (환경에 맞게 수정)
# ──────────────────────────────────────────────────────────
DB_CONFIG = {
    'host':     os.environ.get('DB_HOST', 'localhost'),
    'port':     int(os.environ.get('DB_PORT', '3307')),
    'user':     os.environ.get('DB_USER', 'root'),
    'password': os.environ.get('DB_PASS', '1234'),
    'database': os.environ.get('DB_NAME', 'store'),
    'charset':  'utf8mb4',
    'autocommit': False,
    'connect_timeout': 30,
}

# 생성 목표
NUM_CATEGORIES =    10       # 카테고리 수
NUM_SELLERS    =   500       # 판매자로 사용할 회원 수 (ROLE_USER 동일)
NUM_BUYERS     = 50_000      # 구매자 회원 수
NUM_PRODUCTS   = 10_000      # 상품 수
NUM_ORDERS     = 3_000_000   # 목표 주문 건수
# 주문당 상품 수: randint(1,4) → 평균 2.5 → 총 약 7,500,000건

INSERT_BATCH   = 3_000       # 한 번에 INSERT할 행 수
LOG_EVERY      = 300_000     # 주문 진행 로그 간격

random.seed(42)              # 재현 가능한 데이터

# ──────────────────────────────────────────────────────────
# 공통 상수
# ──────────────────────────────────────────────────────────
GENDERS    = ['MALE', 'FEMALE']
AGE_GROUPS = ['AGE_10S', 'AGE_20S', 'AGE_30S', 'AGE_40S', 'AGE_50S_PLUS']
STATUSES   = ['PENDING', 'CONFIRMED', 'CANCELLED']
STATUS_W   = [0.15, 0.80, 0.05]    # PENDING 15% / CONFIRMED 80% / CANCELLED 5%
PW_HASH    = '$2a$10$abcdefghijklmnopqrstuvwxyz123456'

# ──────────────────────────────────────────────────────────
# 유틸 함수
# ──────────────────────────────────────────────────────────
def log(msg: str):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}", flush=True)

def eta_str(done: int, total: int, elapsed: float) -> str:
    if done == 0:
        return "--:--"
    rate = done / elapsed
    rem_sec = int((total - done) / rate)
    return f"{rem_sec // 60}분 {rem_sec % 60}초"

def get_conn():
    return pymysql.connect(**DB_CONFIG)

def birth_to_age_group(birth: datetime, ref_year: int = 2026) -> str:
    age = ref_year - birth.year
    if age < 20:   return 'AGE_10S'
    if age < 30:   return 'AGE_20S'
    if age < 40:   return 'AGE_30S'
    if age < 50:   return 'AGE_40S'
    return 'AGE_50S_PLUS'

# ──────────────────────────────────────────────────────────
# Phase 0: 스키마 생성 (테이블이 없으면 CREATE TABLE IF NOT EXISTS)
# ──────────────────────────────────────────────────────────
_DDL = [
    """CREATE TABLE IF NOT EXISTS category (
        id   BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",

    """CREATE TABLE IF NOT EXISTS `member` (
        member_id  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
        email      VARCHAR(255),
        name       VARCHAR(50),
        password   VARCHAR(500),
        reg_date   DATETIME,
        gender     VARCHAR(10),
        birth_date DATETIME,
        role       VARCHAR(20)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",

    """CREATE TABLE IF NOT EXISTS delivery (
        id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        recipient        VARCHAR(255),
        address          VARCHAR(255),
        phone_number     VARCHAR(255),
        request          VARCHAR(255),
        delivery_checked VARCHAR(20),
        member_id        BIGINT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",

    """CREATE TABLE IF NOT EXISTS product (
        id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        seller_id     BIGINT,
        title         VARCHAR(255),
        price         INT NOT NULL DEFAULT 0,
        quantity      INT NOT NULL DEFAULT 0,
        sale_quantity INT NOT NULL DEFAULT 0,
        category_id   BIGINT,
        INDEX idx_product_category_sale (category_id, sale_quantity, id),
        INDEX idx_product_title         (title),
        INDEX idx_product_sale_quantity (sale_quantity, id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",

    """CREATE TABLE IF NOT EXISTS product_detail (
        id          BIGINT PRIMARY KEY,
        description TEXT,
        image_url   VARCHAR(255),
        rate        DOUBLE,
        count       INT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",

    """CREATE TABLE IF NOT EXISTS orders (
        order_id    BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        member_id   BIGINT,
        delivery_id BIGINT,
        date        VARCHAR(20),
        total_price INT NOT NULL DEFAULT 0,
        status      VARCHAR(20)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",

    """CREATE TABLE IF NOT EXISTS order_item (
        order_item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        product_id    BIGINT,
        order_id      BIGINT,
        quantity      INT NOT NULL DEFAULT 0,
        unit_price    INT NOT NULL DEFAULT 0,
        seller_id     BIGINT,
        INDEX idx_order_item_product_quantity (product_id, quantity)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",

    """CREATE TABLE IF NOT EXISTS product_order_stats (
        id             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        product_id     BIGINT NOT NULL,
        gender         VARCHAR(10) NOT NULL,
        age_group      VARCHAR(20) NOT NULL,
        order_count    INT NOT NULL DEFAULT 0,
        total_quantity INT NOT NULL DEFAULT 0,
        UNIQUE KEY uk_product_gender_age (product_id, gender, age_group),
        INDEX idx_pos_product_id (product_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
]

def create_schema(conn):
    with conn.cursor() as cur:
        for ddl in _DDL:
            cur.execute(ddl)
    conn.commit()
    log("스키마 확인/생성 완료")

# ──────────────────────────────────────────────────────────
# Phase 0.5: 세션 최적화
# ──────────────────────────────────────────────────────────
def apply_optimizations(conn):
    with conn.cursor() as cur:
        cur.execute("SET foreign_key_checks = 0")
        cur.execute("SET unique_checks = 0")
        try:
            cur.execute("SET sql_log_bin = 0")
        except Exception:
            pass   # 바이너리 로그 미사용 환경이면 무시
    conn.commit()
    log("최적화 세팅 적용 (foreign_key_checks=0, unique_checks=0)")

def restore_optimizations(conn):
    with conn.cursor() as cur:
        cur.execute("SET foreign_key_checks = 1")
        cur.execute("SET unique_checks = 1")
    conn.commit()
    log("최적화 세팅 복원")

# ──────────────────────────────────────────────────────────
# Phase 1: 카테고리
# ──────────────────────────────────────────────────────────
def insert_categories(conn):
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM category")
        if cur.fetchone()[0] >= NUM_CATEGORIES:
            log("카테고리: 이미 존재 → 스킵")
            return

    names = [
        '의류', '전자기기', '식품', '가구', '스포츠',
        '뷰티', '도서', '완구', '주방용품', '생활용품',
    ]
    while len(names) < NUM_CATEGORIES:
        names.append(f'카테고리_{len(names)+1}')
    names = names[:NUM_CATEGORIES]

    with conn.cursor() as cur:
        cur.executemany("INSERT IGNORE INTO category (name) VALUES (%s)", [(n,) for n in names])
    conn.commit()
    log(f"카테고리 {NUM_CATEGORIES}개 삽입 완료")

# ──────────────────────────────────────────────────────────
# Phase 2: 회원
# ──────────────────────────────────────────────────────────
def insert_members(conn) -> dict:
    """
    판매자 NUM_SELLERS명 + 구매자 NUM_BUYERS명 삽입.
    Returns: {member_id: (gender, birth_date)} (구매자만)
    """
    total_need = NUM_SELLERS + NUM_BUYERS
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM `member`")
        existing = cur.fetchone()[0]

    if existing >= total_need:
        log(f"회원: 이미 {existing:,}명 존재 → 스킵 (구매자 정보 로딩 중...)")
        with conn.cursor() as cur:
            cur.execute(
                "SELECT member_id, gender, birth_date FROM `member` "
                "ORDER BY member_id LIMIT %s OFFSET %s",
                (NUM_BUYERS, NUM_SELLERS)
            )
            rows = cur.fetchall()
        return {r[0]: (r[1] or 'MALE', r[2] or datetime(1990, 1, 1)) for r in rows}

    sql = ("INSERT INTO `member` "
           "(email, name, password, reg_date, gender, birth_date, role) "
           "VALUES (%s, %s, %s, %s, %s, %s, %s)")

    age_brackets = [
        (2006, 2015),   # 10대
        (1996, 2005),   # 20대
        (1986, 1995),   # 30대
        (1976, 1985),   # 40대
        (1960, 1975),   # 50대+
    ]

    def make_birth(idx: int) -> datetime:
        lo, hi = age_brackets[idx % 5]
        year = random.randint(lo, hi)
        return datetime(year, random.randint(1, 12), random.randint(1, 28))

    # 판매자
    seller_rows = []
    for i in range(NUM_SELLERS):
        b = make_birth(i)
        seller_rows.append((
            f"seller{i+1}@store.test",
            f"판매자{i+1}",
            PW_HASH,
            datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            random.choice(GENDERS),
            b.strftime('%Y-%m-%d %H:%M:%S'),
            'ROLE_USER',
        ))
    with conn.cursor() as cur:
        cur.executemany(sql, seller_rows)
    conn.commit()
    log(f"판매자 {NUM_SELLERS}명 삽입 완료")

    # 구매자 (배치)
    buyer_meta = {}
    total = 0
    batch = []
    for i in range(NUM_BUYERS):
        g = GENDERS[i % 2]
        b = make_birth(i)
        batch.append((
            f"buyer{i+1}@store.test",
            f"구매자{i+1}",
            PW_HASH,
            datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            g,
            b.strftime('%Y-%m-%d %H:%M:%S'),
            'ROLE_USER',
        ))
        if len(batch) >= INSERT_BATCH:
            with conn.cursor() as cur:
                cur.executemany(sql, batch)
                first_id = cur.lastrowid
            conn.commit()
            for j, row in enumerate(batch):
                buyer_meta[first_id + j] = (row[4], datetime.strptime(row[5], '%Y-%m-%d %H:%M:%S'))
            total += len(batch)
            batch = []
            if total % 10000 == 0:
                log(f"  구매자 {total:,}명 완료")

    if batch:
        with conn.cursor() as cur:
            cur.executemany(sql, batch)
            first_id = cur.lastrowid
        conn.commit()
        for j, row in enumerate(batch):
            buyer_meta[first_id + j] = (row[4], datetime.strptime(row[5], '%Y-%m-%d %H:%M:%S'))
        total += len(batch)

    log(f"구매자 {total:,}명 삽입 완료 (총 {NUM_SELLERS+total:,}명)")
    return buyer_meta

# ──────────────────────────────────────────────────────────
# Phase 3: 배송지
# ──────────────────────────────────────────────────────────
def insert_deliveries(conn) -> dict:
    """
    구매자 1인당 배송지 1개 삽입.
    Returns: {member_id: delivery_id}
    """
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM delivery")
        existing = cur.fetchone()[0]

    if existing >= NUM_BUYERS:
        log(f"배송지: 이미 {existing:,}건 존재 → 스킵 (배송지 매핑 로딩...)")
        with conn.cursor() as cur:
            # 구매자 ID 범위 (seller 이후)
            cur.execute("SELECT member_id FROM `member` ORDER BY member_id LIMIT 1 OFFSET %s",
                        (NUM_SELLERS,))
            first_buyer = cur.fetchone()[0]
            cur.execute("SELECT id, member_id FROM delivery WHERE member_id >= %s", (first_buyer,))
            rows = cur.fetchall()
        return {r[1]: r[0] for r in rows}

    with conn.cursor() as cur:
        cur.execute("SELECT member_id FROM `member` ORDER BY member_id LIMIT %s OFFSET %s",
                    (NUM_BUYERS, NUM_SELLERS))
        buyer_ids = [r[0] for r in cur.fetchall()]

    addresses = [
        "서울시 강남구 테헤란로",   "서울시 서초구 반포대로",
        "경기도 수원시 팔달로",     "경기도 성남시 분당로",
        "부산시 해운대구 해운대로", "대구시 수성구 범어로",
        "인천시 남동구 인주대로",   "광주시 서구 상무대로",
        "대전시 유성구 대덕대로",   "울산시 남구 삼산로",
    ]

    sql = ("INSERT INTO delivery "
           "(recipient, address, phone_number, request, delivery_checked, member_id) "
           "VALUES (%s, %s, %s, %s, %s, %s)")

    delivery_map = {}
    total = 0
    batch = []
    for mid in buyer_ids:
        batch.append((
            f"수령인{mid}",
            f"{random.choice(addresses)} {random.randint(1,999)}번길 {random.randint(1,100)}",
            f"010-{random.randint(1000,9999)}-{random.randint(1000,9999)}",
            "부재 시 문 앞에 놓아주세요",
            'UNCHECKED',
            mid,
        ))
        if len(batch) >= INSERT_BATCH:
            with conn.cursor() as cur:
                cur.executemany(sql, batch)
                first_did = cur.lastrowid
            conn.commit()
            for j, row in enumerate(batch):
                delivery_map[row[5]] = first_did + j
            total += len(batch)
            batch = []
            if total % 10000 == 0:
                log(f"  배송지 {total:,}건 완료")

    if batch:
        with conn.cursor() as cur:
            cur.executemany(sql, batch)
            first_did = cur.lastrowid
        conn.commit()
        for j, row in enumerate(batch):
            delivery_map[row[5]] = first_did + j
        total += len(batch)

    log(f"배송지 {total:,}건 삽입 완료")
    return delivery_map

# ──────────────────────────────────────────────────────────
# Phase 4: 상품
# ──────────────────────────────────────────────────────────
def insert_products(conn) -> list:
    """
    상품 NUM_PRODUCTS개 + product_detail 삽입.
    Returns: [(product_id, price, seller_id), ...]
    """
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM product")
        existing = cur.fetchone()[0]

    if existing >= NUM_PRODUCTS:
        log(f"상품: 이미 {existing:,}개 존재 → 스킵 (상품 정보 로딩...)")
        with conn.cursor() as cur:
            cur.execute("SELECT id, price, seller_id FROM product")
            return cur.fetchall()

    with conn.cursor() as cur:
        cur.execute("SELECT id FROM category")
        cat_ids = [r[0] for r in cur.fetchall()]
        # 판매자: 처음 NUM_SELLERS명
        cur.execute("SELECT member_id FROM `member` ORDER BY member_id LIMIT %s", (NUM_SELLERS,))
        seller_ids = [r[0] for r in cur.fetchall()]

    cat_items = {
        1: ['니트', '셔츠', '바지', '자켓', '코트'],
        2: ['스마트폰', '노트북', '태블릿', '이어폰', '스피커'],
        3: ['과자', '음료', '라면', '커피', '차'],
        4: ['책상', '의자', '소파', '침대', '서랍'],
        5: ['운동화', '요가매트', '덤벨', '자전거', '테니스'],
        6: ['스킨케어', '메이크업', '향수', '헤어케어', '바디케어'],
        7: ['소설', '에세이', '자기계발', '경제', '역사'],
        8: ['레고', '인형', '보드게임', '퍼즐', 'RC카'],
        9: ['냄비', '프라이팬', '칼', '도마', '그릇'],
        10: ['청소기', '세제', '휴지', '수건', '바구니'],
    }

    product_sql = ("INSERT INTO product "
                   "(seller_id, title, price, quantity, sale_quantity, category_id) "
                   "VALUES (%s, %s, %s, %s, %s, %s)")
    detail_sql  = ("INSERT INTO product_detail "
                   "(id, description, image_url, rate, count) "
                   "VALUES (%s, %s, %s, %s, %s)")

    all_products = []
    for batch_start in range(0, NUM_PRODUCTS, INSERT_BATCH):
        batch_end = min(batch_start + INSERT_BATCH, NUM_PRODUCTS)
        p_rows = []
        for i in range(batch_start, batch_end):
            cat_id = cat_ids[i % len(cat_ids)]
            items  = cat_items.get(cat_id, ['상품'])
            title  = f"{random.choice(items)} {i+1}번"
            price  = random.randint(10_000, 1_000_000)
            p_rows.append((
                random.choice(seller_ids),
                title,
                price,
                random.randint(100, 10_000),
                0,
                cat_id,
            ))

        with conn.cursor() as cur:
            cur.executemany(product_sql, p_rows)
            first_pid = cur.lastrowid
        conn.commit()

        d_rows = []
        for j, p in enumerate(p_rows):
            pid = first_pid + j
            all_products.append((pid, p[2], p[0]))   # (id, price, seller_id)
            d_rows.append((
                pid,
                f"상품 {pid}번 상세 설명입니다.",
                f"https://cdn.store.test/products/{pid}.jpg",
                round(random.uniform(1.0, 5.0), 1),
                random.randint(0, 2_000),
            ))

        with conn.cursor() as cur:
            cur.executemany(detail_sql, d_rows)
        conn.commit()
        log(f"  상품 {batch_end:,}/{NUM_PRODUCTS:,} 완료")

    log(f"상품 {NUM_PRODUCTS:,}개 삽입 완료")
    return all_products

# ──────────────────────────────────────────────────────────
# Phase 5: 주문 + 주문상품 (핵심)
# ──────────────────────────────────────────────────────────
def insert_orders_and_items(conn, buyer_meta: dict, delivery_map: dict,
                             products: list) -> dict:
    """
    주문 NUM_ORDERS건 + 주문상품(평균 2.5개/주문) 벌크 삽입.
    Returns: stats_acc {(product_id, gender, age_group): [order_count, total_qty]}
    """
    with conn.cursor() as cur:
        cur.execute("SELECT COALESCE(MAX(order_id), 0) FROM orders")
        existing_orders = 0
        cur.execute("SELECT COUNT(*) FROM orders")
        existing_orders = cur.fetchone()[0]

    if existing_orders >= NUM_ORDERS:
        log(f"주문: 이미 {existing_orders:,}건 존재 → 스킵")
        return {}

    need = NUM_ORDERS - existing_orders
    log(f"주문 {need:,}건 + 주문상품 삽입 시작 (기존 {existing_orders:,}건)")

    # 다음 order_id 시작값
    with conn.cursor() as cur:
        cur.execute("SELECT COALESCE(MAX(order_id), 0) FROM orders")
        next_order_id = cur.fetchone()[0] + 1

    # 구매자 목록 (list of (member_id, gender, age_group, delivery_id))
    buyer_list = []
    for mid, (gender, birth) in buyer_meta.items():
        d_id = delivery_map.get(mid)
        if d_id is None:
            continue
        ag = birth_to_age_group(birth)
        buyer_list.append((mid, gender, ag, d_id))

    if not buyer_list:
        log("ERROR: 구매자 정보가 없습니다. 회원/배송지를 먼저 생성하세요.")
        return {}

    # 상품 데이터
    product_ids   = [p[0] for p in products]
    product_map   = {p[0]: (p[1], p[2]) for p in products}   # id → (price, seller_id)

    order_sql = ("INSERT INTO orders "
                 "(member_id, delivery_id, date, total_price, status) "
                 "VALUES (%s, %s, %s, %s, %s)")
    item_sql  = ("INSERT INTO order_item "
                 "(product_id, order_id, quantity, unit_price, seller_id) "
                 "VALUES (%s, %s, %s, %s, %s)")

    start_dt   = datetime(2023, 1, 1)
    date_range = (datetime(2025, 12, 31) - start_dt).days

    stats_acc = {}    # (product_id, gender, age_group) → [cnt, qty]

    order_batch  = []
    item_batch   = []
    batch_offset = 0
    total_orders = existing_orders
    total_items  = 0
    t0 = time.time()

    for _ in range(need):
        buyer = random.choice(buyer_list)
        mid, gender, age_group, d_id = buyer

        order_dt  = (start_dt + timedelta(days=random.randint(0, date_range))).strftime('%Y-%m-%d')
        status    = random.choices(STATUSES, weights=STATUS_W)[0]
        cur_oid   = next_order_id + batch_offset

        # 주문 상품 생성 (1~4개, 평균 2.5)
        n_items   = random.randint(1, 4)
        pids      = random.sample(product_ids, min(n_items, len(product_ids)))
        total_price = 0

        for pid in pids:
            price, seller_id = product_map[pid]
            qty = random.randint(1, 5)
            total_price += price * qty
            item_batch.append((pid, cur_oid, qty, price, seller_id))
            total_items += 1

            key = (pid, gender, age_group)
            if key not in stats_acc:
                stats_acc[key] = [0, 0]
            stats_acc[key][0] += 1
            stats_acc[key][1] += qty

        order_batch.append((mid, d_id, order_dt, total_price, status))
        batch_offset += 1

        # 배치 커밋
        if len(order_batch) >= INSERT_BATCH:
            with conn.cursor() as cur:
                cur.executemany(order_sql, order_batch)
                cur.executemany(item_sql, item_batch)
            conn.commit()
            next_order_id += len(order_batch)
            total_orders  += len(order_batch)
            batch_offset   = 0
            order_batch    = []
            item_batch     = []

            if total_orders % LOG_EVERY == 0:
                elapsed = time.time() - t0
                done    = total_orders - existing_orders
                log(f"  주문 {total_orders:,}/{NUM_ORDERS:,} | "
                    f"아이템 {total_items:,} | "
                    f"속도 {done/elapsed:,.0f}건/초 | "
                    f"잔여 {eta_str(done, need, elapsed)}")

    # 나머지 처리
    if order_batch:
        with conn.cursor() as cur:
            cur.executemany(order_sql, order_batch)
            cur.executemany(item_sql, item_batch)
        conn.commit()
        total_orders += len(order_batch)
        total_items  += len(item_batch)

    elapsed = time.time() - t0
    log(f"주문 완료: {total_orders:,}건 | 주문상품: {total_items:,}건 | "
        f"소요: {elapsed/60:.1f}분")
    return stats_acc

# ──────────────────────────────────────────────────────────
# Phase 6: product_order_stats
# ──────────────────────────────────────────────────────────
def insert_product_order_stats(conn, stats_acc: dict):
    if not stats_acc:
        log("product_order_stats: 생성할 데이터 없음 → 스킵")
        return

    log(f"product_order_stats {len(stats_acc):,}건 삽입 중...")

    sql = ("INSERT INTO product_order_stats "
           "(product_id, gender, age_group, order_count, total_quantity) "
           "VALUES (%s, %s, %s, %s, %s) "
           "ON DUPLICATE KEY UPDATE "
           "  order_count    = order_count    + VALUES(order_count), "
           "  total_quantity = total_quantity + VALUES(total_quantity)")

    rows = [(pid, g, ag, oc, tq) for (pid, g, ag), (oc, tq) in stats_acc.items()]
    total = 0
    for i in range(0, len(rows), INSERT_BATCH):
        with conn.cursor() as cur:
            cur.executemany(sql, rows[i:i+INSERT_BATCH])
        conn.commit()
        total += len(rows[i:i+INSERT_BATCH])

    log(f"product_order_stats {total:,}건 완료")

# ──────────────────────────────────────────────────────────
# Phase 7: sale_quantity / total_price 업데이트
# ──────────────────────────────────────────────────────────
def update_sale_quantities(conn):
    log("product.sale_quantity 업데이트 중 (배치)...")
    with conn.cursor() as cur:
        cur.execute("""
            UPDATE product p
            JOIN (
                SELECT product_id, SUM(quantity) AS total_qty
                FROM order_item
                GROUP BY product_id
            ) oi ON p.id = oi.product_id
            SET p.sale_quantity = oi.total_qty
        """)
    conn.commit()
    log("product.sale_quantity 업데이트 완료")

# ──────────────────────────────────────────────────────────
# 데이터 검증 쿼리
# ──────────────────────────────────────────────────────────
def print_summary(conn):
    tables = [
        ("member",              "SELECT COUNT(*) FROM `member`"),
        ("category",            "SELECT COUNT(*) FROM category"),
        ("product",             "SELECT COUNT(*) FROM product"),
        ("delivery",            "SELECT COUNT(*) FROM delivery"),
        ("orders",              "SELECT COUNT(*) FROM orders"),
        ("order_item",          "SELECT COUNT(*) FROM order_item"),
        ("product_order_stats", "SELECT COUNT(*) FROM product_order_stats"),
    ]
    log("=" * 50)
    log("최종 데이터 현황")
    log("=" * 50)
    with conn.cursor() as cur:
        for name, sql in tables:
            cur.execute(sql)
            cnt = cur.fetchone()[0]
            log(f"  {name:<25} {cnt:>12,}건")
    log("=" * 50)

# ──────────────────────────────────────────────────────────
# Main
# ──────────────────────────────────────────────────────────
def main():
    log("=" * 60)
    log("MSA Store 대용량 테스트 데이터 생성")
    log(f"목표: 주문 {NUM_ORDERS:,}건 / 주문상품 ~{int(NUM_ORDERS * 2.5):,}건")
    log("=" * 60)

    conn = get_conn()
    t_total = time.time()

    try:
        create_schema(conn)
        apply_optimizations(conn)

        insert_categories(conn)

        buyer_meta   = insert_members(conn)
        delivery_map = insert_deliveries(conn)
        products     = insert_products(conn)

        stats_acc = insert_orders_and_items(conn, buyer_meta, delivery_map, products)

        insert_product_order_stats(conn, stats_acc)
        update_sale_quantities(conn)

        restore_optimizations(conn)
        print_summary(conn)

        elapsed = time.time() - t_total
        log(f"전체 완료! 총 소요시간: {elapsed/60:.1f}분")

    except KeyboardInterrupt:
        log("\n중단됨 (Ctrl+C). 지금까지 커밋된 데이터는 유지됩니다.")
        restore_optimizations(conn)
    except Exception as e:
        log(f"오류 발생: {e}")
        restore_optimizations(conn)
        raise
    finally:
        conn.close()

if __name__ == '__main__':
    main()
