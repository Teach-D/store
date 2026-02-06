import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const searchOrderQuantityTrend = new Trend('search_order_quantity_duration');
const searchRatingTrend = new Trend('search_rating_duration');
const categoryOrderQuantityTrend = new Trend('category_order_quantity_duration');

// 테스트 설정
export const options = {
    scenarios: {
        // 시나리오 1: 상품 검색 (주문순)
        search_order_quantity: {
            executor: 'constant-vus',
            vus: 10,
            duration: '30s',
            exec: 'searchOrderQuantity',
            tags: { scenario: 'search_order_quantity' },
        },
        // 시나리오 2: 상품 검색 (별점순)
        search_rating: {
            executor: 'constant-vus',
            vus: 10,
            duration: '30s',
            exec: 'searchRating',
            startTime: '35s',
            tags: { scenario: 'search_rating' },
        },
        // 시나리오 3: 카테고리별 조회 (주문순)
        category_order_quantity: {
            executor: 'constant-vus',
            vus: 10,
            duration: '30s',
            exec: 'categoryOrderQuantity',
            startTime: '70s',
            tags: { scenario: 'category_order_quantity' },
        },
        // 시나리오 4: 부하 테스트 (동시 접속)
        load_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 50 },
                { duration: '1m', target: 100 },
                { duration: '30s', target: 0 },
            ],
            exec: 'mixedLoad',
            startTime: '105s',
            tags: { scenario: 'load_test' },
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이하
        errors: ['rate<0.1'],               // 에러율 10% 미만
        search_order_quantity_duration: ['p(95)<100'],  // 목표: 100ms 이하
        search_rating_duration: ['p(95)<100'],
        category_order_quantity_duration: ['p(95)<100'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 검색 키워드 목록
const KEYWORDS = ['니트', '셔츠', '바지', '자켓', '코트', '스마트폰', '노트북', '과자', '책상', '운동화'];

// 카테고리 ID 목록 (1~10)
const CATEGORY_IDS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

// 랜덤 선택 함수
function randomChoice(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

// 시나리오 1: 상품 검색 (주문순)
export function searchOrderQuantity() {
    const keyword = randomChoice(KEYWORDS);

    const startTime = new Date();
    const res = http.get(`${BASE_URL}/products/search/order-quantity?keyword=${keyword}`);
    const duration = new Date() - startTime;

    searchOrderQuantityTrend.add(duration);

    const success = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has data': (r) => r.json() !== null,
        'response time < 100ms': (r) => r.timings.duration < 100,
    });

    errorRate.add(!success);
    sleep(0.5);
}

// 시나리오 2: 상품 검색 (별점순)
export function searchRating() {
    const keyword = randomChoice(KEYWORDS);

    const startTime = new Date();
    const res = http.get(`${BASE_URL}/products/search/rating?keyword=${keyword}`);
    const duration = new Date() - startTime;

    searchRatingTrend.add(duration);

    const success = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has data': (r) => r.json() !== null,
        'response time < 100ms': (r) => r.timings.duration < 100,
    });

    errorRate.add(!success);
    sleep(0.5);
}

// 시나리오 3: 카테고리별 조회 (주문순)
export function categoryOrderQuantity() {
    const categoryId = randomChoice(CATEGORY_IDS);

    const startTime = new Date();
    const res = http.get(`${BASE_URL}/products/category/${categoryId}/order-quantity`);
    const duration = new Date() - startTime;

    categoryOrderQuantityTrend.add(duration);

    const success = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has data': (r) => r.json() !== null,
        'response time < 100ms': (r) => r.timings.duration < 100,
    });

    errorRate.add(!success);
    sleep(0.5);
}

// 시나리오 4: 혼합 부하 테스트
export function mixedLoad() {
    const scenario = Math.floor(Math.random() * 3);

    switch (scenario) {
        case 0:
            searchOrderQuantity();
            break;
        case 1:
            searchRating();
            break;
        case 2:
            categoryOrderQuantity();
            break;
    }
}

// 테스트 결과 요약
export function handleSummary(data) {
    const summary = {
        '테스트 결과': {
            '총 요청 수': data.metrics.http_reqs.values.count,
            '성공률': `${((1 - data.metrics.errors.values.rate) * 100).toFixed(2)}%`,
            '평균 응답 시간': `${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`,
            'P95 응답 시간': `${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`,
            'P99 응답 시간': `${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`,
        },
        '시나리오별 결과': {
            '상품 검색 (주문순) P95': data.metrics.search_order_quantity_duration
                ? `${data.metrics.search_order_quantity_duration.values['p(95)'].toFixed(2)}ms`
                : 'N/A',
            '상품 검색 (별점순) P95': data.metrics.search_rating_duration
                ? `${data.metrics.search_rating_duration.values['p(95)'].toFixed(2)}ms`
                : 'N/A',
            '카테고리 조회 (주문순) P95': data.metrics.category_order_quantity_duration
                ? `${data.metrics.category_order_quantity_duration.values['p(95)'].toFixed(2)}ms`
                : 'N/A',
        },
    };

    console.log('\n========================================');
    console.log('           성능 테스트 결과            ');
    console.log('========================================\n');
    console.log(JSON.stringify(summary, null, 2));

    return {
        'stdout': JSON.stringify(summary, null, 2),
        'scripts/performance-test-result.json': JSON.stringify(data, null, 2),
    };
}
