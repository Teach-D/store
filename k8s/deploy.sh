#!/bin/bash

# EKS 배포 스크립트
# 사용법: ./deploy.sh

set -e

echo "🚀 EKS 배포 시작..."

# 1. Namespace 생성
echo "📦 Namespace 생성 중..."
kubectl apply -f base/namespace.yaml

# 2. Secrets & ConfigMaps 생성
echo "🔐 Secrets & ConfigMaps 생성 중..."
kubectl apply -f secrets/
kubectl apply -f configmaps/

# 3. 인프라 배포 (MySQL, RabbitMQ, Zipkin)
echo "🗄️  인프라 배포 중..."
kubectl apply -f infrastructure/mysql/
kubectl apply -f infrastructure/rabbitmq/
kubectl apply -f infrastructure/zipkin/

# MySQL, RabbitMQ, Zipkin이 준비될 때까지 대기
echo "⏳ MySQL, RabbitMQ & Zipkin 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=mysql -n ecommerce --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n ecommerce --timeout=300s
kubectl wait --for=condition=ready pod -l app=zipkin -n ecommerce --timeout=180s

# 4. Discovery Service 배포
echo "🔍 Discovery Service 배포 중..."
kubectl apply -f services/discovery/

# Discovery Service가 준비될 때까지 대기
kubectl wait --for=condition=ready pod -l app=discovery-service -n ecommerce --timeout=180s

# 5. 비즈니스 서비스 배포
echo "💼 비즈니스 서비스 배포 중..."
kubectl apply -f services/order/
kubectl apply -f services/payment/
kubectl apply -f services/member/
kubectl apply -f services/product/

# 6. Gateway 배포
echo "🌐 Gateway 배포 중..."
kubectl apply -f services/gateway/

# 7. Ingress 배포
echo "🔀 Ingress 배포 중..."
kubectl apply -f ingress/

# 8. HPA 배포
echo "📈 HPA(Auto Scaler) 배포 중..."
kubectl apply -f hpa/

# 9. 배포 상태 확인
echo "✅ 배포 완료! 상태 확인 중..."
kubectl get all -n ecommerce

echo ""
echo "🎉 EKS 배포 완료!"
echo ""
echo "Gateway Service URL:"
kubectl get svc gateway-service -n ecommerce -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
echo ""