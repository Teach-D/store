#!/bin/bash

# Docker 이미지 빌드 및 Docker Hub 푸시 스크립트
# 사용법: ./build-push.sh <DOCKERHUB_USERNAME>

set -e

if [ -z "$1" ]; then
    echo "사용법: ./build-push.sh <DOCKERHUB_USERNAME>"
    echo "예시: ./build-push.sh myusername"
    exit 1
fi

DOCKERHUB_USERNAME=$1

echo "🚀 Docker 이미지 빌드 & Docker Hub 푸시 시작..."
echo "Docker Hub Username: ${DOCKERHUB_USERNAME}"

# Docker Hub 로그인
echo "🔐 Docker Hub 로그인 중..."
docker login

# 서비스 목록
SERVICES=("order" "payment" "member" "product" "discovery-service" "gate-way")
SERVICE_NAMES=("order-service" "payment-service" "member-service" "product-service" "discovery-service" "gateway-service")

# 각 서비스별 빌드 및 푸시
for i in "${!SERVICES[@]}"; do
    SERVICE_DIR="${SERVICES[$i]}"
    SERVICE_NAME="${SERVICE_NAMES[$i]}"

    echo "📦 $SERVICE_NAME 빌드 중..."

    # Docker 이미지 빌드
    cd ../${SERVICE_DIR}
    docker build -t ${SERVICE_NAME}:latest .

    # 태그 지정
    docker tag ${SERVICE_NAME}:latest ${DOCKERHUB_USERNAME}/${SERVICE_NAME}:latest
    docker tag ${SERVICE_NAME}:latest ${DOCKERHUB_USERNAME}/${SERVICE_NAME}:$(git rev-parse --short HEAD 2>/dev/null || echo "manual")

    # Docker Hub 푸시
    echo "📤 ${SERVICE_NAME} 푸시 중..."
    docker push ${DOCKERHUB_USERNAME}/${SERVICE_NAME}:latest
    docker push ${DOCKERHUB_USERNAME}/${SERVICE_NAME}:$(git rev-parse --short HEAD 2>/dev/null || echo "manual")

    cd ../k8s
done

echo ""
echo "✅ 모든 이미지 빌드 & 푸시 완료!"
echo ""
echo "다음 단계:"
echo "1. k8s/services/*/deployment.yaml 파일의 이미지 경로를 다음으로 변경:"
echo "   image: ${DOCKERHUB_USERNAME}/<service-name>:latest"
echo "2. kubectl apply로 배포 실행"