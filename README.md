# MSA E-Commerce Platform

Spring Boot 기반 마이크로서비스 이커머스 플랫폼입니다. AI 연동, Kubernetes 배포를 포함한 클라우드 네이티브 아키텍처로 구성되어 있습니다.

---

## 목차

1. [서비스 구성](#서비스-구성)
2. [전체 시스템 아키텍처](#전체-시스템-아키텍처)
3. [기술 스택](#기술-스택)
4. [서비스별 상세](#서비스별-상세)
5. [ERD](#erd)
6. [메시지 브로커 아키텍처](#메시지-브로커-아키텍처)
7. [데이터 흐름도](#데이터-흐름도)
8. [인프라 아키텍처](#인프라-아키텍처)
9. [보안 구조](#보안-구조)
10. [성능 개선 결과](#성능-개선-결과)
11. [실행 방법](#실행-방법)

---

## 서비스 구성

| 서비스 | 언어/프레임워크 | 포트 | 역할 |
|--------|----------------|------|------|
| `discovery-service` | Spring Boot (Eureka) | 8761 | 서비스 레지스트리 |
| `gate-way` | Spring Cloud Gateway | 8000 | API 게이트웨이, JWT 인증 |
| `member` | Spring Boot | 8080 | 회원, 장바구니, 쿠폰, 배송지 |
| `order` | Spring Boot | 8080 | 주문, 정산 |
| `payment` | Spring Boot | 8080 | 결제 (Toss Payments 연동) |
| `product` | Spring Boot | 8080 | 상품, 리뷰, 카테고리, 랭킹 |

---

## 전체 시스템 아키텍처

```mermaid
graph TB
    Client(["Client"])
    GW["API Gateway\nJWT 인증 / 라우팅"]
    EUREKA["Eureka\n서비스 디스커버리"]

    subgraph Services["Microservices"]
        MEMBER["Member\n회원/장바구니/쿠폰"]
        ORDER["Order\n주문/정산"]
        PAYMENT["Payment\n결제"]
        PRODUCT["Product\n상품/리뷰/랭킹"]
    end

    subgraph AI["AI Services"]
        AI_IMG["AI Image\n이미지 생성"]
        AI_REV["AI Review\n감성 분석"]
    end

    subgraph Infra["Infrastructure"]
        MQ["RabbitMQ"]
        REDIS["Redis"]
        DB["MariaDB"]
        ZIPKIN["Zipkin / Prometheus"]
    end

    Client --> GW --> Services
    Services --> EUREKA
    Services --> MQ --> AI_IMG & AI_REV
    Services --> DB
    MEMBER & PRODUCT --> REDIS
    Services --> ZIPKIN
```

---

## 기술 스택

### Backend
| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.2+, Spring Cloud 2023.0+ |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| ORM | Spring Data JPA / Hibernate (MariaDB Dialect) |
| Messaging | Spring AMQP (RabbitMQ) |
| Cache | Spring Data Redis |
| HTTP Client | Spring Cloud OpenFeign |
| Fault Tolerance | Resilience4j (Circuit Breaker) |
| Batch | Spring Batch |
| Auth | JWT (jjwt) |
| Observability | Micrometer Tracing + Brave + Zipkin |
| Build | Gradle (JDK 17 Temurin) |

### Infrastructure
| 분류 | 기술 |
|------|------|
| Container | Docker, Docker Compose |
| Orchestration | Kubernetes (AWS EKS) |
| GitOps | ArgoCD |
| CI/CD | GitHub Actions |
| DB | MariaDB 8.0 |
| Cache/Queue | Redis 7, RabbitMQ 3-management |
| Tracing | Zipkin (OpenZipkin) |
| Monitoring | Prometheus, AlertManager |
| Storage | AWS S3 (이미지) |

---

## 서비스별 상세

### API Gateway

```mermaid
flowchart LR
    REQ["HTTP Request"] --> GW["Spring Cloud Gateway"]

    GW --> JWT_FILTER["JwtAuthenticationFilter\n토큰 파싱 & 검증"]
    JWT_FILTER -->|"유효하지 않음"| REJECT["401 Unauthorized"]
    JWT_FILTER -->|"유효함"| ROLE_FILTER["RoleAuthorizationFilter\n권한 확인"]
    ROLE_FILTER -->|"권한 없음"| FORBIDDEN["403 Forbidden"]
    ROLE_FILTER -->|"통과"| ROUTE["라우팅"]

    ROUTE --> MS["Member Service\n/members/**, /carts/**, /coupons/**, /deliveries/**"]
    ROUTE --> OS["Order Service\n/orders/**"]
    ROUTE --> PS["Payment Service\n/payments/**"]
    ROUTE --> PRS["Product Service\n/products/**, /categories/**, /tags/**, /review/**"]
```

**공개 엔드포인트 (인증 불필요):**
- `GET /products/**`, `GET /categories/**`, `GET /tags/**`
- `POST /members/signup`, `POST /members/login`

---

---

## ERD

### Member 서비스 ERD

```mermaid
erDiagram
    MEMBER {
        bigint id PK
        varchar email UK
        varchar name
        varchar password
        varchar gender
        date birth_date
        varchar role
    }

    CART {
        bigint id PK
        bigint member_id FK
    }

    CART_ITEM {
        bigint id PK
        bigint cart_id FK
        bigint product_id
        int quantity
    }

    COUPON {
        bigint id PK
        varchar coupon_code UK
        int discount_value
        varchar coupon_type
        date valid_from
        date valid_until
    }

    MEMBER_COUPON {
        bigint id PK
        bigint member_id FK
        bigint coupon_id FK
        date issue_date
    }

    DELIVERY {
        bigint id PK
        bigint member_id FK
        varchar address
        varchar phone
        varchar recipient
        varchar delivery_status
    }

    MEMBER ||--o{ CART : ""
    CART ||--o{ CART_ITEM : ""
    MEMBER ||--o{ MEMBER_COUPON : ""
    COUPON ||--o{ MEMBER_COUPON : ""
    MEMBER ||--o{ DELIVERY : ""
```

---

### Order 서비스 ERD

```mermaid
erDiagram
    ORDER {
        bigint order_id PK
        bigint member_id
        bigint delivery_id
        decimal total_price
        varchar status
        datetime date
    }

    ORDER_ITEM {
        bigint id PK
        bigint order_id FK
        bigint product_id
        int quantity
        decimal price
    }

    SETTLEMENT {
        bigint id PK
        bigint order_id
        varchar settlement_status
    }

    SETTLEMENT_ITEM {
        bigint id PK
        bigint settlement_id FK
        bigint product_id
        decimal price
    }

    ORDER ||--o{ ORDER_ITEM : ""
    SETTLEMENT ||--o{ SETTLEMENT_ITEM : ""
```

---

### Product 서비스 ERD

```mermaid
erDiagram
    CATEGORY {
        bigint id PK
        varchar name
    }

    PRODUCT {
        bigint id PK
        bigint seller_id
        bigint category_id FK
        varchar title
        decimal price
        int quantity
        int sale_quantity
    }

    PRODUCT_DETAIL {
        bigint id PK
        bigint product_id FK
        text description
        varchar image_url
        varchar promo_image_url
        decimal average_rating
        int rating_count
    }

    TAG {
        bigint id PK
        varchar name
    }

    PRODUCT_TAG {
        bigint id PK
        bigint product_id FK
        bigint tag_id FK
    }

    REVIEW {
        bigint id PK
        bigint product_id FK
        bigint member_id
        text content
        int rating
        datetime created_at
    }

    PRODUCT_ORDER_STATS {
        bigint id PK
        bigint product_id
        varchar gender
        varchar age_group
        int order_count
    }

    PRODUCT_REVIEW_STATS {
        bigint id PK
        bigint product_id
        varchar sentiment
        decimal avg_rating
        text positive_keywords
        text negative_keywords
    }

    CATEGORY ||--o{ PRODUCT : ""
    PRODUCT ||--|| PRODUCT_DETAIL : ""
    PRODUCT ||--o{ PRODUCT_TAG : ""
    TAG ||--o{ PRODUCT_TAG : ""
    PRODUCT ||--o{ REVIEW : ""
    PRODUCT ||--o{ PRODUCT_ORDER_STATS : ""
    PRODUCT ||--o| PRODUCT_REVIEW_STATS : ""
```

---

### Payment 서비스 ERD

```mermaid
erDiagram
    PAYMENT {
        bigint id PK
        bigint order_id
        bigint user_id
        decimal amount
        varchar status
        varchar pg_payment_key
        varchar payment_method
        text failure_reason
        datetime created_at
        datetime updated_at
    }
```

---

## 메시지 브로커 아키텍처

### Exchange / Queue 구조

```mermaid
graph LR
    ORDER["Order"]
    PAYMENT["Payment"]
    MEMBER["Member"]
    PRODUCT["Product"]
    AI_IMG["AI Image"]
    AI_REV["AI Review"]
    DLQ["DLQ\n(실패 메시지)"]

    ORDER -->|"order.created.payment"| PAYMENT
    ORDER -->|"cart.delete"| MEMBER
    PAYMENT -->|"payment.completed/failed"| ORDER
    PRODUCT -->|"product.created"| AI_IMG
    PRODUCT -->|"review.created"| AI_REV
    AI_IMG -->|"product.image.ready"| PRODUCT
    AI_REV -->|"review.summary.ready"| PRODUCT
    ORDER & PAYMENT & PRODUCT -->|"3회 실패"| DLQ
```

### RabbitMQ 설정 요약

| Exchange | Type | 바인딩 큐 |
|----------|------|-----------|
| `order.exchange` | Direct | `order.created.payment`, `cart.delete` |
| `payment.exchange` | Topic | `payment.completed`, `payment.failed` |
| `product.exchange` | Direct | `product.created` |
| `review.exchange` | Direct | `review.created`, `review.summary.ready` |
| `dlx.exchange` | Direct | `*.dlq` (모든 실패 메시지) |

**신뢰성 설정:**
- Publisher Confirms (CorrelationData) — 발행 확인
- Prefetch Count: Java 서비스 10, AI 서비스 1 (GPU 최적화)
- 재시도: 최대 3회, 지수 백오프 (1s → 2s → 4s, max 10s)
- 실패 시 DLX → DLQ 자동 라우팅

---

## 데이터 흐름도

### 1. 주문 생성 ~ 결제 완료

```mermaid
sequenceDiagram
    actor Client
    participant GW as API Gateway
    participant OS as Order Service
    participant MQ as RabbitMQ
    participant PS as Payment Service
    participant MS as Member Service
    participant PRS as Product Service

    Client->>GW: POST /orders (JWT 포함)
    GW->>OS: 주문 요청 전달

    OS->>MS: [Feign] 장바구니 아이템 조회
    OS->>MS: [Feign] 쿠폰 할인 금액 조회
    OS->>MS: [Feign] 배송지 조회
    OS->>PRS: [Feign] 상품 재고 확인 & 차감

    OS->>OS: Order 저장 (PENDING)
    OS-->>Client: 주문 ID 반환

    OS->>MQ: OrderCreatedEvent 발행 (order.created.payment)
    MQ->>PS: OrderCreatedEvent 수신

    PS->>PS: Payment 생성 (PENDING)
    PS->>PS: Toss Payments API 호출
    PS->>PS: Payment 상태 업데이트 (COMPLETED)
    PS->>MQ: PaymentCompletedEvent 발행 (payment.completed)

    MQ->>OS: PaymentCompletedEvent 수신
    OS->>OS: Order 상태 업데이트 (CONFIRMED)
    OS->>MQ: cart.delete 발행

    MQ->>MS: cart.delete 수신
    MS->>MS: CartItem 삭제

    OS->>OS: Settlement 생성
```

---

### 2. 쿠폰 발급 (Redis 분산 처리)

```mermaid
sequenceDiagram
    actor User
    participant GW as API Gateway
    participant MS as Member Service
    participant REDIS as Redis
    participant DB as MariaDB

    User->>GW: POST /coupons/issue/{couponId}
    GW->>MS: 쿠폰 발급 요청

    MS->>REDIS: 원자적 연산 (잔여량 감소)

    alt 재고 있음
        REDIS-->>MS: 성공
        MS->>DB: MemberCoupon 저장
        MS-->>User: 쿠폰 발급 완료
    else 재고 없음
        REDIS-->>MS: 실패
        MS-->>User: 쿠폰 소진
    end
```

---

## 인프라 아키텍처

### Kubernetes 배포 구조 (AWS EKS)

```mermaid
%%{init: {'flowchart': {'nodeSpacing': 35, 'rankSpacing': 35}}}%%
graph TB
    LB["AWS Load Balancer"]

    subgraph EKS["AWS EKS  |  Namespace: ecommerce"]
        GW["Gateway (HPA 2~10)"]

        subgraph APP["Application Services"]
            MEMBER["Member (HPA 2~10)"]
            ORDER["Order (HPA 2~10)"]
            PAYMENT["Payment (HPA 1~10)"]
            PRODUCT["Product (HPA 2~10)"]
        end

        subgraph INFRA["Infrastructure"]
            EUREKA["Eureka"]
            MQ["RabbitMQ (PVC)"]
            MYSQL["MySQL (PVC)"]
            ZIPKIN["Zipkin"]
        end

        AI["AI Image Service"]
    end

    LB --> GW --> APP
    APP --> EUREKA & MQ & MYSQL & ZIPKIN
    MQ --> AI
```

### HPA (Horizontal Pod Autoscaler) 설정

| 서비스 | Min | Max | CPU 임계값 | 메모리 임계값 | Scale Up | Scale Down |
|--------|-----|-----|-----------|--------------|----------|------------|
| Gateway | 2 | 10 | 70% | 80% | 기본 | 기본 |
| Member | 2 | 10 | 70% | 80% | 기본 | 기본 |
| Order | 2 | 10 | 70% | 80% | 30s | 5min |
| Payment | 1 | 10 | 70% | 80% | 기본 | 기본 |
| Product | 2 | 10 | 70% | 80% | 기본 | 기본 |

### CI/CD Pipeline

```mermaid
flowchart LR
    DEV["개발자 Push"] --> GH["GitHub"]
    GH --> GHA["GitHub Actions\n(.github/workflows/gradle.yml)"]

    subgraph CI["CI (Build Job)"]
        GHA --> BUILD["Gradle Build\n(JDK 17 Temurin)"]
        BUILD --> DOCKER_BUILD["Docker Image Build"]
        DOCKER_BUILD --> PUSH["Docker Hub Push"]
    end

    subgraph TEST["Test Job"]
        GHA --> TEST_RUN["테스트 실행\n(Redis 7 컨테이너)"]
    end

    PUSH --> K8S_UPDATE["k8s Manifest 업데이트\n(deployment.yaml tag 변경)"]
    K8S_UPDATE --> ARGOCD["ArgoCD\n(GitOps 자동 동기화)"]
    ARGOCD --> EKS["EKS 클러스터 배포"]
```

---

## 보안 구조

```mermaid
flowchart TD
    REQ["HTTP Request"] --> CORS["CORS Filter\n(Wildcard Origin)"]
    CORS --> JWT_CHECK{{"Authorization Header\n존재 여부"}}

    JWT_CHECK -->|"없음 (공개 경로)"| PUBLIC["공개 엔드포인트 통과"]
    JWT_CHECK -->|"Bearer Token"| JWT_VALIDATE["JWT 파싱 & 서명 검증\n(HMAC-SHA256)"]

    JWT_VALIDATE -->|"만료/위조"| ERR401["401 Unauthorized"]
    JWT_VALIDATE -->|"유효"| CLAIMS["Claims 추출\n(userId, role)"]

    CLAIMS --> ROLE_CHECK{{"경로 권한 확인"}}
    ROLE_CHECK -->|"권한 없음"| ERR403["403 Forbidden"]
    ROLE_CHECK -->|"통과"| HEADER["X-User-Id, X-User-Role\n헤더에 삽입"]

    HEADER --> SERVICE["마이크로서비스\n(헤더로 사용자 식별)"]
```

**JWT 설정:**
- 알고리즘: HMAC-SHA256
- 유효시간: 24시간 (86400000ms)
- 전달 방식: `Authorization: Bearer <token>` 헤더
- 서비스 간 사용자 식별: `X-User-Id`, `X-User-Role` 헤더

---

## 성능 개선 결과

### 상품 조회 성능 개선

| # | 방법 | 문제 | 성과 |
|---|------|------|------|
| 1 | **커버링 인덱스 적용** | 750만 건 환경에서 Full Scan·filesort로 응답 지연 (2.5s) | filesort·임시 테이블 제거 → **96% 단축 (2.5s → 80ms)** |
| 2 | **역정규화 + Redis Write-Behind** | 다중 JOIN + 실시간 통계 업데이트 충돌로 DB 병목 (3.8s) | **93% 단축 (3.8s → 250ms)**, DB 쓰기 N회 → 1회 |

**커버링 인덱스:** 정렬·조회 컬럼을 모두 포함한 인덱스를 설계해 테이블 재접근을 제거하고 filesort·임시 테이블을 완전히 제거

**Redis Write-Behind:** 역정규화 테이블로 읽기 부하를 분산하고 통계 업데이트는 Redis에 누적 후 10초 주기 배치로 DB에 일괄 반영해 쓰기 충돌 해소

---

### 모놀리식 → 마이크로서비스 전환

| 지표 | Before (Monolith) | After (MSA) | 개선율 |
|------|------------------|-------------|--------|
| 빌드 시간 | 12s | 6s | **50% 감소** |
| Zipkin Span 수 | 61 | 55 | **10% 감소** |
| 평균 응답 시간 | 208ms | 158ms | **24% 개선** |
| Circuit Breaker 처리 | 17s (타임아웃) | 0.01s (즉시 차단) | **1700x 개선** |

### RabbitMQ 비동기 처리 도입 후

| 지표 | Before (동기) | After (비동기) | 개선율 |
|------|--------------|---------------|--------|
| 평균 응답 시간 | 57ms | 37ms | **36% 개선** |
| P95 응답 시간 | 162ms | 80ms | **50% 개선** |

### 신뢰성 패턴

| 패턴 | 목적 | 효과 |
|------|------|------|
| Circuit Breaker | 장애 격리 | Cascade Failure 방지 |
| DLX/DLQ | 메시지 재처리 | 메시지 유실 없이 재시도 |
| Publisher Confirms | 발행 확인 | 브로커 장애 시 재발행 |
| Cache-Aside (Redis) | 읽기 성능 | DB 부하 감소 |

---

## 실행 방법

### 로컬 개발 환경 (Docker Compose)

```bash
# 인프라 서비스 시작 (DB, Redis, RabbitMQ, Zipkin)
docker-compose -f docker-compose-msa.yml up -d mysql redis rabbitmq zipkin

# 서비스 디스커버리 시작
docker-compose -f docker-compose-msa.yml up -d discovery-service

# 전체 서비스 시작
docker-compose -f docker-compose-msa.yml up -d
```

### Kubernetes 배포 (EKS)

```bash
# 네임스페이스 생성
kubectl create namespace ecommerce

# Secrets & ConfigMap 적용
kubectl apply -f k8s/config/ -n ecommerce

# 인프라 서비스 배포
kubectl apply -f k8s/services/mysql/ -n ecommerce
kubectl apply -f k8s/services/rabbitmq/ -n ecommerce
kubectl apply -f k8s/services/zipkin/ -n ecommerce

# 애플리케이션 배포
kubectl apply -f k8s/services/ -n ecommerce

# 배포 스크립트 사용
bash k8s/deploy.sh
```

### 환경 변수 (주요)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `SPRING_DATASOURCE_URL` | MariaDB 연결 URL | `jdbc:mariadb://localhost:3306/store` |
| `SPRING_RABBITMQ_HOST` | RabbitMQ 호스트 | `localhost` |
| `SPRING_REDIS_HOST` | Redis 호스트 | `localhost` |
| `JWT_SECRET` | JWT 서명 키 | (application.yml) |
| `TOSS_PAYMENTS_SECRET` | Toss Payments 시크릿 | - |
| `OPENAI_API_KEY` | OpenAI DALL-E 키 | - |
| `AWS_S3_BUCKET` | S3 버킷명 | - |
| `OLLAMA_URL` | Ollama 서버 URL | - |

---

## 프로젝트 구조

```
store/
├── discovery-service/          # Eureka 서비스 디스커버리
├── gate-way/                   # Spring Cloud Gateway (인증/라우팅)
├── member/                     # 회원/장바구니/쿠폰/배송지
├── order/                      # 주문/정산
├── payment/                    # 결제 (Toss Payments)
├── product/                    # 상품/리뷰/랭킹/카테고리
├── ai-image-service/           # Python AI 이미지 생성
├── ai-review-service/          # Python AI 리뷰 감성 분석
├── k8s/                        # Kubernetes 매니페스트 (EKS)
│   ├── services/               # 각 서비스별 Deployment/Service/HPA
│   └── deploy.sh               # 배포 스크립트
├── monitoring/                 # Prometheus / AlertManager 설정
├── db/                         # MySQL 초기화 스크립트
├── docker-compose-msa.yml      # 로컬 개발용 Docker Compose
└── .github/workflows/          # GitHub Actions CI/CD
```
