## 스토어 애플리케이션
이 프로젝트는 스토어 백엔드 서비스로, 제품 관리, 사용자 인증, 장바구니, 주문 기능 등을 제공합니다. Redis와 Docker를 이용해 세션 관리 및 캐싱 기능을 지원합니다.

## 주요 기능
1. 제품 관리: 제품 추가, 수정, 삭제, 조회
2. 사용자 인증: 로그인, 회원가입, 세션 관리
3. 장바구니: 제품 추가/제거, 수량 조정, 총 가격 확인
4. Redis 연동: 캐싱 및 세션 최적화
5. Docker 환경 구성: Redis용 Docker-Compose 설정

## 기술 스택
Java (Spring Boot)
Redis
Docker
Gradle
JPA

## 설치 방법
레포지토리 클론: git clone https://github.com/Teach-D/store.git

빌드 실행: ./gradlew build

Docker Compose로 애플리케이션 실행: docker-compose up

브라우저에서 http://localhost:8080로 접속.

## Swagger
http://localhost:8080/swagger-ui/index.html#/
