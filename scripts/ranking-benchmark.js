/**
 * 랭킹 조회 성능 비교 벤치마크
 *
 * [Before] DB 집계 쿼리:  GET /products/search/order-quantity?keyword=...
 * [After]  Redis Sorted Set: GET /products/ranking/daily?limit=10
 *
 * 실행 방법:
 *   k6 run scripts/ranking-benchmark.js
 *   k6 run --env BASE_URL=http://localhost:8080 scripts/ranking-benchmark.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// ── 커스텀 메트릭 ────────────────────────────────────────────────────────────
const dbQueryTrend    = new Trend('db_query_duration',    true);  // Before
const redisQueryTrend = new Trend('redis_query_duration', true);  // After
const errorRate       = new Rate('errors');

// ── 테스트 시나리오 설정 ────────────────────────────────────────────────────
export const options = {
    scenarios: {
        // [Before] DB 집계 쿼리
        db_ranking: {
            executor: 'constant-vus',
            vus: 300,
            duration: '30s',
            exec: 'dbRanking',
            tags: { type: 'before' },
        },
        // [After] Redis Sorted Set 조회
        redis_ranking: {
            executor: 'constant-vus',
            vus: 300,
            duration: '30s',
            exec: 'redisRanking',
            startTime: '35s',   // DB 테스트 끝난 후 실행 (결과 혼선 방지)
            tags: { type: 'after' },
        },
    },
    thresholds: {
        // Redis는 DB보다 최소 5배 이상 빠를 것을 목표
        'redis_query_duration{type:after}': ['p(95)<20'],   // 목표: p95 20ms 이하
        'db_query_duration{type:before}':   ['p(95)<500'],  // DB 기준선
        errors: ['rate<0.05'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const KEYWORDS = ['니트', '셔츠', '바지', '자켓', '코트'];

function randomKeyword() {
    return KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
}

// ── [Before] DB ORDER BY 집계 쿼리 ──────────────────────────────────────────
export function dbRanking() {
    const res = http.get(
        `${BASE_URL}/products/search/order-quantity?keyword=${encodeURIComponent(randomKeyword())}`,
        { tags: { endpoint: 'db' } }
    );

    // 첫 요청만 응답 확인
    if (__ITER === 0 && __VU === 1) {
        console.log(`[DB 첫 응답] status=${res.status} body=${res.body.substring(0, 200)}`);
    }

    const ok = check(res, {
        '[DB] status 200':        (r) => r.status === 200,
        '[DB] has body':          (r) => r.body && r.body.length > 0,
    });

    dbQueryTrend.add(res.timings.duration);
    errorRate.add(!ok);
    sleep(0.1);
}

// ── [After] Redis ZREVRANGEBYSCORE ───────────────────────────────────────────
export function redisRanking() {
    const res = http.get(
        `${BASE_URL}/products/ranking/daily?limit=10`,
        { tags: { endpoint: 'redis' } }
    );

    // 첫 요청만 응답 확인
    if (__ITER === 0 && __VU === 1) {
        console.log(`[Redis 첫 응답] status=${res.status} body=${res.body.substring(0, 200)}`);
    }

    const ok = check(res, {
        '[Redis] status 200':     (r) => r.status === 200,
        '[Redis] has entries':    (r) => {
            try { return JSON.parse(r.body).entries !== undefined; }
            catch { return false; }
        },
    });

    redisQueryTrend.add(res.timings.duration);
    errorRate.add(!ok);
    sleep(0.1);
}

// ── 결과 요약 출력 ───────────────────────────────────────────────────────────
export function handleSummary(data) {
    const db    = data.metrics['db_query_duration'];
    const redis = data.metrics['redis_query_duration'];

    const fmt = (val) => val != null ? `${val.toFixed(2)}ms` : 'N/A';

    const dbAvg    = db?.values?.avg;
    const redisAvg = redis?.values?.avg;
    const improvement = (dbAvg && redisAvg && dbAvg > 0)
        ? `${(dbAvg / redisAvg).toFixed(1)}x 빠름`
        : 'N/A';

    const summary = {
        '[Before] DB 집계 쿼리': {
            'avg':  fmt(db?.values?.avg),
            'p95':  fmt(db?.values?.['p(95)']),
            'p99':  fmt(db?.values?.['p(99)']),
            'min':  fmt(db?.values?.min),
            'max':  fmt(db?.values?.max),
        },
        '[After] Redis Sorted Set': {
            'avg':  fmt(redis?.values?.avg),
            'p95':  fmt(redis?.values?.['p(95)']),
            'p99':  fmt(redis?.values?.['p(99)']),
            'min':  fmt(redis?.values?.min),
            'max':  fmt(redis?.values?.max),
        },
        '성능 개선': {
            '평균 응답 시간 개선': improvement,
            '에러율': `${((data.metrics.errors?.values?.rate ?? 0) * 100).toFixed(2)}%`,
        },
    };

    console.log('\n================================================');
    console.log('   DB 집계 쿼리 vs Redis Sorted Set 벤치마크   ');
    console.log('================================================\n');
    console.log(JSON.stringify(summary, null, 2));

    return {
        stdout: JSON.stringify(summary, null, 2),
        'scripts/ranking-benchmark-result.json': JSON.stringify(data, null, 2),
    };
}
